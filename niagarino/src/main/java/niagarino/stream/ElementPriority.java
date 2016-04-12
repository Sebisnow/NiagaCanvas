/*
 * @(#)ElementPriority.java   1.0   Mar 7, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

/**
 * Represents the priority assigned to a stream element. Priority is expressed in an <code>x</code> and
 * <code>y</code> value that are used to compute if a stream element is part of a stream segment with segment
 * identifier <code>id</code>. If <code>id % x == y</code>, then the tuple <strong>is included</strong> in
 * stream segment <code>id</code>, otherwise it <strong>is not included</strong>.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class ElementPriority {

   /** Priority x value. */
   private final int x;
   /** Priority y value. */
   private final int y;

   /**
    * Creates a new priority with the given x and y values, which control in which segment numbers the
    * corresponding tuple will be included.
    *
    * @param x
    *           priority x value
    * @param y
    *           priority y value
    */
   public ElementPriority(final int x, final int y) {
      this.x = x;
      this.y = y;
   }

   /**
    * Returns the x value of this priority.
    *
    * @return priority x value
    */
   public int getX() {
      return this.x;
   }

   /**
    * Returns the y value of this priority.
    *
    * @return priority y value
    */
   public int getY() {
      return this.y;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      final StringBuffer result = new StringBuffer();
      result.append("(");
      result.append(this.getX());
      result.append(",");
      result.append(this.getY());
      result.append(")");
      return result.toString();
   }
}
