package com.hiro.goat.api.chain;

import javax.xml.ws.Provider;

public interface Chainable {

    <T extends Chainable> ChainList<T> chaining();

    <T extends Chainable> ChainList<T> chainingIf(Provider<Boolean> condition);

}
