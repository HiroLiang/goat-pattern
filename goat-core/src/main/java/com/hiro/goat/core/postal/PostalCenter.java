package com.hiro.goat.core.postal;

import com.hiro.goat.core.worker.QueueDispatchWorker;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public abstract class PostalCenter<T> extends QueueDispatchWorker<PostalParcel<T>> {

    private final Map<String, MailBox<T>> registeredHolders = new ConcurrentHashMap<>();

    protected abstract String createPostalCode();

    protected abstract MailBox<T> createMailBox(String postalCode);

    @Override
    protected void processTask(PostalParcel<T> parcel) {
        if (!registeredHolders.containsKey(parcel.getSender())) {
            throw new RuntimeException("Sender " + parcel.getSender() + " is not registered");
        }

        getRecipients(parcel).forEach(recipient -> recipient.deliver(parcel));
    }

    public MailBox<T> register() {
        String postalCode = createPostalCode();
        MailBox<T> mailBox = createMailBox(postalCode);
        registeredHolders.put(postalCode, mailBox);
        return mailBox;
    }

    public boolean isRegistered(MailBox<T> mailBox) {
        return registeredHolders.containsKey(mailBox.getPostalCode());
    }

    private List<MailBox<T>> getRecipients(PostalParcel<T> parcel) {
        switch (parcel.getRecipientType()) {
            case MAILBOX:
                return Collections.singletonList(this.registeredHolders.get(parcel.getRecipient()));
            case GROUP:
                return this.registeredHolders.values().stream()
                        .filter(mailBox -> parcel.getRecipient().equals(mailBox.getGroup()))
                        .collect(Collectors.toList());
            case BROADCAST:
                return new ArrayList<>(this.registeredHolders.values());
            default:
                throw new IllegalArgumentException("Unknown recipient type: " + parcel.getRecipientType());
        }
    }

}
