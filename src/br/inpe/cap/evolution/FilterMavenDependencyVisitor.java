package br.inpe.cap.evolution;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.SCMRepository;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.parser.XmlMavenParser;

public class FilterMavenDependencyVisitor implements CommitVisitor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private File pomDir;
	
	public XmlMavenParser parser = new XmlMavenParser();

	private String repositoryName;

	public FilterMavenDependencyVisitor(String evolutionLogPath, String gitReposLogSubDir) {
		this.repositoryName = gitReposLogSubDir;
		evolutionLogPath = "..\\..\\..\\..\\..\\" + evolutionLogPath; // comente esta linha para manter pom's dentro do diretório do estudo
		this.pomDir = new File(evolutionLogPath + File.separator + gitReposLogSubDir);
	}

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {

		repo.getScm().files().stream()
			.filter(
				(file) -> file.fileNameEndsWith("pom.xml"))
			.forEach( 
				(file) ->	{
					String sourceCode = file.getSourceCode();
					MavenProject pom = parser.readPOM(sourceCode);
					pom.getDependencies().forEach(
						(dependency) -> 
							writeCsvLine(writer, commit, file.getFullName(), dependency)
						);

					try {
						boolean writenUrl = false;
						if(!writenUrl) {
							FileUtils.writeStringToFile(new File("fountain" + File.separator + "stars-maven.urls"), repo.getOrigin() + "\n", true);
							writenUrl = true;
						}
						
						String pomFileName = pomDir.getAbsolutePath() + File.separator + file.getFile().getName();
						FileUtils.writeStringToFile(new File(pomFileName), sourceCode);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		);
	
	}

	private void writeCsvLine(PersistenceMechanism writer, Commit commit, String fileName, MavenDependency mavenDependency) {
		writer.write(
				commit.getHash(),
				DATE_FORMAT.format(commit.getDate().getTime()),
				this.repositoryName,
				fileName,
				mavenDependency.getGroupId(),
				mavenDependency.getArtifactId(),
				mavenDependency.getVersion(),
				replaceLineFeedAndComma(commit.getMsg())
		);
	}

	private String replaceLineFeedAndComma(String commitMessage) {
		return commitMessage.replaceAll("[\\r\\n;]+", "");
	}

	@Override
	public String name() {
		return "dependency-filter_" + this.repositoryName;
	}

}