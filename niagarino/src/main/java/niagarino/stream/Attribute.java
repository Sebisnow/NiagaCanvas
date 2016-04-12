/*
 * @(#)Attribute.java   1.0   Feb 25, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

/**
 * Represents an attribute of a schema in terms of an attribute name and an attribute Type.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Attribute {

   /** Name of the attribute. */
   private final String name;
   /** Type of the attribute. */
   private final Class< ? > type;

   /**
    * Creates a new attribute with the given name and type.
    *
    * @param name
    *           attribute name
    * @param type
    *           attribute type
    */
   public Attribute(final String name, final Class< ? > type) {
      this.name = name;
      this.type = type;
   }

   /**
    * Returns the name of this attribute.
    *
    * @return attribute name
    */
   public String getName() {
      return this.name;
   }

   /**
    * Returns the type of this attribute.
    *
    * @return attribute type
    */
   public Class< ? > getType() {
      return this.type;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      final StringBuffer res = new StringBuffer();
      res.append(this.getName());
      res.append(":");
      res.append(this.getType().getSimpleName());
      return res.toString();
   }
}
