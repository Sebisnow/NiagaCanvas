/*
 * @(#)Schema.java   1.0   Feb 14, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema for tuples and streams.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Schema {

   /** Position of the progressing attribute. */
   private final int progressingAttribute;
   /** All attributes defined by this schema. */
   private final List<Attribute> attributes;
   /** A name-based map for attributes defined by this schema. */
   private final Map<String, Integer> attributeMapping;

   /**
    * Creates a new schema that defines the given attributes and uses the attribute at the given position as
    * its progressing attribute.
    *
    * @param progressingAttribute
    *           progressing attribute position
    * @param attributes
    *           all defined attributes
    */
   public Schema(final int progressingAttribute, final Attribute... attributes) {
      if (progressingAttribute < 0 || progressingAttribute >= attributes.length) {
         throw new IllegalArgumentException("Progressing attribute index out of bounds: "
               + progressingAttribute);
      }
      if (!Comparable.class.isAssignableFrom(attributes[progressingAttribute].getType())) {
         throw new IllegalArgumentException("Progressing attribute type must be comparable.");
      }
      this.progressingAttribute = progressingAttribute;
      this.attributes = Arrays.asList(attributes);
      this.attributeMapping = new HashMap<>();
      for (int i = 0; i < attributes.length; i++) {
         this.attributeMapping.put(attributes[i].getName(), i);
      }
   }

   /**
    * Returns all attributes defined by this schema as an unmodifiable list.
    *
    * @return all defined attributes
    */
   public List<Attribute> getAttributes() {
      return Collections.unmodifiableList(this.attributes);
   }

   /**
    * Returns a specific attribute that is identified by the given position.
    *
    * @param position
    *           attribute position
    * @return attribute definition
    */
   public Attribute getAttribute(final int position) {
      return this.attributes.get(position);
   }

   /**
    * Returns a specific attribute that is identified by the given name.
    *
    * @param name
    *           attribute name
    * @return attribute definition, or {@code null} if attribute cannot be found
    */
   public Attribute getAttribute(final String name) {
      final int index = this.getAttributeIndex(name);
      if (index == -1) {
         return null;
      } else {
         return this.getAttribute(index);
      }
   }

   /**
    * Returns the index of the attribute with the specified name.
    *
    * @param name
    *           attribute name
    * @return attribute index, or {@code -1} if attribute cannot be found
    */
   public int getAttributeIndex(final String name) {
      final Integer index = this.attributeMapping.get(name);
      if (index == null) {
         return -1;
      } else {
         return index;
      }
   }

   /**
    * Returns the size of this schema in terms of the number of defined attributes.
    *
    * @return schema size
    */
   public int getSize() {
      return this.attributes.size();
   }

   /**
    * Returns the position of the progressing attribute.
    * 
    * @return progressing attribute position
    */
   public int getProgressingAttribute() {
      return this.progressingAttribute;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      final StringBuffer buf = new StringBuffer();
      buf.append("<");
      for (int i = 0; i < this.getSize(); i++) {
         if (i == this.getProgressingAttribute()) {
            buf.append("+");
         }
         buf.append(this.attributes.get(i).toString());
         if (i + 1 < this.getSize()) {
            buf.append(",");
         }
      }
      buf.append(">");
      return buf.toString();
   }
}
