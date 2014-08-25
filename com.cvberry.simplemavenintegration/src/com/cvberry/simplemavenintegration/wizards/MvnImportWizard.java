package com.cvberry.simplemavenintegration.wizards;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.logic.Logic;

public class MvnImportWizard extends Wizard implements IImportWizard {

	IWorkbench workbench;
	IStructuredSelection selection;
	MvnImportWizardPage wPage;

	/**
	 * don't think I need to do anything here, as this seems to exist to
	 * facilitate importing resources underneath the selected folder or
	 * whatever, which isn't particularly relevant for what we do.
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	/**
	 * when finish is clicked, mvn's eclipse:eclipse goal is run to generate a
	 * project, and then the import project wizard is opened.
	 */
	@Override
	public boolean performFinish() {
		String pomDirPath = wPage.getPomDirectoryPath();
		ByteArrayOutputStream theOut = new ByteArrayOutputStream();

		try {
			Activator.getDefault().getStateSaveHandler()
					.addTrackedProject(pomDirPath);
		} catch (NoSuchAlgorithmException | IOException e1) {
			// TODO Auto-generated catch block
			Activator.getDefault().showMessage(
					"simple_eclipse_maven_integration error.",
					"project import failed: " + e1.getMessage());
			e1.printStackTrace();
		}

		Consumer<Integer> continueFinishFunction = (i) -> {
			if (i == 0) { // then the execution was successful.
				ExternalProjectImportWizard wizard = new ExternalProjectImportWizard(
						pomDirPath);
				wizard.init(workbench, selection);
				WizardDialog dialog = new WizardDialog(workbench.getDisplay()
						.getActiveShell(), wizard);
				dialog.open();

			} else {
				IStatus warning = new org.eclipse.core.runtime.Status(
						IStatus.WARNING, Activator.PLUGIN_ID, 1,
						"You have been warned.", null);
				ErrorDialog.openError(this.getShell(),
						"This is your final warning", null, warning);
			}
			// statusLabel.setText(Integer.toString(i));
		};

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.setTaskName("running mvn eclipse:eclipse goal, which generates"
						+ " eclipse project files.");
				Logic.runOnMavenSameThread(Activator.getDefault(),
						"eclipse:eclipse", pomDirPath, theOut, theOut,
						continueFinishFunction);

			}
		};

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win != null ? win.getShell() : null;

		try {
			new ProgressMonitorDialog(shell).run(true, false, op);
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Activator.getDefault().showMessage("there was a problem!",
					"progress monitor dialog failure: " + e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		wPage = new MvnImportWizardPage("filechooserpage");
		addPage(wPage);
	}

}
