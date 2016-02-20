package org.splicer.serde.encoder;

import com.google.common.primitives.Ints;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.Map;

/**
 * Created by siyengar on 2/19/16.
 */
public class MapToRoaringBitmapEncoder implements Encoder<Map<Integer, Boolean>> {

    private final int maxMapSize;
    public MapToRoaringBitmapEncoder(int maxMapSize) {
        this.maxMapSize = maxMapSize;
    }

    public int getMaxMapSize() {
        return this.maxMapSize;
    }

    public byte[] encode(final Map<Integer, Boolean> values) {
        RoaringBitmap roaringBitmap = new RoaringBitmap();

        if (values != null && values.size() > maxMapSize) {
            throw new IllegalArgumentException("Not able to accept map size or more than " + this.maxMapSize);
        }

        for (Map.Entry<Integer, Boolean> e : values.entrySet()) {
            if (e.getValue().equals(Boolean.TRUE)) {
                roaringBitmap.add(e.getKey());
            }
        }

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

        //write marker (maxMapSize this bitmap supports)
        byte[] marker = Ints.toByteArray(maxMapSize);

        byte[] finalBiMap = new byte[marker.length + bitmapByteArray.length];
        //Add marker
        System.arraycopy(marker, 0, finalBiMap, 0, marker.length);
        //Add roaring bitmap
        System.arraycopy(bitmapByteArray, 0, finalBiMap, marker.length, bitmapByteArray.length);

        return finalBiMap;
    }

}
