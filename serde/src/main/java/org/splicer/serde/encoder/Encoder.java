package org.splicer.serde.encoder;

/**
 * Encode - Decode (r)
 * Created by siyengar on 2/19/16.
 */
public interface Encoder<A> {
    byte[] encode(A value);
}