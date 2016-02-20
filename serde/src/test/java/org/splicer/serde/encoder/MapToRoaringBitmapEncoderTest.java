package org.splicer.serde.encoder;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Created by siyengar on 2/19/16.
 */
public class MapToRoaringBitmapEncoderTest {
    @Test
    public void test1() {
        int max = 1000000;
        Encoder<Map<Integer, Boolean>> encoder = new MapToRoaringBitmapEncoder(max);


        int[] tobeTrue = {23, 45, 2, 5, 22, 75, 10000};
        Map<Integer, Boolean> map = Maps.newHashMap();

        for (int ii=1 ; ii < max; ii++) {
            map.put(ii, false);
        }

        for (int ii : tobeTrue) {
            map.put(ii, true);
        }

        byte[] byteArray = encoder.encode(map);

        System.out.println(byteArray.length);

        byte[] marker = new byte[4];

        System.arraycopy(byteArray, 0, marker, 0, 4);

        int markerInt = Ints.fromByteArray(marker);

        assertEquals(markerInt, max);

        final byte[] roaringBitMapArray = new byte[byteArray.length - marker.length];

        System.arraycopy(byteArray, marker.length, roaringBitMapArray, 0, roaringBitMapArray.length);

        RoaringBitmap roaringBitmap = new RoaringBitmap();

        try {
            roaringBitmap.deserialize(new java.io.DataInputStream(new java.io.InputStream() {
                int c = 0;

                @Override
                public int read() {
                    return roaringBitMapArray[c++] & 0xff;
                }

                @Override
                public int read(byte b[]) {
                    return read(b, 0, b.length);
                }

                @Override
                public int read(byte[] b, int off, int l) {
                    System.arraycopy(roaringBitMapArray, c, b, off, l);
                    c += l;
                    return l;
                }
            }));
        } catch (IOException ioe) {
            // should never happen because we read from a byte array
            throw new RuntimeException("unexpected error while deserializing from a byte array");
        }

        for (int ii : tobeTrue) {
            assertTrue(roaringBitmap.contains(ii));
        }

    }
}
