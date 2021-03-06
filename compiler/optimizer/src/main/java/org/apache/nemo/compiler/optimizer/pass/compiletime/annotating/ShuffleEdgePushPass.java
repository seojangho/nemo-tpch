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
package org.apache.nemo.compiler.optimizer.pass.compiletime.annotating;

import org.apache.nemo.common.dag.DAG;
import org.apache.nemo.common.ir.edge.IREdge;
import org.apache.nemo.common.ir.edge.executionproperty.CommunicationPatternProperty;
import org.apache.nemo.common.ir.edge.executionproperty.DataFlowProperty;
import org.apache.nemo.common.ir.vertex.IRVertex;
import org.apache.nemo.compiler.optimizer.pass.compiletime.Requires;

import java.util.List;

/**
 * A pass for tagging shuffle edges different from the default ones.
 * It sets DataFlowModel ExecutionProperty as "push".
 */
@Annotates(DataFlowProperty.class)
@Requires(CommunicationPatternProperty.class)
public final class ShuffleEdgePushPass extends AnnotatingPass {
  /**
   * Default constructor.
   */
  public ShuffleEdgePushPass() {
    super(ShuffleEdgePushPass.class);
  }

  @Override
  public DAG<IRVertex, IREdge> apply(final DAG<IRVertex, IREdge> dag) {
    dag.getVertices().forEach(vertex -> {
      final List<IREdge> inEdges = dag.getIncomingEdgesOf(vertex);
      if (!inEdges.isEmpty()) {
        inEdges.forEach(edge -> {
          if (edge.getPropertyValue(CommunicationPatternProperty.class).get()
              .equals(CommunicationPatternProperty.Value.Shuffle)) {
            edge.setProperty(DataFlowProperty.of(DataFlowProperty.Value.Push));
          }
        });
      }
    });
    return dag;
  }
}
