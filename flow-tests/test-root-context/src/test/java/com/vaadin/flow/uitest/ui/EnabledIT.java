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
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;

public class EnabledIT extends ChromeBrowserTest {

    @Test
    public void verifyEnabledState() {
        open();

        // The element is initially disabled. It should be there.
        Assertions.assertTrue(isElementPresent(By.id("enabled")));
        Assertions.assertTrue(isElementPresent(By.id("nested-label")));

        // check that disabled update property button is present.
        Assertions.assertTrue(isElementPresent(By.id("updateProperty")));
        // Validate that button has the default disabled attribute
        Assertions.assertTrue(findElement(By.id("updateProperty")).getAttribute("disabled") != null);

        WebElement main = findElement(By.id("main"));
        WebElement div = main.findElement(By.tagName("div"));

        // try to change some properties for the element and it's child
        findElement(By.id("updateProperty")).click();

        WebElement label = findElement(By.id("nested-label"));

        // assert that no changes occurred due to disabled button.
        Assertions.assertEquals("", div.getAttribute("class"));
        Assertions.assertEquals("", label.getAttribute("class"));

        // make the button enabled
        WebElement enableButton = findElement(By.id("enableButton"));
        scrollIntoViewAndClick(enableButton);

        // change some properties for the element itself and it's child
        findElement(By.id("updateProperty")).click();

        // properties should have changed from the server
        Assertions.assertEquals("foo", div.getAttribute("class"));
        Assertions.assertEquals("bar", label.getAttribute("class"));
    }
}
