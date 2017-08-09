package com.lxz.scala.demo1

import java.sql.{Connection, DriverManager, SQLException}

/**
  * Created by xiaolezheng on 17/8/9.
  */
object MySQLUtils {
  val driverClass = "com.mysql.jdbc.Driver"
  val jdbcUrl = "jdbc:mysql://127.0.0.1:3306/page_db"
  val user = "web"
  val password = "web"

  try {
    Class.forName(driverClass)
  } catch {
    case e: ClassNotFoundException => throw e
    case e: Exception => throw e
  }


  @throws(classOf[SQLException])
  def getConnection: Connection = {
    DriverManager.getConnection(jdbcUrl, user, password)
  }

  @throws(classOf[SQLException])
  def doTrancation(transactions: Set[String]): Unit = {
    val connection = getConnection
    connection.setAutoCommit(false)
    transactions.foreach {
      connection.createStatement.execute(_)
    }
    connection.commit
    connection.close
  }
}