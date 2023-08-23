package com.vaadin.flow.uitest.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class ErrorPageIT extends ChromeBrowserTest {

    @Override
    protected String getTestPath() {
        return "/view/abcd";
    };

    @Test
    public void testErrorViewOpened() {
        open();

        Assertions.assertTrue(getDriver().getPageSource()
                .contains("Could not navigate to 'abcd'"));

        getDriver().get(getTestURL() + "/foobar");

        Assertions.assertTrue(getDriver().getPageSource()
                .contains("Could not navigate to 'abcd/foobar'"));
    }
}
