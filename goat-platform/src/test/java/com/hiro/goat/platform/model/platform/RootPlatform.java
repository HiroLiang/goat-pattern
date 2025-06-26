package com.hiro.goat.platform.model.platform;

import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.postal.PlatformPostalCenter;

import java.util.HashSet;
import java.util.Set;

public class RootPlatform extends Platform {

    /**
     * Constructor
     *
     * @param postalCenter Postal center to register should be defined first.
     * @param parentId     Root parent ID should be -1
     */
    public RootPlatform(PlatformPostalCenter postalCenter, Long parentId) {
        super(postalCenter, parentId);
    }

    @Override
    protected Set<Class<? extends Platform>> defineChildrenClass() {
        Set<Class<? extends Platform>> set = new HashSet<>();

        set.add(ChildPlatform.class);

        return set;
    }

}
