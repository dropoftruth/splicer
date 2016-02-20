package org.splicer.serde.encoder;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Created by siyengar on 2/19/16.
 */
public class MapToRoaringBitmapEncoderTest {

    @Test(expected = IllegalArgumentException.class)
    public void test2() {
        int max = 1000000;
        Encoder<Map<Integer, Boolean>> encoder = new MapToRoaringBitmapEncoder(max);
        Map<Integer, Boolean> values = Maps.newHashMap();
        values.put(max+1, true);
        encoder.encode(values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test3() {
        Encoder<Map<Integer, Boolean>> encoder = new MapToRoaringBitmapEncoder(23);
        Map<Integer, Boolean> values = Maps.newHashMap();
        values.put(0, true);
        encoder.encode(values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test4() {
        Encoder<Map<Integer, Boolean>> encoder = new MapToRoaringBitmapEncoder(23);
        Map<Integer, Boolean> values = Maps.newHashMap();
        values.put(-1, true);
        encoder.encode(values);
    }

    @Test
    public void test1() {
        /**
         * Test to store upto 1m bits with max offset being set to 100000. This uses only 46 bytes.
         */
        int max = 1000000;
        Encoder<Map<Integer, Boolean>> encoder = new MapToRoaringBitmapEncoder(max);

        assertArrayEquals(new byte[0], encoder.encode(null));

        int[] tobeTrue = {23, 45, 2, 5, 22, 75, 100000};
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

        byte[] maxBitBytes = new byte[4];

        System.arraycopy(byteArray, marker.length, maxBitBytes, 0, maxBitBytes.length);

        int maxOffsetSetToTrue = Ints.fromByteArray(maxBitBytes);

        assertEquals(maxOffsetSetToTrue, 100000);

        final byte[] roaringBitMapArray = new byte[byteArray.length - 8];

        System.arraycopy(byteArray, marker.length + maxBitBytes.length, roaringBitMapArray, 0, roaringBitMapArray.length);

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
