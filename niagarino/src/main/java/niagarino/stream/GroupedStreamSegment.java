/*
 * @(#)GroupedStreamSegment.java   1.0   Feb 15, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import niagarino.operator.function.AggregationFunction;
import niagarino.operator.function.Aggregator;

/**
 * A stream segment that groups and aggregates the tuples that fall within its lower and upper boundary. The
 * grouped stream segment is mainly used by the ordered aggregate operator to compute its result tuples.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class GroupedStreamSegment extends AbstractStreamSegment {

   /** Table mapping groups to incrementally computed aggregate values. */
   private final Map<Group, Aggregator[]> groups;
   /** Schema of the tuples that inserted into this stream segment. */
   private final Schema inputSchema;
   /** Schema of the aggregated tuples that are produced by this grouped stream segment. */
   private final Schema outputSchema;
   /** Positions of grouping attributes. */
   private final int[] groupAttributes;
   /** Positions of aggregated attributes. */
   private final int[] aggregatedAttributes;
   /** Aggregate functions used by this grouped stream segment. */
   private final AggregationFunction[] functions;

   /**
    * Creates a new grouped segment with the given input schema, which uses the given grouped attributes,
    * aggregated attributes, and aggregation functions.
    *
    * @param inputSchema
    *           input schema definition
    * @param groupAttributes
    *           grouping attributes
    * @param aggregatedAttributes
    *           aggregated attributes
    * @param functions
    *           aggregation functions.
    */
   public GroupedStreamSegment(final Schema inputSchema, final int[] groupAttributes,
         final int[] aggregatedAttributes, final AggregationFunction... functions) {
      this.groups = new HashMap<Group, Aggregator[]>();
      this.inputSchema = inputSchema;
      this.groupAttributes = groupAttributes;
      this.aggregatedAttributes = aggregatedAttributes;
      this.functions = functions;
      this.outputSchema = this.initSchema();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void insertTuple(final DataTuple tuple) {
      final Object[] groupValues = new Object[this.groupAttributes.length];
      boolean nullGroup = false;
      for (int i = 0; i < groupValues.length; i++) {
         groupValues[i] = tuple.getAttributeValue(this.groupAttributes[i]);
         nullGroup = nullGroup || groupValues[i] == null;
      }
      if (!nullGroup) {
         final Group group = new Group(groupValues);
         Aggregator[] values = this.groups.get(group);
         if (values == null) {
            values = new Aggregator[this.aggregatedAttributes.length];
            this.groups.put(group, values);
         }
         for (int i = 0; i < this.aggregatedAttributes.length; i++) {
            Aggregator aggregate = values[i];
            if (aggregate == null) {
               aggregate = this.functions[i].get();
               values[i] = aggregate;
            }
            aggregate.update(tuple.getAttributeValue(this.aggregatedAttributes[i]));
         }
         this.updateMinimalValue(tuple.getProgressingValue());
         this.updateMaximalValue(tuple.getProgressingValue());
      }
   }

   /**
    * Sets min and max for this segment.
    *
    * @param min
    *           minimum value
    * @param max
    *           maximum value
    */
   public void setMinMax(final Object min, final Object max) {
      this.updateMinimalValue(min);
      this.updateMaximalValue(max);
   }

   @Override
   public List<DataTuple> reportTuples() {
      final List<DataTuple> result = new ArrayList<DataTuple>();
      for (final Map.Entry<Group, Aggregator[]> entry : this.groups.entrySet()) {
         // Construct output tuple
         final Object[] values = new Object[this.outputSchema.getSize()];
         // Set progressing attribute to the maximal value of progressing attribute in this stream segment.
         // The progressing attribute of the output schema is always initialized to 0.
         values[0] = this.getMaximalValue();
         // Add the values of the attributes that are used for grouping.
         final Object[] groupValues = entry.getKey().getValues();
         for (int i = 0; i < groupValues.length; i++) {
            values[i + 1] = groupValues[i];
         }
         // Add the result values of the aggregation functions.
         final Aggregator[] aggregatedValues = entry.getValue();
         for (int i = 0; i < aggregatedValues.length; i++) {
            final int offset = i + groupValues.length + 1;
            values[offset] = null;
            if (aggregatedValues[i] != null) {
               // TODO Do we need a type conversion here?
               values[offset] = aggregatedValues[i].getValue();
            }
         }
         // Construct data tuple and add it to the result.
         final DataTuple tuple = new DataTuple(this.outputSchema, Arrays.asList(values));
         result.add(tuple);
      }
      return result;
   }

   @Override
   public Schema getSchema() {
      return this.outputSchema;
   }

   /**
    * Initializes and returns the output schema of this grouped stream segment.
    *
    * @return output schema definition
    */
   private Schema initSchema() {
      final Schema inputSchema = this.inputSchema;
      final List<Attribute> outAttributes = new ArrayList<Attribute>();
      // handle progressing attribute
      final int outputProgressingAttribute = 0;
      outAttributes.add(inputSchema.getAttribute(inputSchema.getProgressingAttribute()));
      // handle group attributes
      for (final int groupAttribute : this.groupAttributes) {
         outAttributes.add(inputSchema.getAttribute(groupAttribute));
      }
      // handle aggregated attributes
      for (int i = 0; i < this.aggregatedAttributes.length; i++) {
         final Attribute attribute = inputSchema.getAttribute(this.aggregatedAttributes[i]);
         final AggregationFunction function = this.functions[i];
         outAttributes.add(function.getAggregatedAttribute(attribute));
      }
      return new Schema(outputProgressingAttribute, outAttributes.toArray(new Attribute[0]));
   }

   /**
    * Private class to represent grouped values.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   private final class Group {

      /** Grouped values. */
      private final Object[] values;

      /**
       * Create a new group for the given values.
       *
       * @param values
       *           grouped values
       */
      private Group(final Object... values) {
         this.values = values;
      }

      /**
       * Returns the grouped values contained in this group.
       *
       * @return grouped values
       */
      public Object[] getValues() {
         return this.values;
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.values);
      }

      @Override
      public boolean equals(final Object obj) {
         final Group group = (Group) obj;
         return Arrays.equals(this.values, group.getValues());
      }
   }
}
