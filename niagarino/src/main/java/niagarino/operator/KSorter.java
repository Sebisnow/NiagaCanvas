/*
 * @(#)KSorter.java   1.0   May 28, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;

import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Sorts incoming tuples that are k-sorted. When a punctuation comes in, all tuples with a value lower than
 * the punctuation will be sorted and emitted in correct order.<br />
 * <br />
 * TODO This implementation is not very efficient. PriorityQueue has insert and remove runtime O(log n).
 *
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class KSorter extends AbstractOperator {

   /** Comparator for tuples. */
   private final Comparator<DataTuple> comparator;

   /** Buffer for previous tuples. */
   private final PriorityQueue<DataTuple> buffer;

   /** Maximum slack. */
   private long slack;

   /** Minimum of queue. */
   private long min;

   /** Maximum of queue. */
   private long max;

   /**
    * Constructs a new KSorter with the given comparator determining the sort order.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           input schema
    * @param comparator
    *           comparator for sorting tuples
    * @param slack
    *           how much slack the operator shall use to account for disorder
    */
   public KSorter(final String operatorId, final Schema inputSchema, final Comparator<DataTuple> comparator,
         final long slack) {
      super(operatorId, Arrays.asList(inputSchema));
      this.comparator = comparator;
      this.buffer = new PriorityQueue<>(this.comparator);
      this.slack = slack;
      this.min = Long.MAX_VALUE;
      this.max = Long.MIN_VALUE;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final long value = this.getValue(tuple);

      if (this.max - value > this.slack && this.max > value) {
         // Log.warn("Dropped tuple because it arrived too late: " + tuple);
         // System.out.println("Dropped tuple " + tuple.getProgressingValue() + ", current min ["
         // + this.buffer.peek().getProgressingValue() + "], current max [" + new Date(this.max) + "]");
         return;
      }

      this.buffer.add(tuple);
      if (value < this.min) {
         this.min = value;
      }
      if (value > this.max) {
         this.max = value;
      }

      while (this.max - this.min > this.slack) {
         final DataTuple out = this.buffer.poll();
         this.pushTuple(out);
         this.min = this.getValue(this.buffer.peek());
      }
   }

   /**
    * Converts the progressing value of the given tuple to long.
    *
    * @param tuple
    *           the tuple
    * @return the long value
    */
   private long getValue(final DataTuple tuple) {
      final Object value = tuple.getProgressingValue();
      if (value instanceof Number) {
         return ((Number) value).longValue();
      } else if (value instanceof Date) {
         return ((Date) value).getTime();
      }
      throw new IllegalArgumentException("Could not convert " + value.getClass() + " to Long.");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Socket.INPUT.equals(socket)) {
         while (!this.buffer.isEmpty()) {
            this.pushTuple(this.buffer.poll());
         }
      }
      super.handleEoS(socket, input, message);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   /**
    * Factory for new instances of the KSorter operator.
    *
    * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Factory implements PlanOperatorByParametersFactory {

      @Override
      public Operator getOperatorByParameters(final String operatorId, final Schema inputSchema,
            final Parameter parameters) throws Exception {
         final String comparatorName = parameters.get("object").get("class").getString();
         @SuppressWarnings("unchecked")
         final Comparator<DataTuple> comparator = (Comparator<DataTuple>) Class.forName(comparatorName)
               .newInstance();
         final long slack = Long.parseLong(parameters.get("slack").getString());
         return new KSorter(operatorId, inputSchema, comparator, slack);
      }
   }
}
