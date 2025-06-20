package com.hiro.goat.core.utils;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class SnowflakeStepDefs {

    private final Set<Long> identities = ConcurrentHashMap.newKeySet();

    private SnowflakeGenerator generator;

    @Given("a snowflake generator")
    public void a_snow_flake_generator() {
        assertThrows(IllegalArgumentException.class, ()
                -> generator = new SnowflakeGenerator(1999));
        this.generator = new SnowflakeGenerator(31);
        assertNotNull(this.generator);
    }

    @When("I create {int} identities")
    public void i_create_identities(int num) {
        for (int i = 0; i < num; i++) {
            identities.add(generator.nextId());
        }
    }


    @Then("I have {int} identities in Set")
    public void i_have_identities_in_Set(int num) {
        assertEquals(num, identities.size());
    }
}
