package com.hiro.goat.core.worker;

import com.hiro.goat.api.worker.DispatchWorker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
            sleep(100L);
        } catch (InterruptedException e) {
            fail("Test was interrupted: fail to submit a string: " + str);
        }
    }

    @When("I submit dispatcher the following strings:")
    public void i_submit_dispatcher_the_following_strings(List<String> strings) {
        try {
            dispatcher.submit(strings);
            sleep(300L);
        } catch (InterruptedException e) {
            fail("Test was interrupted: fail to submit strings: " + strings);
        }
    }

    @When("I offer dispatcher a str {string}")
    public void i_offer_dispatcher_a_str(String str) {
        boolean success = dispatcher.offer(str);
        assertTrue(success);
        sleep(100L);
    }


    @When("I offer dispatcher a str {string} after {int} millisecond")
    public void i_offer_dispatcher_a_str_after_seconds(String str, int seconds) {
        new Thread(() -> {
            try {
                boolean success = dispatcher.offer(str, seconds, TimeUnit.MILLISECONDS);
                assertTrue(success);
            } catch (InterruptedException e) {
                fail("Test was interrupted: fail to offer a string: " + str);
            }
        }).start();
        assertFalse(words.contains(str));
    }

    @When("I offer dispatcher the following strings:")
    public void i_offer_dispatcher_the_following_strings(List<String> strings) {
        int index = dispatcher.offer(strings);
        assertEquals(strings.size(), index);
        sleep(300L);
    }

    @Then("words should contain {string}")
    public void wordsShouldContain(String str) {
        assertTrue(words.contains(str));
        words.clear();
    }

    @Then("words should contain the following strings:")
    public void words_should_contain_the_following_strings(List<String> strings) {
        for (String str : strings) {
            assertTrue(words.contains(str));
        }
        words.clear();
    }

    @Then("words should contain {string} after {int} millisecond")
    public void words_should_contain_after_seconds(String str, int millisecond) {
        sleep(millisecond);
        assertTrue(words.contains(str));
        words.clear();
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
