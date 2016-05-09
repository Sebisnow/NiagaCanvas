/*
 * @(#)TopKStreamSegment.java   1.0   Jul 9, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * A stream segment that ranks the tuples of the segment and reports the top k tuples according to the
 * preference function.
 *
 * @author Manuel Hotz &lt;manuel.hotz@uni-konstanz.de&gt
 */
public class TopKStreamSegment extends AbstractStreamSegment {

   /** Priority queue, that always holds our values in sorted order. */
   private final PriorityQueue<DataTuple> queue;
   /** The k. */
   private final int k;
   /** The output schema. */
   private final Schema outputSchema;

   /**
    * Constructor for a stream segment that computes the top-k tuples.
    *
    * @param inputSchema
    *           the input schema
    * @param k
    *           the k
    * @param attributePosition
    *           the position of the attribute to order on
    */
   public TopKStreamSegment(final Schema inputSchema, final int k, final int attributePosition) {
      this.k = k;
      this.queue = new PriorityQueue<DataTuple>(getTupleComparatorFor(attributePosition, true));
      this.outputSchema = inputSchema;
   }

   @Override
   public void insertTuple(final DataTuple tuple) {
      this.queue.add(tuple);
      if (this.queue.size() > this.k) {
         this.queue.remove();
      }
      this.updateMinimalValue(tuple.getProgressingValue());
      this.updateMaximalValue(tuple.getProgressingValue());
   }

   @Override
   public List<DataTuple> reportTuples() {
      final List<DataTuple> res = new ArrayList<>();
      while (!this.queue.isEmpty()) {
         res.add(this.queue.remove());
      }
      // reverse b/c our queue is a MinQueue
      Collections.reverse(res);
      return res;
   }

   @Override
   public Schema getSchema() {
      return this.outputSchema;
   }

   /**
    * Updates the minimum and maximum progressing attribute.
    *
    * @param min
    *           minimum value of progressing attribute
    * @param max
    *           maximum value of progressing attribute
    */
   public void setMinMax(final Object min, final Object max) {
      this.updateMinimalValue(min);
      this.updateMaximalValue(max);
   }

   /**
    * Comparator for tuples.
    *
    * @param attributePosition
    *           position of attribute
    * @param descending
    *           <code>true</code> if the values should be sorted in descending order, <code>false</code>
    *           otherwise
    * @return comparison result between tuples
    */
   // adapted from OrderedAggregate
   private static Comparator<DataTuple> getTupleComparatorFor(final int attributePosition,
         final boolean descending) {
      return new Comparator<DataTuple>() {

         @Override
         public int compare(final DataTuple first, final DataTuple second) {

            final Class< ? > firstType = Objects.requireNonNull(first).getSchema()
                  .getAttribute(attributePosition).getType();
            final Class< ? > secondType = Objects.requireNonNull(second).getSchema()
                  .getAttribute(attributePosition).getType();

            if (!firstType.equals(secondType)) {
               throw new IllegalArgumentException("Types of compared attributes have to be the same.");
            }

            final Object firstValue = first.getAttributeValue(attributePosition);
            final Object secondValue = second.getAttributeValue(attributePosition);

            int cmp = 0;

            if (String.class.equals(firstType)) {
               cmp = ((String) firstValue).compareTo((String) secondValue);
            } else if (Long.class.equals(firstType)) {
               cmp = Long.compare((Long) firstValue, (Long) secondValue);
            } else if (Double.class.equals(firstType)) {
               cmp = Double.compare((Double) firstValue, (Double) secondValue);
            } else if (Integer.class.equals(firstType)) {
               cmp = Integer.compare((Integer) firstValue, (Integer) secondValue);
            }

            return descending ? cmp : -cmp;
         }
      };
   }
}
