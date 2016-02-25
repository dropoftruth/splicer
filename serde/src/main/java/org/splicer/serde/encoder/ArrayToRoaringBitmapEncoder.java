package org.splicer.serde.encoder;

import com.google.common.primitives.Ints;
import org.roaringbitmap.RoaringBitmap;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Implementation of Encoder that takes in array of boolean values are to be encoded to RoaringBitmap
 * Created by siyengar on 2/19/16.
 */
public class ArrayToRoaringBitmapEncoder implements Encoder<Boolean[]> {
    private final int largestOffset;

    public ArrayToRoaringBitmapEncoder(int largestOffset) {
        this.largestOffset = largestOffset;
    }

    public int getLargestOffset() {
        return this.largestOffset;
    }

    public byte[] encode(final Boolean[] values) {
        RoaringBitmap roaringBitmap = new RoaringBitmap();

        /* null represents zero byte array */
        if (values == null) {
            return new byte[0];
        }

        int cntr = 1;
        int highestOffsetSet = 0;
        for (Boolean b : values) {
            if (Boolean.TRUE.equals(b)) {
                if (cntr > highestOffsetSet) {
                    highestOffsetSet = cntr;
                }
                roaringBitmap.add(cntr);
            }
        }

        /* Apply run length encoding */
        roaringBitmap.runOptimize();

        final byte[] bitmapByteArray = RoaringBitmapToBytes.toBytes(roaringBitmap);

        /* write marker (largestOffset this bitmap supports) */
        byte[] marker = Ints.toByteArray(largestOffset);
        /*
        write max int set to true in this bitmap
        Adding 4 byte integer to mark the maximum offset that was set to true.
        This could be a small price to pay to prevent deserialization of RoaringBitMap if asking offset is > this
        */
        byte[] maxBitBytes = Ints.toByteArray(highestOffsetSet);

        byte[] finalBiMap = new byte[marker.length + maxBitBytes.length + bitmapByteArray.length];

        //Add marker
        System.arraycopy(marker, 0, finalBiMap, 0, marker.length);

        //Add maxBitByte
        System.arraycopy(maxBitBytes, 0, finalBiMap, marker.length, maxBitBytes.length);

        //Add roaring bitmap
        System.arraycopy(bitmapByteArray, 0, finalBiMap, marker.length + maxBitBytes.length, bitmapByteArray.length);

        return finalBiMap;
    }
}
