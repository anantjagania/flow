/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.uitest.ui.littemplate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class InjectingTemplateIT extends ChromeBrowserTest {

    @Test
    public void mapTemplateViaIdWithNumberProperty_propertyTypeIsNotChangedAfterBidning() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("injecting");
        template.$(TestBenchElement.class).id("show-type").click();

        TestBenchElement container = template.$(TestBenchElement.class)
                .id("container");
        Assertions.assertEquals("number", container.getText());
    }
}
