package com.hiro.goat.platform.order.system;


import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.exception.PlatformException;
import com.hiro.goat.platform.order.task.TaskOrder;

public class Order {

    /**
     * Get a create-order builder
     */
    public static CreateOrderBuilder CREATE() {
        return new CreateOrderBuilder();
    }

    public static class CreateOrderBuilder {

        public CreateOrder platformOf(Class<? extends Platform> platformClass) {
            if (platformClass == null) {
                throw GoatErrors.of("Invalid platformClass", PlatformException.class);
            }

            CreateOrder createOrder = new CreateOrder();
            createOrder.initParams(platformClass);
            return createOrder;
        }

    }

    /**
     * Get a destroy-order builder
     */
    public static DestroyOrderBuilder DESTROY() {
        return new DestroyOrderBuilder();
    }

    public static class DestroyOrderBuilder {

        public DestroyOrder id(long id) {
            if (id <= 0) {
                throw GoatErrors.of("Invalid id", PlatformException.class);
            }

            DestroyOrder destroyOrder = new DestroyOrder();
            destroyOrder.initParams(id);
            return destroyOrder;
        }

    }

    /**
     * Get end-service-order builder
     */
    public static EndServiceOrderBuilder END_SERVICE() {
        return new EndServiceOrderBuilder();
    }

    public static class EndServiceOrderBuilder {

        public EndServiceOrder platformOf(Class<? extends Platform> platformClass) {
            if (platformClass == null) {
                throw GoatErrors.of("Invalid platformClass", PlatformException.class);
            }

            EndServiceOrder endServiceOrder = new EndServiceOrder();
            endServiceOrder.initParams(platformClass);
            return endServiceOrder;
        }

    }

    /**
     * Get a scale-out-order builder
     */
    public static ScaleOutOrderBuilder SCALE_OUT() {
        return new ScaleOutOrderBuilder();
    }

    public static class ScaleOutOrderBuilder {

        public <T extends Platform> ClassHolder<T> platformOf(Class<T> platformClass) {
            if (platformClass == null) {
                throw GoatErrors.of("Invalid platformClass", PlatformException.class);
            }
            return new ClassHolder<>(platformClass);
        }

        public static class ClassHolder<T extends Platform> {

            private final Class<T> platformClass;

            public ClassHolder(Class<T> platformClass) {
                this.platformClass = platformClass;
            }

            public ScaleOutOrder withTask(TaskOrder<?> taskOrder) {
                if (taskOrder == null) {
                    throw GoatErrors.of("Invalid taskOrder", PlatformException.class);
                }

                ScaleOutOrder scaleOutOrder = new ScaleOutOrder(this.platformClass);
                scaleOutOrder.initParams(taskOrder);
                return scaleOutOrder;
            }

        }

    }


}
