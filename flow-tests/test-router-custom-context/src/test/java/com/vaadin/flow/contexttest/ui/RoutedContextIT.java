package com.vaadin.flow.contexttest.ui;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

public class RoutedContextIT extends AbstractContextIT {

    @Override
    protected String getAppContext() {
        return "/routed/";
    }

    @Override
    protected void verifyCorrectUI() {
        Assertions.assertNotNull(findElement(By.id("routed")));
    }

}
