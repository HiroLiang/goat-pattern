package com.hiro.goat.core.worker;

import com.hiro.goat.core.worker.model.TestWorker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class WorkerStepDefs {

    private TestWorker worker;

    @Given("a Test Work")
    public void a_Test_Work() {
        this.worker = new TestWorker();
        assertNotNull("Worker should not be null", worker);
    }

    @When("I start work")
    public void i_start_work() {
        worker.start();
        assertTrue(worker.isRunning());
    }

    @Then("work should renew current every seconds")
    public void work_should_renew_current_every_seconds() {
        long before = worker.getCurren();

        try {
            Thread.sleep(310L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }

        long after = worker.getCurren();

        assertTrue(
                "Expected 'curren' to be updated. First: " + before + ", Second: " + after,
                after > before
        );
    }

    @When("I stop work")
    public void i_stop_work() {
        worker.stop();
        assertFalse(worker.isRunning());
    }

    @Then("work should stop renewing current every seconds")
    public void work_should_stop_renewing_current_every_seconds() {
        long before = worker.getCurren();

        try {
            Thread.sleep(310L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }

        long after = worker.getCurren();

        assertEquals(
                "Expected 'curren' to stop updating after stop(), but it changed.",
                before,
                after
        );
    }
}
