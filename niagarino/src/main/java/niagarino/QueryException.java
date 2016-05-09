/*
 * @(#)QueryException.java   1.0   Jun 27, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino;

/**
 * Exception that is thrown when query execution fails.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 */
public class QueryException extends Exception {

   /** Serial version ID. */
   private static final long serialVersionUID = -6296575115257420714L;

   /**
    * Constructs a new query exception with the given message and cause.
    *
    * @param message
    *           exception message
    * @param cause
    *           cause of the exception
    */
   public QueryException(final String message, final Throwable cause) {
      super(message, cause);
   }

   /**
    * Constructs a new query exception with the given cause.
    *
    * @param cause
    *           cause of the exception
    */
   public QueryException(final Throwable cause) {
      super(cause);
   }
}
