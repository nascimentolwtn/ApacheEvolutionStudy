package br.inpe.cap.evolution.maven;

import static br.inpe.cap.evolution.maven.CommitLine.ARTIFACT_ID_OUTPUT_INDEX;
import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.INPUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.inpe.cap.evolution.maven.CommitLine.CommitLineType;

public class CommitLineTest {
	
	private String header = CommitLine.OUTPUT_HEADER;
	private String line = "0e60b7d9888217be8499fe8b13c7b55e707f269c,10/05/2011 22:35:21,druid,E:\\metricminer-evolution-stars\\druid\\pom.xml,2,4890,0.04090,false,com.alibaba.external,java.servlet,2.5,Share project 'druid' into 'http://code.alibabatech.com/svn/druid'git-svn-id: http://code.alibabatech.com/svn/druid/trunk@2 b9813039-fb51-4c41-a8b9-e21c2acb5095";
	private String outputLine = "c425502f5c82f49535f9e11a7038cf3df11c29b1,31/05/2014 15:22:45,disconf,E:\\metricminer-evolution-stars\\disconf\\pom.xml,2,1154,0.17331,org.reflections,reflections,0.9.9-RC1,initial_version,false,false,first pack";
	private String lineWithoutMessage = "6a6f3d45a521518eec4d8531ec9d8534a3f17bf3,22/07/2013 08:59,Activiti,E:\\metricminer-evolution-stars\\Activiti\\modules\\activiti-bpmn-converter\\pom.xml,3989,7530,52.97477,false,org.activiti,activiti-bpmn-model,5.14-SNAPSHOT,";
	private String invalidTokenLine = "0e60b7,10/05/2011,22:35:21,druid,2,4890,0.04090,java.servlet,2.5,Share project 'druid'";
	
	@Test
	public void regularParseCommitLine() {
		CommitLine parsedCommitLine = CommitLine.parseCommitLine(line, INPUT);
		assertEquals("0e60b7d9888217be8499fe8b13c7b55e707f269c", parsedCommitLine.getHash());
		assertEquals("10/05/2011 22:35:21", parsedCommitLine.getDate());
		assertEquals("druid", parsedCommitLine.getRepository());
		assertEquals("E:\\metricminer-evolution-stars\\druid\\pom.xml", parsedCommitLine.getFile());
		assertEquals(2, parsedCommitLine.getCommitPosition());
		assertEquals(4890, parsedCommitLine.getTotalCommits());
		assertEquals(0.04090f, parsedCommitLine.getPercent(), 0.00001f);
		assertEquals(false, parsedCommitLine.isDependencyManaged());
		assertEquals("com.alibaba.external", parsedCommitLine.getGroupId());
		assertEquals("java.servlet", parsedCommitLine.getArtifactId());
		assertEquals("2.5", parsedCommitLine.getVersion());
		assertEquals("Share project 'druid' into 'http://code.alibabatech.com/svn/druid'git-svn-id: http://code.alibabatech.com/svn/druid/trunk@2 b9813039-fb51-4c41-a8b9-e21c2acb5095", parsedCommitLine.getMessage());
	}

	@Test
	public void parseCommitLineWithoutMessage() {
		CommitLine parsedCommitLine = CommitLine.parseCommitLine(lineWithoutMessage, INPUT);
		assertEquals("6a6f3d45a521518eec4d8531ec9d8534a3f17bf3", parsedCommitLine.getHash());
		assertEquals("22/07/2013 08:59", parsedCommitLine.getDate());
		assertEquals("Activiti", parsedCommitLine.getRepository());
		assertEquals("E:\\metricminer-evolution-stars\\Activiti\\modules\\activiti-bpmn-converter\\pom.xml", parsedCommitLine.getFile());
		assertEquals(3989, parsedCommitLine.getCommitPosition());
		assertEquals(7530, parsedCommitLine.getTotalCommits());
		assertEquals(52.97477f, parsedCommitLine.getPercent(), 0.00001f);
		assertEquals(false, parsedCommitLine.isDependencyManaged());
		assertEquals("org.activiti", parsedCommitLine.getGroupId());
		assertEquals("activiti-bpmn-model", parsedCommitLine.getArtifactId());
		assertEquals("5.14-SNAPSHOT", parsedCommitLine.getVersion());
		assertEquals("", parsedCommitLine.getMessage());
	}
	
	@Test
	public void parseArtifactIdForMemorySaving() {
		assertEquals("reflections", CommitLine.parseArtifactId(outputLine));
	}
	
	@Test
	public void tratamentoRemoveHeaderDeArquivoSemNenhumaLinha() {
		try {
			final List<String> emptyList = Collections.emptyList();
			assertTrue(emptyList.isEmpty());
			CommitLine.removeHeader(emptyList);
			assertTrue(emptyList.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Tratamento deveria ser realizado para evitar falha de processamento de CSV.");
		}
	}

	@Test
	public void invalidLine() {
		try {
			CommitLine.parseCommitLine(invalidTokenLine, INPUT);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			assertSame(NonParseableCommitLineException.class, e.getClass());
			assertTrue(e.getMessage().startsWith("Line cannot be parsed:"));
		}
	}
	
	@Test
	public void invalidEmptyLine() {
		try {
			CommitLine.parseCommitLine("", INPUT);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(NonParseableCommitLineException.class, e.getClass());
			assertEquals("CSV line is empty or null.", e.getMessage());
		}
	}
	
	@Test
	public void invalidNullLine() {
		try {
			CommitLine.parseCommitLine(null, INPUT);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(NonParseableCommitLineException.class, e.getClass());
			assertEquals("CSV line is empty or null.", e.getMessage());
		}
	}
	
	@Test
	public void invalidParseHeader() {
		try {
			CommitLine.parseCommitLine(header, CommitLineType.INPUT);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(NonParseableCommitLineException.class, e.getClass());
			assertEquals("Should not parse the CSV header.", e.getMessage());
		}
	}
	
	@Test
	public void invalidEmptyLineWhenParsingArtifactId() {
		try {
			CommitLine.parseArtifactId("");
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(ArrayIndexOutOfBoundsException.class, e.getClass());
			assertEquals(String.valueOf(ARTIFACT_ID_OUTPUT_INDEX), e.getMessage());
		}
	}
	
}
