package com.gabrielspassos.logger

import org.springframework.stereotype.Component

@Component
class Logger {

  def logInfo(message: String): Unit = {
    println(s"[INFO] $message")
  }

  def logError(message: String): Unit = {
    println(s"[ERROR] $message")
  }

  def logError(message: String, exception: Exception): Unit = {
    println(s"[ERROR] $message $exception")
  }

}
