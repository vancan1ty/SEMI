package com.cvberry.simplemavenintegration.logic;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.runners.MvnExecutor;
import com.cvberry.simplemavenintegration.runners.MvnExecutor2;

public class Logic {

	/**
	 * get the current selected project using the selection service.
	 * intended to be used to update the plugin working state in stateHolder
	 * based on listener invocations.
	 * @return the current selected project!
	 */
	public static IProject getCurrentSelectedProject() {
		IProject project = null;
		ISelectionService selectionService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService();

		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();

			if (element instanceof IResource) {
				project = ((IResource) element).getProject();
			} else if (element instanceof IPackageFragmentRoot) {
				IJavaProject jProject = ((IPackageFragmentRoot) element)
						.getJavaProject();
				project = jProject.getProject();
			} else if (element instanceof IJavaElement) {
				IJavaProject jProject = ((IJavaElement) element)
						.getJavaProject();
				project = jProject.getProject();
			}
		}
		return project;
	}


	/**
	 * @param workspace
	 * @param currProject
	 * @return full path to project, or null if can't find path for some reason
	 */
	public static String getFullPathToProject(IWorkspace workspace,
			IProject currProject) {
		if (workspace == null || currProject == null) {
			return null;
		}
		IWorkspaceRoot root = workspace.getRoot();
		IPath rootLoc = root.getLocation();
		String rootPath = rootLoc.toString();

		return rootPath + currProject.getFullPath().toString();
	}

	public static String invokeMaven(Activator plugin, String path, String goals)
			throws IOException, InterruptedException {
		String runWith = "cmd /c mvn -f " + path + " " + goals;
		return runCommand(plugin, runWith);
	}

	public static int invokeMaven(Activator plugin, String path, 
			String goals, OutputStream out)
			throws IOException, InterruptedException {
		String runWith = "cmd /c mvn -f " + path + " " + goals;
		int resultCode = runCommandPrintToStream(plugin, runWith,out);
		return resultCode;
	}
	
	public static String invokeDir(Activator plugin) 
			throws IOException, InterruptedException {
		return runCommand(plugin, "cmd /c mvn -v");
	}
	
	public static String runCommand(Activator plugin, String toRun) 
			throws IOException, InterruptedException {

		OutputStream receiver = new ByteArrayOutputStream();
		runCommandPrintToStream(plugin, toRun, receiver);

		return receiver.toString();

	}
	
	/**
	 * runs a system command, prints the output of the command to out as it
	 * is received, returns the exit status of the command.
	 * 
	 * @param plugin
	 * @param toRun
	 * @param out
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int runCommandPrintToStream(
			Activator plugin, String toRun, OutputStream out) 
					throws IOException, InterruptedException {
		plugin.log(Status.INFO, toRun);
		Process p = Runtime.getRuntime().exec(toRun);
//		p.waitFor();

		String line = "";
		InputStream iStream = p.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				iStream));
		while ((line = reader.readLine()) != null ) {
			out.write((line + "\n").getBytes());
		}

		return p.exitValue();
	}

	/* public static String convertFilePathToDirPath(String filePath) {
		filePath.split("/");
	} */
	
	/**
	 * runs the given commands on maven, printing results to output streams. 
	 * 
	 * @param plugin
	 * @param commands
	 * @param rootPath
	 * @param stdOut
	 * @param stdErr
	 * @param callBackProcedure will get called after the operation is done!
	 */
	public static void runOnMavenNewThread(Activator plugin, String commands, 
			String rootPath, OutputStream stdOut, OutputStream stdErr,
			Consumer<Integer> callBackProcedure) {
		Executor ex = Executors.newSingleThreadExecutor();
		MvnExecutor2 mvnExecutor = new MvnExecutor2(plugin, commands, rootPath, 
				stdOut, stdErr, callBackProcedure); 
		Thread t = new Thread(mvnExecutor);
		t.start();
	}

	public static void runOnMavenSameThread(Activator plugin, String commands, 
			String rootPath, OutputStream stdOut, OutputStream stdErr,
			Consumer<Integer> callBackProcedure) {
		Executor ex = Executors.newSingleThreadExecutor();
		MvnExecutor2 mvnExecutor = new MvnExecutor2(plugin, commands, rootPath, 
				stdOut, stdErr, callBackProcedure); 
		mvnExecutor.run();
	}
}