/*
 * Copyright 2000-2016 Vaadin Ltd.
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

package com.vaadin.ui;

import java.io.Serializable;
import java.util.EventObject;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.event.EventRouter;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.event.UIEvents.PollNotifier;
import com.vaadin.hummingbird.StateNode;
import com.vaadin.hummingbird.dom.Element;
import com.vaadin.hummingbird.dom.EventRegistrationHandle;
import com.vaadin.hummingbird.namespace.ElementDataNamespace;
import com.vaadin.hummingbird.namespace.LoadingIndicatorConfigurationNamespace;
import com.vaadin.hummingbird.namespace.PollConfigurationNamespace;
import com.vaadin.hummingbird.namespace.ReconnectDialogConfigurationNamespace;
import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandlingRunnable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.communication.PushConnection;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.util.CurrentInstance;

/**
 * The topmost component in any component hierarchy. There is one UI for every
 * Vaadin instance in a browser window. A UI may either represent an entire
 * browser window (or tab) or some part of a html page where a Vaadin
 * application is embedded.
 * <p>
 * The UI is the server side entry point for various client side features that
 * are not represented as components added to a layout, e.g notifications, sub
 * windows, and executing javascript in the browser.
 * </p>
 * <p>
 * When a new UI instance is needed, typically because the user opens a URL in a
 * browser window which points to e.g. {@link VaadinServlet}, the UI mapped to
 * that servlet is opened. The selection is based on the <code>UI</code> init
 * parameter.
 * </p>
 * <p>
 * After a UI has been created by the application, it is initialized using
 * {@link #init(VaadinRequest)}. This method is intended to be overridden by the
 * developer to add components to the user interface and initialize
 * non-component functionality.
 * </p>
 *
 * @see #init(VaadinRequest)
 *
 * @since 7.0
 */
public abstract class UI implements Serializable, PollNotifier {

    public static final String POLL_DOM_EVENT_NAME = "ui-poll";

    /**
     * The Vaadin session to which this UI belongs.
     */
    private volatile VaadinSession session;

    /**
     * The id of this UI, used to find the server side instance of the UI form
     * which a request originates. A negative value indicates that the UI id has
     * not yet been assigned by the Application.
     *
     * @see VaadinSession#getNextUIid()
     */
    private int uiId = -1;

    private boolean closing = false;

    private PushConfiguration pushConfiguration;

    private Locale locale = Locale.getDefault();

    private EventRouter eventRouter;

    private EventRegistrationHandle domPollListener = null;

    private final FrameworkData frameworkData = new FrameworkData(this);

    /**
     * Creates a new empty UI.
     */
    public UI() {
        getNode().getNamespace(ElementDataNamespace.class).setTag("body");
        pushConfiguration = new PushConfigurationImpl(this);
    }

    /**
     * Gets the application object to which the component is attached.
     *
     * <p>
     * The method will return {@code null} if the component is not currently
     * attached to an application.
     * </p>
     *
     * <p>
     * Getting a null value is often a problem in constructors of regular
     * components and in the initializers of custom composite components. A
     * standard workaround is to use {@link VaadinSession#getCurrent()} to
     * retrieve the application instance that the current request relates to.
     * Another way is to move the problematic initialization to
     * {@link #attach()}, as described in the documentation of the method.
     * </p>
     *
     * @return the parent application of the component or <code>null</code>.
     * @see #attach()
     */
    public VaadinSession getSession() {
        return session;
    }

