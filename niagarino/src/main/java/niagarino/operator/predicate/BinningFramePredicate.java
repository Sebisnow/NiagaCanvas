/*
 * @(#)BinningFramePredicate.java   1.0   Mar 22, 2011
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
 * A frame predicate that segments the stream in fixed-size data-dependent bins.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class BinningFramePredicate implements Predicate {

   /** Position of the attribute that is binned into frames. */
   private final int attributePosition;
   /** Size of the bins. */
   private final double size;
   /** Offset of the bins. */
   private final double offset;
   /** Number of the current bin. */
   private long currentBin;

   /**
    * Creates a new binning frame predicate on the attribute given by its position that uses bins with the
    * given size and offset.
    *
    * @param attributePosition
    *           attribute position
    * @param size
    *           size of the bins
    * @param offset
    *           offset of the bins
    */
   public BinningFramePredicate(final int attributePosition, final double size, final double offset) {
      this.attributePosition = attributePosition;
      this.size = size;
      this.offset = offset;
      this.currentBin = 0;
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
      if (number != null) {
         final double value = number.doubleValue();
         final long bin = (long) ((value - this.offset) / this.size);
         if (this.currentBin != bin) {
            this.currentBin = bin;
            return true;
         }
      }
      return false;
   }
}
