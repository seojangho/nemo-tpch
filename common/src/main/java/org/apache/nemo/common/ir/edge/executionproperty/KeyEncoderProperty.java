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
package org.apache.nemo.common.ir.edge.executionproperty;

import org.apache.nemo.common.coder.EncoderFactory;
import org.apache.nemo.common.ir.executionproperty.EdgeExecutionProperty;

/**
 * KeyEncoder ExecutionProperty.
 */
public final class KeyEncoderProperty extends EdgeExecutionProperty<EncoderFactory> {
  /**
   * Constructor.
   *
   * @param value value of the execution property.
   */
  private KeyEncoderProperty(final EncoderFactory value) {
    super(value);
  }

  /**
   * Static method exposing the constructor.
   *
   * @param value value of the new execution property.
   * @return the newly created execution property.
   */
  public static KeyEncoderProperty of(final EncoderFactory value) {
    return new KeyEncoderProperty(value);
  }
}
