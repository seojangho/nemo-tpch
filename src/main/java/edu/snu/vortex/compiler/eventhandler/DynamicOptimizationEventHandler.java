/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.snu.vortex.compiler.eventhandler;

import edu.snu.vortex.common.PubSubEventHandlerWrapper;
import edu.snu.vortex.common.Pair;
import edu.snu.vortex.compiler.ir.MetricCollectionBarrierVertex;
import edu.snu.vortex.compiler.optimizer.Optimizer;
import edu.snu.vortex.runtime.common.plan.physical.PhysicalPlan;
import edu.snu.vortex.runtime.common.plan.physical.TaskGroup;
import edu.snu.vortex.runtime.master.eventhandler.DynamicOptimizationEvent;
import org.apache.reef.wake.impl.PubSubEventHandler;

import javax.inject.Inject;

/**
 * Class for handling event to perform dynamic optimization.
 */
public final class DynamicOptimizationEventHandler implements RuntimeEventHandler<DynamicOptimizationEvent> {
  private final PubSubEventHandler pubSubEventHandler;

  @Inject
  private DynamicOptimizationEventHandler(final PubSubEventHandlerWrapper pubSubEventHandlerWrapper) {
    this.pubSubEventHandler = pubSubEventHandlerWrapper.getPubSubEventHandler();
  }

  @Override
  public Class<DynamicOptimizationEvent> getEventClass() {
    return DynamicOptimizationEvent.class;
  }

  @Override
  public void onNext(final DynamicOptimizationEvent dynamicOptimizationEvent) {
    final PhysicalPlan physicalPlan = dynamicOptimizationEvent.getPhysicalPlan();
    final MetricCollectionBarrierVertex metricCollectionBarrierVertex =
            dynamicOptimizationEvent.getMetricCollectionBarrierVertex();

    final Pair<String, TaskGroup> taskInfo = dynamicOptimizationEvent.getTaskInfo();

    final PhysicalPlan newPlan = Optimizer.dynamicOptimization(physicalPlan, metricCollectionBarrierVertex);

    pubSubEventHandler.onNext(new UpdatePhysicalPlanEvent(newPlan, taskInfo));
  }
}
