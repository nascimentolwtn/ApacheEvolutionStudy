package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.parser.XmlMavenParser.replaceLineFeedAndComma;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.ConfigurableMavenEffectivePom;
import br.inpe.cap.evolution.maven.UnparsableEffectivePomException;
import br.inpe.cap.evolution.parser.XmlMavenParser;

public class EffectivePomSynchronousCheckoutOnlyProcessor extends SynchronousCheckOutOnlyRepositoryProcessor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private final PersistenceMechanism writer;
	private final int totalCommits;
	private int currentHashPosition;
	private float percent;
	private final VersionEvolutionDetectorPostProcessor versionEvolutionDetector = new VersionEvolutionDetectorPostProcessor();
	private final ConfigurableMavenEffectivePom configurableMavenEffectivePom;
	private final XmlMavenParser parser = new XmlMavenParser();
	private final Logger logger;

	public EffectivePomSynchronousCheckoutOnlyProcessor(final CheckoutObserver observer, final PersistenceMechanism writer, final int totalCommits, final Logger logger, final ConfigurableMavenEffectivePom configurableMavenEffectivePom) {
		super(observer);
		this.writer = writer;
		this.totalCommits = totalCommits;
		this.logger = logger;
		this.versionEvolutionDetector.writeCsvHeader(writer);
		this.configurableMavenEffectivePom = configurableMavenEffectivePom;
	}

	@Override
	protected void processFile(final SCMRepository repo, final Commit commit, final RepositoryFile file) {
//		setupCurrentThread(file.getFullName(), repo.getLastDir());
		final MavenProject mavenProject = parser.readPOM(getEffectiveOrOriginalPom(file));
		for (MavenDependency dependency : mavenProject.getDependencies()) {
			versionEvolutionDetector.processLine(
				writer, this.obtainCsvLine(repo.getLastDir(), commit, this.currentHashPosition, this.totalCommits, this.percent, file.getFullName(), dependency));
		}
	}

	private void setupCurrentThread(final String fileFullName, final String repositoryName) {
		try {
			final Thread currentThread = Thread.currentThread();
			currentThread.setName(fileFullName.substring(fileFullName.indexOf(repositoryName)));
			currentThread.setPriority(Thread.MIN_PRIORITY);
//			Thread.sleep(TimeUnit.SECONDS.toMillis(10));
		} catch (Exception e) {}
	}

	private String getEffectiveOrOriginalPom(final RepositoryFile file) {
		String effectivePom = "";
		try {
			effectivePom = configurableMavenEffectivePom.resolveEffectivePom(file.getFile());
		} catch (final UnparsableEffectivePomException e) {
			logger.error("Effective POM from file " + file.getFullName()
						+ " could not be extracted. Proceeding with original POM."
//						+ " See result:\n"
//						+ e.getMessage()
						);
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

	public void setCurrentHashPosition(final int currentHashPosition) {
		this.currentHashPosition = currentHashPosition;
	}
	
	public void setPercent(final float percent) {
		this.percent = percent;
	}

	private String obtainCsvLine(final String repositoryName, final Commit commit, final int currentHashPosition, final int totalCommits, final float percent, final String fileName, final MavenDependency mavenDependency) {
		final StringBuilder sb = new StringBuilder();
		sb.append(commit.getHash());								sb.append(",");
		sb.append(DATE_FORMAT.format(commit.getDate().getTime()));	sb.append(",");
		sb.append(repositoryName);									sb.append(",");
		sb.append(fileName);										sb.append(",");
		sb.append(currentHashPosition);								sb.append(",");
		sb.append(totalCommits);									sb.append(",");
		sb.append(CommitLine.roundFiveDigits(percent));				sb.append(",");
		sb.append(mavenDependency.isDependencyManaged());			sb.append(",");
		sb.append(mavenDependency.getGroupId());					sb.append(",");
		sb.append(mavenDependency.getArtifactId());					sb.append(",");
		sb.append(mavenDependency.getVersion());					sb.append(",");
		sb.append(replaceLineFeedAndComma(commit.getMsg()));
		return sb.toString();
	}
	
}