package org.splicer.serde.encoder;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Created by siyengar on 2/19/16.
 */
public class ArrayToRoaringBitmapEncoder implements Encoder<boolean[]> {
    public byte[] encode(final boolean[] value) {
        return new byte[0];
    }
}
