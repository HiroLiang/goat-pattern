package com.hiro.goat.platform;

import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.postal.PostalParcel;
import com.hiro.goat.core.postal.RecipientType;
import com.hiro.goat.core.task.AbstractTask;
import com.hiro.goat.core.worker.QueueDispatchWorker;
import com.hiro.goat.platform.exception.PlatformException;
import com.hiro.goat.platform.factory.PlatformFactory;
import com.hiro.goat.platform.order.PlatformOrder;
import com.hiro.goat.platform.order.system.Order;
import com.hiro.goat.platform.order.system.SystemOrder;
import com.hiro.goat.platform.order.task.TaskOrder;
import com.hiro.goat.platform.order.task.TaskWrapper;
import com.hiro.goat.platform.postal.PlatformMailbox;
import com.hiro.goat.platform.postal.PlatformPostalCenter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public abstract class Platform extends QueueDispatchWorker<PlatformOrder<?, ?>> {

    /**
     * Postal code of mailbox
     */
    @Getter
    protected final long id;

    /**
     * Parent's postal code. If the parent of this platform does not exist, id should be -1;
     */
    protected final long parentId;

    /**
     * Receiver of platform order.
     * 1. Register from the postal center.
     * 2. Mailbox is required while asking parcel / delivering parcel.
     * 3.Need to give it a consumer of order.
     */
    protected final PlatformMailbox mailbox;

    /**
     * Order processor to distinguish system / task order, then execute the task.
     */
    protected final OrderProcessor processor;

    /**
     * Postal center for the platform.
     * 1. This is a generic postal center. If there isn't extra design, use PlatformPostalCenter directly.
     */
    protected final PlatformPostalCenter postalCenter;

    protected final PlatformFactory factory;

    protected final Set<Class<? extends Platform>> childrenClass = defineChildrenClass();

    protected final Map<Class<? extends Platform>, Set<PlatformHolder>> children = new ConcurrentHashMap<>();

    protected final Consumer<PlatformOrder<?, ?>> lostOrderConsumer = defineLostOrderConsumer();

    /**
     * Constructor
     *
     * @param postalCenter Postal center to register should be defined first.
     * @param parentId     Root parent ID should be -1
     */
    public Platform(PlatformPostalCenter postalCenter, Long parentId) {
        this.parentId = parentId;
        this.mailbox = postalCenter.register().setTaskConsumer(this::offer);
        this.id = this.mailbox.getPostalCode();
        this.processor = defineProcessor();
        this.postalCenter = postalCenter;
        this.factory = definePlatformFactory(postalCenter);
    }

    /**
     * Define what class this platform accepted to pu in children platforms.
     *
     * @return Set of class
     */
    protected abstract Set<Class<? extends Platform>> defineChildrenClass();

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
            this.waitUntilSubmit(order);
            return true;
        }

        sendScaleOutOrder((TaskOrder<?>) order);
        return true;
    }

    @Override
    protected void beforeDestroy() {
        for (Set<PlatformHolder> platformHolders : this.children.values()) {
            for (PlatformHolder holder : platformHolders) {
                holder.platform.destroy();
            }
        }
        this.children.clear();
    }

    public void orderPlatform(long postalCode, SystemOrder<?, ?> order) {
        if (postalCode > 0) {
            this.postalCenter.offer(getParcel(postalCode, order));
        } else {
            throw GoatErrors.of("Postal Code \"" + postalCode + "\" is not accepted.", PlatformException.class);
        }
    }

    public <P extends Platform, T extends AbstractTask<?, R>, R> void deliverTask(Class<P> platformClazz, T task) {
        long targetId = getPostalCode(platformClazz);

        if (targetId > 0) {
            this.postalCenter.offer(getParcel(targetId, new TaskWrapper<>(task)));
        } else {
            scaleOut(platformClazz, new TaskWrapper<>(task));
        }

    }

    public synchronized Platform create(Class<? extends Platform> platformClazz) {
        if (!childrenClass.contains(platformClazz)) {
            throw GoatErrors.of("Child Platform \"" + platformClazz.getName() + "\" is not accepted.", PlatformException.class);
        }

        Platform platform = this.factory.newInstance(platformClazz, this.id);
        platform.start();
        addChild(platform);
        return platform;
    }

    public synchronized Platform scaleOut(Class<? extends Platform> platformClass, TaskOrder<?> order) {
        Set<PlatformHolder> holders = this.children.get(platformClass);
        if (holders != null) {
            for (PlatformHolder holder : holders) {
                if (!holder.deprecated && !holder.platform.isOverloaded() && holder.platform.isAcceptTask()) {
                    holder.platform.offer(order);
                    return holder.platform;
                }
            }
        }

        Platform platform = create(platformClass);
        platform.offer(order);
        return platform;
    }

    public synchronized void destroy(long id) {
        this.children.values().forEach(set ->
                set.removeIf(holder -> {
                    if (holder.platform.getId() == id) {
                        holder.deprecated = true;
                        holder.platform.destroy();
                        return true;
                    }
                    return false;
                })
        );

        this.children.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public synchronized void destroy(Class<? extends Platform> platformClazz) {
        Set<PlatformHolder> removed = this.children.remove(platformClazz);
        if (removed != null) {
            removed.forEach(holder -> {
                holder.deprecated = true;
                holder.platform.destroy();
            });
        }
    }

    public long getPostalCode(Class<? extends Platform> platformClazz) {
        for (Set<PlatformHolder> platformHolders : this.children.values()) {
            for (PlatformHolder holder : platformHolders) {
                if (holder.platform.getClass().equals(platformClazz) && !holder.deprecated) {
                    return holder.platform.getId();
                }
            }
        }

        return -1;
    }

    /**
     * Definition of Platform components:
     * 1. OrderProcessor: Processor to consume received orders.
     */
    protected OrderProcessor defineProcessor() {
        return new OrderProcessor(this);
    }

    /**
     * 2. Lost Order Consumer: Define how to deal with unexcepted lost orders
     */
    protected Consumer<PlatformOrder<?, ?>> defineLostOrderConsumer() {
        return order -> log.warn("An order \"{}\" lost.", order.getClass().getSimpleName());
    }

    /**
     * 3. Platform Factory: To use on creating / scale out Platforms.
     *
     * @param postalCenter inject in constructor
     */
    protected PlatformFactory definePlatformFactory(PlatformPostalCenter postalCenter) {
        return new PlatformFactory(postalCenter);
    }

    private PostalParcel<PlatformOrder<?, ?>> getParcel(long postalCode, PlatformOrder<?, ?> order) {
        PostalParcel<PlatformOrder<?, ?>> parcel = postalCenter.getParcel(this.mailbox, postalCode, RecipientType.MAILBOX);
        parcel.put(order);
        return parcel;
    }

    private void addChild(Platform child) {
        if (!childrenClass.contains(child.getClass())) {
            throw GoatErrors.of("Platform \"" + child.getClass().getName() + "\" is not accepted.", PlatformException.class);
        }

        Set<PlatformHolder> set = this.children.computeIfAbsent(child.getClass(),
                k -> ConcurrentHashMap.newKeySet());
        set.add(new PlatformHolder(child));
    }

    private void waitUntilSubmit(PlatformOrder<?, ?> order) {
        try {
            this.submit(order);
        } catch (InterruptedException e) {
            this.lostOrderConsumer.accept(order);
        }
    }

    private void sendScaleOutOrder(TaskOrder<?> order) {
        PostalParcel<PlatformOrder<?, ?>> parcel = postalCenter.getParcel(this.mailbox, this.parentId, RecipientType.MAILBOX);

        parcel.put(Order.SCALE_OUT()
                .platformOf(this.getClass())
                .withTask(order));

        this.postalCenter.offer(parcel);
    }

    public static class PlatformHolder {

        public final long createTime;

        public final Platform platform;

        public volatile boolean deprecated = false;

        public PlatformHolder(Platform platform) {
            this.createTime = System.currentTimeMillis();
            this.platform = platform;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PlatformHolder) {
                return ((PlatformHolder) obj).platform.getId() == this.platform.getId();
            }
            return false;
        }

    }

}
