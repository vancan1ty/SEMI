package com.cvberry.simplemavenintegration.console;

import java.io.OutputStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class SimpleMvnConsoleFactory implements IConsoleFactory {
	String CONSOLE_NAME = "SimpleMvnConsole";

	@Override
	public void openConsole() {
		MessageConsole myConsole = findConsole(CONSOLE_NAME);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println("if you run maven commands from the SimpleMvn view, "
				+ "output will show up here!");
	}
	
	public MessageConsole getSimpleMvnConsole() {
		MessageConsole myConsole = findConsole(CONSOLE_NAME);
		return myConsole;
	}

   private MessageConsole findConsole(String name) {
      ConsolePlugin plugin = ConsolePlugin.getDefault();
      IConsoleManager conMan = plugin.getConsoleManager();
      IConsole[] existing = conMan.getConsoles();
      for (int i = 0; i < existing.length; i++) {
         if (name.equals(existing[i].getName())) {
            return (MessageConsole) existing[i];
         }
      }
      //no console found, so create a new one
      MessageConsole myConsole = new MessageConsole(name, null);
      conMan.addConsoles(new IConsole[]{myConsole});
      return myConsole;
   }

}