package com.hiro.goat.core.worker;

import com.hiro.goat.api.worker.DispatchWorker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class DispatcherStepDefs {

    private DispatchWorker<String> dispatcher;

    private final Set<String> words = ConcurrentHashMap.newKeySet();

    @Given("a Dispatch worker")
    public void a_dispatch_worker() {
        this.dispatcher = new QueueDispatchWorker<>(words::add);
        assertNotNull(this.dispatcher);
        this.dispatcher.start();
    }

    @When("I submit dispatcher a str {string}")
    public void iSubmitAStr(String str) {
        try {
            dispatcher.submit(str);
            sleep(200L);
        } catch (InterruptedException e) {
            fail("Test was interrupted: fail to submit a string: " + str);
        }
    }

    @Then("words should contain {string}")
    public void wordsShouldContain(String str) {
        assertTrue(words.contains(str));
        words.clear();
    }

    @When("I submit dispatcher the following strings:")
    public void i_submit_dispatcher_the_following_strings(List<String> strings) {
        try {
            dispatcher.submit(strings);
            sleep(500L);
        } catch (InterruptedException e) {
            fail("Test was interrupted: fail to submit strings: " + strings);
        }
    }

    @Then("words should contain the following strings:")
    public void words_should_contain_the_following_strings(List<String> strings) {
        for (String str : strings) {
            assertTrue(words.contains(str));
        }
        words.clear();
    }

    @When("I offer dispatcher a str {string}")
    public void i_offer_dispatcher_a_str(String str) {
        boolean success = dispatcher.offer(str);
        assertTrue(success);
        sleep(200L);
    }

    @When("I offer dispatcher the following strings:")
    public void i_offer_dispatcher_the_following_strings(List<String> strings) {
        int index = dispatcher.offer(strings);
        assertEquals(strings.size(), index);
        sleep(500L);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }
}
