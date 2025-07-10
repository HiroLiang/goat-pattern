package com.hiro.goat.api.signature;

/**
 * Signature generator
 */
public interface Signer {

    /**
     * Accept Signable and give it a signature.
     *
     * @param signable Signable object
     */
    void sign(Signable signable);

}
