/*
 * @(#)AbstractStreamSegment.java   1.0   Feb 15, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

/**
 * Abstract implementation of a stream segment that handles the management of the upper and
 * lower boundary.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public abstract class AbstractStreamSegment implements StreamSegment {

   /** Minimum value, i.e., lower boundary. */
   private Object minValue;
   /** Maximum value, i.e., upper boundary. */
   private Object maxValue;

   /**
    * Constructs a new stream segment and initializes its lower and upper boundary to
    * <code>null</code>.
    */
   protected AbstractStreamSegment() {
      this.minValue = null;
      this.maxValue = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getMinimalValue() {
      return this.minValue;
   }

   /**
    * Updates the lower boundary to the given minimum value.
    *
    * @param minValue
    *           minimum value
    */
   @SuppressWarnings("unchecked")
   protected void updateMinimalValue(final Object minValue) {
      if (minValue != null && minValue instanceof Comparable) {
         if (this.minValue == null) {
            this.minValue = minValue;
         } else {
            if (((Comparable<Object>) minValue).compareTo(this.minValue) < 0) {
               this.minValue = minValue;
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getMaximalValue() {
      return this.maxValue;
   }

   /**
    * Updates the upper boundary to the given maximum value.
    *
    * @param maxValue
    *           maximum value
    */
   @SuppressWarnings("unchecked")
   protected void updateMaximalValue(final Object maxValue) {
      if (maxValue != null && maxValue instanceof Comparable) {
         if (this.maxValue == null) {
            this.maxValue = maxValue;
         } else {
            if (((Comparable<Object>) maxValue).compareTo(this.maxValue) > 0) {
               this.maxValue = maxValue;
            }
         }
      }
   }
}
