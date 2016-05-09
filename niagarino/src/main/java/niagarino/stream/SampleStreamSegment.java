/*
 * @(#)SampleStreamSegment.java   1.0   Jul 07, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.ArrayList;
import java.util.List;

/**
 * StreamSegment with samples(analogous to reservoir).
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public class SampleStreamSegment extends AbstractStreamSegment {

   /** Insert at any position in SampleList(append is preferred). */
   private static final int INSERT_INDEX_LAST = -1;

   /** Schema of the tuples that inserted into this stream segment. */
   private final Schema inputSchema;

   /** Segments with the sampled tuples. */
   private final List<DataTuple> sampleSegment;

   /**
    * @param inputSchema
    *           Input Schema of the segment.
    */
   public SampleStreamSegment(final Schema inputSchema) {
      this.inputSchema = inputSchema;
      this.sampleSegment = new ArrayList<>();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void insertTuple(final DataTuple tuple) {
      this.updateMinimalValue(tuple.getProgressingValue());
      this.updateMaximalValue(tuple.getProgressingValue());
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public List<DataTuple> reportTuples() {
      return this.sampleSegment;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getSchema() {
      return this.inputSchema;
   }

   /**
    * Inserts tuple into the sampleSegment at the given Index.
    *
    * @param tuple
    *           tuple
    * @param insertIndex
    *           index to insert the tuple
    */
   public void insertTuple(final DataTuple tuple, final int insertIndex) {
      if (insertIndex == SampleStreamSegment.INSERT_INDEX_LAST || insertIndex >= this.sampleSegment.size()) {
         this.sampleSegment.add(tuple);
      } else {
         // Replace the sample at the insertPosition
         this.sampleSegment.set(insertIndex, tuple);
      }
      this.updateMinimalValue(tuple.getProgressingValue());
      this.updateMaximalValue(tuple.getProgressingValue());
   }

   /**
    * Sets min and max for this segment.
    *
    * @param min
    *           minimum value
    * @param max
    *           maximum value
    */
   public void setMinMax(final Object min, final Object max) {
      this.updateMinimalValue(min);
      this.updateMaximalValue(max);
   }
}
