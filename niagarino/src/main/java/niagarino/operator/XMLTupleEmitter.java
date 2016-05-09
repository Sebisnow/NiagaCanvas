/*
 * @(#)XMLTupleEmitter.java   1.0   May 31, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import org.xml.sax.helpers.DefaultHandler;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Implementation of a SAX {@link DefaultHandler} that offers {@link Tuple}s based on incoming XML data. It is
 * used for the {@link XMLParser}.
 *
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public abstract class XMLTupleEmitter extends DefaultHandler {

   /** Empty Control array. */
   private static final ControlTuple[] CONTROLS_EMPTY = new ControlTuple[0];

   /**
    * Returns the schema of emitted tuples.
    *
    * @return the schema of emitted tuples
    */
   public abstract Schema getOutputSchema();

   /**
    * Resets this {@link XMLTupleEmitter} to congest the next document.
    */
   public abstract void reset();

   /**
    * Whether this {@link XMLTupleEmitter} has a tuple ready to be extracted via
    * {@link XMLTupleEmitter#nextTuple()}.
    *
    * @return <code>true</code> if a tuple is available, <code>false</code> otherwise
    */
   public abstract boolean tupleAvailable();

   /**
    * Returns the next tuple.
    *
    * @return the next tuple
    */
   public abstract DataTuple nextTuple();

   /**
    * Returns pending Controls in an array.
    *
    * @return pending Controls
    */
   public ControlTuple[] nextControls() {
      return CONTROLS_EMPTY;
   }
}
