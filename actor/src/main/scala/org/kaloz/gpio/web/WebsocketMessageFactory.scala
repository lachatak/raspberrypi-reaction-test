package org.kaloz.gpio.web

import akka.http.scaladsl.model.ws.TextMessage
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.kaloz.gpio.TestResult
import org.kaloz.gpio.reaction.SingleLedReactionTestActor.{Missed, Failure}

object WebSocketMessageFactory {

  implicit val jsonFormat = org.json4s.DefaultFormats

  def waitingStartSignalMessage = asTextMessage(("type" -> "waitingStartSignal"))

  def openUserRegistrationMessage = asTextMessage(("type" -> "openUserRegistration"))

  def gameInProgressMessage = asTextMessage(("type" -> "gameInProgress"))

  def currentResultMessage(testResult: TestResult) = asTextMessage(("type" -> "currentResult") ~ ("currentResult" -> asJValue(testResult)))

  def leaderBoardStateMessage(leaderBoard: List[TestResult]) = {
    asTextMessage(("type" -> "leaderBoard") ~ ("leaderBoard" -> leaderBoard.map(asJValue)))
  }

  private def asJValue(result: TestResult) = {
    ("nickName" -> result.user.nickName) ~ ("email" -> result.user.email) ~ ("score" -> result.result.score) ~ ("iterations" -> result.result.iterations) ~ ("average" -> result.result.average) ~ ("std" -> result.result.std) ~ ("failures" -> result.result.numberOf(Failure)) ~ ("missed" -> result.result.numberOf(Missed))
  }

  private def asTextMessage(json: JObject) = TextMessage(compact(render(json)))
}