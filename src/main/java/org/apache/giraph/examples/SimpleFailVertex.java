/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.examples;

import org.apache.giraph.graph.EdgeListVertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

/**
 * Vertex to allow unit testing of failure detection
 */
public class SimpleFailVertex extends EdgeListVertex<
    LongWritable, DoubleWritable, FloatWritable, DoubleWritable> {
  /** Class logger */
  private static Logger LOG = Logger.getLogger(SimpleFailVertex.class);
  /** TODO: Change this behavior to WorkerContext */
  private static long SUPERSTEP = 0;

  @Override
  public void compute(Iterable<DoubleWritable> messages) {
    if (getSuperstep() >= 1) {
      double sum = 0;
      for (DoubleWritable message : messages) {
        sum += message.get();
      }
      DoubleWritable vertexValue =
          new DoubleWritable((0.15f / getTotalNumVertices()) + 0.85f * sum);
      setValue(vertexValue);
      if (getSuperstep() < 30) {
        if (getSuperstep() == 20) {
          if (getId().get() == 10L) {
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
              LOG.info("Sleep interrupted ", e);
            }
            System.exit(1);
          } else if (getSuperstep() - SUPERSTEP > 10) {
            return;
          }
        }
        long edges = getNumEdges();
        sendMessageToAllEdges(
            new DoubleWritable(getValue().get() / edges));
      } else {
        voteToHalt();
      }
      SUPERSTEP = getSuperstep();
    }
  }
}
