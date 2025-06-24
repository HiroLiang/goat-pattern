package com.hiro.goat.platform;

import com.hiro.goat.core.postal.PostalParcel;
import com.hiro.goat.core.postal.RecipientType;
import com.hiro.goat.core.worker.QueueDispatchWorker;
import com.hiro.goat.platform.order.PlatformOrder;
import com.hiro.goat.platform.order.system.ScaleOutOrder;
import com.hiro.goat.platform.order.system.SystemOrder;
import com.hiro.goat.platform.order.task.TaskOrder;
import com.hiro.goat.platform.postal.PlatformMailbox;
import com.hiro.goat.platform.postal.PlatformPostalCenter;
import com.hiro.goat.platform.utils.PlatformFactory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public abstract class Platform extends QueueDispatchWorker<PlatformOrder<?, ?>> {

    @Getter
    protected final long id;

    protected final long parentId;

    protected final PlatformMailbox mailbox;

    protected final OrderProcessor processor;

    protected final PlatformPostalCenter postalCenter;

    protected final PlatformFactory factory;

    protected final Map<Class<? extends Platform>, Set<Platform>> children = new ConcurrentHashMap<>();

    protected final Consumer<PlatformOrder<?, ?>> lostOrderConsumer = defineLostOrderConsumer();

    protected Platform(PlatformPostalCenter postalCenter, long parentId) {
        this.parentId = parentId;
        this.mailbox = postalCenter.register().setTaskConsumer(this::offer);
        this.id = this.mailbox.getPostalCode();
        this.processor = createProcessor();
        this.postalCenter = postalCenter;
        this.factory = new PlatformFactory(postalCenter);
    }

    @Override
    protected void processTask(PlatformOrder<?, ?> task) {
        this.processor.process(task);
    }

    @Override
    protected void alert() {
        super.alert();
    }

    @Override
    public boolean offer(PlatformOrder<?, ?> order) {
        if (super.offer(order)) {
            return true;
        }

        if (this.parentId <= 0 || order instanceof SystemOrder) {
            this.put(order);
            return true;
        }

        sendScaleOutOrder(order);
        return true;
    }

    @Override
    protected void beforeDestroy() {
        for (Set<Platform> platforms : this.children.values()) {
            for (Platform platform : platforms) {
                platform.destroy();
            }
        }
        this.children.clear();
    }

    public synchronized Platform create(Class<? extends Platform> platformClazz) {
        Platform platform = this.factory.newInstance(platformClazz, this.id);
        this.children.computeIfAbsent(platformClazz, k -> ConcurrentHashMap.newKeySet()).add(platform);
        return platform;
    }

    public synchronized void destroy(long id) {
        this.children.values().forEach(set ->
                set.removeIf(platform -> {
                    if (platform.getId() == id) {
                        platform.destroy();
                        return true;
                    }
                    return false;
                })
        );

        this.children.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public synchronized void destroy(Class<? extends Platform> platformClazz) {
        Set<Platform> removed = this.children.remove(platformClazz);
        if (removed != null) {
            removed.forEach(Platform::destroy);
        }
    }

    protected OrderProcessor createProcessor() {
        return new OrderProcessor(this);
    }

    protected Consumer<PlatformOrder<?, ?>> defineLostOrderConsumer() {
        return order -> log.warn("An order \"{}\" lost.", order.getClass().getSimpleName());
    }

    private void put(PlatformOrder<?, ?> order) {
        try {
            this.submit(order);
        } catch (InterruptedException e) {
            this.lostOrderConsumer.accept(order);
        }
    }

    private void sendScaleOutOrder(PlatformOrder<?, ?> order) {
        PostalParcel<PlatformOrder<?, ?>> parcel = postalCenter.getParcel(this.mailbox, this.parentId, RecipientType.MAILBOX);

        ScaleOutOrder scaleOutOrder = new ScaleOutOrder();
        scaleOutOrder.initParams((TaskOrder<?>) order);

        parcel.put(scaleOutOrder);
        this.postalCenter.offer(parcel);
    }

    public static class PlatformHolder {

        private final Platform platform;

        private boolean depatched = false;

        public PlatformHolder(Platform platform) {
            this.platform = platform;
        }

    }

}
