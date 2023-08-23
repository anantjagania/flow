package com.vaadin.flow.contexttest.ui;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

public class SubContextSubPathIT extends AbstractContextIT {

    @Override
    protected String getAppContext() {
        return "/SubContext/foo/bar";
    }

    @Override
    protected void verifyCorrectUI() {
        Assertions.assertNotNull(findElement(By.id("sub")));
    }

}
