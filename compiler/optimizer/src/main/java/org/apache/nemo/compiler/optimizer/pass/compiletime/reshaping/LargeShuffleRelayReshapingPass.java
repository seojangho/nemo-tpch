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
package org.apache.nemo.compiler.optimizer.pass.compiletime.reshaping;

import org.apache.nemo.common.dag.DAG;
import org.apache.nemo.common.dag.DAGBuilder;
import org.apache.nemo.common.ir.edge.executionproperty.DecoderProperty;
import org.apache.nemo.common.ir.edge.executionproperty.EncoderProperty;
import org.apache.nemo.common.ir.edge.executionproperty.CommunicationPatternProperty;
import org.apache.nemo.common.ir.edge.IREdge;
import org.apache.nemo.common.ir.vertex.IRVertex;
import org.apache.nemo.common.ir.vertex.OperatorVertex;
import org.apache.nemo.common.ir.vertex.executionproperty.ResourcePriorityProperty;
import org.apache.nemo.common.ir.vertex.transform.RelayTransform;
import org.apache.nemo.compiler.optimizer.pass.compiletime.Requires;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pass to modify the DAG for a job to batch the disk seek.
 * It adds a {@link OperatorVertex} with {@link RelayTransform} before the vertices
 * receiving shuffle edges,
 * to merge the shuffled data in memory and write to the disk at once.
 */
@Requires(CommunicationPatternProperty.class)
public final class LargeShuffleRelayReshapingPass extends ReshapingPass {

  /**
   * Default constructor.
   */
  public LargeShuffleRelayReshapingPass() {
    super(LargeShuffleRelayReshapingPass.class);
  }

  @Override
  public DAG<IRVertex, IREdge> apply(final DAG<IRVertex, IREdge> dag) {
    final Set<IRVertex> flattenChildrenSet = new HashSet<>();

    final DAGBuilder<IRVertex, IREdge> builder = new DAGBuilder<>();
    dag.topologicalDo(v -> {
      if (flattenChildrenSet.contains(v)) {
        return;
      }

      // We care about OperatorVertices that have any incoming edge that
      // has Shuffle as data communication pattern.
      if (v instanceof OperatorVertex && dag.getIncomingEdgesOf(v).stream().anyMatch(irEdge ->
              CommunicationPatternProperty.Value.Shuffle
          .equals(irEdge.getPropertyValue(CommunicationPatternProperty.class).get()))) {

        // Flatten hack for join Sailfish
        final List<IREdge> inEdges = dag.getIncomingEdgesOf(v);
        final boolean allShuffle = inEdges.stream()
          .allMatch(inEdge -> CommunicationPatternProperty.Value.Shuffle
            .equals(inEdge.getPropertyValue(CommunicationPatternProperty.class).get()));

        if (inEdges.size() > 1 && allShuffle) {
          final OperatorVertex iFileMergerVertex = new OperatorVertex(new RelayTransform());
          builder.addVertex(iFileMergerVertex);

          inEdges.forEach(edge -> {
            final IREdge newEdgeToMerger =
              new IREdge(CommunicationPatternProperty.Value.Shuffle, edge.getSrc(), iFileMergerVertex);
            edge.copyExecutionPropertiesTo(newEdgeToMerger);
            builder.connectVertices(newEdgeToMerger);
          });

          dag.getOutgoingEdgesOf(v)
            .stream()
            .map(outEdge -> outEdge.getDst())
            .forEach(child -> {
              final IREdge newEdgeFromMerger = new IREdge(CommunicationPatternProperty.Value.OneToOne,
                iFileMergerVertex, child);
              newEdgeFromMerger.setProperty(
                EncoderProperty.of(inEdges.get(0).getPropertyValue(EncoderProperty.class).get()));
              newEdgeFromMerger.setProperty(
                DecoderProperty.of(inEdges.get(0).getPropertyValue(DecoderProperty.class).get()));
              builder.addVertex(child);
              builder.connectVertices(newEdgeFromMerger);
              flattenChildrenSet.add(child);
            });

          return;
        }

        builder.addVertex(v);

        dag.getIncomingEdgesOf(v).forEach(edge -> {
          if (CommunicationPatternProperty.Value.Shuffle
                .equals(edge.getPropertyValue(CommunicationPatternProperty.class).get())) {
            // Insert a merger vertex having transform that write received data immediately
            // before the vertex receiving shuffled data.
            final OperatorVertex iFileMergerVertex = new OperatorVertex(new RelayTransform());

            builder.addVertex(iFileMergerVertex);
            final IREdge newEdgeToMerger =
              new IREdge(CommunicationPatternProperty.Value.Shuffle, edge.getSrc(), iFileMergerVertex);
            edge.copyExecutionPropertiesTo(newEdgeToMerger);
            final IREdge newEdgeFromMerger = new IREdge(CommunicationPatternProperty.Value.OneToOne,
                iFileMergerVertex, v);
            newEdgeFromMerger.setProperty(EncoderProperty.of(edge.getPropertyValue(EncoderProperty.class).get()));
            newEdgeFromMerger.setProperty(DecoderProperty.of(edge.getPropertyValue(DecoderProperty.class).get()));
            builder.connectVertices(newEdgeToMerger);
            builder.connectVertices(newEdgeFromMerger);
          } else {
            builder.connectVertices(edge);
          }
        });
      } else { // Others are simply added to the builder.
        builder.addVertex(v);
        dag.getIncomingEdgesOf(v).forEach(builder::connectVertices);
      }
    });
    return builder.build();
  }
}
