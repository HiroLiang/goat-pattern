package com.hiro.goat.platform.dock;

import com.hiro.goat.api.task.Task;
import com.hiro.goat.core.postal.MailBox;
import com.hiro.goat.core.postal.PostalCenter;

public class DistributionCenter extends PostalCenter<Task> {

    @Override
    protected String createPostalCode() {
        return "";
    }

    @Override
    protected MailBox<Task> createMailBox(String postalCode) {
        return null;
    }

}
