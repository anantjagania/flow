/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.uitest.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.testutil.AbstractParallelTestBenchTest;

public class RouterParallelIT extends AbstractParallelTestBenchTest {

    @BeforeEach
    public void openAndFindElement() {
        getDriver().get(getRootURL() + "/new-router-session/ElementQueryView");
        waitForDevServer();
    }

    @Test
    public void a() {
        Assertions.assertTrue("No button was found on page",
                isElementPresent(By.tagName(Tag.BUTTON)));
    }

    @Test
    public void b() {
        Assertions.assertTrue("No button was found on page",
                isElementPresent(By.tagName(Tag.BUTTON)));
    }

    @Test
    public void c() {
        Assertions.assertTrue("No button was found on page",
                isElementPresent(By.tagName(Tag.BUTTON)));
    }

    @Test
    public void d() {
        Assertions.assertTrue("No button was found on page",
                isElementPresent(By.tagName(Tag.BUTTON)));
    }

    @Test
    public void e() {
        Assertions.assertTrue("No button was found on page",
                isElementPresent(By.tagName(Tag.BUTTON)));
    }

}
