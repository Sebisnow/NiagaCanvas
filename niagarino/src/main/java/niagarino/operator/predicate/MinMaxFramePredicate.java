/*
 * @(#)MinMaxFramePredicate.java   1.0   Mar 10, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.predicate;

import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Frame predicate that uses the condition <code>max(x)-min(x) &gt; k</code> to segment a
 * stream.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class MinMaxFramePredicate implements Predicate {

   /** Position of the attribute to which the predicate is applied. */
   private final int attributePosition;
   /** Maximum threshold used in the predicate. */
   private final double k;
   /** Current minimum value of the attribute used in this predicate. */
   private double min;
   /** Current maximum value of the attribute used in this predicate. */
   private double max;

   /**
    * Creates a new predicate that uses the delta between the minimum and maximum value of
    * the attribute given by its position to create a new frame.
    *
    * @param attributePosition
    *           position of the attribute for which the min-max delta is computed
    * @param k
    *           maximum threshold
    */
   public MinMaxFramePredicate(final int attributePosition, final Number k) {
      this.attributePosition = attributePosition;
      this.k = k.doubleValue();
      this.min = Double.MAX_VALUE;
      this.max = Double.MIN_VALUE;
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
      // update minimum and maximum values
      final Number number = (Number) tuple.getAttributeValue(this.attributePosition);
      if (number != null) {
         final double value = number.doubleValue();
         this.min = Math.min(this.min, value);
         this.max = Math.max(this.max, value);
         // check condition
         if (Math.abs(this.max - this.min) > this.k) {
            this.min = value;
            this.max = value;
            return true;
         }
      }
      return false;
   }
}
