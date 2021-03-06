/*
 * Copyright (C) 2018 Seoul National University
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
package org.apache.nemo.runtime.master;

/**
 * Handler for aggregating data used in dynamic optimization.
 */
public interface DynOptDataHandler {
  /**
   * Updates data for dynamic optimization sent from Tasks.
   * @param dynOptData data used for dynamic optimization.
   */
  void updateDynOptData(Object dynOptData);

  /**
   * Returns aggregated data for dynamic optimization.
   * @return aggregated data used for dynamic optimization.
   */
  Object getDynOptData();
}
