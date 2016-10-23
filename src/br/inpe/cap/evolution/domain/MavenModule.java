package br.inpe.cap.evolution.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class MavenModule {
	
	@XStreamImplicit(itemFieldName="module")
	private List<String> modules = new ArrayList<>();
	
	public List<String> getModules() {
		return modules;
	}

}
