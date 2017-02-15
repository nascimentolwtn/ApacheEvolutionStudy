package br.inpe.cap.evolution.processor.date.search;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import br.inpe.cap.evolution.processor.date.Library;
import br.inpe.cap.evolution.processor.date.Version;
import br.inpe.cap.evolution.processor.date.exception.VersionNotFoundException;

public class MavenCentralSearch {

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final HttpHost target = new HttpHost("search.maven.org", 80, "http");
	private static final HttpGet getRequest = new HttpGet();

	private static Map<String, Library> libraryCache = new HashMap<>();
	private static int cacheHit = 0;

	static Library getLibrary(String groupId, String artifactId) {
		final String key = groupId + "&" + artifactId;
		Library library = libraryCache.get(key);
		if(library == null) {
			library = new Library(groupId, artifactId, getAllVersions(groupId, artifactId));
			libraryCache.put(key, library);
		} else {
//			System.err.println("Cache hit #" + ++cacheHit + "! size:" + libraryCache.size());
		}
		return library;
	}

	private static Set<Version> getAllVersions(String groupId, String artifactId){
		Set<Version> versions = new TreeSet<Version>();
		JSONArray itensJSON = getJsonArrayResponseWithRest(groupId, artifactId);

		for (int i = 0; i < itensJSON.length(); i++) {
			final JSONObject libJSON = itensJSON.getJSONObject(i);
			final String version = libJSON.getString("v");
			final long date = libJSON.getLong("timestamp");
			versions.add(new Version(date, version));
		}
		
		return versions;
	}

	static String montaRequest(String groupId, String artifactId){
		return "/solrsearch/select?q=g:" + groupId +
				"+AND+a:" + artifactId +
				"&rows=200&wt=json&core=gav";
	}

	static JSONArray getJsonArrayResponseWithRest(String groupId, String artifactId){
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			getRequest.setURI(new URI(montaRequest(groupId, artifactId)));
			getRequest.addHeader("accept", "application/json");
			
			final HttpEntity entity = httpclient.execute(target, getRequest).getEntity();
			final String json = EntityUtils.toString(entity);
			final JSONObject myResponse = new JSONObject(json).getJSONObject("response");
			
			return (JSONArray) myResponse.get("docs");

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
	
	public static String getLibraryVersionReleaseDate(String groupId, String artifactId, String versionNumber) {
		try {
			final Library lib = getLibrary(groupId, artifactId);
			return SIMPLE_DATE_FORMAT.format(lib.getReleaseDateOfLibraryVersionNumber(versionNumber));
		} catch (VersionNotFoundException e) {
			return "Date Not Found";
		}
	}

	public static String getLibraryVersionOnCommitDate(String groupId, String artifactId, String date) {
		try {
			final Library lib = getLibrary(groupId, artifactId);
			return lib.getVersionLibraryOnDate(SIMPLE_DATE_FORMAT.parse(date).getTime());
		} catch (ParseException e) {
			return "Unparseable date " + date;
		}
	}
}
