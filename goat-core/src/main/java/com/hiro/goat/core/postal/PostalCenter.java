package com.hiro.goat.core.postal;

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

    private final Map<Long, Mailbox<T>> registeredHolders = new ConcurrentHashMap<>();

    private final Map<String, Set<Long>> registeredGroups = new ConcurrentHashMap<>();

    protected final HmacSha256Signer signer;

    protected PostalCenter(String secret) {
        this.signer = new HmacSha256Signer(secret);
    }

    protected abstract long createPostalCode();

    protected abstract Mailbox<T> createMailBox(long postalCode);

    @Override
    protected void processTask(PostalParcel<T> parcel) {
        verifyParcel(parcel);

        parcel.signing(this, signer).seal();
        getRecipients(parcel).forEach(recipient -> recipient.deliver(parcel));
    }

    public Mailbox<T> register() {
        long postalCode = createPostalCode();
        Mailbox<T> mailBox = createMailBox(postalCode);
        registeredHolders.put(postalCode, mailBox);
        return mailBox;
    }

    public void unregister(Mailbox<T> mailBox) {
        registeredHolders.remove(mailBox.getPostalCode());
    }

    public void registerGroup(Mailbox<T> mailBox, String group) {
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("group is blank");
        }

        verifyMailbox(mailBox);

        if (registeredGroups.containsKey(group) && registeredGroups.get(group).contains(mailBox.getPostalCode())) {
            return;
        }

        registeredGroups.computeIfAbsent(group, k -> ConcurrentHashMap.newKeySet()).add(mailBox.getPostalCode());
    }

    public void unregisterGroup(Mailbox<T> mailBox, String group) {
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("group is blank");
        }

        verifyMailbox(mailBox);

        if (registeredGroups.containsKey(group)) {
            registeredGroups.get(group).remove(mailBox.getPostalCode());
        }
    }

    public PostalParcel<T> getParcel(Mailbox<T> sender, long recipient, RecipientType type) {
        verifyMailbox(sender);
        if (!isRegistered(recipient)) {
            throw new IllegalArgumentException("Recipient is not registered");
        }
        return new PostalParcel<>(sender.getPostalCode(), recipient, type);
    }

    public PostalParcel<T> getParcel(Mailbox<T> sender, String group) {
        verifyMailbox(sender);
        return new PostalParcel<>(sender.getPostalCode(), group, RecipientType.GROUP);
    }

    public boolean isRegistered(long postalCode) {
        return registeredHolders.containsKey(postalCode);
    }

    public boolean isRegistered(Mailbox<T> mailBox) {
        return registeredHolders.containsKey(mailBox.getPostalCode());
    }

    private void verifyMailbox(Mailbox<T> mailBox) {
        if (!isRegistered(mailBox)) {
            throw new IllegalArgumentException("MailBox is not registered");
        }
    }

    private void verifyParcel(PostalParcel<T> parcel) {
        if (!isRegistered(parcel.getSender())) {
            throw new IllegalArgumentException("Sender is not registered");
        }

        if (RecipientType.MAILBOX.equals(parcel.getRecipientType())) {
            if (!isRegistered(parcel.getRecipient())) {
                throw new IllegalArgumentException("Recipient is not registered");
            }
        }
    }

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
                log.warn("Unknown recipient type: {}", parcel.getRecipientType());
                throw new IllegalArgumentException("Unknown recipient type: " + parcel.getRecipientType());
        }
    }

}
