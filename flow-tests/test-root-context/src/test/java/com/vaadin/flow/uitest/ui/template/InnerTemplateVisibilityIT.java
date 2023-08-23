package com.vaadin.flow.uitest.ui.template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;

public class InnerTemplateVisibilityIT extends ChromeBrowserTest {

    @Test
    public void innerTemplateIsHiddenWithDisplayNone() {
        open();

        // when inner is hidden
        NativeButtonElement toggleButton = $(NativeButtonElement.class)
                .id(InnerTemplateVisibilityView.TOGGLE_INNER_VISIBILITY_BUTTON_ID);
        toggleButton.click();

        // then: element is not visible, attribute 'hidden' and 'display: none'
        // set
        TestBenchElement outer = $("*")
                .id(InnerTemplateVisibilityView.OUTER_ID);
        TestBenchElement inner = outer.$("*")
                .id(InnerTemplateVisibilityView.INNER_ID);
        Assertions.assertFalse("expected inner to be hidden", inner.isDisplayed());
        Assertions.assertNotNull("expected attribute hidden on inner",
                inner.getAttribute("hidden"));
        Assertions.assertEquals("expected 'display: none' on inner", "none",
                inner.getCssValue("display"));
    }

    @Test
    public void innerTemplateDisplayStyleRestored() {
        open();

        // when inner is hidden and unhidden
        NativeButtonElement toggleButton = $(NativeButtonElement.class)
                .id(InnerTemplateVisibilityView.TOGGLE_INNER_VISIBILITY_BUTTON_ID);
        toggleButton.click();
        toggleButton.click();

        // then: element is visible, attribute and 'display: none' are no longer
        // present
        TestBenchElement outer = $("*")
                .id(InnerTemplateVisibilityView.OUTER_ID);
        TestBenchElement inner = outer.$("*")
                .id(InnerTemplateVisibilityView.INNER_ID);
        Assertions.assertTrue("expected inner to be visible", inner.isDisplayed());
        Assertions.assertNull("inner should not have attribute hidden",
                inner.getAttribute("hidden"));
        Assertions.assertEquals("expected 'display: block' on inner", "block",
                inner.getCssValue("display"));
    }

    @Test
    public void outerTemplateIsHiddenWithAttributeOnly() {
        open();

        // when hidden
        NativeButtonElement toggleButton = $(NativeButtonElement.class)
                .id(InnerTemplateVisibilityView.TOGGLE_OUTER_VISIBILITY_BUTTON_ID);
        toggleButton.click();

        // then: element is not visible, attribute 'hidden' is set but
        // 'display: none' is not set
        WebElement outer = findElement(
                By.id(InnerTemplateVisibilityView.OUTER_ID));
        Assertions.assertFalse("expected outer to be hidden", outer.isDisplayed());
        Assertions.assertNotNull("expected attribute hidden on outer",
                outer.getAttribute("hidden"));
        Assertions.assertEquals("expected no style attribute", "",
                outer.getAttribute("style"));
    }
}
