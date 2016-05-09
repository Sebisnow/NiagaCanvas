/*
 * @(#)StringFunction.java   1.0   Jun 18, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

/**
 * Represents a function that accepts a string-valued argument and produces a result. This is the {@code long}
 * -consuming primitive specialization for {@link Function}.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #apply(string)}.
 * 
 * @param <R> the type of the result of the function
 *
 * @see Function
 * @since 1.8
 * @author Andreas Weiler &lt;andreas.weiler@uni.kn&gt;
 * @version 1.0
 */
@FunctionalInterface
public interface StringFunction<R> {

   /**
    * Applies this function to the given argument.
    *
    * @param value
    *           the function argument
    * @return the function result
    */
   R apply(String value);
}
