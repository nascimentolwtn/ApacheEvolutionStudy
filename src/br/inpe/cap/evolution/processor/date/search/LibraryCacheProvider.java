package br.inpe.cap.evolution.processor.date.search;

import static br.inpe.cap.evolution.processor.date.search.MavenCentralSearch.getAllVersions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import br.inpe.cap.evolution.processor.date.domain.Library;

public class LibraryCacheProvider {

	private static final String TEMP_DIRECTORY = FileUtils.getTempDirectory().getAbsolutePath() + 
												 File.separator + "MavenCentralSearch" + File.separator;
	
	private Map<String, Library> libraryCache = new HashMap<>();
	
	private boolean updateLibraryOnDisk;
	
	public LibraryCacheProvider(boolean updateLibraryOnDisk) {
		File tempFile = new File(TEMP_DIRECTORY);
		tempFile.mkdir();
		this.updateLibraryOnDisk = updateLibraryOnDisk;
	}

	private void updateDiskCache(String key, Library library) {
		try {
			File tempFile = new File(TEMP_DIRECTORY + key);
			tempFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(library);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized Library get(String groupId, String artifactId) {
		String key = groupId + "_" + artifactId;
		Library library = this.libraryCache.get(key);
		if(library == null) {
			
			File tempFile = new File(TEMP_DIRECTORY + key);
			if(tempFile.exists() && !updateLibraryOnDisk) {
				try {
					FileInputStream fis = new FileInputStream(tempFile);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					library = (Library) ois.readObject();
					ois.close();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			} else {
				library = new Library(groupId, artifactId, getAllVersions(groupId, artifactId));
				this.updateDiskCache(key, library);
			}
			
			this.libraryCache.put(key, library);
			
		}
		
		return library;
	}

}
