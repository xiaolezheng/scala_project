package com.lxz.scala.demo2

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future

/**
  * Created by xiaolezheng on 17/8/9.
  */
object Demo2 extends App {

  case class BasicInfo(id: Int, name: String, age: Int)

  case class InterestInfo(id: Int, interest: String)

  case class Person(basicInfo: BasicInfo, interestInfo: InterestInfo)

  class BasicInfoActor extends Actor with ActorLogging {
    override def receive = {
      case id: Int => {
        log.info("basic id= {}", id)
        sender ! new BasicInfo(id, "Join", 20)
      }

      case _ => {
        log.warning("received unknown message")
      }
    }
  }

  class InterestInfoActor extends Actor with ActorLogging {
    override def receive = {
      case id: Int => {
        log.info("interest id: {}", id)
        sender ! new InterestInfo(id, "乒乓球")
      }

      case _ => {
        log.warning("received unknown message")
      }
    }
  }

  class PersonActor extends Actor with ActorLogging {
    override def receive = {
      case person: Person => log.info("Person=" + person)
      case _ => log.warning("received unknown message")
    }
  }

  class CombineActor extends Actor with ActorLogging {
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)
    val basicInfoActor = context.actorOf(Props[BasicInfoActor], name = "BasicInfo-Actor")
    val interestInfoActor = context.actorOf(Props[InterestInfoActor], name = "InterestInfo-Actor")
    val personActor = context.actorOf(Props[PersonActor], name = "Person-Actor")


    override def receive = {
      case id: Int => {
        val combineResult: Future[Person] =
          for {
          //向basicInfoActor发送Send-And-Receive-Future消息，mapTo方法将返回结果映射为BasicInfo类型
            basicInfo <- (basicInfoActor ? id).mapTo[BasicInfo]
            //向interestInfoActor发送Send-And-Receive-Future消息，mapTo方法将返回结果映射为InterestInfo类型
            interestInfo <- (interestInfoActor ? id).mapTo[InterestInfo]
          } yield Person(basicInfo, interestInfo)

        pipe(combineResult).to(personActor)
      }
    }
  }


  val _system = ActorSystem("Send-And-Receive-Future")
  val combineActor = _system.actorOf(Props[CombineActor], name = "CombineActor")
  combineActor ! 12345
  Thread.sleep(5000)
  _system.terminate()

}
