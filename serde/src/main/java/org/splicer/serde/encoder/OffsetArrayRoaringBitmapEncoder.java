/*
 * Licensed under the Apache License, Version 2.0.
 */

package org.splicer.serde.encoder;

import com.google.common.primitives.Ints;
import org.roaringbitmap.RoaringBitmap;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Implementation of Encoder that takes in array of offsets that are to be set in the Roaring Bitmap
 * Created by siyengar on 2/19/16.
 */
public class OffsetArrayRoaringBitmapEncoder implements Encoder<int[]> {
    private final int largestOffset;

    public OffsetArrayRoaringBitmapEncoder(int largestOffset) {
        this.largestOffset = largestOffset;
    }

    public int getLargestOffset() {
        return this.largestOffset;
    }

    public byte[] encode(final int[] values) {
        RoaringBitmap roaringBitmap = new RoaringBitmap();

        /* null represents zero byte array */
        if (values == null) {
            return new byte[0];
        }

        int highestOffsetSet = 0;
        for (int b : values) {
            if (b <= 0) {
                throw new IllegalArgumentException("offset must be a positive integer");
            }

            if (b > largestOffset) {
                throw new IllegalArgumentException("Offset cannot be more than " + largestOffset);
            }

            roaringBitmap.add(b);

            if (b > highestOffsetSet) {
                highestOffsetSet = b;
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
