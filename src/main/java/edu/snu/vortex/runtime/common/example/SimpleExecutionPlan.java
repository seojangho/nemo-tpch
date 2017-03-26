/*
 * Copyright (C) 2017 Seoul National University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.snu.vortex.runtime.common.example;

import edu.snu.vortex.runtime.common.plan.logical.ExecutionPlanBuilder;

/**
 * Simple Execution Plan.
 */
public final class SimpleExecutionPlan {
  private SimpleExecutionPlan() {
  }

  public static void main(final String[] args) {
    // TODO #000: Move this example to a test.
    final ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
  }
}