package com.hiro.goat.platform;

import com.hiro.goat.core.exception.GoatErrors;
import com.hiro.goat.core.task.AbstractProcessor;
import com.hiro.goat.platform.exception.PlatformException;
import com.hiro.goat.platform.graph.Graph;
import com.hiro.goat.platform.graph.GraphState;
import com.hiro.goat.platform.order.PlatformOrder;
import com.hiro.goat.platform.order.system.SystemOrder;
import com.hiro.goat.platform.order.task.TaskOrder;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OrderProcessor extends AbstractProcessor<PlatformOrder<?, ?>> {

    private final Platform platform;

    protected final Map<String, Graph> graphs = new ConcurrentHashMap<>();

    public OrderProcessor(Platform platform) {
        this.platform = platform;
    }

    @Override
    public void process(PlatformOrder<?, ?> order) {
        if (order instanceof SystemOrder) {
            SystemOrder<?, ?> systemOrder = (SystemOrder<?, ?>) order;
            executeSystemOrder(systemOrder);

            if (!order.isSuccess()) {
                rollbackSystemOrder(systemOrder);
            }
        } else if (order instanceof TaskOrder) {
            TaskOrder<?> taskOrder = (TaskOrder<?>) order;
            executeTaskOrder(taskOrder);

            if (!order.isSuccess()) {
                rollbackTaskOrder(taskOrder);
            }
        }
    }

    protected void executeTaskOrder(TaskOrder<?> order) {
        order.execute();
        if (order.getGraphClass() != null) {
            updateGraph(order, getGraph(order.getGraphId(), order.getGraphClass()));
        }
    }

    protected void rollbackTaskOrder(TaskOrder<?> order) {
        log.warn("Order {} rollback.", order.getTask().getClass().getSimpleName());
        order.rollback(true).execute();
    }

    private void executeSystemOrder(SystemOrder<?, ?> order) {
        order.inject(platform);
        order.execute();
    }

    private void rollbackSystemOrder(SystemOrder<?, ?> order) {
        log.warn("System Order {} rollback.", order.getClass().getSimpleName());
        order.rollback(true).execute();
    }

    private void updateGraph(TaskOrder<?> order, Graph graph) {
        graph.offer(order.getTask());

        GraphState state = graph.state();
        order.setGraphId(graph.getId()).setGraphState(state);

        if (!state.isCompleted()) {
            graphs.put(graph.getId(), graph);
        } else {
            graphs.remove(graph.getId());
        }
    }

    private Graph getGraph(String graphId, Class<? extends Graph> graphClass) {
        try {
            return StringUtils.isNoneBlank(graphId) ? graphs.get(graphId) :
                    graphClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw GoatErrors.of("Error create Graph: \"" + graphClass.getName() + "\".", PlatformException.class);
        }
    }


}
