package br.inpe.cap.evolution.parser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;

import com.google.common.io.Resources;

public abstract class MavenParserAbstractTest {

	protected static String pom;
	protected static String pomNoDependencies;
	protected static String pomNoVersionDefined;
	protected static String pomNoPropertiesDefined;
	
	@BeforeClass
	public static void readPOMs() throws URISyntaxException, IOException {
		String pomFile = "pomBasicTest.xml";
		pom = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomNoDependencies.xml";
		pomNoDependencies = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomNoVersionDefined.xml";
		pomNoVersionDefined = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomNoPropertiesDefined.xml";
		pomNoPropertiesDefined = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
	}


}