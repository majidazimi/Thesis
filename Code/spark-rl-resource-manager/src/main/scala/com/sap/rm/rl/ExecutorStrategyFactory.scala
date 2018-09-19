package com.sap.rm.rl

import com.sap.rm.ResourceManagerConfig
import com.sap.rm.rl.impl.executor.{LinearExecutorStrategy, RelativeIncreaseStaticDecreaseExecutorStrategy, StaticExecutorStrategy}

object ExecutorStrategyFactory {
  def getExecutorStrategy(config: ResourceManagerConfig, rm: TemporalDifferenceResourceManager): ExecutorStrategy = {
    config.ExecutorStrategy match {
      case "static" => StaticExecutorStrategy(config)
      case "linear" => LinearExecutorStrategy(config)
      case "relative" => RelativeIncreaseStaticDecreaseExecutorStrategy(config, rm)
    }
  }
}