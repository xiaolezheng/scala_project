package com.lxz.scala.demo1

/**
  * Created by xiaolezheng on 17/8/9.
  */
case class WebUrl(link: String)

case class ScheduledWebUrl(link: String, config: Map[String, Any])

case class CrawledWeb(link: String, domain: String, encoding: String, contentLength: Int, outLinks: Set[String])

case class Stored(link: String, outLinkCount: Int)