    /**
     * Sets the session to which this UI is assigned.
     * <p>
     * This method is for internal use by the framework. To explicitly close a
     * UI, see {@link #close()}.
     * </p>
     *
     * @param session
     *            the session to set
     *
     * @throws IllegalStateException
     *             if the session has already been set
     *
     * @see #getSession()
     */
    public void setSession(VaadinSession session) {
        if (session == null && this.session == null) {
            throw new IllegalStateException(
                    "Session should never be set to null when UI.session is already null");
        } else if (session != null && this.session != null) {
            throw new IllegalStateException(
                    "Session has already been set. Old session: "
                            + getSessionDetails(this.session)
                            + ". New session: " + getSessionDetails(session)
                            + ".");
        } else {
            if (session == null) {
                try {
                    detach();
                } catch (Exception e) {
                    getLogger().log(Level.WARNING,
                            "Error while detaching UI from session", e);
                }
                // Disable push when the UI is detached. Otherwise the
                // push connection and possibly VaadinSession will live on.
                getPushConfiguration().setPushMode(PushMode.DISABLED);
                getFrameworkData().setPushConnection(null);
            }
            this.session = session;
        }

        if (session != null) {
            attach();
        }
    }

    private static String getSessionDetails(VaadinSession session) {
        if (session == null) {
            return null;
        } else {
            return session.toString() + " for "
                    + session.getService().getServiceName();
        }
    }

    /**
     * Gets the id of the UI, used to identify this UI within its application
     * when processing requests. The UI id should be present in every request to
     * the server that originates from this UI.
     * {@link VaadinService#findUI(VaadinRequest)} uses this id to find the
     * route to which the request belongs.
     * <p>
     * This method is not intended to be overridden. If it is overridden, care
     * should be taken since this method might be called in situations where
     * {@link UI#getCurrent()} does not return this UI.
     *
     * @return the id of this UI
     */
    public int getUIId() {
        return uiId;
    }

    /**
     * Internal initialization method, should not be overridden. This method is
     * not declared as final because that would break compatibility with e.g.
     * CDI.
     *
     * @param request
     *            the initialization request
     * @param uiId
     *            the id of the new ui
     *
     * @see #getUIId()
     */
    public void doInit(VaadinRequest request, int uiId) {
        if (this.uiId != -1) {
            String message = "This UI instance is already initialized (as UI id "
                    + this.uiId
                    + ") and can therefore not be initialized again (as UI id "
                    + uiId + "). ";

            if (getSession() != null
                    && !getSession().equals(VaadinSession.getCurrent())) {
                message += "Furthermore, it is already attached to another VaadinSession. ";
            }
            message += "Please make sure you are not accidentally reusing an old UI instance.";

            throw new IllegalStateException(message);
        }
        this.uiId = uiId;

        // Call the init overridden by the application developer
        init(request);

    }

    /**
     * Initializes this UI. This method is intended to be overridden by
     * subclasses to build the view and configure non-component functionality.
     * Performing the initialization in a constructor is not suggested as the
     * state of the UI is not properly set up when the constructor is invoked.
     * <p>
     * The {@link VaadinRequest} can be used to get information about the
     * request that caused this UI to be created.
     * </p>
     *
     * @param request
     *            the Vaadin request that caused this UI to be created
     */
    protected abstract void init(VaadinRequest request);

    /**
     * Sets the thread local for the current UI. This method is used by the
     * framework to set the current application whenever a new request is
     * processed and it is cleared when the request has been processed.
     * <p>
     * The application developer can also use this method to define the current
     * UI outside the normal request handling, e.g. when initiating custom
     * background threads.
     * <p>
     * The UI is stored using a weak reference to avoid leaking memory in case
     * it is not explicitly cleared.
     *
     * @param ui
     *            the UI to register as the current UI
     *
     * @see #getCurrent()
     * @see ThreadLocal
     */
    public static void setCurrent(UI ui) {
        CurrentInstance.setInheritable(UI.class, ui);
    }

    /**
     * Gets the currently used UI. The current UI is automatically defined when
     * processing requests to the server. In other cases, (e.g. from background
     * threads), the current UI is not automatically defined.
     * <p>
     * The UI is stored using a weak reference to avoid leaking memory in case
     * it is not explicitly cleared.
     *
     * @return the current UI instance if available, otherwise <code>null</code>
     *
     * @see #setCurrent(UI)
     */
    public static UI getCurrent() {
        return CurrentInstance.get(UI.class);
    }

