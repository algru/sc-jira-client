package com.github.algru.common.config

import com.typesafe.config.{Config, ConfigFactory}

trait Configuration {
  val baseName = ""
  val config: Config = ConfigFactory.load(baseName)
}
