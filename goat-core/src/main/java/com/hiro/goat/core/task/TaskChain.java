package com.hiro.goat.core.task;

import com.hiro.goat.api.chain.ChainList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TaskChain implements ChainList<AbstractTask> {

    private final List<AbstractTask> tasks = new ArrayList<>();

    @Override
    public ChainList<AbstractTask> chain(AbstractTask chain) {
        this.tasks.add(chain);
        return this;
    }

    @Override
    public ChainList<AbstractTask> chainIf(AbstractTask chain, Predicate<AbstractTask> condition) {
        if  (condition.test(chain)) this.tasks.add(chain);
        return this;
    }

    @Override
    public List<AbstractTask> build() {
        return this.tasks;
    }
}
