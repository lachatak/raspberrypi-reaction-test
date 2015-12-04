package org.kaloz.gpio.reaction

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, SnapshotOffer}
import org.kaloz.gpio.common.BcmPinConversions.GPIOPinConversion
import org.kaloz.gpio.common.BcmPins.{BCM_24, BCM_25}
import org.kaloz.gpio.common.PinController
import org.kaloz.gpio.reaction.ReactionTestControllerActor._
import org.kaloz.gpio.reaction.ReactionTestSessionControllerActor.StartReactionTestSessionCommand
import org.kaloz.gpio.{ReactionTestState, TestResult, User}

class ReactionTestControllerActor(pinController: PinController, reactionTestSessionController: ActorRef) extends PersistentActor with ActorLogging {

  override val persistenceId: String = "reactionTestControllerPersistenceId"

  val startButton = pinController.digitalInputPin(BCM_25("Start"))
  val shutdownButton = pinController.digitalInputPin(BCM_24("Shutdown"))

  var reactionTestState = ReactionTestState()

  initializeDefaultButtons()

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, offeredSnapshot: ReactionTestState) =>
      reactionTestState = offeredSnapshot
      log.info(s"Snapshot has been loaded with ${reactionTestState.testResults.size} test results!")
  }

  def updateState(evt: ReactionTestResultArrivedEvent): Unit = {
    reactionTestState = reactionTestState.update(evt.testResult)
    saveSnapshot(reactionTestState)

    log.info(s"Result for ${evt.testResult.user.nickName} has been persisted --> ${evt.testResult.result.id}!")
    log.info(s"Final score is ${evt.testResult.result.score} --> ${evt.testResult.result.iterations} iterations - ${evt.testResult.result.average} ms avg response time - ${evt.testResult.result.std} std")

    context.system.eventStream.publish(evt)
    context.system.eventStream.publish(ReactionTestResultsUpdatedEvent(reactionTestState.testResults))
    initializeDefaultButtons()
  }

  override def receiveCommand: Receive = LoggingReceive {
    case SaveReactionTestResultCommand(testResult) => persist(ReactionTestResultArrivedEvent(testResult))(updateState)
    case ReactionTestAbortedEvent(userOption) =>
      userOption.fold(log.info(s"Test is aborted without user data..")) { user => log.info(s"Test is aborted for user $user") }
      initializeDefaultButtons()
    case ReactionTestResultsRequest => sender ! ReactionTestResultsResponse(reactionTestState.testResults)
  }

  private def initializeDefaultButtons() = {
    log.info("Waiting test to be started!!")
    startButton.addStateChangeFallEventListener { event =>
      startButton.removeAllListeners()
      shutdownButton.removeAllListeners()
      reactionTestSessionController ! StartReactionTestSessionCommand(self)
    }
    shutdownButton.addStateChangeFallEventListener { event =>
      log.info("Shutdown...")
      pinController.shutdown()
      context.system.terminate()
    }
  }
}

object ReactionTestControllerActor {
  def props(pinController: PinController, reactionTestSessionController: ActorRef) = Props(classOf[ReactionTestControllerActor], pinController, reactionTestSessionController)

  case class SaveReactionTestResultCommand(testResult: TestResult)

  case object ReactionTestResultsRequest

  case class ReactionTestResultsResponse(testResults: List[TestResult])

  case class ReactionTestResultArrivedEvent(testResult: TestResult)

  case class ReactionTestResultsUpdatedEvent(testResults: List[TestResult])

  case class ReactionTestAbortedEvent(user: Option[User])

}
