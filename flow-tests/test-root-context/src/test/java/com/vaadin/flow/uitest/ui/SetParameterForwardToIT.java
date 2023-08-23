package com.vaadin.flow.uitest.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class SetParameterForwardToIT extends ChromeBrowserTest {

    @Test
    public void testForwardingToViewInSetParameter() {
        final String baseLoc = "/view/com.vaadin.flow.uitest.ui.SetParameterForwardToView";
        getDriver().get(getRootURL() + baseLoc + "/one");
        waitForDevServer();

        waitForElementPresent(By.id(SetParameterForwardToView.LOCATION_ID));
        final String locationId = findElement(
                By.id(SetParameterForwardToView.LOCATION_ID)).getText();
        Assertions.assertTrue("should redirect to " + baseLoc + "/two",
                locationId.endsWith("/two"));
        Assertions.assertTrue("should update the URL",
                getDriver().getCurrentUrl().endsWith(baseLoc + "/two"));
    }
}
