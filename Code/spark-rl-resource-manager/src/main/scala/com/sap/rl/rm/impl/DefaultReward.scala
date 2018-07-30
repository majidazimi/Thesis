package com.sap.rl.rm.impl

import java.lang.Math.abs

import com.sap.rl.rm.Action._
import com.sap.rl.rm.{ResourceManagerConfig, Reward, State, StateSpace}

class DefaultReward(config: ResourceManagerConfig, stateSpace: StateSpace) extends Reward {

  import config._

  override def forAction(lastState: State, lastAction: Action, currentState: State): Double = {
    if (isStateInDangerZone(currentState))
      if (lastAction == ScaleOut)
        abs(dangerZoneLatencyDifference(currentState))
      else
        dangerZoneLatencyDifference(currentState)
    else
      MaximumExecutors / currentState.numberOfExecutors
  }

  def dangerZoneLatencyDifference(s: State): Double = CoarseTargetLatency - s.latency + 1

  def safeZoneLatencyDifference(s: State): Double = CoarseTargetLatency - s.latency

  def isStateInDangerZone(s: State): Boolean = s.latency >= CoarseTargetLatency
}

object DefaultReward {
  def apply(constants: ResourceManagerConfig, stateSpace: StateSpace): DefaultReward = new DefaultReward(constants, stateSpace)
}
