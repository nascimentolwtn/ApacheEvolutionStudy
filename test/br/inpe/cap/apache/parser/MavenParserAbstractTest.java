package br.inpe.cap.apache.parser;

public abstract class MavenParserAbstractTest {

	protected String pom;
	
	public MavenParserAbstractTest() {
		StringBuilder sb = new StringBuilder();
		sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n");
		sb.append("	<modelVersion>4.0.0</modelVersion>\n");
		sb.append("	<groupId>br.usp</groupId>\n");
		sb.append("	<artifactId>metricminer</artifactId>\n");
		sb.append("	<packaging>jar</packaging>\n");
		sb.append("	<version>2.4.2-SNAPSHOT</version>\n");
		sb.append("	<name>metricminer2</name>\n");
		sb.append("	<url>http://www.metricminer.org.br</url>\n");
		sb.append("	<description>Framework for researchers in MSR</description>\n");
		sb.append("\n");
		sb.append("	<dependencies>\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.eclipse.tycho</groupId>\n");
		sb.append("			<artifactId>org.eclipse.jdt.core</artifactId>\n");
		sb.append("			<version>3.10.0.v20140604-1726</version>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.eclipse.tycho</groupId>\n");
		sb.append("			<artifactId>org.eclipse.jdt.core</artifactId>\n");
		sb.append("			<version>3.10.0.v20140604-1726</version>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.apache.commons</groupId>\n");
		sb.append("			<artifactId>commons-lang3</artifactId>\n");
//		sb.append("			<version>3.3.2</version>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.apache.velocity</groupId>\n");
		sb.append("			<artifactId>velocity-tools</artifactId>\n");
		sb.append("			<scope>provided</scope>\n");
		sb.append("			<optional>true</optional>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("	</dependencies>\n");
		sb.append("\n");
		sb.append("	<build>\n");
		sb.append("		<plugins>\n");
		sb.append("			<plugin>\n");
		sb.append("				<groupId>org.apache.felix</groupId>\n");
		sb.append("				<artifactId>maven-bundle-plugin</artifactId>\n");
		sb.append("			</plugin>\n");
		sb.append("\n");
		sb.append("			<plugin>\n");
		sb.append("				<groupId>org.apache.felix</groupId>\n");
		sb.append("				<artifactId>maven-bundle-plugin</artifactId>\n");
		sb.append("				<version>2.10.1</version>\n");
		sb.append("			</plugin>\n");
		sb.append("		</plugins>\n");
		sb.append("	</build>\n");
		sb.append("\n");
		sb.append("</project>\n");
                
		pom = sb.toString();
	}

}