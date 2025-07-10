package com.hiro.goat.api.signature;

/**
 * Signature checker
 */
public interface Verifier {

    /**
     * Verify signature in signable object
     *
     * @param signable signable object
     *
     * @return Is signature pass verify.
     */
    boolean verify(Signable signable);

}
