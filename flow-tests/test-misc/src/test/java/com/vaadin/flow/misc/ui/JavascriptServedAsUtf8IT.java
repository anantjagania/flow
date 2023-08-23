package com.vaadin.flow.misc.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class JavascriptServedAsUtf8IT extends ChromeBrowserTest {

    @Test
    public void loadJavascriptWithUtf8() {
        getDriver().get(getRootURL() + "/test-files/js/unicode.js");
        String source = getDriver().getPageSource();
        Assertions.assertTrue(
                "Page should have contained umlaut characters but contained: "
                        + source,
                source.contains("åäöü°"));
    }
}
