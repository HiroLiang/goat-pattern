package com.hiro.goat.platform.mechanism;

import com.hiro.goat.platform.Platform;
import com.hiro.goat.platform.model.platform.ChildPlatform;
import com.hiro.goat.platform.model.platform.RootPlatform;
import com.hiro.goat.platform.order.system.Order;
import com.hiro.goat.platform.postal.PlatformPostalCenter;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;

@Slf4j
public class ManageChildrenStepDefs {

    private final PlatformPostalCenter postalCenter = new PlatformPostalCenter(0, "sha256-secret");

    private Platform root;

    private long childId;

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
        root.orderPlatform(root.getId(), Order.CREATE().platformOf(ChildPlatform.class));
    }

    @Then("root should has same class child")
    public void root_should_has_same_class_child() {
        sleep(300);
        childId = root.getPostalCode(ChildPlatform.class);
        assertTrue(childId > 0);
        log.info("Child platform(ID:\"{}\") created.", childId);
    }

    @When("I destroy the child")
    public void i_destroy_the_child() {
        root.orderPlatform(root.getId(), Order.DESTROY().id(childId));
    }

    @Then("I can not find any child")
    public void iCanNotFindAnyChild() {
        sleep(100);
        childId = root.getPostalCode(ChildPlatform.class);
        assertFalse(childId > 0);
        log.info("Child platform(ID:\"{}\") destroyed.", childId);
    }

    @When("I have two children in root")
    public void i_have_two_children_in_root() {
        root.orderPlatform(root.getId(), Order.CREATE().platformOf(ChildPlatform.class));
        root.orderPlatform(root.getId(), Order.CREATE().platformOf(ChildPlatform.class));
        sleep(100);
        assertTrue(root.getPostalCode(ChildPlatform.class) > 0);
        log.info("Two child platforms created.");
    }

    @And("I ask to end child platform service")
    public void i_ask_to_end_child_platform_service() {
        root.orderPlatform(root.getId(), Order.END_SERVICE().platformOf(ChildPlatform.class));
    }

    @Then("it would destroy all children platform")
    public void it_would_destroy_all_children_platform() {
        sleep(200);
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

}
