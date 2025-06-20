package com.hiro.goat.platform.dock;

import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.postal.Mailbox;
import com.hiro.goat.core.postal.PostalCenter;

public class DistributionCenter extends PostalCenter<Task> {

    protected DistributionCenter(String secret) {
        super(secret);
    }

    @Override
    protected long createPostalCode() {
        return 0;
    }

    @Override
    protected Mailbox<Task> createMailBox(long postalCode) {
        return null;
    }

}
