package com.vaadin.flow.uitest.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.flow.uitest.servlet.RouterTestServlet;
import com.vaadin.flow.uitest.servlet.RouterTestServlet.ChildNavigationTarget;
import com.vaadin.flow.uitest.servlet.RouterTestServlet.FooBarNavigationTarget;
import com.vaadin.flow.uitest.servlet.RouterTestServlet.FooNavigationTarget;
import com.vaadin.flow.uitest.servlet.ViewTestLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

public class RouterIT extends ChromeBrowserTest {

    @Override
    protected String getTestPath() {
        return "/new-router-session/";
    }

    @Test
    public void rootNavigationTarget() {
        open();
        Assertions.assertEquals(
                ViewTestLayout.BaseNavigationTarget.class.getSimpleName(),
                findElement(By.id("name-div")).getText());
    }

    @Test
    public void fooNavigationTarget() {
        openRouteUrl("foo");
        Assertions.assertEquals(FooNavigationTarget.class.getSimpleName(),
                findElement(By.id("name-div")).getText());

        // Test that url with trailing slash also works
        openRouteUrl("foo/");
        Assertions.assertEquals(FooNavigationTarget.class.getSimpleName(),
                findElement(By.id("name-div")).getText());
    }

    @Test
    public void fooBarNavigationTarget() {
        openRouteUrl("foo/bar");
        Assertions.assertEquals(FooBarNavigationTarget.class.getSimpleName(),
                findElement(By.id("name-div")).getText());
    }

    @Test
    public void childIsInsideRouterLayout() {
        openRouteUrl("baz");

        Assertions.assertTrue(isElementPresent(By.id("layout")));
        WebElement layout = findElement(By.id("layout"));

        Assertions.assertEquals(ChildNavigationTarget.class.getSimpleName(),
                layout.findElement(By.id("name-div")).getText());
    }

    @Test
    public void stringRouteParameter() {
        openRouteUrl("greeting/World");
        Assertions.assertEquals("Hello, World!",
                findElement(By.id("greeting-div")).getText());
    }

    @Test
    public void targetHasMultipleParentLayouts() {
        openRouteUrl("target");

        Assertions.assertTrue("Missing top most level: main layout",
                isElementPresent(By.id("mainLayout")));
        Assertions.assertTrue("Missing center layout: middle layout",
                isElementPresent(By.id("middleLayout")));

        WebElement layout = findElement(By.id("middleLayout"));

        Assertions.assertEquals("Child layout is the wrong class",
                RouterTestServlet.TargetLayout.class.getSimpleName(),
                layout.findElement(By.id("name-div")).getText());
    }

    @Test
    public void faultyRouteShowsExpectedErrorScreen() {
        openRouteUrl("exception");

        Assertions.assertTrue(getDriver().getPageSource()
                .contains("Could not navigate to 'exception'"));

        Assertions.assertTrue(getDriver().getPageSource()
                .contains(RouterTestServlet.AliasLayout.class
                        .getAnnotation(Route.class).value()));

        Assertions.assertTrue(getDriver().getPageSource()
                .contains(RouterTestServlet.AliasLayout.class
                        .getAnnotation(RouteAlias.class).value()));
    }

    @Test
    public void routeWithRouteAliasHasNoParents() {
        openRouteUrl(RouterTestServlet.AliasLayout.class
                .getAnnotation(Route.class).value());

        Assertions.assertFalse(
                "Found parent layouts even though none should be available.",
                isElementPresent(By.id("mainLayout")));
        Assertions.assertFalse(
                "Found parent layouts even though none should be available.",
                isElementPresent(By.id("middleLayout")));
        Assertions.assertEquals("Layout content has the wrong class",
                RouterTestServlet.AliasLayout.class.getSimpleName(),
                findElement(By.id("name-div")).getText());
    }

    @Test
    public void routeAliasHasTwoParentsWhenRouteHasNone() {
        openRouteUrl(RouterTestServlet.AliasLayout.class
                .getAnnotation(RouteAlias.class).value());

        Assertions.assertTrue("Missing top most level: main layout.",
                isElementPresent(By.id("mainLayout")));
        Assertions.assertTrue("Missing center layout: middle layout.",
                isElementPresent(By.id("middleLayout")));

        Assertions.assertEquals("Layout content has the wrong class",
                RouterTestServlet.AliasLayout.class.getSimpleName(),
                findElement(By.id("name-div")).getText());
    }

    private void openRouteUrl(String route) {
        getDriver().get(getRootURL() + getTestPath() + route);
        waitForDevServer();
    }
}
