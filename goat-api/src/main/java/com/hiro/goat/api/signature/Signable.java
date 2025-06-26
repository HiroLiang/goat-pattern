package com.hiro.goat.api.signature;

public interface Signable {

    String signableData();

    void setSignature(String signature);

    String getSignature();

}