    /**
     * Marks this UI to be {@link #detach() detached} from the session at the
     * end of the current request, or the next request if there is no current
     * request (if called from a background thread, for instance.)
     * <p>
     * The UI is detached after the response is sent, so in the current request
     * it can still update the client side normally. However, after the response
     * any new requests from the client side to this UI will cause an error, so
     * usually the client should be asked, for instance, to reload the page
     * (serving a fresh UI instance), to close the page, or to navigate
     * somewhere else.
     * <p>
     * Note that this method is strictly for users to explicitly signal the
     * framework that the UI should be detached. Overriding it is not a reliable
     * way to catch UIs that are to be detached. Instead, {@code UI.detach()}
     * should be overridden.
     */
    public void close() {
        closing = true;

        // FIXME Send info to client

        PushConnection pushConnection = getFrameworkData().getPushConnection();
        if (pushConnection != null) {
            // Push the Rpc to the client. The connection will be closed when
            // the UI is detached and cleaned up.

            // Can't use UI.push() directly since it checks for a valid session
            if (session != null) {
                session.getService().runPendingAccessTasks(session);
            }
            pushConnection.push();
        }

    }

    /**
     * Returns whether this UI is marked as closed and is to be detached.
     * <p>
     * This method is not intended to be overridden. If it is overridden, care
     * should be taken since this method might be called in situations where
     * {@link UI#getCurrent()} does not return this UI.
     *
     * @see #close()
     *
     * @return whether this UI is closing.
     */
    public boolean isClosing() {
        return closing;
    }

    /**
     * Called after the UI is added to the session. A UI instance is attached
     * exactly once, before its {@link #init(VaadinRequest) init} method is
     * called.
     *
     */
    public void attach() {
    }

    /**
     * Called before the UI is removed from the session. A UI instance is
     * detached exactly once, either:
     * <ul>
     * <li>after it is explicitly {@link #close() closed}.
     * <li>when its session is closed or expires
     * <li>after three missed heartbeat requests.
     * </ul>
     * <p>
     * Note that when a UI is detached, any changes made in the {@code detach}
     * methods of any children that would be communicated to the client are
     * silently ignored.
     */
    public void detach() {
    }

    /**
     * Locks the session of this UI and runs the provided Runnable right away.
     * <p>
     * It is generally recommended to use {@link #access(Runnable)} instead of
     * this method for accessing a session from a different thread as
     * {@link #access(Runnable)} can be used while holding the lock of another
     * session. To avoid causing deadlocks, this methods throws an exception if
     * it is detected than another session is also locked by the current thread.
     * <p>
     * This method behaves differently than {@link #access(Runnable)} in some
     * situations:
     * <ul>
     * <li>If the current thread is currently holding the lock of the session,
     * {@link #accessSynchronously(Runnable)} runs the task right away whereas
     * {@link #access(Runnable)} defers the task to a later point in time.</li>
     * <li>If some other thread is currently holding the lock for the session,
     * {@link #accessSynchronously(Runnable)} blocks while waiting for the lock
     * to be available whereas {@link #access(Runnable)} defers the task to a
     * later point in time.</li>
     * </ul>
     *
     * @since 7.1
     *
     * @param runnable
     *            the runnable which accesses the UI
     * @throws UIDetachedException
     *             if the UI is not attached to a session (and locking can
     *             therefore not be done)
     * @throws IllegalStateException
     *             if the current thread holds the lock for another session
     *
     * @see #access(Runnable)
     * @see VaadinSession#accessSynchronously(Runnable)
     */
    public void accessSynchronously(Runnable runnable)
            throws UIDetachedException {
        Map<Class<?>, CurrentInstance> old = null;

        VaadinSession session = getSession();

        if (session == null) {
            throw new UIDetachedException();
        }

        VaadinService.verifyNoOtherSessionLocked(session);

        session.lock();
        try {
            if (getSession() == null) {
                // UI was detached after fetching the session but before we
                // acquired the lock.
                throw new UIDetachedException();
            }
            old = CurrentInstance.setCurrent(this);
            runnable.run();
        } finally {
            session.unlock();
            if (old != null) {
                CurrentInstance.restoreInstances(old);
            }
        }

    }

