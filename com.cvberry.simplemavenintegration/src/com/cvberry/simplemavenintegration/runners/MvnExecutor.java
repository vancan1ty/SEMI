package com.cvberry.simplemavenintegration.runners;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.MessageConsole;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.console.SimpleMvnConsoleFactory;
import com.cvberry.simplemavenintegration.logic.Logic;

public class MvnExecutor implements Runnable {
	
	private Activator plugin;
	String commandsToRun;
	private IWorkspace workspace;
	private IProject currProject;

	public MvnExecutor(Activator plugin, IWorkspace workspace, 
			IProject currProject, String commandsToRun) {
		this.plugin = plugin;
		this.workspace = workspace;
		this.currProject = currProject;
		this.commandsToRun = commandsToRun;
	}

	@Override
	public void run() {

		SimpleMvnConsoleFactory factory = new SimpleMvnConsoleFactory();
		MessageConsole toUse = factory.getSimpleMvnConsole();
		try {
			Logic.invokeMaven(plugin,
					Logic.getFullPathToProject(workspace, currProject),
					commandsToRun, toUse.newMessageStream());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			plugin.log(Status.ERROR, "error while executing maven stuff");
			throw new RuntimeException("error processing maven",e);
		}
	}

	public static void runMvnOnNewThreadWithConsoleOut(Activator plugin,
			IWorkspace workspace, IProject currProject, String commandsToRun) {
		MvnExecutor executor = new MvnExecutor(plugin, workspace, 
				currProject, commandsToRun);
		Thread t = new Thread(executor);
		t.start();
	}
}