package com.cvberry.simplemavenintegration.data;

/**
 * this class is intended to model all the data SimpleMvnIntegration maintained
 * for a given project.
 * 
 * @author vancan1ty
 *
 */
public class ProjectModel {
	
	public ProjectModel() {}
	
	public ProjectModel(String projectPath, String pomCheckSum) {
		this.projectPath = projectPath;
		this.pomCheckSum = pomCheckSum;
	}

	public String projectPath;
	public String pomCheckSum;

}
