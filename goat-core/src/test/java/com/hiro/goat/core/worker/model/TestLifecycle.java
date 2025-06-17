package com.hiro.goat.core.worker.model;

import com.hiro.goat.core.worker.AbstractLifecycle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestLifecycle extends AbstractLifecycle {

    @Override
    protected void onStart() {
        log.info("TestLifecycle start");
    }

    @Override
    protected void onStop() {
        log.info("TestLifecycle stop");
    }

    @Override
    protected void onPause() {
        log.info("TestLifecycle pause");
    }

    @Override
    protected void onResume() {
        log.info("TestLifecycle resume");
    }

    @Override
    protected void onDestroy() {
        log.info("TestLifecycle destroy");
    }
}
