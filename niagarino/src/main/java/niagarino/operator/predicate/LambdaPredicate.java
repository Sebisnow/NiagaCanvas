/*
 * @(#)LambdaPredicate.java   1.0   Jun 17, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.predicate;

import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import niagarino.operator.function.StringFunction;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * LambdaPredicate that applies a lambda expression to each tuple.
 * 
 * @author Andreas Weiler &lt;andreas.weiler@uni.kn&gt;
 * @version 1.0
 */
public class LambdaPredicate implements Predicate {
   
   /** Lambda expression. */
   private final Object expression;
   /** Position of attribute to apply the expression. */
   private final int attributePosition;
   /** Type of lambda expression. */
   private final int type;
   
   /**
    * Creates a new lambda predicate that is applied to each tuple.
    * 
    * @param expression
    *           lambda expression
    * @param attributePosition
    *           position of attribute to apply the lambda expression
    */
   public LambdaPredicate(final IntFunction<Boolean> expression, final int attributePosition) {
      this.type = 0;
      this.expression = expression;
      this.attributePosition = attributePosition;
   }
   
   /**
    * Creates a new lambda predicate that is applied to each tuple.
    * 
    * @param expression
    *           lambda expression
    * @param attributePosition
    *           position of attribute to apply the lambda expression
    */
   public LambdaPredicate(final DoubleFunction<Boolean> expression, final int attributePosition) {
      this.type = 1;
      this.expression = expression;
      this.attributePosition = attributePosition;
   }
   
   /**
    * Creates a new lambda predicate that is applied to each tuple.
    * 
    * @param expression
    *           lambda expression
    * @param attributePosition
    *           position of attribute to apply the lambda expression
    */
   public LambdaPredicate(final LongFunction<Boolean> expression, final int attributePosition) {
      this.type = 2;
      this.expression = expression;
      this.attributePosition = attributePosition;
   }
   
   /**
    * Creates a new lambda predicate that is applied to each tuple.
    * 
    * @param expression
    *           lambda expression
    * @param attributePosition
    *           position of attribute to apply the lambda expression
    */
   public LambdaPredicate(final StringFunction<Boolean> expression, final int attributePosition) {
      this.type = 3;
      this.expression = expression;
      this.attributePosition = attributePosition;
   }

   @Override
   public boolean isApplicable(final Schema schema) {
      final Class< ? > type = schema.getAttribute(this.attributePosition).getType();
      switch (this.type) {
         case 0:
            return Integer.class.isAssignableFrom(type);
         case 1:
            return Double.class.isAssignableFrom(type);
         case 2:
            return Long.class.isAssignableFrom(type);
         case 3:
            return String.class.isAssignableFrom(type);
         default:
            break;
      }
      return false;
   }

   @SuppressWarnings("unchecked")
   @Override
   public boolean evaluate(final DataTuple tuple) {
      switch (this.type) {
         case 0:
            final IntFunction<Boolean> exp0 = (IntFunction<Boolean>) this.expression;
            return exp0.apply((int) tuple.getAttributeValue(this.attributePosition));
         case 1:
            final DoubleFunction<Boolean> exp1 = (DoubleFunction<Boolean>) this.expression;
            return exp1.apply((double) tuple.getAttributeValue(this.attributePosition));
         case 2:
            final LongFunction<Boolean> exp2 = (LongFunction<Boolean>) this.expression;
            return exp2.apply((long) tuple.getAttributeValue(this.attributePosition));
         case 3:
            final StringFunction<Boolean> exp3 = (StringFunction<Boolean>) this.expression;
            return exp3.apply((String) tuple.getAttributeValue(this.attributePosition));
         default:
            break;
      }
      return false;
   }
}
