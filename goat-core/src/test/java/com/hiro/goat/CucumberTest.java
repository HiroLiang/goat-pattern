package com.hiro.goat;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.hiro.goat",
        plugin = {"pretty"}
)
public class CucumberTest {}
