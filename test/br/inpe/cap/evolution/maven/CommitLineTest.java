package br.inpe.cap.evolution.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

public class CommitLineTest {
	
	private String header = CommitLine.HEADER;
	private String line = "0e60b7d9888217be8499fe8b13c7b55e707f269c,10/05/2011 22:35:21,druid,E:\\metricminer-evolution-stars\\druid\\pom.xml,2,4890,0.04090,false,com.alibaba.external,java.servlet,2.5,Share project 'druid' into 'http://code.alibabatech.com/svn/druid'git-svn-id: http://code.alibabatech.com/svn/druid/trunk@2 b9813039-fb51-4c41-a8b9-e21c2acb5095";
	private String invalidTokenLine = "0e60b7,10/05/2011,22:35:21,druid,2,4890,0.04090,java.servlet,2.5,Share project 'druid'";
	
	@Test
	public void regularParseCommitLine() {
		CommitLine parsedCommitLine = CommitLine.parseCommitLine(line);
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
	public void invalidLine() {
		try {
			CommitLine.parseCommitLine(invalidTokenLine);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(RuntimeException.class, e.getClass());
			assertEquals("Line cannot be parsed.", e.getMessage());
		}
	}
	
	@Test
	public void invalidEmptyLine() {
		try {
			CommitLine.parseCommitLine("");
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(RuntimeException.class, e.getClass());
			assertEquals("CSV line is empty or null.", e.getMessage());
		}
	}
	
	@Test
	public void invalidNullLine() {
		try {
			CommitLine.parseCommitLine(null);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(RuntimeException.class, e.getClass());
			assertEquals("CSV line is empty or null.", e.getMessage());
		}
	}
	
	@Test
	public void invalidParseHeader() {
		try {
			CommitLine.parseCommitLine(header);
			fail("Shoud throw Unparse Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(RuntimeException.class, e.getClass());
			assertEquals("Should not parse the CSV header.", e.getMessage());
		}
	}
	
}
