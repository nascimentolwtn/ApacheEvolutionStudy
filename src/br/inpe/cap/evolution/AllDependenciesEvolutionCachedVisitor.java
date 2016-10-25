package br.inpe.cap.evolution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import br.com.metricminer2.domain.ChangeSet;
import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.SCMRepository;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.parser.XmlMavenParser;
import br.inpe.cap.evolution.processor.LoggerCheckoutObserver;
import br.inpe.cap.evolution.processor.AsynchronousCheckOutFileRepositoryProcessor;

public class AllDependenciesEvolutionCachedVisitor implements CommitVisitor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	/**
	 * Formato da porcentagem com "." para não confundir com a "," do CSV
	 */
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));
	
	private static Logger logger;

	private String repositoryName;
	private List<String> hashes;
	
	public XmlMavenParser parser = new XmlMavenParser();

	@Override
	public void process(final SCMRepository repo, final Commit commit, final PersistenceMechanism writer) {
		this.initHashes(repo);
		
		final int totalCommits = hashes.size();
		final int currentHashPosition = totalCommits - hashes.indexOf(commit.getHash());
		
		final float percent = ((currentHashPosition*100)/(float)totalCommits);
		
		try {
			
			AsynchronousCheckOutFileRepositoryProcessor processor = new AsynchronousCheckOutFileRepositoryProcessor(
				new LoggerCheckoutObserver(logger)
			){
			
			@Override
			protected void processFile(final SCMRepository repo, final Commit commit, final String fileName, final ByteArrayInputStream file) {
				
				// CommitFilter processa apenas commits que possuem MODIFICAÇÕES com esse FileType.
				// Porém, aqui a ideia é fazer CHECKOUT todos arquivos mesmo, e então filtrar novamente para processar somente os poms.xml.
				if(isntPOM(fileName)) {
					return;
				}
				
				try {
					final String sourceCode = new String(IOUtils.toCharArray(file));
					final MavenProject pom = parser.readPOM(sourceCode);
					pom.getDependencies().forEach(
						(dependency) -> 
							writeCsvLine(writer, commit, currentHashPosition, totalCommits, percent, fileName, dependency)
						);
				} catch (final IOException e) {
					logger.error(e.getMessage());
				}
			}

			private boolean isntPOM(final String fileName) {
				return !fileName.endsWith("pom.xml");
			}};
		
			processor.processCommit(repo, commit);
			final String percentMessage = repositoryName
					+ " progress: commit #"
					+ currentHashPosition
					+ "/"
					+ totalCommits
					+ " - "
					+ percent
					+ "%";
			System.err.println(percentMessage);
		} catch (final IOException e) {
			logger.error(e.getMessage());
		}
		
	}

	private void writeCsvLine(final PersistenceMechanism writer, final Commit commit, final int currentHashPosition, final int totalCommits,
			final float percent, final String fileName, final MavenDependency mavenDependency) {
		writer.write(
				commit.getHash(),
				DATE_FORMAT.format(commit.getDate().getTime()),
				repositoryName,
				fileName,
				currentHashPosition,
				totalCommits,
				PERCENT_FORMAT.format(percent),
				mavenDependency.getGroupId(),
				mavenDependency.getArtifactId(),
				mavenDependency.getVersion(),
				XmlMavenParser.replaceLineFeedAndComma(commit.getMsg())
		);
	}

	private void initHashes(final SCMRepository repo) {
		if(this.hashes == null) {
			repositoryName = repo.getLastDir();
			final List<ChangeSet> changeSets = repo.getScm().getChangeSets();
			hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
			Thread.currentThread().setName("Visitor " + this.repositoryName);
		}
	}

	@Override
	public String name() {
		return "all-dependency_" + this.repositoryName;
	}

	public static void setLogger(final Logger logger) {
		AllDependenciesEvolutionCachedVisitor.logger = logger;
	}
	
}