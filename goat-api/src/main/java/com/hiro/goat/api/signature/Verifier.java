package com.hiro.goat.api.signature;

public interface Verifier {

    boolean verify(Signable signable);

}
