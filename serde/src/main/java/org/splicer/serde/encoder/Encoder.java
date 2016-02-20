package org.splicer.serde.encoder;

/**
 * Licensed under the Apache License, Version 2.0.
 *
 * Encoder
 *
 * Created by siyengar on 2/19/16.
 */
public interface Encoder<A> {
    byte[] encode(A value);
}