package com.hiro.goat.core.worker;

import com.hiro.goat.api.worker.Lifecycle;
import com.hiro.goat.core.worker.model.TestLifecycle;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class LifecycleStepDefs {

    private Lifecycle lifecycle;

    @Given("a Test Lifecycle")
    public void a_test_lifecycle() {
        this.lifecycle = new TestLifecycle();
        assertNotNull(this.lifecycle);
    }

    @When("I start lifecycle")
    public void i_start_lifecycle() {
        this.lifecycle.start();
    }

    @When("I stop lifecycle")
    public void i_stop_lifecycle() {
        this.lifecycle.stop();
    }

    @When("I pause lifecycle")
    public void i_pause_lifecycle() {
        this.lifecycle.pause();
    }

    @When("I resume lifecycle")
    public void i_resume_lifecycle() {
        this.lifecycle.resume();
    }

    @When("I destroy lifecycle")
    public void i_destroy_lifecycle() {
        this.lifecycle.destroy();
    }

    @Then("is running should be {string}, is pause should bo {string}")
    public void check_is_running_and_is_pause(String isRunning, String isPause) {
        assertRunning(isRunning);
        assertPause(isPause);
    }

    private void assertRunning(String isRunning) {
        switch ( isRunning ) {
            case "true":
                assertTrue(this.lifecycle.isRunning());
                break;
            case "false":
                assertFalse(this.lifecycle.isRunning());
                break;
            default:
                throw new IllegalArgumentException("Invalid isRunning value: " + isRunning);
        }
    }

    private void assertPause(String isPause) {
        switch ( isPause ) {
            case "true":
                assertTrue(this.lifecycle.isPaused());
                break;
            case "false":
                assertFalse(this.lifecycle.isPaused());
                break;
            default:
                throw new IllegalArgumentException("Invalid isPause value: " + isPause);
        }
    }
}
