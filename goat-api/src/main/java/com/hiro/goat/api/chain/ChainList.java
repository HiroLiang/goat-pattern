package com.hiro.goat.api.chain;

import java.util.List;
import java.util.function.Predicate;

public interface ChainList<T extends Chainable> {

    ChainList<T> chain(T chain);

    ChainList<T> chainIf(T chain, Predicate<T> condition);

    List<T> build();

}
