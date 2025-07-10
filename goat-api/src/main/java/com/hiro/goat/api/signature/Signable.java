package com.hiro.goat.api.signature;

/**
 * Item to be signed
 */
public interface Signable {

    /**
     * Get String to be signed
     *
     * @return String
     */
    String signableData();

    /**
     * Set signature from Signer
     *
     * @param signature Generate by signable fata
     */
    void setSignature(String signature);

    /**
     * For Verifier to get signature
     *
     * @return signature
     */
    String getSignature();

}
