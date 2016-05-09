/*
 * @(#)TupleListIterator.java   1.0   Jun 2, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * A {@link TupleIterator} which emits given values and controls in the given order.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TupleListIterator implements TupleIterator {

   /** Empty {@link ControlTuple} array. */
   private static final ControlTuple[] EMPTY_CONTROL = new ControlTuple[0];

   /** ArrayList of controls to emit. */
   private final ArrayList<ControlTuple> ctrlList;

   /** The schema of the tuples. */
   private final Schema schema;

   /** The iterator over tuple values and controls. */
   private Iterator<Object> iterator;

   /** The next element to emit. */
   private Object nextElement;

   /**
    * Constructs a new {@link TupleListIterator} which emits the given values and controls. The values have to
    * be {@link List}&lt;Object&gt; if the schema is multi-attribute.
    *
    * @param schema
    *           schema of tuples
    * @param elements
    *           elements/values to emit
    */
   public TupleListIterator(final Schema schema, final Object... elements) {
      this.ctrlList = new ArrayList<>();
      this.schema = schema;
      this.iterator = Arrays.asList(elements).iterator();
      this.nextElement = this.iterator.next();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasNext() {
      return this.nextElement != null;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public DataTuple next() throws NoSuchElementException {
      if (!this.hasNext()) {
         throw new NoSuchElementException("No more tuples available.");
      }
      final DataTuple tuple;
      if (this.schema.getSize() > 1) {
         if (this.nextElement instanceof List< ? >) {
            tuple = new DataTuple(this.schema, (List<Object>) this.nextElement);
         } else {
            throw new NoSuchElementException("Element has wrong type.");
         }
      } else {
         tuple = new DataTuple(this.schema, Arrays.asList(this.nextElement));
      }
      this.advance();
      return tuple;
   }

   /**
    * Advances internal variables to the next element.
    */
   private void advance() {
      if (this.iterator.hasNext()) {
         this.nextElement = this.iterator.next();
      } else {
         this.nextElement = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ControlTuple[] nextControls() {
      while (this.nextElement != null && this.nextElement instanceof ControlTuple) {
         this.ctrlList.add((ControlTuple) this.nextElement);
         this.advance();
      }
      if (this.ctrlList.size() > 0) {
         final ControlTuple[] ctrls = this.ctrlList.toArray(new ControlTuple[this.ctrlList.size()]);
         this.ctrlList.clear();
         return ctrls;
      } else {
         return EMPTY_CONTROL;
      }
   }
}
