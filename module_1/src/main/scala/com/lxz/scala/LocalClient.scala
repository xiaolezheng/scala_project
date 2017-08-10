package com.lxz.scala

import akka.actor.{ActorSystem, Props}
import com.lxz.scala.common._

/**
  * Created by xiaolezheng on 17/8/9.
  */
object LocalClient extends App {
  // Local actor
  val system = ActorSystem("local-system") // 创建一个ActorSystem对象，用来管理Actor实例
  println(system)
  val localActorRef = system.actorOf(Props[LocalActor], "local-actor") // 通过ActorSystem对象，获取到一个Actor的引用
  println(localActorRef)
  localActorRef ! Start // 向LocalActor发送Start消息
  localActorRef ! Heartbeat("3099100", 0xabcd) // 向LocalActor发送Heartbeat消息
  localActorRef ! Packet("3000001", System.currentTimeMillis(), "hello") // 向LocalActor发送Packet消
  localActorRef ! Stop // 停止LocalActor实例

  Thread.sleep(2000)
  system.terminate() // 终止ActorSystem对象，释放资源
}
