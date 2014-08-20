package com.cvberry.simplemavenintegration.runners;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.logic.Logic;

public class MvnExecutor2 implements Runnable {
	
	/**
	 * used for logging.
	 */
	private Activator plugin;

	String commandsToRun;
	String pathToRunAt;
	OutputStream stdOut;
	OutputStream stdErr;
	Consumer<Integer> functionToCallOnCompletion;

	public MvnExecutor2(Activator plugin, 
			String commandsToRun, String pathToRunAt, OutputStream stdOut,
			OutputStream stdErr, Consumer<Integer> functionToCallOnCompletion) {
		this.plugin = plugin;
		this.commandsToRun = commandsToRun;
		this.pathToRunAt = pathToRunAt;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
		this.functionToCallOnCompletion = functionToCallOnCompletion;
	}

	@Override
	public void run() {

		try {
			int resultCode = Logic.invokeMaven(plugin, pathToRunAt, commandsToRun, stdOut);
			//run this ui update on the 
			Display.getDefault().asyncExec(() -> {
				functionToCallOnCompletion.accept(resultCode);
			});

		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			plugin.log(Status.ERROR, "error while executing maven stuff");
			Display.getDefault().asyncExec(() -> {
				functionToCallOnCompletion.accept(1);
			});
			throw new RuntimeException("error processing maven",e); 
		}
	}
}
