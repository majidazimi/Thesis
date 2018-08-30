package com.sap.rm.rl.impl.statespace

import com.sap.rm.ResourceManagerConfig
import com.sap.rm.rl.{StateSpace, StateSpaceInitializer}

class OptimizedStateSpaceInitializer(config: ResourceManagerConfig) extends StateSpaceInitializer {
  import config._

  override def initialize(space: StateSpace): StateSpace = {
    for {
      exe <- MinimumExecutors to MaximumExecutors
      lat <- 0 until CoarseMaximumLatency
    } {
      if (lat < CoarseTargetLatency) {
        if (exe == MinimumExecutors) {
          space.addState(exe, lat, loadIsIncreasing = false, scaleOutReward = -BestReward, noActionReward = BestReward, scaleInReward = NoReward)
          space.addState(exe, lat, loadIsIncreasing = true, scaleOutReward = -BestReward, noActionReward = BestReward, scaleInReward = NoReward)
        } else {
          val normalizedLatency = normalize(lat)
          space.addState(exe, lat, loadIsIncreasing = true, scaleOutReward = -BestReward, noActionReward = normalizedLatency, scaleInReward = 1 - normalizedLatency)
          space.addState(exe, lat, loadIsIncreasing = false, scaleOutReward = -BestReward, noActionReward = NoReward, scaleInReward = 1 - normalizedLatency)
        }
      } else {
        if (exe == MaximumExecutors) {
          space.addState(exe, lat, loadIsIncreasing = true, scaleOutReward = NoReward, noActionReward = BestReward, scaleInReward = -BestReward)
        } else {
          space.addState(exe, lat, loadIsIncreasing = true, scaleOutReward = BestReward, noActionReward = NoReward, scaleInReward = -BestReward)
        }
        space.addState(exe, lat, loadIsIncreasing = false, scaleOutReward = NoReward, noActionReward = BestReward, scaleInReward = -BestReward)
      }
    }

    space
  }

  private def normalize(latency: Int): Double = latency.toDouble / CoarseTargetLatency
}

object OptimizedStateSpaceInitializer {
  def apply(config: ResourceManagerConfig): OptimizedStateSpaceInitializer = new OptimizedStateSpaceInitializer(config)
}
