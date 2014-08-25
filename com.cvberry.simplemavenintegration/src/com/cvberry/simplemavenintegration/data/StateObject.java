package com.cvberry.simplemavenintegration.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StateObject {
	public StateObject() {
		trackedProjects = new ArrayList<>();
	}
	public final int SCHEMA_VERSION=1;

	public List<ProjectModel> trackedProjects;
	
}
