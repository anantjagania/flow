package com.vaadin.flow.contexttest.ui;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

public class RootContextIT extends AbstractContextIT {

    @Override
    protected String getAppContext() {
        return "";
    }

    @Override
    protected void verifyCorrectUI() {
        Assertions.assertNotNull(findElement(By.id("root")));
    }

}
