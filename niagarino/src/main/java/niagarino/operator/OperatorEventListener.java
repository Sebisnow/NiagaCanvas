/*
 * @(#)OperatorEventListener.java   1.0   Jun 28, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import niagarino.stream.DataTuple;

/**
 * Listener interface to monitor operator execution.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 */
public interface OperatorEventListener {

   /**
    * Invoked whenever the operator consumes an input tuple.
    *
    * @param source
    *           operator that triggers the event
    * @param tuple
    *           input tuple
    */
   void onInputTuple(Operator source, DataTuple tuple);

   /**
    * Invoked whenever the operator produces an output tuple.
    *
    * @param source
    *           operator that triggers the event
    * @param tuple
    *           output tuple
    */
   void onOutputTuple(Operator source, DataTuple tuple);

   /**
    * Invoked when the operator shuts down.
    *
    * @param source
    *           operator that triggers the event
    */
   void onShutdown(Operator source);
}
