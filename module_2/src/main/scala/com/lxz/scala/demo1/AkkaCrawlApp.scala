package com.lxz.scala.demo1

import akka.actor.{ActorSystem, Props}

/**
  * Created by xiaolezheng on 17/8/9.
  */
object AkkaCrawlApp {

  def main(args: Array[String]) {
    val system = ActorSystem("crawler-system") // 创建一个ActorSystem
    system.log.info(system.toString)

    system.actorOf(Props[ScheduleActor], name = "schedule-actor")
    // 创建ScheduleActor
    system.actorOf(Props[PageStoreActor], name = "store-actor")
    // 创建PageStoreActor
    val crawlActorRef = system.actorOf(Props[CrawlActor], name = "crawl-actor")
    // 创建CrawlActor
    val links =
      """
        |http://apache.org
        |http://csdn.net
        |http://hadoop.apache.org
        |http://spark.apache.org
        |http://nutch.apache.org
        |http://storm.apache.org
        |http://mahout.apache.org
        |http://flink.apache.org
      """.stripMargin
    val seeds: Seq[String] = links.split("\\s+").toSeq
    ScheduleActor.sendFeeds(crawlActorRef, seeds) // 调用ScheduleActor的伴生对象的sendFeeds，将爬虫入口seed链接发送给CrawlActor
  }

}
