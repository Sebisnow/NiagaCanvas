/*
 * @(#)ElementMetadata.java   1.0   Feb 14, 2011
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the system metadata associated with a tuple or stream.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public class ElementMetadata implements Cloneable {

   /** Numbers of the segments to which the corresponding stream element belongs. */
   private List<Long> segmentIds;
   /** Operator-specific metadata. */
   private Map<Class< ? >, Object> opData;
   /** Priority of the corresponding stream element. */
   private ElementPriority priority;

   /**
    * Creates a new empty element metadata record.
    */
   public ElementMetadata() {
      this.segmentIds = new ArrayList<Long>();
      this.opData = new HashMap<Class< ? >, Object>();
      this.priority = new ElementPriority(1, 0);
   }

   /**
    * Adds the number of a segment to which the corresponding stream element belongs.
    *
    * @param id
    *           segment number
    */
   public void addSegmentId(final long id) {
      if (this.segmentIds.contains(Long.valueOf(id))) {
         throw new IllegalStateException("Tuple is already contained in segment " + id);
      }
      if (id % this.priority.getX() == this.priority.getY()) {
         this.segmentIds.add(Long.valueOf(id));
      }
   }

   /**
    * Clears segment ids of tuple.
    */
   public void clearSegmentIds() {
      this.segmentIds.clear();
   }

   /**
    * Returns the numbers of all segments to which the corresponding stream element belongs.
    *
    * @return segment numbers
    */
   public List<Long> getSegmentIds() {
      return Collections.unmodifiableList(this.segmentIds);
   }

   /**
    * Sets the priority of the corresponding stream element.
    *
    * @param priority
    *           stream element priority
    */
   public void setElementPriority(final ElementPriority priority) {
      if (priority != null) {
         this.priority = priority;
      } else {
         this.priority = new ElementPriority(1, 0);
      }
   }

   /**
    * Returns the priority of the corresponding stream element.
    *
    * @return stream element priority
    */
   public ElementPriority getElementPriority() {
      return this.priority;
   }

   @Override
   public ElementMetadata clone() throws CloneNotSupportedException {
      final ElementMetadata clone = (ElementMetadata) super.clone();
      clone.segmentIds = new ArrayList<Long>(this.segmentIds);
      clone.opData = new HashMap<Class< ? >, Object>(this.opData);
      clone.priority = new ElementPriority(this.priority.getX(), this.priority.getY());
      return clone;
   }

   @Override
   public String toString() {
      final StringBuffer result = new StringBuffer();
      result.append(this.getClass().getSimpleName());
      result.append("[segmentIds=");
      result.append(this.segmentIds);
      result.append(",priority=");
      result.append(this.priority);
      result.append("]");
      return result.toString();
   }
}
