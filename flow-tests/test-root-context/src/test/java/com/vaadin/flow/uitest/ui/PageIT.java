package com.vaadin.flow.uitest.ui;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

public class PageIT extends ChromeBrowserTest {

    @Test
    public void testPageTitleUpdates() {
        open();

        updateTitle("Page title 1");
        verifyTitle("Page title 1");

        updateTitle("FOObar");
        verifyTitle("FOObar");
    }

    @Test
    public void testOnlyMostRecentPageUpdate() {
        open();

        updateTitle("Page title 1");
        verifyTitle("Page title 1");

        findElement(By.id("input")).sendKeys("title 2" + Keys.TAB);
        findElement(By.id("override")).click();

        verifyTitle("OVERRIDDEN");
    }

    @Test
    public void testPageTitleClears() {
        open();

        findElement(By.id("override")).click();
        verifyTitle("OVERRIDDEN");

        updateTitle("");
        verifyTitle("");
    }

    private void verifyTitle(String title) {
        Assertions.assertEquals("Page title does not match", title,
                getDriver().getTitle());
    }

    private void updateTitle(String title) {
        $(InputTextElement.class).id("input").setValue(title);
        findElement(By.id("button")).click();
    }

    @Test
    public void testReload() {
        open();

        InputTextElement input = $(InputTextElement.class).id("input");
        input.setValue("foo");
        Assertions.assertEquals("foo", input.getPropertyString("value"));
        findElement(By.id("reload")).click();
        input = $(InputTextElement.class).id("input");
        Assertions.assertEquals("", input.getValue());
    }

    @Test
    public void testSetLocation() {
        open();

        findElement(By.id("setLocation")).click();
        MatcherAssert.assertThat(getDriver().getCurrentUrl(),
                Matchers.endsWith(BaseHrefView.class.getName()));
    }

    @Test
    public void testOpenUrlInNewTab() {
        open();

        findElement(By.id("open")).click();
        ArrayList<String> tabs = new ArrayList<>(
                getDriver().getWindowHandles());
        MatcherAssert.assertThat(
                getDriver().switchTo().window(tabs.get(1)).getCurrentUrl(),
                Matchers.endsWith(BaseHrefView.class.getName()));
    }

    @Test
    public void testOpenUrlInIFrame() throws InterruptedException {
        open();

        findElement(By.id("openInIFrame")).click();

        waitUntil(driver -> !getIframeUrl().equals("about:blank"));

        MatcherAssert.assertThat(getIframeUrl(),
                Matchers.endsWith(BaseHrefView.class.getName()));
    }

    private String getIframeUrl() {
        return (String) ((JavascriptExecutor) driver).executeScript(
                "return document.getElementById('newWindow').contentWindow.location.href;");
    }
}
