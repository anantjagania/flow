package com.vaadin.flow.uitest.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

// IT for https://github.com/vaadin/flow/issues/12356
public class PreserveOnRefreshNestedBeforeEnterIT extends ChromeBrowserTest {

    @Test
    public void refreshViewWithNestedLayouts_eachBeforeEnterIsCalledOnlyOnce() {
        open();

        Assertions.assertEquals("1", $(SpanElement.class)
                .id("RootLayout-before-enter-count").getText());
        Assertions.assertEquals("1", $(SpanElement.class)
                .id("NestedLayout-before-enter-count").getText());
        Assertions.assertEquals("1", $(SpanElement.class)
                .id("PreserveOnRefreshNestedBeforeEnterView-before-enter-count")
                .getText());

        open();

        Assertions.assertEquals("2", $(SpanElement.class)
                .id("RootLayout-before-enter-count").getText());
        Assertions.assertEquals("2", $(SpanElement.class)
                .id("NestedLayout-before-enter-count").getText());
        Assertions.assertEquals("2", $(SpanElement.class)
                .id("PreserveOnRefreshNestedBeforeEnterView-before-enter-count")
                .getText());
    }
}
