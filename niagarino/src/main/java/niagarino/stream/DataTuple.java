/*
 * @(#)DataTuple.java   1.0   Feb 7, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Very simple representation of tuples.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class DataTuple extends AbstractTupleElement {

   /** Schema of this tuple. */
   private Schema schema;
   /** Values of this tuple. */
   private List<Object> values;

   /**
    * Creates a new tuple with the given schema, values, and stream element metadata.
    *
    * @param schema
    *           schema definition
    * @param values
    *           tuple values
    * @param metadata
    *           stream element metadata
    */
   public DataTuple(final Schema schema, final List<Object> values, final ElementMetadata metadata) {
      super(metadata);
      if (schema.getSize() != values.size()) {
         throw new IllegalStateException("Wrong number of tuple values: " + schema.getSize() + " != "
               + values.size() + ".");
      }
      this.schema = schema;
      this.values = values;
   }

   /**
    * Creates a new tuple with the given schema and values. The stream element metadata is initialized to an
    * empty metadata description.
    *
    * @param schema
    *           schema definition
    * @param values
    *           tuple values
    */
   public DataTuple(final Schema schema, final List<Object> values) {
      this(schema, values, new ElementMetadata());
   }

   /**
    * Returns the tuple value of the progressing attribute as defined by the schema of this tuple.
    *
    * @return progressing attribute value
    */
   public Object getProgressingValue() {
      return this.getAttributeValue(this.schema.getProgressingAttribute());
   }

   /**
    * Returns the tuple value of the attribute at the given position.
    *
    * @param attributePosition
    *           attribute position
    * @return tuple value
    */
   public Object getAttributeValue(final int attributePosition) {
      return this.values.get(attributePosition);
   }

   /**
    * Returns the tuple value of the attribute with the given name.
    *
    * @param attributeName
    *           attribute name
    * @return tuple value
    */
   public Object getAttributeValue(final String attributeName) {
      return this.getAttributeValue(this.schema.getAttributeIndex(attributeName));
   }

   /**
    * Returns all values of this tuple as an unmodifiable list.
    *
    * @return tuple values
    */
   public List<Object> getValues() {
      return Collections.unmodifiableList(this.values);
   }

   /**
    * Returns the schema of this tuple.
    *
    * @return schema definition
    */
   public Schema getSchema() {
      return this.schema;
   }

   @Override
   public DataTuple clone() throws CloneNotSupportedException {
      final DataTuple clone = (DataTuple) super.clone();
      clone.schema = this.schema;
      clone.values = new ArrayList<Object>(this.values);
      return clone;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      final StringBuffer res = new StringBuffer();
      res.append("<");
      for (int i = 0; i < this.values.size(); i++) {
         res.append(this.values.get(i));
         if (i == this.schema.getProgressingAttribute()) {
            res.append("+");
         }
         if (i + 1 < this.values.size()) {
            res.append(",");
         }
      }
      res.append(">");
      return res.toString();
   }
}
