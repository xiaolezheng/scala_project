package com.lxz.scala.demo1

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, ActorLogging, ActorRef}

/**
  * Created by xiaolezheng on 17/8/9.
  */
class ScheduleActor extends Actor with ActorLogging {
  val config = Map(
    "domain.black.list" -> Seq("google.com", "facebook.com", "twitter.com"),
    "crawl.retry.times" -> 3,
    "filter.page.url.suffixes" -> Seq(".zip", ".avi", ".mkv")
  )

  val counter = new ConcurrentHashMap[String, Int]()

  def receive = {
    case WebUrl(url) => {
      sender ! ScheduledWebUrl(url, config)
    }
    case (link: String, count: Int) => {
      counter.put(link, count)
      log.info("Counter: " + counter.toString)
    }
  }
}

object ScheduleActor {
  def sendFeeds(crawlerActorRef: ActorRef, seeds: Seq[String]): Unit = {
    seeds.foreach(crawlerActorRef ! _)
  }
}

