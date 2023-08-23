package com.vaadin.flow.testnpmonlyfeatures.nobuildmojo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class MultipleNpmPackageAnnotationsIT extends ChromeBrowserTest {
    @BeforeEach
    public void init() {
        open();
    }

    @Test
    public void pageShouldContainTwoPaperComponents() {
        TestBenchElement paperInput = $("paper-input").first();
        TestBenchElement paperCheckbox = $("paper-checkbox").first();

        // check that elements are on the page
        Assertions.assertNotNull(paperInput);
        Assertions.assertNotNull(paperCheckbox);

        // verify that the paper components are upgraded
        Assertions.assertNotNull(paperInput.$("paper-input-container"));
        Assertions.assertNotNull(paperCheckbox.$("checkboxContainer"));
    }

    // Tests funtionaity of TaskCopyLocalFrontendFiles
    @Test
    public void lazyComponentShouldExistInBody() {
        waitForElementPresent(By.id("lazy-element"));
        WebElement element = findElement(By.id("lazy-element"));

        Assertions.assertTrue("Lazy created element should be displayed", element.isDisplayed());
    }
}
