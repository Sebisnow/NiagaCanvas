/*
 * @(#)MaxRMSPredicate.java   1.0   Mar 10, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.predicate;

import java.util.ArrayList;
import java.util.List;

import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Frame predicate that uses the condition <code>rms(x) &gt; k</code> to segment a stream,
 * where <code>rms(x)</code> is the root-mean-squared error of the computed aggregate and the
 * actual value series of attribute <code>x</code>.
 *
 * @author Maximilian Ortwein &lt;maximilian.ortwein@uni.kn&gt;
 * @version 1.0
 */
public class MaxRMSPredicate implements Predicate {

   /** Position of the attribute for which the RMS error is computed. */
   private final int attributePosition;
   /** Maximum allowed RMS error. */
   private final double maxError;
   /** Current sum aggregation value of the attribute in question. */
   private double curSum = 0;
   /** Current average aggregation value of the attribute in question. */
   private double curAvg = 0;
   /** Buffer storing the value series of the attribute in question. */
   private final List<Double> values;
   /** Indicates whether the satisfying tuple will be included in the frame. */
   private final boolean includeSatisfyingTuple;

   /**
    * Creates a new predicate that uses the maximum root-mean-squared error as a threshold to
    * create a new frame.
    *
    * @param attributePosition
    *           position of the attribute for which the RMS error is computed
    * @param maxError
    *           maximum allowed RMS error
    * @param includeSatisfyingTuple
    *           indicates whether the satisfying tuple will be included in the frame or not
    */
   public MaxRMSPredicate(final int attributePosition, final double maxError,
         final boolean includeSatisfyingTuple) {
      this.values = new ArrayList<Double>();
      this.attributePosition = attributePosition;
      this.maxError = maxError;
      this.includeSatisfyingTuple = includeSatisfyingTuple;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final Schema schema) {
      final Class< ? > type = schema.getAttribute(this.attributePosition).getType();
      return Number.class.isAssignableFrom(type);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean evaluate(final DataTuple tuple) {
      final Number number = (Number) tuple.getAttributeValue(this.attributePosition);
      if (number != null && !number.equals(0)) {
         final double value = number.doubleValue();

         if (this.values.isEmpty()) {
            this.curAvg = value;
         } else {
            this.curAvg = (this.curAvg + value) / this.values.size();
         }

         this.values.add(value);

         this.curSum = 0;
         for (final double i : this.values) {
            this.curSum = this.curSum + Math.pow(i - this.curAvg, 2);
         }
         final double rms = Math.sqrt(this.curSum / this.values.size());

         if (rms > this.maxError) {
            if (this.includeSatisfyingTuple) {
               this.curAvg = value;
               this.values.clear();
               this.values.add(value);
            } else {
               this.curAvg = 0;
               this.values.clear();
            }
            return true;
         }
      }
      return false;
   }
}
