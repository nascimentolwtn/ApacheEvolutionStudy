package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.OUTPUT;

import java.util.stream.Stream;

import org.repodriller.persistence.PersistenceMechanism;

import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.NonParseableCommitLineException;

public class RemoveUnchangedArtifactsPostProcessor {
	
	private final boolean keepInitialVersions;
	
	public RemoveUnchangedArtifactsPostProcessor(boolean keepInitialVersions) {
		this.keepInitialVersions = keepInitialVersions;
	}

	public void processCsvLines(final PersistenceMechanism writer, final Stream<String> listCsvLines) {
		writeCsvHeader(writer);
		listCsvLines.forEach((line) -> processLine(writer, line));
	}

	public void processLine(final PersistenceMechanism writer, final String line) {
		try {
			final CommitLine currentCommit = CommitLine.parseCommitLine(line, OUTPUT);
			
			if(this.keepInitialVersions && CommitLine.INITIAL_VERSION.equals(currentCommit.getPreviousVersion()) || currentCommit.versionHasChanged()) {
				writeCsvLine(writer, currentCommit);
			}
			
		} catch (NonParseableCommitLineException e) {
			// Silenced in order to remove if(removeHeader) check
		}
	}

	public void writeCsvHeader(final PersistenceMechanism writer) {
		final String[] header = CommitLine.OUTPUT_HEADER.split(",");
		writer.write(header[0], header[1], header[2], header[3],
					 header[4], header[5], header[6], header[7],
					 header[8], header[9], header[10],header[11],
					 header[12],header[13]);
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
			commitLine.getMessage()
		);
		
	}

}