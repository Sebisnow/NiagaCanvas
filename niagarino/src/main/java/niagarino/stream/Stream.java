/*
 * @(#)Stream.java   1.0   Feb 14, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import niagarino.util.PropertiesReader;

/**
 * A stream connects a tuple producer to a tuple consumer.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Stream {

   /**
    * Enumeration of stream flow directions.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   public enum Flow {
      /** Forward stream flow direction. */
      FORWARD,
      /** Backward stream flow direction. */
      BACKWARD
   };

   /** Map that maintains a queue of stream elements for each stream flow direction. */
   private final Map<Flow, BlockingQueue<StreamElement>> stream;

   /**
    * Constructs a new stream.
    */
   public Stream() {
      this.stream = new HashMap<Flow, BlockingQueue<StreamElement>>();
      int streamSize = Integer.parseInt(PropertiesReader.getPropertiesReader().getProperties()
            .getProperty(PropertiesReader.STREAM_SIZE));
      final boolean isPaging = Boolean.parseBoolean(PropertiesReader.getPropertiesReader().getProperties()
            .getProperty(PropertiesReader.PAGING_ENABLED));
      if (isPaging) {
         final int pageSize = Integer.parseInt(PropertiesReader.getPropertiesReader().getProperties()
               .getProperty(PropertiesReader.PAGING_PAGESIZE));
         streamSize = streamSize / pageSize;
      }
      this.stream.put(Flow.FORWARD, new ArrayBlockingQueue<StreamElement>(streamSize));
      this.stream.put(Flow.BACKWARD, new ArrayBlockingQueue<StreamElement>(streamSize));
   }

   /**
    * Push the given stream element onto this stream with the given stream flow direction.
    *
    * @param flow
    *           stream flow direction
    * @param element
    *           stream element
    */
   public void pushElement(final Flow flow, final StreamElement element) {
      try {
         final BlockingQueue<StreamElement> queue = this.stream.get(flow);
         if (queue != null) {
            queue.put(element);
         }
      } catch (final InterruptedException e) {
         e.printStackTrace();
      }
   }

   /**
    * Pull a stream element from this stream with the given stream flow direction.
    *
    * @param flow
    *           stream flow direction
    * @return stream element or <code>null</code> if not stream element is available
    */
   public StreamElement pullElement(final Flow flow) {
      final Queue<StreamElement> queue = this.stream.get(flow);
      if (queue != null) {
         final StreamElement element = queue.poll();
         return element;
      }
      return null;
   }

   /**
    * Returns the size of this stream in terms of the number of stream elements. The size is defined as the
    * sum of the sizes of the forward and backward queue of this stream.
    *
    * @return stream size
    */
   public int getSize() {
      final int forwardSize = this.stream.get(Flow.FORWARD).size();
      final int backwardSize = this.stream.get(Flow.BACKWARD).size();
      return forwardSize + backwardSize;
   }

   /**
    * Clears the given direction of this {@link Stream}.
    *
    * @param flow
    *           the direction to clear
    */
   public synchronized void clearStream(final Flow flow) {
      final Queue<StreamElement> queue = this.stream.get(flow);
      if (queue != null) {
         queue.clear();
      }
   }
}
