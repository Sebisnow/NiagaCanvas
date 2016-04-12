/*
 * @(#)AbstractAggregationFunction.java   1.0   Jul 24, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

import niagarino.stream.Attribute;

/**
 * Common abstract superclass of aggregation functions.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public abstract class AbstractAggregationFunction implements AggregationFunction {

   /** Name of this aggregation function. */
   private final String name;
   /** Type of this aggregation function. */
   private final Type type;
   /** Type of the output values of this aggregation function. */
   private final Class< ? > outputType;

   /**
    * Constructs a new aggregation function with the given type, name, and output type.
    *
    * @param type
    *           aggregation function type
    * @param name
    *           aggregation function name
    * @param outputType
    *           output type
    */
   private AbstractAggregationFunction(final Type type, final String name, final Class< ? > outputType) {
      this.type = type;
      this.name = name;
      this.outputType = outputType;
   }

   /**
    * Constructs a new aggregation function with the given type and output type.
    *
    * @param type
    *           aggregation function type
    * @param outputType
    *           output type
    */
   AbstractAggregationFunction(final Type type, final Class< ? > outputType) {
      this(type, type.name(), outputType);
   }

   /**
    * Constructs a new user-defined function with the given output type.
    *
    * @param name
    *           aggregation function name
    * @param outputType
    *           output type
    */
   protected AbstractAggregationFunction(final String name, final Class< ? > outputType) {
      this(Type.UDF, name, outputType);
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public Type getType() {
      return this.type;
   }

   @Override
   public Attribute getAggregatedAttribute(final Attribute inputAttribute) {
      return new Attribute(this.getName() + "(" + inputAttribute.getName() + ")", this.outputType);
   }
}
