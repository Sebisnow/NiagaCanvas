/*
 * @(#)SequentialGenerationIterator.java   1.0   May 19, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import niagarino.operator.TupleIterator;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Generates tuples with a progressing value according to the given sequences and random values in all other
 * fields (except for Date, which always is the current Date).
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SequentialGenerationIterator implements TupleIterator {

   /** Empty {@link ControlTuple} array. */
   private static final ControlTuple[] EMPTY_CONTROL = new ControlTuple[0];

   /** The source of randomness. */
   private final Random random;

   /** The schema to use for the tuples. */
   private final Schema schema;

   /** All sequences to generate. */
   private final List<Sequence> sequences;

   /** An iterator over all sequences to generate. */
   private final Iterator<Sequence> iterator;

   /** The sequence currently being generated. */
   private Sequence currentSequence;

   /** The progress within the current sequence. */
   private long currentCount;

   /**
    * Constructs a new {@link SequentialGenerationIterator} with the given schema and sequences.
    *
    * @param schema
    *           the schema to use for the tuples
    * @param sequences
    *           the sequences to generate the progressing value from
    */
   public SequentialGenerationIterator(final Schema schema, final List<Sequence> sequences) {
      this.random = new Random();
      this.schema = schema;
      this.sequences = sequences;
      this.iterator = this.sequences.iterator();
      this.currentSequence = this.iterator.next();
      this.currentCount = 0;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasNext() {
      if (this.currentSequence.getCount() > this.currentCount) {
         return true;
      } else if (this.iterator.hasNext()) {
         return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DataTuple next() {
      if (this.currentSequence.getCount() > this.currentCount) {
         // Continue counting
         final long rawValue = this.currentSequence.getStart() + this.currentCount
               * this.currentSequence.getStepSize();
         final List<Object> values = this.getValues(rawValue);
         this.currentCount++;
         return new DataTuple(this.schema, values);
      } else {
         // Switch to next sequence
         if (!this.iterator.hasNext()) {
            // Return null if no more tuples are available
            return null;
         }
         this.currentSequence = this.iterator.next();
         this.currentCount = 0;
         return this.next();
      }
   }

   /**
    * Creates a list of values according to the schema of this {@link SequentialGenerationIterator}.
    *
    * @param progressingValue
    *           the progressing value to use
    * @return the list of values for the next tuple
    */
   protected List<Object> getValues(final long progressingValue) {
      final List<Object> list = new ArrayList<>(this.schema.getSize());
      for (int position = 0; position < this.schema.getSize(); position++) {
         final Class< ? > type = this.schema.getAttribute(position).getType();
         if (position == this.schema.getProgressingAttribute()) {
            final Object pValue;
            if (type.equals(Integer.class)) {
               pValue = (int) progressingValue;
            } else if (type.equals(Double.class)) {
               pValue = (double) progressingValue;
            } else if (type.equals(Date.class)) {
               pValue = new Date(progressingValue);
            } else {
               pValue = progressingValue;
            }
            list.add(pValue);
            continue;
         }
         final Object currentValue;
         if (type.equals(Integer.class)) {
            currentValue = this.random.nextInt();
         } else if (type.equals(Double.class)) {
            currentValue = this.random.nextDouble() * 1000000000;
         } else if (type.equals(Date.class)) {
            currentValue = new Date();
         } else {
            currentValue = this.random.nextLong();
         }
         list.add(currentValue);
      }
      return list;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ControlTuple[] nextControls() {
      return EMPTY_CONTROL;
   }

   /**
    * Discribes a monotonic sequence.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Sequence {

      /** Start of the sequence. */
      private final long start;

      /** Step count for the sequence. */
      private final long count;

      /** Step size of the sequence. */
      private final long stepSize;

      /**
       * Constructs a new sequence constraint object.
       *
       * @param start
       *           start of the sequence
       * @param count
       *           count for the sequence
       * @param stepSize
       *           step size of the sequence
       */
      public Sequence(final long start, final long count, final long stepSize) {
         this.start = start;
         this.count = count;
         this.stepSize = stepSize;
      }

      /**
       * Returns the start of the sequence.
       *
       * @return the start of the sequence
       */
      public long getStart() {
         return this.start;
      }

      /**
       * Returns the count of steps of the sequence.
       *
       * @return the count of steps of the sequence
       */
      public long getCount() {
         return this.count;
      }

      /**
       * Returns the step size of the sequence.
       *
       * @return the step size of the sequence
       */
      public long getStepSize() {
         return this.stepSize;
      }
   }
}
