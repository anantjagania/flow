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
package com.vaadin.flow.uitest.ui.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class InjectScriptTagIT extends ChromeBrowserTest {

    @Test
    public void openPage_scriptIsEscaped() {
        open();

        TestBenchElement parent = $("inject-script-tag-template").first();

        TestBenchElement div = parent.$(TestBenchElement.class).id("value-div");
        Assertions.assertEquals("<!-- <script>", div.getText());

        WebElement slot = findElement(By.id("slot-1"));
        Assertions.assertEquals("<!-- <script> --><!-- <script></script>",
                slot.getText());

        TestBenchElement button = parent.$(TestBenchElement.class)
                .id("change-value");
        button.click();

        Assertions.assertEquals("<!-- <SCRIPT>", div.getText());
        slot = findElement(By.id("slot-2"));
        Assertions.assertEquals("<!-- <SCRIPT> --><!-- <SCRIPT></SCRIPT>",
                slot.getText());
    }

}
