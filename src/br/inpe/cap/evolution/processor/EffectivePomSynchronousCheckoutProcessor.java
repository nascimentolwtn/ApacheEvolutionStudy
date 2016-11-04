package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.parser.XmlMavenParser.replaceLineFeedAndComma;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.MavenEffectivePom;
import br.inpe.cap.evolution.maven.UnparsableEffectivePomException;
import br.inpe.cap.evolution.parser.XmlMavenParser;

public class EffectivePomSynchronousCheckoutProcessor extends SynchronousCheckOutRepositoryProcessor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	/**
	 * Formato da porcentagem com "." para não confundir com a "," do CSV
	 */
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));

	private final PersistenceMechanism writer;
	private final int totalCommits;
	private int currentHashPosition;
	private float percent;
	private final VersionEvolutionDetectorPostProcessor versionEvolutionDetector = new VersionEvolutionDetectorPostProcessor();
	private final MavenEffectivePom mavenEffectivePom = new MavenEffectivePom();
	private final XmlMavenParser parser = new XmlMavenParser();
	private final Logger logger;

	public EffectivePomSynchronousCheckoutProcessor(final CheckoutObserver observer, final PersistenceMechanism writer, final int totalCommits, final Logger logger) {
		super(observer);
		this.writer = writer;
		this.totalCommits = totalCommits;
		this.logger = logger;
	}

	@Override
	protected void processFile(final SCMRepository repo, final Commit commit, final RepositoryFile file) {
		
		// CommitFilter processa apenas commits que possuem MODIFICAÇÕES com esse FileType.
		// Porém, aqui a ideia é fazer CHECKOUT todos arquivos mesmo, e então filtrar novamente para processar somente os poms.xml.
		if(isntPOMFile(file)) {
			return;
		}
		
//		Thread.currentThread().setPriority(3);
		
		final MavenProject mavenProject = parser.readPOM(getEffectiveOrOriginalPom(file));
		mavenProject.getDependencies().forEach(
			(dependency) -> 
				versionEvolutionDetector.processLine(
					writer, this.obtainCsvLine(repo.getLastDir(), commit, this.currentHashPosition, this.totalCommits, this.percent, file.getFullName(), dependency))
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

	private String obtainCsvLine(final String repositoryName, final Commit commit, final int currentHashPosition, final int totalCommits, final float percent, final String fileName, final MavenDependency mavenDependency) {
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
		sb.append(replaceLineFeedAndComma(commit.getMsg()));
		return sb.toString();
	}
	
}