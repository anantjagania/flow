package com.vaadin.flow.uitest.ui.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class SetInitialTextIT extends ChromeBrowserTest {

    @Test
    public void setText_updateDomViaClientSide_updateElementViaServerSide_allElementsArePreserved() {
        open();

        TestBenchElement template = $(TestBenchElement.class)
                .id("set-initial-text");
        // add a child via client side
        template.$(TestBenchElement.class).id("addClientSideChild").click();

        // add a child via sever side
        template.$(TestBenchElement.class).id("add-child").click();

        // Both children should be now in DOM

        TestBenchElement child = template.$(TestBenchElement.class).id("child");

        Assertions.assertTrue(child.$(TestBenchElement.class)
                .attribute("id", "client-side").exists());
        Assertions.assertTrue(child.$(TestBenchElement.class)
                .attribute("id", "new-child").exists());
    }

}
