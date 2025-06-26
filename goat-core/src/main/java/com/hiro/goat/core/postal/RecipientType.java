package com.hiro.goat.core.postal;

/**
 * Recipient type for delivery
 */
public enum RecipientType {

    /**
     * deliver to particular mailbox
     */
    MAILBOX,

    /**
     * deliver to all mailbox registered particular group
     */
    GROUP,

    /**
     * deliver to all mailboxes in the postal center
     */
    BROADCAST

}
