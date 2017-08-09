package com.lxz.scala.demo1

import java.io.ByteArrayOutputStream
import java.net.{HttpURLConnection, URL}
import java.util.concurrent.LinkedBlockingQueue

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by xiaolezheng on 17/8/9.
  */
class CrawlActor extends Actor with ActorLogging {
  // 获取到ScheduleActor和PageStoreActor的引用
  private val scheduleActor = context.actorOf(Props[ScheduleActor], "schedule_actor")
  private val storeActor = context.actorOf(Props[PageStoreActor], "store_actor")
  private val q = new LinkedBlockingQueue[String]()
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool())

  def receive = {
    case link: String => {
      // 系统启动时，ScheduleActor伴生对象将入口seed链接字符串发过来
      if (link != null && link.startsWith("http://")) {
        // 简单验证链接合法性
        log.info("Checked: " + link)
        scheduleActor ! WebUrl(link) // 将字符串链接link包装成为WebUrl发送回ScheduleActor，等待进一步调度
      }
    }
    case ScheduledWebUrl(link, _) => {
      // ScheduleActor将包含调度信息和链接信息的链接任务发过来，指派CrawlActor下载网页内容
      val crawlFuture = Future {
        // 创建一个Future，里面的逻辑包含了下载网页的代码
        try {
          var encoding = "utf-8"
          var outlinks: Set[String] = Set[String]()
          val u = new URL(link)
          val domain = u.getHost
          val uc = u.openConnection().asInstanceOf[HttpURLConnection]
          uc.setConnectTimeout(5000)
          uc.connect()
          if (uc.getResponseCode == 200) {
            // page encoding
            if (uc.getContentEncoding != null) {
              encoding = uc.getContentEncoding
            }
            // page content
            if (uc.getContentLength > 0) {
              val in = uc.getInputStream
              val buffer = Array.fill[Byte](512)(0)
              val baos = new ByteArrayOutputStream
              var bytesRead = in.read(buffer)
              while (bytesRead > -1) {
                baos.write(buffer, 0, bytesRead)
                bytesRead = in.read(buffer)
              }
              outlinks = extractOutlinks(link, baos.toString(encoding)) // 抽取网页中的出链接
              baos.close
            }
            log.info("Page: link=" + link + ", encoding=" + encoding + ", outlinks=" + outlinks)
            CrawledWeb(link, domain, encoding, uc.getContentLength, outlinks)
          }
        } catch {
          case e: Throwable => {
            log.error("Crawl error: " + e.toString)
          }
        }
      }
      // 这里设置了一个回调，当下载网页的Future完成后，会调用这里的回调方法
      crawlFuture.onSuccess {
        // 下载成功，则Future返回CrawledWeb对象
        case crawledWeb: CrawledWeb => {
          log.info("Succeed to crawl: link=" + link + ", crawledWeb=" + crawledWeb)
          if (crawledWeb != null) {
            storeActor ! crawledWeb // 通知PageStoreActor保存网页相关信息
            log.info("Sent crawled data to store actor.")
            q add link // 将链接缓存到队列中
          }
        }
      }
      crawlFuture.onFailure {
        // 下载失败，则Future返回Throwable异常对象，这里简单打印出异常内容
        case exception: Throwable => log.error("Fail to crawl: " + exception.toString)
      }
    }
    case Stored(link, count) => {
      // 当PageStoreActor保存网页信息成功，会给一个Stored通知
      q.remove(link)
      scheduleActor ! (link, count) // 向ScheduleActor汇总统计结果
    }
  }

  def extractOutlinks(parentUrl: String, content: String): Set[String] = {
    // 使用正则表达式抽取页面上的链接
    val outLinks = "href\\s*=\\s*\"([^\"]+)\"".r.findAllMatchIn(content).map { m =>
      var url = m.group(1)
      if (!url.startsWith("http")) {
        url = new URL(new URL(parentUrl), url).toExternalForm
      }
      url
    }.toSet
    // 只保留页面上以html和htm结尾的链接
    outLinks.filter(url => !url.isEmpty && (url.endsWith("html") || url.endsWith("htm")))
  }
}
