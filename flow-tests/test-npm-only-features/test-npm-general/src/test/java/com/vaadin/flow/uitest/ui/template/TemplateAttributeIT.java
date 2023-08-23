package com.vaadin.flow.uitest.ui.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class TemplateAttributeIT extends ChromeBrowserTest {

    @Test
    public void readTemplateAttribute() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        TestBenchElement info = template.$(TestBenchElement.class).id("info");
        Assertions.assertEquals("foo bar true", info.getText());

        TestBenchElement textInfo = template.$(TestBenchElement.class)
                .id("text-info");
        Assertions.assertEquals("foo |", textInfo.getText());
    }
}
