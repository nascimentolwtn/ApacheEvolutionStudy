package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.parser.XmlMavenParser;

public class AllDependenciesPostProcessor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public XmlMavenParser parser = new XmlMavenParser();
	
	public static void main(String[] args) throws IOException {
		System.out.println("Starting...");

		AllDependenciesPostProcessor postProcessor = new AllDependenciesPostProcessor();
		
		CSVFile csv = new CSVFile("study" + File.separator + "effective_pom.csv", true);
		postProcessor.writeCsvHeader(csv);

		File disconfiDir = new File("C:\\Users\\VANT\\workspace\\disconf-poms");
		File[] listFiles = disconfiDir.listFiles();
		postProcessor.processDirectory(csv, listFiles);
		
		System.out.println("Finish!");
	}

	private void processDirectory(CSVFile csv, File[] listFiles) throws IOException {
		for (File file : listFiles) {
			if(file.isDirectory()) {
				this.processDirectory(csv, file.listFiles());
			}
			if(file.getName().startsWith("_")) {
				this.process(file, csv);
			}
		}
	}

	public void process(File pomFile, PersistenceMechanism writer) throws IOException {
		
//		int totalCommits = hashes.size();
		int currentHashPosition = Integer.valueOf(pomFile.getName().substring(pomFile.getName().lastIndexOf(".")+1));
//		int currentHashPosition = 1;
		
//		float percent = 100 - ((currentHashPosition*100)/(float)totalCommits);
		
		List<MavenDependency> previousDependencies = new ArrayList<>();
		
		String sourceCode = FileUtils.readFileToString(pomFile);
		MavenProject pom = parser.readPOM(sourceCode);
		
		List<MavenDependency> currentDependencies = pom.getDependencies();
		List<MavenDependency> dependencyManagedDependencies = pom.getDependencyManagement().getDependencies();
		currentDependencies.addAll(dependencyManagedDependencies);

		currentDependencies.forEach(
			(dependency) -> 
				writeCsvLine(writer, currentHashPosition, pomFile , dependency)
			);
		
		dependencyManagedDependencies.forEach(
			(dependency) -> 
				writeCsvLine(writer, currentHashPosition, pomFile , dependency)
			);
			
		
	}

	private void writeCsvHeader(PersistenceMechanism writer) {
		writer.write(
//				"DATE",
				"FILE",
				"COMMIT_POSISTION",
				"IS_DependencyManeged",
				"GROUP_ID",
				"ARTIFACT_ID",
				"VERSION"
		);
	}
	
	private void writeCsvLine(PersistenceMechanism writer, int currentHashPosition, 
							  File pomFile, MavenDependency mavenDependency) {
		
		try {
			Path pomPath = Paths.get(pomFile.toURI());
			BasicFileAttributes attr = Files.readAttributes(pomPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			writer.write(
					// não faz sentido gravar dessa forma a data de criação, pois todos foram "criados" novamento no momento do checkou pelo método FileUtils.writeStringToFile
//					DATE_FORMAT.format(new Date(attr.creationTime().toMillis())), 
					pomFile.getAbsolutePath(),
					currentHashPosition,
					mavenDependency.isDependencyManaged(),
					mavenDependency.getGroupId(),
					mavenDependency.getArtifactId(),
					mavenDependency.getVersion()
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}