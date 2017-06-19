package br.inpe.cap.evolution.processor.date;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.OUTPUT;

import java.util.stream.Stream;

import org.repodriller.persistence.PersistenceMechanism;

import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.CommitLine.CommitLineType;
import br.inpe.cap.evolution.maven.NonParseableCommitLineException;
import br.inpe.cap.evolution.processor.date.search.MavenCentralSearch;

public class MavenCentralSearchPostProcessor {
	
	public void processCsvLines(final PersistenceMechanism writer, final Stream<String> listCsvLines) {
		processCsvLines(writer, listCsvLines, OUTPUT);
	}

	public void processCsvLines(final PersistenceMechanism writer, final Stream<String> listCsvLines, final CommitLineType commitLineType) {
		writeCsvHeader(writer);
		listCsvLines.forEach((line) -> processLine(writer, line, commitLineType));
	}

	public synchronized void processLine(final PersistenceMechanism writer, final String line, final CommitLineType commitLineType) {
		try {
			final CommitLine currentCommit = CommitLine.parseCommitLine(line, commitLineType);
			
			final String groupId = currentCommit.getGroupId();
			final String artifactId = currentCommit.getArtifactId();
			final String commitDate = currentCommit.getDate();
			final String commitArtifactVersion = currentCommit.getVersion();
			
			final String releaseDateOfLibraryVersion = MavenCentralSearch.getLibraryVersionReleaseDate(groupId, artifactId, commitArtifactVersion);
			final String newerVersionOnCommitDate = MavenCentralSearch.getLibraryVersionOnCommitDate(groupId, artifactId, commitDate);
			
			currentCommit.setVersionReleaseDate(releaseDateOfLibraryVersion);
			currentCommit.setNewerVersionOnCommitDate(newerVersionOnCommitDate);
			
			writeCsvLine(writer, currentCommit);
		} catch (NonParseableCommitLineException e) {
			// Silenced in order to remove if(removeHeader) check
		}
	}

	public void writeCsvHeader(final PersistenceMechanism writer) {
		final String[] header = CommitLine.OUTPUT_DATESEARCH_HEADER.split(",");
		writer.write(header[0], header[1], header[2], header[3],
					 header[4], header[5], header[6], header[7],
					 header[8], header[9], header[10],header[11],
					 header[12],header[13],header[14],header[15],header[16]);
	}
	
	private void writeCsvLine(final PersistenceMechanism writer, final CommitLine commitLine) {
		writer.write(
			commitLine.getHash(),
			commitLine.getDate(),
			commitLine.getRepository(),
			commitLine.getFile(),
			commitLine.getCommitPosition(),
			commitLine.getTotalCommits(),
			commitLine.getPercent(),
			commitLine.getGroupId(),
			commitLine.getArtifactId(),
			commitLine.getVersion(),
			commitLine.getPreviousVersion(),
			commitLine.versionHasChanged(),
			commitLine.versionHasEverChanged(),
			commitLine.getVersionReleaseDate(),
			commitLine.getNewerVersionOnCommitDate(),
			commitLine.isUsingNewestVersion(),
			commitLine.getMessage()
		);
		
	}

}