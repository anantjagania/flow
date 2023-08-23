package com.vaadin.flow.component.html.testbench;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class SelectElementIT extends ChromeBrowserTest {

    private SelectElement input;
    private DivElement log;

    @BeforeEach
    public void open() {
        getDriver().get("http://localhost:8888/Select");
        input = $(SelectElement.class).id("input");
        log = $(DivElement.class).id("log");
    }

    @Test
    public void selectByText() {
        input.selectByText("Visible text 5");
        Assertions.assertEquals("value5", input.getValue());
        Assertions.assertEquals("Value is 'value5'", log.getText());
        input.selectByText("Visible text 1");
        Assertions.assertEquals("value1", input.getValue());
        Assertions.assertEquals("Value is 'value1'", log.getText());
    }

}
