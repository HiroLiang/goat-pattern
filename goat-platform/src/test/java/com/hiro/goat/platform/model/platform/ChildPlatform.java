package com.hiro.goat.platform.model.platform;

import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.postal.PlatformPostalCenter;

import java.util.Collections;
import java.util.Set;

public class ChildPlatform extends Platform {

    public ChildPlatform(PlatformPostalCenter postalCenter, Long parentId) {
        super(postalCenter, parentId);
    }

    @Override
    protected Set<Class<? extends Platform>> defineChildrenClass() {
        return Collections.emptySet();
    }


}
