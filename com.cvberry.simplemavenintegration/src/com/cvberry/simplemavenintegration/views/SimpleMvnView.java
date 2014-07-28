package com.cvberry.simplemavenintegration.views;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.console.SimpleMvnConsoleFactory;
import com.cvberry.simplemavenintegration.logic.Logic;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SimpleMvnView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.cvberry.simplemavenintegration.views.SimpleMvnView";

	private Action action1;
	private Action mvnaction;
	//private Action doubleClickAction;
	public StateHolder stateHolder;
	private Label pomLocation;
	Composite topParent;
	
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * The constructor.
	 */
	public SimpleMvnView() {
		stateHolder = new StateHolder(Activator.getDefault());
	}
	
	final static String MVN_STATUS_START = "maven status: ";
	
	public void refreshState() {
		stateHolder.currProject = Logic.getCurrentSelectedProject();
		stateHolder.workspace = ResourcesPlugin.getWorkspace();
		if (stateHolder.currProject != null && stateHolder.workspace != null) {
			String projectLoc = Logic.getFullPathToProject(
					stateHolder.workspace, stateHolder.currProject);
			if (projectLoc == null) {
				pomLocation.setText(MVN_STATUS_START+ "couldn't get path to project");
			} else {
				String pomLoc = projectLoc + "/pom.xml";
				File f = new File(pomLoc);
				if (!f.exists() || f.isDirectory())  {
					pomLocation.setText(MVN_STATUS_START+ "no pom in current project.");
				} else {
					pomLocation.setText("pom location: " + pomLoc);
				}
			}
		} else {
			pomLocation.setText(MVN_STATUS_START+ "not in maven project!");
		}
		/* relayout the shell so that the text widget gets resized properly */
		pomLocation.getShell().layout(true,true);
	}

	public static class StateHolder {
		/**
		 * takes in our plugin's activator class.
		 * 
		 * @param plugin
		 */
		public StateHolder(Activator plugin) {
			this.plugin = plugin;
		}
		public Activator plugin;
		public IWorkspace workspace;
		public IProject currProject;
	}

	ISelectionListener listener = new ISelectionListener() {

		/**
		 * I update the shared state according to the rules of the below
		 * selection listener.
		 */
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				refreshState();
			} else {
				return;
			}
		}

	};

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			v.refresh();
		}

		public void dispose() {
		}

		/**
		 * or is the input the inputElement?
		 */
		public Object[] getElements(Object newInput) {

			IProject project = stateHolder.currProject;
			String projectName = "";
			if (project != null) {
				projectName = project.getName();
			}

			String fullPath = Logic.getFullPathToProject(stateHolder.workspace,
					stateHolder.currProject);

			return new String[] { projectName != null ? projectName : "",
					fullPath != null ? fullPath : "" };
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}
	

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		topParent = parent;
		parent.setLayout(GridLayoutFactory.swtDefaults()
				.numColumns(5).create());


		pomLocation = new Label(parent,0);
		Text input = new Text(parent, SWT.SINGLE | SWT.BORDER);
		Button btn = new Button(parent, 0);

		pomLocation.setText(MVN_STATUS_START+ "not in project.");
		GridData gd = GridDataFactory.swtDefaults()
				.span(5, 2).grab(true, false)
				.align(SWT.FILL, SWT.BEGINNING).create();
		pomLocation.setLayoutData(gd);




		//across.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

		input.setMessage("type maven command here");
		btn.setText("run");

		
		//Composite viewerWrapper = new Composite(parent,0);
		//viewerWrapper.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		//viewerWrapper.setLayout(new FillLayout());

		//viewerWrapper.setLayoutData(gd);

		
		/*viewer = new TableViewer(parent , SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		viewer.getTable().setLayoutData(gd); */
		//viewer.getTable().setHeaderVisible(true);
		

		SelectionListener btnlistener = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IProject currProject = stateHolder.currProject;
				if (currProject == null) {
					showMessage("can't run maven, since you aren't in project!");
				}

				try {

					SimpleMvnConsoleFactory factory = new SimpleMvnConsoleFactory();
					MessageConsole toUse = factory.getSimpleMvnConsole();
					Logic.invokeMaven(stateHolder.plugin, 
							Logic.getFullPathToProject(stateHolder.workspace,
									currProject), 
									input.getText(),
									toUse.newMessageStream());
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					showMessage("invocation of maven failed: "
							+ e1.getMessage());
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

		};
		btn.addSelectionListener(btnlistener);

		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(listener);
		pomLocation.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getSite().getWorkbenchWindow().getSelectionService()
						.removeSelectionListener(listener);
			}
		});

	// Create the help context id for the viewer's control
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(topParent, "com.cvberry.simplemavenintegration.viewer"); 
		makeActions();
		hookContextMenu();
		//hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SimpleMvnView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(topParent);
		topParent.setMenu(menu);
		ISelectionProvider prvdr = new ISelectionProvider() {

			@Override
			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
				showMessage("selection changed!");
			}

			@Override
			public ISelection getSelection() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSelection(ISelection selection) {
				// TODO Auto-generated method stub
				
			}
			
		};
		getSite().registerContextMenu(menuMgr, prvdr);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(mvnaction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(mvnaction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(mvnaction);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				try {
					String results = Logic.invokeDir(stateHolder.plugin);
					showMessage(results);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showMessage("couldn't execute dir: " + e.getMessage());
				}
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		mvnaction = new Action() {
			public void run() {

				String fullPath = Logic.getFullPathToProject(
						stateHolder.workspace, stateHolder.currProject);
				if (fullPath == null) {
					showMessage("can't run maven!  not in project!");
				} else {
					try {
						String results = Logic.invokeMaven(
								stateHolder.plugin, 
								fullPath,
								"clean package");
						showMessage(results);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
						showMessage("mvn action failed with following error: "
								+ e.getMessage());
					}
				}

			}
		};
		mvnaction.setText("mvnaction");
		mvnaction.setToolTipText("mvnaction tooltip");
		mvnaction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		/*doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};*/
	}

	/*private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}*/

	private void showMessage(String message) {
		MessageDialog.openInformation(topParent.getShell(),
				"SimpleMvnView", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		topParent.setFocus();
	}
}