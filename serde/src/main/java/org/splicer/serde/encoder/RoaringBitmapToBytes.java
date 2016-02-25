/*
 * Licensed under the Apache License, Version 2.0.
 */

package org.splicer.serde.encoder;

import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;

/**
 * Created by siyengar on 2/24/16.
 */
public class RoaringBitmapToBytes {
    public static byte[] toBytes(RoaringBitmap roaringBitmap) {
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

        return bitmapByteArray;

    }
}
