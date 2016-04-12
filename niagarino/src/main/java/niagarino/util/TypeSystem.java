/*
 * @(#)TypeSystem.java   1.0   Feb 15, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class to convert values from strings to Java objects.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public final class TypeSystem {

   /**
    * Hidden constructor of utility class.
    */
   private TypeSystem() {
      // hidden constructor of utility class
   }

   /**
    * Converts the given value into a value of the give type.
    *
    * @param type
    *           target type
    * @param value
    *           object value
    * @return converted value
    */
   public static Object convertValue(final Class< ? > type, String value) {
      if ("".equals(value) || "NA".equals(value)) {
         return null;
      } else if (String.class.equals(type)) {
         return value;
      } else if (Double.class.equals(type)) {
         Double tmp = null;
         try {
            tmp = Double.valueOf(value);
         } catch (final Exception e) {
            return tmp;
         }
         return tmp;
      } else if (Float.class.equals(type)) {
         Float tmp = null;
         try {
            tmp = Float.valueOf(value);
         } catch (final Exception e) {
            return tmp;
         }
         return tmp;
      } else if (Long.class.equals(type)) {
         Long tmp = null;
         try {
            tmp = Long.valueOf(value);
         } catch (final Exception e) {
            return tmp;
         }
         return tmp;
      } else if (Integer.class.equals(type)) {
         Integer tmp = null;
         try {
            tmp = Integer.valueOf(value);
         } catch (final Exception e) {
            return tmp;
         }
         return tmp;
      } else if (Boolean.class.equals(type)) {
         Boolean tmp = null;
         try {
            tmp = Boolean.valueOf(value);
         } catch (final Exception e) {
            return tmp;
         }
         return tmp;
      } else if (Date.class.equals(type)) {
         value = value.replaceAll("^\"|\"$", "");
         String dt;
         final char sign = value.charAt(value.length() - 3);
         if (sign == '-' || sign == '+') {
            dt = "yyyy-MM-dd HH:mm:ssX";
         } else {
            dt = "yyyy-MM-dd HH:mm:ss";
         }
         final SimpleDateFormat[] sdfs = { new SimpleDateFormat(dt),
               new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH) };
         for (final SimpleDateFormat sdf : sdfs) {
            try {
               return sdf.parse(value);
            } catch (final ParseException ex) {
               continue;
            }
         }
         try {
            final long timestamp = Long.parseLong(value);
            return new Date(timestamp);
         } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Value type not supported: "
                  + type.getSimpleName() + " " + value + ".", e);
         }
      }
      throw new IllegalArgumentException("Value type not supported: " + type.getSimpleName()
            + " " + value + ".");
   }

   /**
    * Checks whether the given type is a numeric type.
    *
    * @param type
    *           type class
    * @return <code>true</code> if the type is numeric, <code>false</code> otherwise
    */
   public static boolean isNumeric(final Class< ? > type) {
      return Number.class.isAssignableFrom(type);
   }

   /**
    * Finds and returns the common type of the two given types. Strings and dates only have a
    * common type, if both given types are the same. For numeric types, the common type is
    * defined as the type, which can represent values of both given types, i.e., Double is
    * the common type of Integer and Double.
    *
    * @param typeOne
    *           first type
    * @param typeTwo
    *           second type
    * @return common type or <code>null</code> if there exist no common type.
    */
   public static Class< ? > commonType(final Class< ? > typeOne, final Class< ? > typeTwo) {
      if (TypeSystem.isNumeric(typeOne) && TypeSystem.isNumeric(typeTwo)) {
         // TODO what should happen with Long/Float or Float/Long?
         if (Double.class.equals(typeOne) || Float.class.equals(typeOne)) {
            return typeOne;
         } else {
            return typeTwo;
         }
      } else if (String.class.equals(typeOne) && String.class.equals(typeTwo)) {
         return typeOne;
      } else if (Date.class.equals(typeOne) && Date.class.equals(typeTwo)) {
         return typeOne;
      }
      return null;
   }

   /**
    * Converts the given numeric value into a value of the given numeric type.
    *
    * @param type
    *           numeric type
    * @param value
    *           numeric value
    * @return converted value, <code>null</code> if the given value is not a number, or an
    *         exception if the given type is not a numeric type
    */
   public static Number convertNumber(final Class< ? > type, final double value) {
      if (Double.isNaN(value)) {
         return null;
      }
      if (TypeSystem.isNumeric(type)) {
         if (Double.class.equals(type)) {
            return Double.valueOf(value);
         } else if (Long.class.equals(type)) {
            return Long.valueOf((long) value);
         } else if (Float.class.equals(type)) {
            return Float.valueOf((float) value);
         } else if (Integer.class.equals(type)) {
            return Integer.valueOf((int) value);
         }
      }
      throw new IllegalArgumentException("Type must be a number.");
   }
}
