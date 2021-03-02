package com.github.algru.common.exception.util

import org.slf4j.Logger

object ExceptionThrower {
  def throwAndLog[T <: Exception](message: String, exceptionClass: Class[T], log: Logger): Nothing = {
    val exception = exceptionClass.getDeclaredConstructor(classOf[String]).newInstance(message)
    log.error(message, exception)
    throw exception
  }

  def throwAndLog[T <: Exception](message: String, log: Logger): Nothing = throwAndLog(message, classOf[Exception], log)
}
