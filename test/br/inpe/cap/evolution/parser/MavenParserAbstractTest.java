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
	protected static String pomProjectVariables;
	protected static String pomProjectParentVariables;
	protected static String pomProjectParentInvalidVariables;
	protected static String pom8290_startOfDependencyManagement;
		
	@BeforeClass
	public static void carregarPOMs() throws URISyntaxException, IOException {
		String pomFile = "pomBasicTest.xml";
		pom = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomNoDependencies.xml";
		pomNoDependencies = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomNoVersionDefined.xml";
		pomNoVersionDefined = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomNoPropertiesDefined.xml";
		pomNoPropertiesDefined = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomProjectVariables.xml";
		pomProjectVariables = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomProjectParentVariables.xml";
		pomProjectParentVariables = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pomProjectParentInvalidVariables.xml";
		pomProjectParentInvalidVariables = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
		pomFile = "pom.xml.8290_startOfDependencyManagement";
		pom8290_startOfDependencyManagement = FileUtils.readFileToString(new File(Resources.getResource(pomFile).toURI()));
		
	}

}