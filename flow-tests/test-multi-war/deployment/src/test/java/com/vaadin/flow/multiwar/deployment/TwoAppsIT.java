package com.vaadin.flow.multiwar.deployment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class TwoAppsIT extends ChromeBrowserTest {
    @Override
    protected String getTestPath() {
        return "/both.html";
    }

    @Test
    public void testWar1Works() {
        testWarWorks("war1");
    }

    @Test
    public void testWar2Works() {
        testWarWorks("war2");
    }

    private void testWarWorks(String warId) {
        getDriver().get(
                getTestURL(getRootURL(), "/test-" + warId, new String[] {}));
        waitForDevServer();
        WebElement helloText = findElement(By.id("hello"));
        Assertions.assertEquals(
                "Hello from com.vaadin.flow.multiwar." + warId + ".MainView",
                helloText.getText());
        helloText.click();
        Assertions.assertEquals("Hello Hello from com.vaadin.flow.multiwar." + warId
                + ".MainView", helloText.getText());

    }

    @Test
    public void bothWebComponentsEmbedded() {
        open();
        WebElement hello1 = findElement(By.id("hello1"));
        WebElement hello2 = findElement(By.id("hello2"));

        Assertions.assertEquals("Hello from com.vaadin.flow.multiwar.war1.HelloComponent", hello1.getText());
        Assertions.assertEquals("Hello from com.vaadin.flow.multiwar.war2.HelloComponent", hello2.getText());
    }

}
