package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

public class ForwardToIT extends ChromeBrowserTest {

    @Test
    public void testForwardingToView() {
        String initUrl = getDriver().getCurrentUrl();
        open();

        Assertions.assertTrue("should forward to specified view",
                findElement(By.id("root")).isDisplayed());
        Assertions.assertTrue("should update update the URL",
                getDriver().getCurrentUrl()
                        .endsWith("com.vaadin.flow.uitest.ui.BasicComponentView"));

        getDriver().navigate().back();
        Assertions.assertEquals("should replace history state",
                getDriver().getCurrentUrl(), initUrl);
    }
}