    /**
     * Provides exclusive access to this UI from outside a request handling
     * thread.
     * <p>
     * The given runnable is executed while holding the session lock to ensure
     * exclusive access to this UI. If the session is not locked, the lock will
     * be acquired and the runnable is run right away. If the session is
     * currently locked, the runnable will be run before that lock is released.
     * </p>
     * <p>
     * RPC handlers for components inside this UI do not need to use this method
     * as the session is automatically locked by the framework during RPC
     * handling.
     * </p>
     * <p>
     * Please note that the runnable might be invoked on a different thread or
     * later on the current thread, which means that custom thread locals might
     * not have the expected values when the runnable is executed. Inheritable
     * values in {@link CurrentInstance} will have the same values as when this
     * method was invoked. {@link UI#getCurrent()},
     * {@link VaadinSession#getCurrent()} and {@link VaadinService#getCurrent()}
     * are set according to this UI before executing the runnable.
     * Non-inheritable CurrentInstance values including
     * {@link VaadinService#getCurrentRequest()} and
     * {@link VaadinService#getCurrentResponse()} will not be defined.
     * </p>
     * <p>
     * The returned future can be used to check for task completion and to
     * cancel the task.
     * </p>
     *
     * @see #getCurrent()
     * @see #accessSynchronously(Runnable)
     * @see VaadinSession#access(Runnable)
     * @see VaadinSession#lock()
     *
     * @since 7.1
     *
     * @param runnable
     *            the runnable which accesses the UI
     * @throws UIDetachedException
     *             if the UI is not attached to a session (and locking can
     *             therefore not be done)
     * @return a future that can be used to check for task completion and to
     *         cancel the task
     */
    public Future<Void> access(final Runnable runnable) {
        VaadinSession session = getSession();

        if (session == null) {
            throw new UIDetachedException();
        }

        return session.access(new ErrorHandlingRunnable() {
            @Override
            public void run() {
                accessSynchronously(runnable);
            }

            @Override
            public void handleError(Exception exception) {
                try {
                    if (runnable instanceof ErrorHandlingRunnable) {
                        ErrorHandlingRunnable errorHandlingRunnable = (ErrorHandlingRunnable) runnable;
                        errorHandlingRunnable.handleError(exception);
                    } else {
                        getSession().getErrorHandler()
                                .error(new ErrorEvent(exception));
                    }
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, e.getMessage(), e);
                }
            }
        });
    }

    /**
     * Sets the interval with which the UI should poll the server to see if
     * there are any changes. Polling is disabled by default.
     * <p>
     * Note that it is possible to enable push and polling at the same time but
     * it should not be done to avoid excessive server traffic.
     * </p>
     * <p>
     * Add-on developers should note that this method is only meant for the
     * application developer. An add-on should not set the poll interval
     * directly, rather instruct the user to set it.
     * </p>
     *
     * @param intervalInMillis
     *            The interval (in ms) with which the UI should poll the server
     *            or -1 to disable polling
     */
    public void setPollInterval(int intervalInMillis) {
        getNode().getNamespace(PollConfigurationNamespace.class)
                .setPollInterval(intervalInMillis);
    }

    /**
     * Returns the interval with which the UI polls the server.
     *
     * @return The interval (in ms) with which the UI polls the server or -1 if
     *         polling is disabled
     */
    public int getPollInterval() {
        return getNode().getNamespace(PollConfigurationNamespace.class)
                .getPollInterval();
    }

    /**
     * Returns the event router instance, which is created on the first call to
     * this method.
     *
     * @return the event router for UI
     */
    private EventRouter getEventRouter() {
        if (eventRouter == null) {
            eventRouter = new EventRouter();
        }
        return eventRouter;
    }

    private void fireEvent(EventObject event) {
        getEventRouter().fireEvent(event);
    }

    @Override
    public void addPollListener(PollListener listener) {
        if (domPollListener == null) {
            domPollListener = getElement().addEventListener(POLL_DOM_EVENT_NAME,
                    () -> fireEvent(new PollEvent(UI.this)));
        }

        getEventRouter().addListener(PollEvent.class, listener,
                PollListener.POLL_METHOD);
    }

    @Override
    public void removePollListener(PollListener listener) {
        getEventRouter().removeListener(PollEvent.class, listener);
        if (!getEventRouter().hasListeners(PollEvent.class)) {
            domPollListener.remove();
            domPollListener = null;
        }
    }

    /**
     * Retrieves the object used for configuring the loading indicator.
     *
     * @return The instance used for configuring the loading indicator
     */
    public LoadingIndicatorConfiguration getLoadingIndicatorConfiguration() {
        return getNode()
                .getNamespace(LoadingIndicatorConfigurationNamespace.class);
    }

    /**
     * Pushes the pending changes and client RPC invocations of this UI to the
     * client-side.
     * <p>
     * If push is enabled, but the push connection is not currently open, the
     * push will be done when the connection is established.
     * <p>
     * As with all UI methods, the session must be locked when calling this
     * method. It is also recommended that {@link UI#getCurrent()} is set up to
     * return this UI since writing the response may invoke logic in any
     * attached component or extension. The recommended way of fulfilling these
     * conditions is to use {@link #access(Runnable)}.
     *
     * @throws IllegalStateException
     *             if push is disabled.
     * @throws UIDetachedException
     *             if this UI is not attached to a session.
     *
     * @see #getPushConfiguration()
     *
     * @since 7.1
     */
    public void push() {
        VaadinSession session = getSession();

        if (session == null) {
            throw new UIDetachedException("Cannot push a detached UI");
        }
        assert session.hasLock();

        if (!getPushConfiguration().getPushMode().isEnabled()) {
            throw new IllegalStateException("Push not enabled");
        }

        PushConnection pushConnection = getFrameworkData().getPushConnection();
        assert pushConnection != null;

        /*
         * Purge the pending access queue as it might mark a connector as dirty
         * when the push would otherwise be ignored because there are no changes
         * to push.
         */
        session.getService().runPendingAccessTasks(session);

        if (!getFrameworkData().getStateTree().hasDirtyNodes()) {
            // Do not push if there is nothing to push
            return;
        }

        pushConnection.push();
    }

    /**
     * Retrieves the object used for configuring the push channel.
     * <p>
     * Note that you cannot change push parameters on the fly, you need to
     * configure the push channel at the same time (in the same request) it is
     * enabled.
     *
     * @since 7.1
     * @return The instance used for push configuration
     */
    public PushConfiguration getPushConfiguration() {
        return pushConfiguration;
    }

    /**
     * Retrieves the object used for configuring the reconnect dialog.
     *
     * @since 7.6
     * @return The instance used for reconnect dialog configuration
     */
    public ReconnectDialogConfiguration getReconnectDialogConfiguration() {
        return getNode()
                .getNamespace(ReconnectDialogConfigurationNamespace.class);
    }

    private static Logger getLogger() {
        return Logger.getLogger(UI.class.getName());
    }

    /**
     * * Gets the locale for this UI.
     *
     * @return the locale in use
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale for this UI.
     *
     * @param locale
     *            the locale to use
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Gets the element for this UI.
     * <p>
     * The UI element corresponds to the {@code <body>} tag on the page
     *
     * @return the element for this UI
     */
    public Element getElement() {
        return Element.get(getNode());
    }

    /**
     * Gets the state node for this UI.
     *
     * @return the state node for the UI, in practice the state tree root node
     */
    private StateNode getNode() {
        return getFrameworkData().getStateTree().getRootNode();
    }

    /**
     * Gets the framework data object for this UI.
     *
     * @return the framework data object
     */
    public FrameworkData getFrameworkData() {
        return frameworkData;
    }

    /**
     * Gets the object representing the page on which this UI exists.
     *
     * @return an object representing the page on which this UI exists
     */
    public Page getPage() {
        return new Page(getFrameworkData().getStateTree().getRootNode());
    }
}
