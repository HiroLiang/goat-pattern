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
    private final Map<Long, Mailbox<T>> registeredMailboxesHolder = new ConcurrentHashMap<>();

    /**
     * Hold groups and who join in this group.
     */
    private final Map<String, Set<Long>> registeredGroupsHolder = new ConcurrentHashMap<>();

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
     * Customize the way to generate postal code
     *
     * @return postal code (long)
     */
    protected abstract long createPostalCode();

    /**
     * Customize the way to generate mailbox
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
        registeredMailboxesHolder.put(postalCode, mailBox);
        return mailBox;
    }

    /**
     * Unregister a mailbox
     *
     * @param mailBox Mailbox
     */
    public void unregister(Mailbox<T> mailBox) {
        verifyMailbox(mailBox);
        removeFromGroups(mailBox);
        registeredMailboxesHolder.remove(mailBox.getPostalCode());
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
        registeredGroupsHolder.computeIfAbsent(group, k -> ConcurrentHashMap.newKeySet()).add(mailBox.getPostalCode());
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
        removeFromGroups(mailBox, group);
    }

    /**
     * Unregister all groups
     *
     * @param mailBox Mailbox
     */
    public void unregisterAllGroups(Mailbox<T> mailBox) {
        verifyMailbox(mailBox);
        removeFromGroups(mailBox);
    }

    /**
     * Get PostalParcel to particular recipient
     *
     * @param sender    sender's Mailbox
     * @param recipient recipient postal code
     *
     * @return PostalParcel
     */
    public PostalParcel<T> getParcel(Mailbox<T> sender, long recipient) {
        verifyMailbox(sender);
        if (!isRegistered(recipient)) {
            throw GoatErrors.of("Recipient is not registered.", PostalException.class);
        }
        return new PostalParcel<>(sender.getPostalCode(), recipient, RecipientType.MAILBOX);
    }

    /**
     * Get PostalParcel to a particular recipient for delivering to a group
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
     * Get PostalParcel to all recipients who registered this postal center
     *
     * @param sender sender's Mailbox
     *
     * @return PostalParcel
     */
    public PostalParcel<T> getBroadcastParcel(Mailbox<T> sender) {
        verifyMailbox(sender);
        return new PostalParcel<>(sender.getPostalCode(), -1L, RecipientType.BROADCAST);
    }

    /**
     * Check is postal code registered
     *
     * @param postalCode Long
     *
     * @return true if this code registered
     */
    public boolean isRegistered(long postalCode) {
        return registeredMailboxesHolder.containsKey(postalCode);
    }

    /**
     * CHeck is mailbox registered
     *
     * @param mailBox Mailbox
     *
     * @return true if Mailbox registered
     */
    public boolean isRegistered(Mailbox<T> mailBox) {
        return mailBox != null && registeredMailboxesHolder.containsKey(mailBox.getPostalCode());
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

            if (!registeredGroupsHolder.containsKey(parcel.getGroup())) {
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
                return Collections.singletonList(this.registeredMailboxesHolder.get(parcel.getRecipient()));
            case GROUP:
                Set<Long> mailBoxIds = this.registeredGroupsHolder.get(parcel.getGroup());
                return this.registeredMailboxesHolder.values().stream()
                        .filter(mailBox -> StringUtils.isBlank(parcel.getGroup()) ||
                                mailBoxIds.contains(mailBox.getPostalCode()))
                        .collect(Collectors.toList());
            case BROADCAST:
                return new ArrayList<>(this.registeredMailboxesHolder.values());
            default:
                throw GoatErrors.of("Unknown recipient type: " + parcel.getRecipientType(), PostalException.class);
        }
    }

    /**
     * Remove mailbox from groups holder
     *
     * @param mailBox  to remove
     * @param groupIds group IDs to remove
     */
    private void removeFromGroups(Mailbox<T> mailBox, String... groupIds) {
        if (mailBox == null) return;

        if (groupIds == null || groupIds.length == 0) {
            Set<String> groups = this.registeredGroupsHolder.keySet().stream()
                    .filter(group -> this.registeredGroupsHolder.get(group).contains(mailBox.getPostalCode()))
                    .collect(Collectors.toSet());
            groups.forEach(group -> this.registeredGroupsHolder.get(group).remove(mailBox.getPostalCode()));
        } else {
            Arrays.stream(groupIds).forEach(groupId -> {
                if (this.registeredGroupsHolder.containsKey(groupId)) {
                    this.registeredGroupsHolder.get(groupId).remove(mailBox.getPostalCode());
                }
            });
        }

        for (String groupId : registeredGroupsHolder.keySet()) {
            if (registeredGroupsHolder.get(groupId).isEmpty()) {
                registeredGroupsHolder.remove(groupId);
            }
        }
    }

}
