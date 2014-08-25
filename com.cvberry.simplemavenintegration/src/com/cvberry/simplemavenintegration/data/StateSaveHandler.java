package com.cvberry.simplemavenintegration.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXB;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.progress.IProgressService;

import com.cvberry.simplemavenintegration.Activator;
import com.cvberry.simplemavenintegration.logic.Logic;

/**
 * an instance of this class is managed by Activator. this class is in charge of
 * managing plugin state.
 * 
 * @author vancan1ty
 */
public class StateSaveHandler {

	Activator plugin;
	final static String APP_DATA_FILE_NAME = "simplemvnstate";
	private StateObject stateObj;

	public StateSaveHandler(Activator plugin) throws IOException {
		this.plugin = plugin;
		try {
			stateObj = readAppData();
		} catch (IOException e) { 
			//couldn't read the plugin file?  let's regenerate it
			e.printStackTrace();
			stateObj = new StateObject();
			saveAppData(stateObj); //this also could throw an exception.
		}
	};

	/**
	 * checks the checksums of the tracked project POMs. If a checksum has
	 * changed, then returns the corresponding ProjectModel to indicate that the
	 * project's related data in stateObj is indeed, out of date.
	 * (eclipse:eclipse prob needs to be run if this is the case)
	 * 
	 * @return the first project model it finds which is represents an out of
	 *         date project, or null if no such project is found.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public List<ProjectModel> getOutOfDateProjectModels()
			throws NoSuchAlgorithmException, IOException {
		List<ProjectModel> out = new ArrayList<>();
		List<ProjectModel> trackedProjects = stateObj.trackedProjects;
		for (ProjectModel p : trackedProjects) {
			byte[] inMemCheckSum = p.pomCheckSum;
			Path pomLoc = Paths.get(p.projectPath, "pom.xml");
			byte[] nCheckSum = computeFileCheckSum(pomLoc);
			if (!(Arrays.equals(nCheckSum,inMemCheckSum))) {
				out.add(p);
			}
		}
		return out;
	}

	public void updateProjectModels(IProgressService progService)
			throws InvocationTargetException, InterruptedException,
			NoSuchAlgorithmException, IOException {

		List<ProjectModel> needUpdates = getOutOfDateProjectModels();

		for (ProjectModel p : needUpdates) {

			ByteArrayOutputStream theOut = new ByteArrayOutputStream();
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.setTaskName("running mvn eclipse:eclipse goal, "
							+ "which generates eclipse project files.");
					Logic.runOnMavenSameThread(Activator.getDefault(),
							"eclipse:eclipse", p.projectPath, theOut, theOut,
							null);

				}
			};

			progService.busyCursorWhile(op);
		}
	}

	public void addTrackedProject(String pomDirPath)
			throws NoSuchAlgorithmException, IOException {
		ProjectModel p = new ProjectModel();
		p.projectPath = pomDirPath;
		p.pomCheckSum = computeFileCheckSum(Paths.get(pomDirPath, "pom.xml"));
		stateObj.trackedProjects.add(p);
	}

	public void saveAppData(StateObject toSave) throws IOException {
		byte[] sObjAsBytes = marshalObjectToByteArray(stateObj);
		IPath pluginPath = plugin.getStateLocation();
		IPath nPath = pluginPath.append(APP_DATA_FILE_NAME);
		Path path = Paths.get(nPath.toString());
		Files.write(path, sObjAsBytes, StandardOpenOption.CREATE);
	}

	public StateObject readAppData() throws IOException {
		IPath pluginPath = plugin.getStateLocation();
		IPath nPath = pluginPath.append(APP_DATA_FILE_NAME);
		Path path = Paths.get(nPath.toString());
		byte[] raw = Files.readAllBytes(path);
		File equivFile = path.toFile();
		StateObject out = JAXB.unmarshal(equivFile, StateObject.class);
		return out;
	}

	public static byte[] marshalObjectToByteArray(Object toSave) {
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		JAXB.marshal(toSave, oStream);
		return oStream.toByteArray();
	}

	public static byte[] computeFileCheckSum(Path pomFile)
			throws NoSuchAlgorithmException, IOException {
		byte[] pomBytes = Files.readAllBytes(pomFile);
		MessageDigest md5Digester = MessageDigest.getInstance("MD5");
		byte[] md5Digest = md5Digester.digest(pomBytes);
		return md5Digest;
	}

	public StateObject getAppStateObject() {
		return stateObj;
	}

	public void recalculateAppStateObject() throws IOException {
		this.stateObj = this.readAppData();
	}
}
