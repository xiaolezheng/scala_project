package com.lxz.scala.module3.demo1

import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by xiaolezheng on 17/8/10.
  */
object IOTest extends App {
  val log = LoggerFactory.getLogger("IOTest")
  implicit val ec = ExecutionContext.global

  val f = Future {
    val inputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream("logback.xml")
    val source = scala.io.Source.fromInputStream(inputStream)
    source.toSeq.size
  }

  f.onComplete {
    case Success(size) => log.info("success: {}", size)
    case Failure(e) => log.error("", e)
  }

  TimeUnit.SECONDS.sleep(5)
}
