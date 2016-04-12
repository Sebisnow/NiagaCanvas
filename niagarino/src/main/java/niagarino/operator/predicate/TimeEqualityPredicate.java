/*
 * @(#)TimeEqualityPredicate.java   1.0   Mar 1, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.predicate;

import java.util.Calendar;
import java.util.Date;

import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * A predicate that compares the configured time of day to a date for equality.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class TimeEqualityPredicate implements Predicate {

   /** Position of the date attribute. */
   private final int dateAttribute;
   /** Hours to match to the date attribute. */
   private int hours;
   /** Minutes to match to the date attribute. */
   private int minutes;
   /** Seconds to match to the date attribute. */
   private int seconds;
   /** Calendar instance used in this predicate. */
   private Calendar calendar;

   /**
    * Creates a new predicate that tests whether the date attribute given by its position
    * matches the given hours, minutes, and seconds.
    *
    * @param dateAttribute
    *           date attribute position
    * @param hours
    *           hours to match
    * @param minutes
    *           minutes to match
    * @param seconds
    *           seconds to match
    */
   public TimeEqualityPredicate(final int dateAttribute, final int hours, final int minutes,
         final int seconds) {
      this.dateAttribute = dateAttribute;
      if (0 <= hours && hours < 24 && 0 <= minutes && minutes < 60 && 0 <= seconds
            && seconds < 60) {
         this.hours = hours;
         this.minutes = minutes;
         this.seconds = seconds;
         this.calendar = Calendar.getInstance();
      } else {
         throw new IllegalArgumentException("Invalid time: " + hours + ":" + minutes + ":"
               + seconds + ".");
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isApplicable(final Schema schema) {
      if (0 <= this.dateAttribute && this.dateAttribute < schema.getSize()) {
         final Attribute attribute = schema.getAttribute(this.dateAttribute);
         return Date.class.equals(attribute.getType());
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean evaluate(final DataTuple tuple) {
      final Date date = (Date) tuple.getAttributeValue(this.dateAttribute);
      this.calendar.setTime(date);
      return this.hours == this.calendar.get(Calendar.HOUR_OF_DAY)
            && this.minutes == this.calendar.get(Calendar.MINUTE)
            && this.seconds == this.calendar.get(Calendar.SECOND);
   }
}
