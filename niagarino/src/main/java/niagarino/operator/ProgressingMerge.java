/*
 * @(#)ProgressingMerge.java   1.0   Feb 21, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Merges the given streams along the progressing attribute of the first stream in the list. This operator
 * assumes that the values of the progressing attribute are unique and ordered.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class ProgressingMerge extends AbstractOperator {

   /** Output schema of this operator. */
   private final Schema outputSchema;
   /** Bit vector to keep track of which inputs have received an EOF message. */
   private final boolean[] inputEoS;
   /** Tuple buffer for each operator input. */
   private final List<Queue<DataTuple>> buffers;
   /** Tuples that are currently being merged. */
   private final DataTuple[] currentTuples;

   /**
    * Constructs a new progressing merge operators with the given input schemas.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchemas
    *           input schemas
    */
   public ProgressingMerge(final String operatorId, final Schema... inputSchemas) {
      super(operatorId, Arrays.asList(inputSchemas));
      this.outputSchema = this.initSchema();
      this.inputEoS = new boolean[inputSchemas.length];
      Arrays.fill(this.inputEoS, false);
      this.buffers = new ArrayList<Queue<DataTuple>>();
      for (int i = 0; i < inputSchemas.length; i++) {
         this.buffers.add(new LinkedList<DataTuple>());
      }
      this.currentTuples = new DataTuple[inputSchemas.length];
   }

   /**
    * Constructs a new progressing merge operators with the given input schemas.
    *
    * @param inputSchemas
    *           input schemas
    */
   public ProgressingMerge(final Schema... inputSchemas) {
      this(ProgressingMerge.class.getSimpleName(), inputSchemas);
   }

   /**
    * Initializes the output schema of this operator.
    *
    * @return output schema
    */
   private Schema initSchema() {
      final List<Schema> inputSchemas = this.getInputSchemas();
      if (inputSchemas.size() <= 1) {
         throw new IllegalArgumentException("Progressing merge needs at least two inputs.");
      }
      final Schema baseSchema = inputSchemas.get(0);
      final int progressingAttribute = baseSchema.getProgressingAttribute();
      final List<Attribute> attributes = new ArrayList<Attribute>(baseSchema.getAttributes());
      for (int i = 1; i < inputSchemas.size(); i++) {
         final Schema mergeSchema = inputSchemas.get(i);
         for (int j = 0; j < mergeSchema.getSize(); j++) {
            if (j != mergeSchema.getProgressingAttribute()) {
               attributes.add(mergeSchema.getAttribute(j));
            }
         }
      }
      return new Schema(progressingAttribute, attributes.toArray(new Attribute[0]));
   }

   @Override
   public Schema getOutputSchema() {
      return this.outputSchema;
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      // enqueue tuple
      final Queue<DataTuple> queue = this.buffers.get(input);
      queue.add(tuple);
      // flush all possible tuples
      this.flushTuples();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Socket.INPUT.equals(socket)) {
         this.inputEoS[input] = true;
         if (this.allInputEof()) {
            this.flushTuples();
         }
      }
      super.handleEoS(socket, input, message);
   }

   /**
    * Checks whether all inputs have received an EOF message.
    *
    * @return <code>true</code> if all inputs have received an EOF message, <code>false</code> otherwise
    */
   private boolean allInputEof() {
      boolean done = true;
      for (final boolean b : this.inputEoS) {
         done = done && b;
      }
      return done;
   }

   /**
    * Checks if the given array contains no <code>null</code> values.
    *
    * @param array
    *           object array
    * @return <code>true</code> if the array contains no <code>null</code> values, <code>false</code>
    *         otherwise
    */
   private boolean noNulls(final Object[] array) {
      boolean result = true;
      for (final Object object : array) {
         result = result && object != null;
      }
      return result;
   }

   /**
    * Returns the progressing value of the given tuple and converts it to a double.
    *
    * @param tuple
    *           data tuple
    * @return progressing value as a double
    */
   private double getProgressingValue(final DataTuple tuple) {
      final Object object = tuple.getProgressingValue();
      if (object instanceof Number) {
         return ((Number) object).doubleValue();
      } else if (object instanceof Date) {
         return ((Date) object).getTime();
      } else {
         return Double.NaN;
      }
   }

   /**
    * Emits all possible matching tuples from the input buffers.
    */
   private void flushTuples() {
      boolean done = false;
      do {
         // initialize current tuples
         for (int i = 0; i < this.currentTuples.length; i++) {
            if (this.currentTuples[i] == null) {
               this.currentTuples[i] = this.buffers.get(i).poll();
            }
         }
         // check if complete tuples can be emitted
         if (this.noNulls(this.currentTuples)) {
            final List<Object> values = new ArrayList<Object>();
            values.addAll(this.currentTuples[0].getValues());
            final double baseValue = this.getProgressingValue(this.currentTuples[0]);
            for (int i = 1; i < this.currentTuples.length; i++) {
               final DataTuple peek = this.buffers.get(i).peek();
               if (peek != null && baseValue >= this.getProgressingValue(peek)) {
                  this.currentTuples[i] = this.buffers.get(i).poll();
               }
               if (peek != null || this.inputEoS[i]) {
                  for (int j = 0; j < this.currentTuples[i].getValues().size(); j++) {
                     if (j != this.currentTuples[i].getSchema().getProgressingAttribute()) {
                        values.add(this.currentTuples[i].getValues().get(j));
                     }
                  }
               }
            }
            if (values.size() == this.getOutputSchema().getSize()) {
               final DataTuple tuple = new DataTuple(this.getOutputSchema(), values);
               this.pushTuple(tuple);
               // mark base tuple as processed
               this.currentTuples[0] = null;
            } else {
               done = true;
            }
         } else {
            done = true;
         }
      } while (!done);
   }
}
