package com.lxz.scala.module3.demo1

import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by xiaolezheng on 17/8/10.
  */
object FutureTest extends App {
  val log = LoggerFactory.getLogger("FutureTest")
  implicit val ec = ExecutionContext.global

  case class Ratio(ratio: Double)

  case class Number(count: Int)

  case class Amount(r: Ratio, n: Number) {
    def sum(): Double = {
      return r.ratio * n.count
    }
  }


  var ratioF = Future {
    new Ratio(2.5)
  }

  var numberF = Future {
    new Number(10)
  }

  var amountF = for {
    ratio <- ratioF
    number <- numberF
  } yield new Amount(ratio, number)


  amountF.onSuccess {
    case a: Amount => log.info("success, result: {}", a.sum())
  }

  TimeUnit.SECONDS.sleep(5)
}
