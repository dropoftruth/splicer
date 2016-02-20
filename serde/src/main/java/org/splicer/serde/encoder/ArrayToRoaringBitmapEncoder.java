package org.splicer.serde.encoder;

/**
 * Created by siyengar on 2/19/16.
 */
public class ArrayToRoaringBitmapEncoder implements Encoder<boolean[]> {
    public byte[] encode(final boolean[] value) {
        return new byte[0];
    }
}
