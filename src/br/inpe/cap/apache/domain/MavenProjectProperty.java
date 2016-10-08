package br.inpe.cap.apache.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("property")
public class MavenProjectProperty implements Comparable<MavenProjectProperty> {
	
	private String name;
	private String value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(MavenProjectProperty o) {
		return this.name.compareTo(o.name);
	}
		
}
