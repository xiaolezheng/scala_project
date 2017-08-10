package com.lxz.scala

import akka.actor.{Actor, ActorLogging}
import com.lxz.scala.common._

/**
  * Created by xiaolezheng on 17/8/9.
  */
class RemoteActor extends Actor with ActorLogging {
  // 模拟处理结果状态，发送给消息的发送方
  val SUCCESS = "SUCCESS"
  val FAILURE = "FAILURE"

  override def receive = {
    case Start => {
      // 处理Start消息
      log.info("RECV event: " + Start)
    }
    case Stop => {
      // 处理Stop消息
      log.info("RECV event: " + Stop)
    }
    case Shutdown(waitSecs) => {
      // 处理Shutdown消息
      log.info("Wait to shutdown: waitSecs=" + waitSecs)
      Thread.sleep(waitSecs)
      log.info("Shutdown this system.")
      context.system.terminate() // 停止当前ActorSystem系统
    }
    case Heartbeat(id, magic) => log.info("RECV heartbeat: " + (id, magic)) // 处理Heartbeat消息
    case Header(id, len, encrypted) => log.info("RECV header: " + (id, len, encrypted)) // 处理Header消息
    case Packet(id, seq, content) => {
      // 处理Packet消息
      val originalSender = sender // 获取到当前发送方的Actor引用
      log.info("RECV packet: " + (id, seq, content))
      originalSender ! (seq, SUCCESS) // 响应给发送方消息处理结果，类似发送一个ACK
    }
    case _ =>

  }

}
