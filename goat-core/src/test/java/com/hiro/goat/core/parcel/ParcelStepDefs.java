package com.hiro.goat.core.parcel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiro.goat.api.parcel.Parcel;
import com.hiro.goat.core.parcel.model.TestValue;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;

@Slf4j
public class ParcelStepDefs {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Parcel<TestValue> parcel;

    private TestValue revealed;

    @Given("a new {word}")
    public void a_new_parcel(String parcelName) throws Exception {

        String fullName = parcelName.contains(".") ? parcelName : "com.hiro.goat.core.parcel." + parcelName;
        this.parcel = getParcel(fullName);

        assertTrue(this.parcel.isEmpty());
    }

    @When("I put in a value:")
    public void i_put_in_a_value(String doString) throws JsonProcessingException {

        TestValue value = objectMapper.readValue(doString, TestValue.class);
        this.parcel.put(value);
    }

    @Then("I can put other value:")
    public void i_can_put_other_value(String doString) throws JsonProcessingException {

        TestValue value = objectMapper.readValue(doString, TestValue.class);
        this.parcel.put(value);
    }

    @When("I seal the parcel")
    public void i_seal_the_parcel() {

        this.parcel.seal();
    }

    @Then("I can't put value in it")
    public void i_cant_put_value_in_it() {

        assertTrue(this.parcel.isSealed());

        TestValue value = new TestValue();
        Exception e = assertThrows(IllegalStateException.class, () -> this.parcel.put(value));
        assertEquals("the parcel is sealed", e.getMessage());
    }

    @When("I reveal the parcel")
    public void i_reveal_the_parcel() {

        this.revealed = this.parcel.reveal();
    }

    @Then("the name should be {string}")
    public void assert_name(String name) {

        assertEquals(revealed.getName(), name);
    }

    @And("the value should be {int}")
    public void assert_value(int value) {

        assertEquals(revealed.getValue(), value);
    }

    @SuppressWarnings("unchecked")
    private Parcel<TestValue> getParcel(String fullName) throws Exception {

        Class<?> clazz = Class.forName(fullName);
        return (Parcel<TestValue>) clazz.getConstructor().newInstance();
    }

}
