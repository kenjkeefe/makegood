/**
 * Copyright (c) 2011, 2013 KUBO Atsuhiro <kubo@iteman.jp>,
 *               2012 MATSUFUJI Hideharu <matsufuji2008@gmail.com>
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;

import com.piece_framework.makegood.core.TestResultsLayout;
import com.piece_framework.makegood.core.continuoustesting.ContinuousTesting;
import com.piece_framework.makegood.core.preference.MakeGoodPreference;
import com.piece_framework.makegood.launch.TestLifecycle;
import com.piece_framework.makegood.ui.launch.TestRunner;

/**
 * @since 1.6.0
 */
public class MakeGoodContext implements IWorkbenchListener {
    private static MakeGoodContext soleInstance;
    private boolean isShuttingDown = false;
    private ActivePart activePart = new ActivePart();
    private ActiveEditor activeEditor = new ActiveEditor();
    private ProjectValidation projectValidation = new ProjectValidation();
    private TestRunner testRunner = new TestRunner();
    private MakeGoodStatusMonitor statusMonitor = new MakeGoodStatusMonitor();
    private MakeGoodStatus status;
    private List<MakeGoodStatusChangeListener> statusChangeListeners = new ArrayList<MakeGoodStatusChangeListener>();

    /**
     * @since 2.5.0
     */
    private TestResultsLayout testResultsLayout;

    /**
     * @since 2.5.0
     */
    private List<TestResultsLayoutChangeListener> testResultsLayoutChangeListeners = new ArrayList<TestResultsLayoutChangeListener>();

    /**
     * @since 2.5.0
     */
    private ContinuousTesting continuousTesting;

    /**
     * @since 2.5.0
     */
    private boolean debug = false;

    private MakeGoodContext() {
        MakeGoodPreference preference = new MakeGoodPreference();
        continuousTesting = new ContinuousTesting(preference.getContinuousTestingEnabled(), preference.getContinuousTestingScope());
        testResultsLayout = preference.getTestResultsLayout();
    }

    public static MakeGoodContext getInstance() {
        if (soleInstance == null) {
            soleInstance = new MakeGoodContext();
        }

        return soleInstance;
    }

    /**
     * @since 2.5.0
     */
    public void setTestResultsLayout(TestResultsLayout testResultsLayout) {
        this.testResultsLayout = testResultsLayout;

        for (TestResultsLayoutChangeListener listener: testResultsLayoutChangeListeners) {
            listener.layoutChanged(this.testResultsLayout);
        }
    }

    /**
     * @since 2.5.0
     */
    public TestResultsLayout getTestResultsLayout() {
        return testResultsLayout;
    }

    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        isShuttingDown = true;

        return true;
    }

    @Override
    public void postShutdown(IWorkbench workbench) {
    }

    public boolean isShuttingDown() {
        return isShuttingDown;
    }

    public ActivePart getActivePart() {
        return activePart;
    }

    /**
     * @since 2.3.0
     */
    public ActiveEditor getActiveEditor() {
        return activeEditor;
    }

    public ProjectValidation getProjectValidation() {
        return projectValidation;
    }

    public TestRunner getTestRunner() {
        return testRunner;
    }

    public MakeGoodStatusMonitor getStatusMonitor() {
        return statusMonitor;
    }

    public void updateStatus() {
        if (TestLifecycle.isRunning()) return;
        try {
            if (projectValidation.validate(activePart.getProject())) {
                updateStatus(MakeGoodStatus.WaitingForTestRun, activePart.getProject());
            }
        } catch (CoreException e) {
            Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
    }

    public void updateStatus(MakeGoodStatus status) {
        this.status = status;
        for (MakeGoodStatusChangeListener listener: statusChangeListeners) {
            listener.statusChanged(this.status);
        }
    }

    public void updateStatus(MakeGoodStatus status, IProject project) {
        status.setProject(project);
        updateStatus(status);
    }

    public void addStatusChangeListener(MakeGoodStatusChangeListener listener) {
        if (!statusChangeListeners.contains(listener)) {
            statusChangeListeners.add(listener);
        }
    }

    public void removeStatusChangeListener(MakeGoodStatusChangeListener listener) {
        statusChangeListeners.remove(listener);
    }

    /**
     * @since 2.5.0
     */
    public void addTestResultsLayoutChangeListener(TestResultsLayoutChangeListener listener) {
        if (!testResultsLayoutChangeListeners.contains(listener)) {
            testResultsLayoutChangeListeners.add(listener);
        }
    }

    /**
     * @since 2.5.0
     */
    public void removeTestResultsLayoutChangeListener(TestResultsLayoutChangeListener listener) {
        testResultsLayoutChangeListeners.remove(listener);
    }

    /**
     * @since 2.5.0
     */
    public ContinuousTesting getContinuousTesting() {
        return continuousTesting;
    }

    /**
     * @since 2.5.0
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @since 2.5.0
     */
    public boolean isDebug() {
        return debug;
    }
}
