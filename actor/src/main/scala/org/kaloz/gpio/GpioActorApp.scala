package org.kaloz.gpio

import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Await
import scala.concurrent.duration._

object GpioActorApp extends App with GpioActorAppDI with StrictLogging {

  logger.info("Waiting for termination...")
  Await.result(system.whenTerminated, Duration.Inf)

}