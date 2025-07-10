package com.hiro.goat.core.postal;

import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.exception.IllegalModifyException;
import com.hiro.goat.core.exception.PostalException;
import com.hiro.goat.core.worker.QueueDispatchWorker;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Dispatcher to deliver postal parcel to registered mail box
 *
 * @param <T> Parcel contain type / mailbox accept type
 */
@Slf4j
public abstract class PostalCenter<T> extends QueueDispatchWorker<PostalParcel<T>> {

    /**
     * Hold registered mailboxes.
     */
    private final Map<Long, Mailbox<T>> registeredHolders = new ConcurrentHashMap<>();

    /**
     * Hold groups and who join in this group.
     */
    private final Map<String, Set<Long>> registeredGroups = new ConcurrentHashMap<>();

    /**
     * Provide and verify signature (use sha-256)
     */
    protected final HmacSha256Signer signer;

    /**
     * Constructor
     *
     * @param secret secret for signature generator
     */
    protected PostalCenter(String secret) {
        this.signer = new HmacSha256Signer(secret);
    }

    /**
     * Customize way to generate postal code
     *
     * @return postal code (long)
     */
    protected abstract long createPostalCode();

    /**
     * Customize way to generate mailbox
     *
     * @param postalCode from the previous method
     *
     * @return MailBox
     */
    protected abstract Mailbox<T> createMailBox(long postalCode);

    /**
     * Consumer of the postal center:
     * 1. CVerify sender and recipients of parcel
     * 2. Sign signature and seal parcel
     * 3. Deliver parcel to all recipients
     *
     * @param parcel parcel to deliver
     */
    @Override
    protected void processTask(PostalParcel<T> parcel) {
        verifyParcel(parcel);

        parcel.signing(this, signer).seal();
        getRecipients(parcel).forEach(recipient -> recipient.deliver(parcel));
    }

    /**
     * Get a registered Mailbox
     *
     * @return Mailbox
     */
    public Mailbox<T> register() {
        long postalCode = createPostalCode();
        Mailbox<T> mailBox = createMailBox(postalCode);
        registeredHolders.put(postalCode, mailBox);
        return mailBox;
    }

    /**
     * Unregister a mailbox
     *
     * @param mailBox Mailbox
     */
    public void unregister(Mailbox<T> mailBox) {
        registeredHolders.remove(mailBox.getPostalCode());
    }

    /**
     * Register mailbox to particular group
     *
     * @param mailBox MailBox
     * @param group   String
     */
    public void registerGroup(Mailbox<T> mailBox, String group) {
        if (StringUtils.isBlank(group)) {
            throw GoatErrors.of("Register group is blank.", IllegalModifyException.class);
        }

        verifyMailbox(mailBox);

        if (registeredGroups.containsKey(group) && registeredGroups.get(group).contains(mailBox.getPostalCode())) {
            return;
        }

        registeredGroups.computeIfAbsent(group, k -> ConcurrentHashMap.newKeySet()).add(mailBox.getPostalCode());
    }

    /**
     * Cancel register group
     *
     * @param mailBox Mailbox
     * @param group   String
     */
    public void unregisterGroup(Mailbox<T> mailBox, String group) {
        if (StringUtils.isBlank(group)) {
            throw GoatErrors.of("Unregister group is blank.", IllegalModifyException.class);
        }

        verifyMailbox(mailBox);

        if (registeredGroups.containsKey(group)) {
            registeredGroups.get(group).remove(mailBox.getPostalCode());
        }
    }

    /**
     * Get PostalParcel to particular recipient
     *
     * @param sender    sender's Mailbox
     * @param recipient recipient postal code
     * @param type      type of recipient (for a particular mailbox / all)
     *
     * @return PostalParcel
     */
    public PostalParcel<T> getParcel(Mailbox<T> sender, long recipient, RecipientType type) {
        verifyMailbox(sender);
        if (RecipientType.MAILBOX.equals(type) && !isRegistered(recipient)) {
            throw GoatErrors.of("Recipient is not registered.", PostalException.class);
        } else if (RecipientType.GROUP.equals(type)) {
            throw GoatErrors.of("Recipient type is GROUP, can't get parcel by postal code.", PostalException.class);
        }
        return new PostalParcel<>(sender.getPostalCode(), recipient, type);
    }

    /**
     * Get PostalParcel to particular recipient for delivering to group
     *
     * @param sender sender's Mailbox
     * @param group  recipients' group
     *
     * @return PostalParcel
     */
    public PostalParcel<T> getParcel(Mailbox<T> sender, String group) {
        verifyMailbox(sender);
        return new PostalParcel<>(sender.getPostalCode(), group, RecipientType.GROUP);
    }

    /**
     * Check is postal code registered
     *
     * @param postalCode Long
     *
     * @return true if this code registered
     */
    public boolean isRegistered(long postalCode) {
        return registeredHolders.containsKey(postalCode);
    }

    /**
     * CHeck is mailbox registered
     *
     * @param mailBox Mailbox
     *
     * @return true if Mailbox registered
     */
    public boolean isRegistered(Mailbox<T> mailBox) {
        return registeredHolders.containsKey(mailBox.getPostalCode());
    }

    /**
     * Throw exception if mailbox not registered.
     *
     * @param mailBox Mailbox
     */
    private void verifyMailbox(Mailbox<T> mailBox) {
        if (!isRegistered(mailBox)) {
            throw GoatErrors.of("MailBox is not registered.", PostalException.class);
        }
    }

    /**
     * Verify parcel to deliver. Throw if the sender is not registered or the recipient is not registered / exist.
     *
     * @param parcel parcel to deliver
     */
    private void verifyParcel(PostalParcel<T> parcel) {
        if (!isRegistered(parcel.getSender())) {
            throw GoatErrors.of("Sender is not registered.", PostalException.class);
        }

        if (RecipientType.MAILBOX.equals(parcel.getRecipientType())) {
            if (!isRegistered(parcel.getRecipient())) {
                throw GoatErrors.of("Recipient is not registered.", PostalException.class);
            }
        } else if (RecipientType.GROUP.equals(parcel.getRecipientType())) {
            if (StringUtils.isBlank(parcel.getGroup())) {
                throw GoatErrors.of("Group is blank.", PostalException.class);
            }

            if (!registeredGroups.containsKey(parcel.getGroup())) {
                throw GoatErrors.of("Group is not registered.", PostalException.class);
            }
        }
    }

    /**
     * Get recipients of parcel
     *
     * @param parcel parcel to deliver
     *
     * @return List of mailbox
     */
    private List<Mailbox<T>> getRecipients(PostalParcel<T> parcel) {
        switch (parcel.getRecipientType()) {
            case MAILBOX:
                return Collections.singletonList(this.registeredHolders.get(parcel.getRecipient()));
            case GROUP:
                Set<Long> mailBoxIds = this.registeredGroups.get(parcel.getGroup());
                return this.registeredHolders.values().stream()
                        .filter(mailBox -> StringUtils.isBlank(parcel.getGroup()) ||
                                mailBoxIds.contains(mailBox.getPostalCode()))
                        .collect(Collectors.toList());
            case BROADCAST:
                return new ArrayList<>(this.registeredHolders.values());
            default:
                throw GoatErrors.of("Unknown recipient type: " + parcel.getRecipientType(), PostalException.class);
        }
    }

}
