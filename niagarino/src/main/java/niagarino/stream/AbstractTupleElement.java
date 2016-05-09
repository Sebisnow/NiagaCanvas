/*
 * @(#)AbstractTupleElement.java   1.0   Feb 16, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

/**
 * Abstract implementation and common superclass of all stream elements.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public abstract class AbstractTupleElement implements TupleElement {

   /** Metadata of this stream element. */
   private ElementMetadata metadata;

   /**
    * Constructs a new stream element and initializes its metadata.
    */
   protected AbstractTupleElement() {
      this(new ElementMetadata());
   }

   /**
    * Constructs a new stream element and initializes it with the given metadata.
    *
    * @param metadata
    *           stream element metadata
    */
   protected AbstractTupleElement(final ElementMetadata metadata) {
      this.metadata = metadata;
   }

   @Override
   public ElementMetadata getElementMetadata() {
      return this.metadata;
   }

   @Override
   public AbstractTupleElement clone() throws CloneNotSupportedException {
      final AbstractTupleElement clone = (AbstractTupleElement) super.clone();
      clone.metadata = this.metadata.clone();
      return clone;
   }
}
