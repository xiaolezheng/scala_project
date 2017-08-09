package com.lxz.scala.demo1

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by xiaolezheng on 17/8/9.
  */
class PageStoreActor extends Actor with ActorLogging {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool())
  var crawlerRef = context.actorOf(Props[CrawlActor], name = "crawl-actor")

  def receive = {
    case CrawledWeb(link, domain, encoding, contentLength, outLinks) => {
      val future = Future {
        var sqls = Set[String]()
        try {
          val createTime = DatetimeUtils.format(System.currentTimeMillis)
          val sql = "INSERT INTO web_link VALUES ('" + link + "','" + domain + "','" + encoding + "'," + contentLength + ",'" + createTime + "')"
          log.info("Link SQL: " + sql)
          sqls += sql
          var outLinksSql = "INSERT INTO web_outlink VALUES "
          outLinksSql += outLinks.map("('" + link + "','" + _ + "','" + createTime + "')").mkString(",")
          log.info("Outlinks SQL: " + outLinksSql)
          sqls += outLinksSql
          // 使用了事务操作
          MySQLUtils.doTrancation(sqls)
          (link, outLinks.size)
        } catch {
          case e: Throwable => throw e
        }
      }
      // 这里也使用了Future的回调功能
      future.onSuccess {
        case (link: String, outlinkCount: Int) => {
          log.info("SUCCESS: link=" + link + ", outlinkCount=" + outlinkCount)
          crawlerRef ! Stored(link, outlinkCount) // 将持久化后的链接及其outlink数量回馈给CrawlActor
        }
      }
      future.onFailure {
        case e: Throwable => log.error("failed: {}", e.getMessage)
      }
    }
  }

}
