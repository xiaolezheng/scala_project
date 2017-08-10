package com.lxz.scala

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.lxz.scala.common._
import scala.util.Random

/**
  * Created by xiaolezheng on 17/8/9.
  */
object AkkaClientApplication extends App {
  val system = ActorSystem("client-system", ConfigFactory.load().getConfig("MyRemoteClientSideActor")) // 通过配置文件application.conf配置创建ActorSystem系统

  val log = system.log

  val clientActor = system.actorOf(Props[ClientActor], "clientActor")
  val ID = new AtomicLong(90760000)
  @volatile var running = true
  val hbInterval = 1000
  lazy val hbWorker = createHBWorker

  /**
    * create heartbeat worker thread
    */
  def createHBWorker: Thread = {
    // 心跳发送线程
    new Thread("HB-WORKER") {
      override def run(): Unit = {
        while (running) {
          clientActor ! Heartbeat("HB", 39264)
          Thread.sleep(hbInterval)
        }
      }
    }
  }

  def format(timestamp: Long, format: String): String = {
    val df = new SimpleDateFormat(format)
    df.format(new Date(timestamp))
  }

  def createPacket(packet: Map[String, _]): Map[String, _] = {
    return packet;
  }

  def nextTxID: Long = {
    ID.incrementAndGet()
  }

  def nextProvider(seq: Seq[String]): String = {
    seq(r.nextInt(seq.size))
  }

  clientActor ! Start // 发送一个Start消息，第一次与远程Actor握手（通过本地ClientActor进行转发）
  Thread.sleep(2000)

  clientActor ! Header("HEADER", 20, encrypted = false) // 发送一个Header消息到远程Actor（通过本地ClientActor进行转发）
  Thread.sleep(2000)

  hbWorker.start // 启动心跳线程
  // send some packets

  val DT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"

  val r = Random
  val packetCount = 100
  val serviceProviders = Seq("CMCC", "AKBBC", "OLE")
  val payServiceProvicers = Seq("PayPal", "CMB", "ICBC", "ZMB", "XXB")


  val startWhen = System.currentTimeMillis()
  for (i <- 0 until packetCount) {
    // 持续发送packetCount个Packet消息
    val pkt = createPacket(Map[String, Any](
      "txid" -> nextTxID,
      "pvid" -> nextProvider(serviceProviders),
      "txtm" -> format(System.currentTimeMillis(), DT_FORMAT),
      "payp" -> nextProvider(payServiceProvicers),
      "amount" -> 1000 * r.nextFloat()))
    clientActor ! Packet("PKT", System.currentTimeMillis, pkt.toString)
  }

  val finishWhen = System.currentTimeMillis()
  log.info("FINISH: timeTaken=" + (finishWhen - startWhen) + ", avg=" + packetCount / (finishWhen - startWhen))

  Thread.sleep(2000)
  // ask remote actor to shutdown
  val waitSecs = hbInterval

  clientActor ! Shutdown(waitSecs) // 发送Packet消息完成，通知远程Actor终止服务

  running = false
  while (hbWorker.isAlive) {
    // 终止心跳线程
    log.info("Wait heartbeat worker to exit...")
    TimeUnit.SECONDS.sleep(2)
  }
  system.terminate() // 终止本地ActorSystem系统
}