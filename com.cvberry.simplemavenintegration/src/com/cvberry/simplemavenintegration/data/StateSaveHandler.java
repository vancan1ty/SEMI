package com.cvberry.simplemavenintegration.data;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXB;

import org.eclipse.core.runtime.IPath;

import com.cvberry.simplemavenintegration.Activator;

public class StateSaveHandler {
	
	public static void saveAppData(Activator plugin, StateObject toSave) 
			throws IOException {
		byte[] sObjAsBytes = marshalObjectToByteArray(toSave);
		storeData(plugin,"simplemvnstate", sObjAsBytes);
	}
	
	public static byte[] marshalObjectToByteArray(Object toSave) {
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		JAXB.marshal(toSave,oStream);
		return oStream.toByteArray();
	}
	
	public static byte[] computeFileCheckSum(Activator plugin, Path pomFile) 
			throws NoSuchAlgorithmException, IOException {
		byte[] pomBytes = Files.readAllBytes(pomFile);
		MessageDigest md5Digester = MessageDigest.getInstance("MD5");
		byte[] md5Digest = md5Digester.digest(pomBytes);
		return md5Digest;
	}
	
	public static void storeData(Activator plugin, String label, byte[] data) 
			throws IOException {
		IPath pluginPath = plugin.getStateLocation();
		IPath nPath = pluginPath.append(label);
		Path path = Paths.get(nPath.toString());
		Files.write(path, data, StandardOpenOption.CREATE);
	}
	

}
