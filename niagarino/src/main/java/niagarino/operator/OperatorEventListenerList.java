/*
 * @(#)OperatorEventListenerList.java   1.0   Jul 25, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.ArrayList;
import java.util.List;

import niagarino.stream.DataTuple;

/**
 * Utility class that implements a list to manage all operator event listeners that are registered for an
 * operator.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 */
public final class OperatorEventListenerList {

   /** List of operator event listeners. */
   private final List<OperatorEventListener> listeners;

   /**
    * Constructs a new operator event listener list.
    */
   public OperatorEventListenerList() {
      this.listeners = new ArrayList<>();
   }

   /**
    * Adds the given operator event listener to this listener list.
    *
    * @param listener
    *           operator event listener
    */
   public void addOperatorEventListener(final OperatorEventListener listener) {
      this.listeners.add(listener);
   }

   /**
    * Removes the given operator event listener from this listener list.
    *
    * @param listener
    *           operator event listener
    * @return {@code true} if the given listener is contained in this listener list, {@code false} otherwise
    */
   public boolean removeOperatorEventListener(final OperatorEventListener listener) {
      return this.listeners.remove(listener);
   }

   /**
    * Fires the event that indicates that an operator consumed an input tuple.
    *
    * @param source
    *           operator that triggers the event
    * @param tuple
    *           input tuple
    */
   public void fireOnInputTuple(final Operator source, final DataTuple tuple) {
      for (final OperatorEventListener listener : this.listeners) {
         listener.onInputTuple(source, tuple);
      }
   }

   /**
    * Fires the event that indicates that an operator produced an output tuple.
    *
    * @param source
    *           operator that triggers the event
    * @param tuple
    *           input tuple
    */
   public void fireOnOutputTuple(final Operator source, final DataTuple tuple) {
      for (final OperatorEventListener listener : this.listeners) {
         listener.onOutputTuple(source, tuple);
      }
   }

   /**
    * Fires the event that indicates that an operator has shut down.
    *
    * @param source
    *           operator that triggers the event
    */
   public void fireOnShutdown(final Operator source) {
      for (final OperatorEventListener listener : this.listeners) {
         listener.onShutdown(source);
      }
   }
}
