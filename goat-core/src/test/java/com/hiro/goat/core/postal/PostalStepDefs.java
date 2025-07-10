package com.hiro.goat.core.postal;

import com.hiro.goat.core.exception.IllegalModifyException;
import com.hiro.goat.core.exception.PostalException;
import com.hiro.goat.core.postal.model.TestMailbox;
import com.hiro.goat.core.postal.model.TestPostalCenter;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class PostalStepDefs {

    private PostalCenter<String> postalCenter;

    private Mailbox<String> mailbox1;

    private TestMailbox mailbox2;

    @Given("a postal center")
    public void a_postal_center() {
        this.postalCenter = new TestPostalCenter("dev-secret-123456");
        this.postalCenter.start();
    }

    @When("I register two mailbox")
    public void i_register_two_mailbox() {
        this.mailbox1 = this.postalCenter.register();
        this.mailbox2 = (TestMailbox) this.postalCenter.register();

        assertTrue(postalCenter.isRegistered(mailbox1));
        assertTrue(postalCenter.isRegistered(mailbox2));
    }

    @And("deliver a test parcel with word {string}")
    public void deliver_a_test_parcel_with_word(String word) {
        PostalParcel<String> parcel = postalCenter.getParcel(mailbox1, mailbox2.getPostalCode(), RecipientType.MAILBOX);
        parcel.put(word);
        assertTrue(postalCenter.offer(parcel));
    }

    @Then("the other mailbox should get a parcel contains word {string}")
    public void the_other_mailbox_should_get_a_parcel_contains_word(String word) {
        sleep(210L);
        assertEquals(word, mailbox2.getWord());
        mailbox2.setWord("");
    }

    @When("I register a group {string}")
    public void i_register_a_group(String group) {
        assertThrows(IllegalModifyException.class, () -> postalCenter.registerGroup(mailbox2, null));
        postalCenter.registerGroup(mailbox2, group);
    }

    @And("deliver a test parcel with word {string} with group {string}")
    public void deliver_a_test_parcel_with_word_with_group(String word, String group) {
        PostalParcel<String> parcel = postalCenter.getParcel(mailbox1, group);
        parcel.put(word);
        assertTrue(postalCenter.offer(parcel));
    }

    @When("I unregister group {string}")
    public void i_unregister_group(String group) {
        assertThrows(IllegalModifyException.class, () -> postalCenter.unregisterGroup(mailbox2, null));
        postalCenter.unregisterGroup(mailbox2, group);
    }

    @Then("the other mailbox can't get parcel with word {string}")
    public void the_other_mailbox_cant_get_parcel_with_word(String word) {
        sleep(110L);
        assertNotEquals(word, mailbox2.getWord());
        mailbox2.setWord("");
    }

    @When("I unregister mailbox")
    public void i_unregister_mailbox() {
        this.postalCenter.unregister(mailbox1);
        this.postalCenter.unregister(mailbox2);

        assertFalse(postalCenter.isRegistered(mailbox1));
        assertFalse(postalCenter.isRegistered(mailbox2));
    }

    @Then("I can not deliver parcel")
    public void i_can_not_deliver_parcel() {
        assertThrows(PostalException.class, () ->
                postalCenter.getParcel(mailbox1, mailbox2.getPostalCode(), RecipientType.MAILBOX));

        assertThrows(PostalException.class, () ->
                postalCenter.getParcel(mailbox1, "test-group"));
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
