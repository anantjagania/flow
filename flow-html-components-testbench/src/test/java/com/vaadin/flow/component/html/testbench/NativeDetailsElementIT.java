package com.vaadin.flow.component.html.testbench;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class NativeDetailsElementIT extends ChromeBrowserTest {

    private NativeDetailsElement details;
    private DivElement log;
    private NativeButtonElement button;

    @Test
    public void openDetails() {
        prepareTest(false);

        details.setProperty("open", true);
        Assertions.assertEquals("Toggle event number '1' is 'true'", log.getText());
    }

    @Test
    public void openAndCloseDetails() {
        prepareTest(false);

        details.setProperty("open", true);
        Assertions.assertEquals("Toggle event number '1' is 'true'", log.getText());

        details.setProperty("open", false);
        Assertions.assertEquals("Toggle event number '2' is 'false'",
                log.getText());
    }

    @Test
    public void closingAlreadyClosedDetails() {
        prepareTest(false);

        details.setProperty("open", false);
        // Event should not be triggered, because details open property
        // defaults to false
        Assertions.assertEquals("", log.getText());
    }

    @Test
    public void openAndCloseDetailsFromOtherServerSideComponent() {
        prepareTest(false);

        button.click();
        Assertions.assertEquals("Toggle event number '1' is 'true'", log.getText());

        button.click();
        Assertions.assertEquals("Toggle event number '2' is 'false'",
                log.getText());
    }

    @Test
    public void toggleNativeDetailsWithTestBenchElement() {
        prepareTest(false);

        details.toggle();
        Assertions.assertEquals("Toggle event number '1' is 'true'", log.getText());

        details.toggle();
        Assertions.assertEquals("Toggle event number '2' is 'false'",
                log.getText());
    }

    @Test
    public void openDetailsFromServerSideOnInitialRendering() {
        prepareTest(true);

        // Event should be triggered once, because details was already opened on
        // server side
        // triggering the toggle event.
        Assertions.assertEquals("Toggle event number '1' is 'true'", log.getText());

        details.setProperty("open", true);
        // Event should not be triggered again, because details was already
        // opened.
        Assertions.assertEquals("Toggle event number '1' is 'true'", log.getText());
    }

    private void prepareTest(boolean detailsOpen) {
        getDriver().get(
                "http://localhost:8888/Details" + (detailsOpen ? "/open" : ""));
        details = $(NativeDetailsElement.class).id("details");
        log = $(DivElement.class).id("log");
        button = $(NativeButtonElement.class).id("btn");
    }
}
