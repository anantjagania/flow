package com.vaadin.flow.contexttest.ui;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

public class RoutedSubContextIT extends AbstractContextIT {

    @Override
    protected String getAppContext() {
        return "/routed/sub-context/";
    }

    @Override
    protected void verifyCorrectUI() {
        Assertions.assertNotNull(findElement(By.id("routed-sub")));
    }

}
