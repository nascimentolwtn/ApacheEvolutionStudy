package br.inpe.cap.evolution.processor.date.search;

import static br.inpe.cap.evolution.processor.date.search.MavenCentralSearch.DATE_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import br.inpe.cap.evolution.processor.date.domain.Version;
import br.inpe.cap.evolution.processor.date.exception.VersionNotFoundException;

public class MavenCentralSearchTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testaMontaRequest() {
		String montaRequest = MavenCentralSearch.montaRequest("org.apache.logging.log4j", "log4j");
		assertEquals("/solrsearch/select?q=g:org.apache.logging.log4j+AND+a:log4j&rows=200&wt=json&core=gav", montaRequest);
	}

	@Test
	public void testGetAllVersionsOfLibraryLog4J() {
		Set<Version> versions = MavenCentralSearch.getLibrary("org.apache.logging.log4j", "log4j").getVersions();
		
		assertTrue(versions.toString().contains("2.0-alpha1 @ 1343589101000, 2.0-alpha2 @ 1345840804000, 2.0-beta1 @ 1347976681000, "
				+ "2.0-beta2 @ 1349636944000, 2.0-beta3 @ 1352650925000, 2.0-beta4 @ 1359359106000, 2.0-beta5 @ 1366492850000, "
				+ "2.0-beta6 @ 1367891898000, 2.0-beta7 @ 1370362337000, 2.0-beta8 @ 1373490873000, 2.0-beta9 @ 1379168197000, "
				+ "2.0-rc1 @ 1391972631000, 2.0-rc2 @ 1403393436000, 2.0 @ 1405207554000, 2.0.1 @ 1406681590000, 2.0.2 @ 1408253019000, "
				+ "2.1 @ 1413769113000, 2.2 @ 1424643417000, 2.3 @ 1431292358000, 2.4 @ 1442768309000, 2.4.1 @ 1444351792000, "
				+ "2.5 @ 1449447524000, 2.6 @ 1464188035000, 2.6.1 @ 1465173144000, 2.6.2 @ 1467772580000, 2.7 @ 1475432042000, 2.8 @ 1485061690000" 
				));
	}
	
	@Test
	public void testGetDateOfLibraryVersion() {
		assertEquals("25/05/2016 11:53:55", MavenCentralSearch.getLibraryVersionReleaseDate("org.apache.logging.log4j", "log4j", "2.6"));
	}

	@Test
	public void testGetVersionOfLibraryOnDate() {
		String myDate = "26/05/2016 00:00:00";
		String version = MavenCentralSearch.getLibraryVersionOnCommitDate("org.apache.logging.log4j", "log4j", myDate);
		assertEquals("2.6", version);

		myDate = "26/12/2016 00:00:00";
		version = MavenCentralSearch.getLibraryVersionOnCommitDate("org.apache.logging.log4j", "log4j", myDate);
		assertEquals("2.7", version);
	}

	@Test
	public void testGetVersionOfLibraryOnDateAsString() throws ParseException {
		String version = MavenCentralSearch.getLibraryVersionOnCommitDate("org.apache.logging.log4j", "log4j", "20/11/2016 11:53:55");
		assertEquals("2.7", version);

		version = MavenCentralSearch.getLibraryVersionOnCommitDate("org.apache.logging.log4j", "log4j", "26/05/2016 11:53:55");
		assertEquals("2.6", version);
	}

	@Test
	public void testInvalidVersionNumber() throws ParseException{
		final String releaseDate = MavenCentralSearch.getLibraryVersionReleaseDate("org.apache.logging.log4j", "log4j","2.6.3");
		assertEquals(DATE_NOT_FOUND, releaseDate);
	}

	@Test
	public void testGetAllVersionsOfLibraryJUnit() {
		Set<Version> versions = MavenCentralSearch.getLibrary("junit", "junit").getVersions();

		assertTrue(versions.toString().contains("[4.0 @ 1140290641000, 4.1 @ 1160134625000, 4.2 @ 1170373650000, 4.3.1 @ 1179178447000, "
				+ "3.8.1 @ 1179178458000, 3.8.2 @ 1179178493000, 3.8 @ 1179178521000, 3.7 @ 1179178534000, 4.3 @ 1179178561000, "
				+ "4.4 @ 1187718463000, 4.5 @ 1220534968000, 4.6 @ 1241897886000, 4.7 @ 1250092447000, 4.8.1 @ 1267100518000, "
				+ "4.8 @ 1286268656000, 4.8.2 @ 1286268687000, 4.9 @ 1314036946000, 4.10 @ 1317323543000, 4.11-beta-1 @ 1350330353000, "
				+ "4.11 @ 1352920907000, 4.12-beta-1 @ 1406493687000, 4.12-beta-2 @ 1411624481000, 4.12-beta-3 @ 1415548414000, 4.12 @ 1417709863000]" 
				));
	}
	
	@Test
	public void testGetVersionOfLibraryJUnitOnDateAsString() throws ParseException {
		String version = MavenCentralSearch.getLibraryVersionOnCommitDate("junit", "junit", "04/01/2008 23:21:20");
		assertEquals("4.4", version);

		version = MavenCentralSearch.getLibraryVersionOnCommitDate("junit", "junit", "09/11/2011 13:00:36");
		assertEquals("4.10", version);
	}
	
	@Test
	public void testaInvalidRequest() {
		Set<Version> versions = MavenCentralSearch.getLibrary("${groupId}", "hibernate-core").getVersions();
		assertEquals(0, versions.size());

		versions = MavenCentralSearch.getLibrary("??andbackslash/", "lib??and\\slash").getVersions();
		assertEquals(0, versions.size());

		versions = MavenCentralSearch.getLibrary("groupId", "cifrao_${").getVersions();
		assertEquals(0, versions.size());
	}

	@Test
	public void testaEmptyArtifactIdRequest() {
		Set<Version> versions = MavenCentralSearch.getLibrary("junit", "").getVersions();
		assertEquals(0, versions.size());
	}

//	@Test
	public void testExceptionInvalidVersionNumber() throws ParseException {
		exception.expect(VersionNotFoundException.class);
		exception.expectMessage("There isn't g:org.apache.logging.log4j a:log4jin version 2.6.3 available in maven central.");
		MavenCentralSearch.getLibraryVersionReleaseDate("org.apache.logging.log4j", "log4j","2.6.3");
	}

}
