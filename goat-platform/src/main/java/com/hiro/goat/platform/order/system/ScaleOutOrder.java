package com.hiro.goat.platform.order.system;

import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.order.task.TaskOrder;

public class ScaleOutOrder extends SystemOrder<TaskOrder<?>, Void> {

    private final Class<? extends Platform> platformClass;

    private Platform newPlatform;

    protected ScaleOutOrder(Class<? extends Platform> platformClass) {
        this.platformClass = platformClass;
    }

    @Override
    protected void process() {
        this.newPlatform = platform.scaleOut(platformClass, param);
    }

    @Override
    protected void rollback() {
        if (this.newPlatform != null && this.newPlatform.getId() > 0) {
            platform.destroy(newPlatform.getId());
        }
    }

}
