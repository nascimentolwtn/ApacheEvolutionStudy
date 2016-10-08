package br.inpe.cap.apache.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MavenProjectPropertyConverter implements Converter {

	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
		return ArrayList.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		throw new RuntimeException("Writing maven pom.xml not implemented. Only reading is available.");
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		List<MavenProjectProperty> properties = new ArrayList<>();
		while(reader.hasMoreChildren()) {
			reader.moveDown();

			MavenProjectProperty mavenProperty = new MavenProjectProperty();
			mavenProperty.setName(reader.getNodeName());
			mavenProperty.setValue(reader.getValue());
			properties.add(mavenProperty);
			
			reader.moveUp();
		}
		
		Collections.sort(properties);
		return properties;
	}

}
