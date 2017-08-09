package com.lxz.scala.demo1

import org.apache.commons.lang3.time.FastDateFormat

import scala.collection.mutable

/**
  * Created by xiaolezheng on 17/8/9.
  */
object DatetimeUtils {
  val DEFAULT_DT_FORMAT = "yyyy-MM-dd HH:mm:ss"
  val DEFAULT_FORMAT = FastDateFormat.getInstance(DEFAULT_DT_FORMAT)
  val CACHE = mutable.HashMap[String, FastDateFormat](DEFAULT_DT_FORMAT -> DEFAULT_FORMAT)

  def format(timeStamp: Long, format: String): String = {
    var df: FastDateFormat = null
    if (CACHE.contains(format)) {
      df = CACHE(format)
    } else {
      df = FastDateFormat.getInstance(format)
      CACHE.put(format, df)
    }

    return df.format(timeStamp)
  }

  def format(timeStamp: Long): String = {
    return DEFAULT_FORMAT.format(timeStamp)
  }

  def main(args: Array[String]): Unit = {
    println(DatetimeUtils.format(System.currentTimeMillis()))
    println(DatetimeUtils.format(System.currentTimeMillis(), "yyyy-MM-dd"))
  }

}
