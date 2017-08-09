package com.lxz.scala

import akka.actor.{Actor, ActorLogging}

/**
  * Created by xiaolezheng on 17/8/9.
  */
class LocalActor extends Actor with ActorLogging {
  def receive = {
    case Start => log.info("start")
    case Stop => log.info("stop")
    case Heartbeat(id, magic) => log.info("Heartbeat" + (id, magic))
    case Header(id, len, encrypted) => log.info("Header" + (id, len, encrypted))
    case Packet(id, seq, content) => log.info("Packet" + (id, seq, content))
    case _ =>
  }
}

