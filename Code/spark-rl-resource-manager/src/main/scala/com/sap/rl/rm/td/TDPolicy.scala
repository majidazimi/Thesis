package com.sap.rl.rm.td

import com.sap.rl.rm.{Action, Policy, State}
import org.apache.log4j.LogManager
import org.apache.spark.streaming.scheduler.RMConstants

class TDPolicy(constants: RMConstants, stateSpace: TDStateSpace) extends Policy {

  @transient private lazy val log = LogManager.getLogger(this.getClass)

  import constants._
  import Action._
  import com.sap.rl.rm.LogStatus._

  override def nextActionFrom(lastState: State, lastAction: Action, currentState: State): Action = {
    if (currentState.latency >= CoarseTargetLatency) {
      log.warn(s""" --- SLO VIOLATION ---
              | lastState=$lastState
              | lastAction=$lastAction
              | currentState=$currentState""".stripMargin)
    }


    if (currentState.latency < CoarseMinimumLatency) return ScaleIn

    val currentExecutors = currentState.numberOfExecutors
    val qValues = currentExecutors match {
      case MinimumExecutors =>
        log.warn(s""" --- $EXEC_NOT_ENOUGH ---
                    | lastState=$lastState
                    | lastAction=$lastAction
                    | currentState=$currentState""".stripMargin)
        stateSpace(currentState).filterKeys{ _ != ScaleIn }
      case MaximumExecutors =>
        log.warn(s""" --- $EXEC_EXCESSIVE ---
                    | lastState=$lastState
                    | lastAction=$lastAction
                    | currentState=$currentState""".stripMargin)
        stateSpace(currentState).filterKeys{ _ != ScaleOut }
      case _ => stateSpace(currentState)
    }

    // monotonicity property
    if (currentState.latency < lastState.latency && lastAction == ScaleIn)
      qValues.filterKeys{ _ != ScaleOut }.maxBy{ _._2 }._1
    else if (currentState.latency > lastState.latency && lastAction == ScaleOut)
      qValues.filterKeys{ _ != ScaleIn }.maxBy{ _._2 }._1
    else
      qValues.maxBy{_._2}._1
  }
}

object TDPolicy {
  def apply(constants: RMConstants, stateSpace: TDStateSpace): TDPolicy = new TDPolicy(constants, stateSpace)
}
