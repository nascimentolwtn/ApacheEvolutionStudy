package br.inpe.cap.evolution;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import br.com.metricminer2.domain.ChangeSet;
import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.RepositoryFile;
import br.com.metricminer2.scm.SCMRepository;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.MavenEffectivePom;
import br.inpe.cap.evolution.maven.UnparsableEffectivePomException;
import br.inpe.cap.evolution.parser.XmlMavenParser;
import br.inpe.cap.evolution.processor.CheckoutObserver;
import br.inpe.cap.evolution.processor.LoggerCheckoutObserver;
import br.inpe.cap.evolution.processor.SynchronousCheckOutRepositoryProcessor;
import br.inpe.cap.evolution.processor.VersionEvolutionDetectorPostProcessor;

public class AllDependenciesEvolutionVisitor implements CommitVisitor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	/**
	 * Formato da porcentagem com "." para não confundir com a "," do CSV
	 */
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));
	
	private static Logger logger;

	private String repositoryName;
	private List<String> hashes;
	private int totalCommits; 
	
	private final XmlMavenParser parser = new XmlMavenParser();
	private final MavenEffectivePom mavenEffectivePom = new MavenEffectivePom();
	private EffectivePomSynchronousCheckoutProcessor effectivePomProcessor;

	@Override
	public void process(final SCMRepository repo, final Commit commit, final PersistenceMechanism writer) {
		this.initVisitor(repo, writer);
		
		try {
			
			final int currentHashPosition = totalCommits - hashes.indexOf(commit.getHash());
			final float percent = ((currentHashPosition*100)/(float)totalCommits);
			
			effectivePomProcessor.setCurrentHashPosition(currentHashPosition);
			effectivePomProcessor.setPercent(percent);
			effectivePomProcessor.processCommit(repo, commit);
			printPercentageMessage(currentHashPosition, percent);
			
		} catch (final IOException | InterruptedException e) {
			logger.error(e.getMessage());
		}
		
	}

	private synchronized String obtainCsvLine(final Commit commit, final int currentHashPosition, final int totalCommits,
			final float percent, final String fileName, final MavenDependency mavenDependency) {
		final StringBuilder sb = new StringBuilder();
		sb.append(commit.getHash());								sb.append(",");
		sb.append(DATE_FORMAT.format(commit.getDate().getTime()));	sb.append(",");
		sb.append(repositoryName);									sb.append(",");
		sb.append(fileName);										sb.append(",");
		sb.append(currentHashPosition);								sb.append(",");
		sb.append(totalCommits);									sb.append(",");
		sb.append(PERCENT_FORMAT.format(percent));					sb.append(",");
		sb.append(mavenDependency.isDependencyManaged());			sb.append(",");
		sb.append(mavenDependency.getGroupId());					sb.append(",");
		sb.append(mavenDependency.getArtifactId());					sb.append(",");
		sb.append(mavenDependency.getVersion());					sb.append(",");
		sb.append(XmlMavenParser.replaceLineFeedAndComma(commit.getMsg()));
		return sb.toString();
	}
	
	private void printPercentageMessage(final int currentHashPosition, final float percent) {
		final StringBuilder percentMessage = new StringBuilder(); 
		percentMessage.append(repositoryName);
		percentMessage.append(" progress: commit #");
		percentMessage.append(currentHashPosition);
		percentMessage.append("/");
		percentMessage.append(totalCommits);
		percentMessage.append(" - ");
		percentMessage.append(percent);
		percentMessage.append("%");
		System.err.println(percentMessage.toString());
	}

	private void initVisitor(final SCMRepository repo, final PersistenceMechanism writer) {
		if(this.hashes == null) {
			this.repositoryName = repo.getLastDir();
			Thread.currentThread().setName("Visitor " + this.repositoryName);
			final List<ChangeSet> changeSets = repo.getScm().getChangeSets();
			this.hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
			this.totalCommits = hashes.size();
			this.effectivePomProcessor = new EffectivePomSynchronousCheckoutProcessor(new LoggerCheckoutObserver(logger), writer, totalCommits, new VersionEvolutionDetectorPostProcessor());
			this.writeCsvHeader(writer);
		}
	}

	private void writeCsvHeader(final PersistenceMechanism writer) {
		writer.write(CommitLine.HEADER);
	}
	
	@Override
	public String name() {
		return "all-dependency_" + this.repositoryName;
	}

	public static void setLogger(final Logger logger) {
		AllDependenciesEvolutionVisitor.logger = logger;
	}
	
	private class EffectivePomSynchronousCheckoutProcessor extends SynchronousCheckOutRepositoryProcessor {
		
		private final PersistenceMechanism writer;
		private final int totalCommits;
		private int currentHashPosition;
		private float percent;
		private final VersionEvolutionDetectorPostProcessor versionEvolutionDetector;

		public EffectivePomSynchronousCheckoutProcessor(final CheckoutObserver observer, final PersistenceMechanism writer, final int totalCommits, final VersionEvolutionDetectorPostProcessor versionEvolutionDetector) {
			super(observer);
			this.writer = writer;
			this.totalCommits = totalCommits;
			this.versionEvolutionDetector = versionEvolutionDetector;
		}

		@Override
		protected void processFile(final SCMRepository repo, final Commit commit, final RepositoryFile file) {
			
			// CommitFilter processa apenas commits que possuem MODIFICAÇÕES com esse FileType.
			// Porém, aqui a ideia é fazer CHECKOUT todos arquivos mesmo, e então filtrar novamente para processar somente os poms.xml.
			if(isntPOMFile(file)) {
				return;
			}
			
			final MavenProject mavenProject = parser.readPOM(getEffectiveOrOriginalPom(file));
			mavenProject.getDependencies().forEach(
				(dependency) -> 
					versionEvolutionDetector.processLine(
						writer, obtainCsvLine(commit, this.currentHashPosition, this.totalCommits, this.percent, file.getFullName(), dependency))
				);
		}

		private String getEffectiveOrOriginalPom(final RepositoryFile file) {
			String effectivePom = "";
			try {
				effectivePom = mavenEffectivePom.resolveEffectivePom(file.getFile());
			} catch (final UnparsableEffectivePomException e) {
				logger.error("Effective POM from file " + file.getFullName()
							+ " could not be extracted. Proceeding with original POM. See result:\n"
							+ e.getMessage());
				effectivePom = getOriginalPom(file, effectivePom);
			}
			return effectivePom;
		}

		private String getOriginalPom(final RepositoryFile file, String effectivePom) {
			try {
				effectivePom = FileUtils.readFileToString(file.getFile());
			} catch (final IOException ioe) {
				logger.error("Error reading POM from file " + file.getFullName());
			}
			return effectivePom;
		}

		private boolean isntPOMFile(final RepositoryFile file) {
			return !file.fileNameEndsWith("pom.xml");
		}
		
		public void setCurrentHashPosition(final int currentHashPosition) {
			this.currentHashPosition = currentHashPosition;
		}
		
		public void setPercent(final float percent) {
			this.percent = percent;
		}
		
	}
	
}