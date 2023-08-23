package com.vaadin.flow.contexttest.ui;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

public class SubContextNoEndingSlashIT extends AbstractContextIT {

    @Override
    protected String getAppContext() {
        return "/SubContext";
    }

    @Override
    protected void verifyCorrectUI() {
        Assertions.assertNotNull(findElement(By.id("sub")));
    }

}
