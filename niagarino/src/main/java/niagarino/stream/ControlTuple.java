/*
 * @(#)Control.java   1.0   Feb 16, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

/**
 * A stream element that represents a control message.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public class ControlTuple extends AbstractTupleElement {

   /**
    * Enumeration of different types of control messages.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   public enum Type {
      /** End-of-stream (EOS) control message. */
      EOS,
      /** Punctuation control message. */
      PUNCTUATION,
      /** End-of-segment control message. */
      ENDOFSEGMENT,
      /** Priority control message. */
      PRIORITY;
   };

   /** Type of this control message. */
   private Type type;

   /**
    * Creates a new control message of the given type.
    *
    * @param type
    *           control message type
    */
   public ControlTuple(final Type type) {
      this.type = type;
   }

   /**
    * Creates a new control message with the given element metadata.
    *
    * @param metadata
    *           stream element metadata
    */
   protected ControlTuple(final ElementMetadata metadata) {
      super(metadata);
      this.type = null;
   }

   /**
    * Returns the type of this control message.
    *
    * @return control message type
    */
   public Type getType() {
      return this.type;
   }

   @Override
   public ControlTuple clone() throws CloneNotSupportedException {
      final ControlTuple clone = (ControlTuple) super.clone();
      clone.type = this.type;
      return clone;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return "<" + this.type.name() + ">";
   }
}
