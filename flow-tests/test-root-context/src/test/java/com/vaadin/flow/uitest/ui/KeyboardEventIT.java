package com.vaadin.flow.uitest.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class KeyboardEventIT extends ChromeBrowserTest {
    @Test
    public void verify_that_keys_are_received_correctly() {
        open();

        // make sure both elements are present
        Assertions.assertTrue(isElementPresent(By.id("input")));
        Assertions.assertTrue(isElementPresent(By.id("paragraph")));

        WebElement input = findElement(By.id("input"));
        WebElement paragraph = findElement(By.id("paragraph"));

        input.sendKeys("q");

        Assertions.assertEquals(
                "q:KeyQ",
                paragraph.getText()
        );

        input.sendKeys("%");

        Assertions.assertEquals(
                "%:Digit5",
                paragraph.getText()
        );
        // next tests rely on
        // https://github.com/SeleniumHQ/selenium/blob/master/javascript/node/selenium-webdriver/lib/input.js#L52

        // arrow right
        input.sendKeys("\uE014");

        Assertions.assertEquals(
                "ArrowRight:ArrowRight",
                paragraph.getText()
        );

        // physical * key
        input.sendKeys("\uE024");

        Assertions.assertEquals(
                "*:NumpadMultiply",
                paragraph.getText()
        );
    }

    @Test // #5989
    public void verify_that_invalid_keyup_event_is_ignored() {
        open();

        WebElement input = findElement(By.id("input"));
        WebElement sendInvalidKeyUp = findElement(By.id("sendInvalidKeyUp"));
        WebElement paragraph = findElement(By.id("keyUpParagraph"));

        input.sendKeys("q");
        Assertions.assertEquals("q", paragraph.getText());

        sendInvalidKeyUp.click();

        Assertions.assertEquals("q", paragraph.getText());
    }
}
