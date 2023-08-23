package com.vaadin.flow.uitest.ui.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

public class PolymerTemplateWithoutShadowRootIT extends ChromeBrowserTest {

    @Test
    public void componentMappedCorrectly() {
        open();
        DivElement content = $(DivElement.class).attribute("real", "deal")
                .first();
        Assertions.assertEquals("Hello", content.getText());
        DivElement special = $(DivElement.class).id("special!#id");
        Assertions.assertEquals("Special", special.getText());
        DivElement map = $(DivElement.class).id("map");
        Assertions.assertEquals("Map", map.getText());
        content.click();
        Assertions.assertEquals("Goodbye", content.getText());
    }
}
