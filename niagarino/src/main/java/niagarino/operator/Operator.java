/*
 * @(#)Operator.java   1.0   Feb 14, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import niagarino.stream.Schema;
import niagarino.stream.Stream;

/**
 * Interface describing a stream operator. In Niagarino, each operator is modeled as a stream and connected to
 * other operators by input and output streams.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public interface Operator extends Runnable {

   /**
    * Returns the operator name of this operator.
    *
    * @return operator name
    */
   String getName();

   /**
    * Adds an output stream to this operator.
    *
    * @param stream
    *           output stream
    */
   void addOutputStream(Stream stream);

   /**
    * Adds an input stream to this operator.
    *
    * @param stream
    *           input stream
    */
   void addInputStream(Stream stream);

   /**
    * Returns the output schema of this operator, which reflect all eventual schema changes caused by the
    * operator.
    *
    * @return output schema
    */
   Schema getOutputSchema();

   /**
    * Returns the input arity, i.e., the required number of input streams, of this operator.
    *
    * @return input arity
    */
   int getInputArity();

   /**
    * Returns this output arity, i.e., the provided number of output streams, of this operator.
    *
    * @return output arity
    */
   int getOutputArity();

   /**
    * Sets whether this operator is a sink operator.
    *
    * @param sink
    *           {@code true} if this operator is a sink operator, {@code false} otherwise
    */
   void setSink(boolean sink);

   /**
    * Returns whether this operator is a sink operator.
    *
    * @return {@code true} if this operator is a sink operator, {@code false} otherwise
    */
   boolean isSink();

   /**
    * Stops this operator, dropping all pending tuples.
    */
   void stop();

   /**
    * Returns whether this operator is currently running.
    *
    * @return {@code true} if the operator is running, {@code false} otherwise.
    */
   boolean isRunning();

   /**
    * Adds the given operator event listener to this listener list.
    *
    * @param listener
    *           operator event listener
    */
   void addOperatorEventListener(OperatorEventListener listener);

   /**
    * Removes the given operator event listener from this listener list.
    *
    * @param listener
    *           operator event listener
    * @return {@code true} if the given listener is contained in this listener list, {@code false} otherwise
    */
   boolean removeOperatorEventListener(OperatorEventListener listener);
}
