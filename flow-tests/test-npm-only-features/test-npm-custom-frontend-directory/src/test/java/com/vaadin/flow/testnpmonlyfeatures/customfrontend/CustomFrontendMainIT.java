package com.vaadin.flow.testnpmonlyfeatures.customfrontend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class CustomFrontendMainIT extends ChromeBrowserTest {
    @Test
    public void javascriptShouldHaveBeenExecuted() {
        open();
        Assertions.assertNotNull($("div").id("executed"));
    }
}
