/*
 * @(#)SegmentationKeyFunction.java   1.0   Feb 18, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;

/**
 * A SegmentationKeyFunction calculates the keys for the segment IDs of a given tuple.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public interface SegmentationKeyFunction {

   /**
    * Calculates the keys for the segment IDs of the given tuple.
    *
    * @param tuple
    *           the tuple the segment IDs belong to
    * @return the segment keys
    */
   Object[] getSegmentKeys(final DataTuple tuple);

   /**
    * Calculates the keys for the segment IDs of the given {@link PunctuationControl}.
    *
    * @param punctuation
    *           the punctuation from which to derive
    * @return the segment keys
    */
   Object[] getSegmentKeys(final PunctuationControl punctuation);
}
