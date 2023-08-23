package com.vaadin.flow.component.html.testbench;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class InputTextElementIT extends ChromeBrowserTest {

    private InputTextElement input;
    private DivElement log;

    @BeforeEach
    public void open() {
        getDriver().get("http://localhost:8888/InputText");
        input = $(InputTextElement.class).id("input");
        log = $(DivElement.class).id("log");
    }

    @Test
    public void getSetValue() {
        Assertions.assertEquals("", input.getValue());
        input.setValue("foo");
        Assertions.assertEquals("foo", input.getValue());
        Assertions.assertEquals("Value is 'foo'", log.getText());
    }

    @Test
    public void setValueEmpty() {
        input.setValue("foo");
        input.setValue("");
        Assertions.assertEquals("", input.getValue());
        Assertions.assertEquals("Value is ''", log.getText());
    }

    @Test
    public void clearEmpty() {
        input.clear();
        Assertions.assertEquals("", input.getValue());
        Assertions.assertEquals("", log.getText());
    }

    @Test
    public void clearWithValue() {
        input.setValue("foobar");
        input.clear();
        Assertions.assertEquals("", input.getValue());
        Assertions.assertEquals("Value is ''", log.getText());
    }
}
