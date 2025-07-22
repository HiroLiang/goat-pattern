package com.hiro.goat.platform.mechanism;

import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.model.platform.ChildPlatform;
import com.hiro.goat.platform.model.platform.RootPlatform;
import com.hiro.goat.platform.model.task.TestTask;
import com.hiro.goat.platform.order.system.Orders;
import com.hiro.goat.platform.postal.PlatformPostalCenter;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

@Slf4j
public class ManageChildrenStepDefs {

    private final PlatformPostalCenter postalCenter = new PlatformPostalCenter(0, "sha256-secret");

    private Platform root;

    private TestTask task;

    private long childId;

    private long triggerTime = 0L;

    @Given("a root platform")
    public void a_root_platform() {
        postalCenter.start();
        root = new RootPlatform(postalCenter, -1L);
        assertTrue(root.getId() > 0);
        root.start();
        log.info("Root platform(ID:\"{}\") started.", root.getId());
    }

    @When("I create a child platform")
    public void i_create_a_child_platform() {
        root.orderPlatform(root.getId(), Orders.CREATE().platformOf(ChildPlatform.class));
    }

    @Then("root should has same class child")
    public void root_should_has_same_class_child() {
        sleep(100);
        childId = root.getPostalCode(ChildPlatform.class);
        assertTrue(childId > 0);
        log.info("Child platform(ID:\"{}\") created.", childId);
    }

    @When("I destroy the child")
    public void i_destroy_the_child() {
        root.orderPlatform(root.getId(), Orders.DESTROY().id(childId));
    }

    @Then("I can not find any child")
    public void iCanNotFindAnyChild() {
        sleep(50);
        childId = root.getPostalCode(ChildPlatform.class);
        assertFalse(childId > 0);
        log.info("Child platform(ID:\"{}\") destroyed.", childId);
    }

    @When("I have two children in root")
    public void i_have_two_children_in_root() {
        root.orderPlatform(root.getId(), Orders.CREATE().platformOf(ChildPlatform.class));
        root.orderPlatform(root.getId(), Orders.CREATE().platformOf(ChildPlatform.class));
        sleep(50);
        assertTrue(root.getPostalCode(ChildPlatform.class) > 0);
        log.info("Two child platforms created.");
    }

    @And("I ask to end child platform service")
    public void i_ask_to_end_child_platform_service() {
        root.orderPlatform(root.getId(), Orders.END_SERVICE().platformOf(ChildPlatform.class));
    }

    @Then("it would destroy all children platform")
    public void it_would_destroy_all_children_platform() {
        sleep(100);
        assertFalse(root.getPostalCode(ChildPlatform.class) > 0);
        log.info("All child platforms destroyed.");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    @When("I deliver a test task")
    public void i_deliver_a_test_task() {
        task = new TestTask(null);
        root.orderPlatform(root.getId(), Orders.CREATE().platformOf(ChildPlatform.class));
        sleep(50);
        triggerTime = System.currentTimeMillis();
        root.deliverTask(ChildPlatform.class, task);
    }

    @Then("task result should be true")
    public void task_result_should_be_true() {
        assertTrue(task.takeResult());
        log.info("Task result: {}", task.getResult());
        log.info("Task execution time: {}ms", System.currentTimeMillis() - triggerTime);
    }

    @When("do pressure test {int} threads {int} tasks")
    public void do_pressure_test_threads_tasks(int thread, int task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(thread);
        CountDownLatch latch = new CountDownLatch(thread * task);

        triggerTime = System.currentTimeMillis();
        AtomicLong count = new AtomicLong();

        for (int i = 0; i < thread; i++) {
            executor.submit(() -> {
                for (int j = 0; j < task; j++) {
                    TestTask testTask = new TestTask(null);
                    root.deliverTask(ChildPlatform.class, testTask);
                    assertTrue(testTask.takeResult());
                    latch.countDown();
                    count.getAndIncrement();
                }
            });
        }

        latch.await();
        long costTime = System.currentTimeMillis() - triggerTime;
        log.info("Pressure test {} tasks, execution time: {}ms ({} tasks /s)", count.get(), costTime, count.get() * 1000 / costTime);

        executor.shutdown();
    }

    @Then("get pressure test result")
    public void getPressureTestResult() {
        root.destroy();
        postalCenter.destroy();
    }

}
