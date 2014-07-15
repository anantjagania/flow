/*
 * Copyright 2000-2014 Vaadin Ltd.
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
package com.vaadin.tests.components.grid.basicfeatures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;

public class GridStructureTest extends GridBasicFeaturesTest {

    /*
     * TODO unignore once column header captions are reimplemented
     */
    @Test
    @Ignore
    public void testHidingColumn() throws Exception {
        openTestURL();

        // Column 0 should be visible
        List<TestBenchElement> cells = getGridHeaderRowCells();
        assertEquals("Column0", cells.get(0).getText());

        // Hide column 0
        selectMenuPath("Component", "Columns", "Column0", "Visible");

        // Column 1 should now be the first cell
        cells = getGridHeaderRowCells();
        assertEquals("Column1", cells.get(0).getText());
    }

    /*
     * TODO unignore once column header captions are reimplemented
     */
    @Test
    @Ignore
    public void testRemovingColumn() throws Exception {
        openTestURL();

        // Column 0 should be visible
        List<TestBenchElement> cells = getGridHeaderRowCells();
        assertEquals("Column0", cells.get(0).getText());

        // Hide column 0
        selectMenuPath("Component", "Columns", "Column0", "Remove");

        // Column 1 should now be the first cell
        cells = getGridHeaderRowCells();
        assertEquals("Column1", cells.get(0).getText());
    }

    @Test
    public void testDataLoadingAfterRowRemoval() throws Exception {
        openTestURL();

        // Remove columns 2,3,4
        selectMenuPath("Component", "Columns", "Column2", "Remove");
        selectMenuPath("Component", "Columns", "Column3", "Remove");
        selectMenuPath("Component", "Columns", "Column4", "Remove");

        // Scroll so new data is lazy loaded
        scrollGridVerticallyTo(1000);

        // Let lazy loading do its job
        sleep(1000);

        // Check that row is loaded
        assertThat(getBodyCellByRowAndColumn(11, 0).getText(), not("..."));
    }

    @Test
    public void testFreezingColumn() throws Exception {
        openTestURL();

        // Freeze column 2
        selectMenuPath("Component", "Columns", "Column2", "Freeze");

        WebElement cell = getBodyCellByRowAndColumn(0, 0);
        assertTrue(cell.getAttribute("class").contains("frozen"));

        cell = getBodyCellByRowAndColumn(0, 1);
        assertTrue(cell.getAttribute("class").contains("frozen"));
    }

    @Test
    public void testInitialColumnWidths() throws Exception {
        openTestURL();

        WebElement cell = getBodyCellByRowAndColumn(0, 0);
        assertEquals(100, cell.getSize().getWidth());

        cell = getBodyCellByRowAndColumn(0, 1);
        assertEquals(150, cell.getSize().getWidth());

        cell = getBodyCellByRowAndColumn(0, 2);
        assertEquals(200, cell.getSize().getWidth());
    }

    @Test
    public void testColumnWidths() throws Exception {
        openTestURL();

        // Default column width is 100px
        WebElement cell = getBodyCellByRowAndColumn(0, 0);
        assertEquals(100, cell.getSize().getWidth());

        // Set first column to be 200px wide
        selectMenuPath("Component", "Columns", "Column0", "Column0 Width",
                "200px");

        cell = getBodyCellByRowAndColumn(0, 0);
        assertEquals(200, cell.getSize().getWidth());

        // Set second column to be 150px wide
        selectMenuPath("Component", "Columns", "Column1", "Column1 Width",
                "150px");
        cell = getBodyCellByRowAndColumn(0, 1);
        assertEquals(150, cell.getSize().getWidth());

        // Set first column to be auto sized (defaults to 100px currently)
        selectMenuPath("Component", "Columns", "Column0", "Column0 Width",
                "Auto");

        cell = getBodyCellByRowAndColumn(0, 0);
        assertEquals(100, cell.getSize().getWidth());
    }

    @Test
    public void testPrimaryStyleNames() throws Exception {
        openTestURL();

        // v-grid is default primary style namea
        assertPrimaryStylename("v-grid");

        selectMenuPath("Component", "State", "Primary style name",
                "v-escalator");
        assertPrimaryStylename("v-escalator");

        selectMenuPath("Component", "State", "Primary style name", "my-grid");
        assertPrimaryStylename("my-grid");

        selectMenuPath("Component", "State", "Primary style name", "v-grid");
        assertPrimaryStylename("v-grid");
    }

    /**
     * Test that the current view is updated when a server-side container change
     * occurs (without scrolling back and forth)
     */
    @Test
    public void testItemSetChangeEvent() throws Exception {
        openTestURL();

        final By newRow = By.xpath("//td[text()='newcell: 0']");

        assertTrue("Unexpected initial state", !isElementPresent(newRow));

        selectMenuPath("Component", "Body rows", "Add first row");
        assertTrue("Add row failed", isElementPresent(newRow));

        selectMenuPath("Component", "Body rows", "Remove first row");
        assertTrue("Remove row failed", !isElementPresent(newRow));
    }

    /**
     * Test that the current view is updated when a property's value is reflect
     * to the client, when the value is modified server-side.
     */
    @Test
    public void testPropertyValueChangeEvent() throws Exception {
        openTestURL();

        assertEquals("Unexpected cell initial state", "(0, 0)",
                getBodyCellByRowAndColumn(0, 0).getText());

        selectMenuPath("Component", "Body rows",
                "Modify first row (getItemProperty)");
        assertEquals("(First) modification with getItemProperty failed",
                "modified: 0", getBodyCellByRowAndColumn(0, 0).getText());

        selectMenuPath("Component", "Body rows",
                "Modify first row (getContainerProperty)");
        assertEquals("(Second) modification with getItemProperty failed",
                "modified: Column0", getBodyCellByRowAndColumn(0, 0).getText());
    }

    private void assertPrimaryStylename(String stylename) {
        assertTrue(getGridElement().getAttribute("class").contains(stylename));

        String tableWrapperStyleName = getTableWrapper().getAttribute("class");
        assertTrue(tableWrapperStyleName.contains(stylename + "-tablewrapper"));

        String hscrollStyleName = getHorizontalScroller().getAttribute("class");
        assertTrue(hscrollStyleName.contains(stylename + "-scroller"));
        assertTrue(hscrollStyleName
                .contains(stylename + "-scroller-horizontal"));

        String vscrollStyleName = getVerticalScroller().getAttribute("class");
        assertTrue(vscrollStyleName.contains(stylename + "-scroller"));
        assertTrue(vscrollStyleName.contains(stylename + "-scroller-vertical"));
    }

    private WebElement getBodyCellByRowAndColumn(int row, int column) {
        return getGridElement().getCell(row, column);
    }

    private WebElement getVerticalScroller() {
        return getGridElement().findElement(By.xpath("./div[1]"));
    }

    private WebElement getHorizontalScroller() {
        return getGridElement().findElement(By.xpath("./div[2]"));
    }

    private WebElement getTableWrapper() {
        return getGridElement().findElement(By.xpath("./div[3]"));
    }
}
