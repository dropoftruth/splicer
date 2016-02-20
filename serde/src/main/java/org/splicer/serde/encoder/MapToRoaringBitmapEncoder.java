package org.splicer.serde.encoder;

import com.google.common.primitives.Ints;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.Map;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Created by siyengar on 2/19/16.
 *
 * Encoder for provided type to Roaring BitMap - a data structure that is compressed bitmaps.
 * Roaring bitmaps apply run length encoding to minimize the storage without largely affecting (de)serialization costs
 * Refer https://github.com/RoaringBitmap/RoaringBitmap
 *
 * The instance must be re-used for a batch of encodings. All encodings done by an instance will all have same marker (max possible offset that can be set)
 *
 */

public class MapToRoaringBitmapEncoder implements Encoder<Map<Integer, Boolean>> {

    private final int maxOffsetToBeTrue;
    public MapToRoaringBitmapEncoder(int maxOffsetToBeTrue) {
        this.maxOffsetToBeTrue = maxOffsetToBeTrue;
    }

    public int getMaxOffsetToBeTrue() {
        return this.maxOffsetToBeTrue;
    }

    public byte[] encode(final Map<Integer, Boolean> values) {
        RoaringBitmap roaringBitmap = new RoaringBitmap();

        /* null represents zero byte array */
        if (values == null) {
            return new byte[0];
        }

        int highestOffsetSet = 0;
        for (Map.Entry<Integer, Boolean> e : values.entrySet()) {
            if (e.getKey() <= 0) {
                throw new IllegalArgumentException("offset must be a positive integer");
            }

            if (e.getKey() > maxOffsetToBeTrue) {
                throw new IllegalArgumentException("Offset cannot be more than " + maxOffsetToBeTrue);
            }

            if (Boolean.TRUE.equals(e.getValue())) {
                if (e.getKey() > highestOffsetSet) {
                    highestOffsetSet = e.getKey();
                }
                roaringBitmap.add(e.getKey());
            }
        }

        /* Apply run length encoding */
        roaringBitmap.runOptimize();

        final byte[] bitmapByteArray = new byte[roaringBitmap.serializedSizeInBytes()];
        try {
            roaringBitmap.serialize(new java.io.DataOutputStream(new java.io.OutputStream() {
                int c = 0;

                @Override
                public void close() {
                }

                @Override
                public void flush() {
                }

                @Override
                public void write(int b) {
                    bitmapByteArray[c++] = (byte)b;
                }

                @Override
                public void write(byte[] b) {
                    write(b,0,b.length);
                }

                @Override
                public void write(byte[] b, int off, int l) {
                    System.arraycopy(b, off, bitmapByteArray, c, l);
                    c += l;
                }
            }));
        } catch (IOException ioe) {
            // should never happen because we write to a byte array
            throw new RuntimeException("unexpected error while serializing to a byte array");
        }

        /* write marker (maxOffsetToBeTrue this bitmap supports) */
        byte[] marker = Ints.toByteArray(maxOffsetToBeTrue);
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
