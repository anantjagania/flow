package com.vaadin.flow.uitest.ui.push;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.experimental.categories.Category;

import com.vaadin.flow.testcategory.PushTests;
import com.vaadin.flow.testutil.ChromeBrowserTest;

@Category(PushTests.class)
public abstract class IdlePushChannelIT extends ChromeBrowserTest {

    private static final int SEVEN_MINUTES_IN_MS = 7 * 60 * 1000;

    @Test
    public void longWaitBetweenActions() throws Exception {
        open();
        BasicPushIT.getIncrementButton(this).click();
        Assertions.assertEquals(1, BasicPushIT.getClientCounter(this));
        Thread.sleep(SEVEN_MINUTES_IN_MS);
        BasicPushIT.getIncrementButton(this).click();
        Assertions.assertEquals(2, BasicPushIT.getClientCounter(this));
    }

}
