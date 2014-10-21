package com.cvberry.simplemavenintegration;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cvberry.simplemavenintegration.data.StateObject;
import com.cvberry.simplemavenintegration.data.StateSaveHandler;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.cvberry.simplemavenintegration"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private StateSaveHandler stateHandler;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		stateHandler = new StateSaveHandler(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		stateHandler.saveAppData(stateHandler.getAppStateObject());
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public void log(int level, String msg) {
		getLog().log(
				new Status(level, Activator.PLUGIN_ID, Status.OK, msg, null));
		
	}
	public void log(int level, String msg, Exception e) {
		getLog().log(new Status(level, Activator.PLUGIN_ID, Status.OK, msg, e));
	}

	public void showMessage(String title, String msg) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win != null ? win.getShell() : null;
		/*IStatus warning = new org.eclipse.core.runtime.Status(IStatus.INFO,
				Activator.PLUGIN_ID, 1, msg, null); */
		MessageDialog.open(MessageDialog.INFORMATION, shell, title, msg,
				SWT.SHEET);
	}

	public StateSaveHandler getStateSaveHandler() {
		return stateHandler;
	}

}
