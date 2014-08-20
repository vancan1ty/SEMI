package com.cvberry.simplemavenintegration.wizards;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.logic.Logic;

public class MvnImportWizardPage extends WizardPage {
	
	
	private Text pomPathText;
	private Label statusLabel;

	protected MvnImportWizardPage(String pageName) {
		super(pageName);
		this.setMessage("Please select the location of the pom associated with"
				+ " the project you would like to import.");
	}

	@Override
	public void createControl(Composite parent) {
		this.setPageComplete(false);
		Composite child = new Composite(parent, 0);
		child.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		//child.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		pomPathText = new Text(child , SWT.SINGLE | SWT.BORDER);
		pomPathText.setLayoutData(GridDataFactory.swtDefaults().
				grab(true, false).span(3, 1).align(SWT.FILL, SWT.CENTER)
				.create());
		//pomPathText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));

		Button findPomButton = new Button(child , SWT.PUSH);
		findPomButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		
		statusLabel = new Label(child, 0);
		statusLabel.setText("");
		

		findPomButton.setText("Select Project pom.xml");
		findPomButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
				dialog.setText("locate the pom!");
				String pomPath = dialog.open();
				pomPathText.setText(pomPath);
				statusLabel.setText("validating specified pom...");
				statusLabel.pack();

				File fPath = new File(pomPath);
				String dirPathStr = fPath.getParent();
				ByteArrayOutputStream theOut = new ByteArrayOutputStream();
				Consumer<Integer> updateValidationStatusFnction = (i) -> {
					if (i == 0) {
						statusLabel.setText("mvn validate successful.");
						statusLabel.pack();
						MvnImportWizardPage.this.setPageComplete(true);
					} else {
						statusLabel.setText("mvn validate unsuccessful!  "
								+ "check to see if pom file exists, or is in error?  Error code: " + Integer.toString(i));
						statusLabel.pack();
					}
					//statusLabel.setText(Integer.toString(i));
				};

			Logic.runOnMaven(Activator.getDefault(), "validate", 
						dirPathStr, theOut, theOut, updateValidationStatusFnction);

			}
		});

		this.setControl(child);

		/*
		 * Display display = new Display (); Shell shell = new Shell (display);
		 * shell.open (); DirectoryDialog dialog = new DirectoryDialog (shell);
		 * String platform = SWT.getPlatform(); dialog.setFilterPath
		 * (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
		 * System.out.println ("RESULT=" + dialog.open ()); while
		 * (!shell.isDisposed()) { if (!display.readAndDispatch ())
		 * display.sleep (); } display.dispose (); }
		 */
	}

	public String getPomDirectoryPath() {
		File fPath = new File(pomPathText.getText());
		return fPath.getParent();
	}

	public void setStatusLabel(String toSet) {
		statusLabel.setText(toSet);
	}

}
