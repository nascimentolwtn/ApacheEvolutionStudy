package br.inpe.cap.evolution.processor.date.search;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.inpe.cap.evolution.processor.date.domain.Library;
import br.inpe.cap.evolution.processor.date.domain.Version;
import br.inpe.cap.evolution.processor.date.exception.VersionNotFoundException;

public class MavenCentralSearch {

	public static final String VERSION_NOT_FOUND = "No version found";
	public static final String DATE_NOT_FOUND = "Date Not Found";
	
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final HttpHost target = new HttpHost("search.maven.org", 80, "http");
	
	private static final LibraryCacheProvider libraryCacheProvider = new LibraryCacheProvider(false); 

	static synchronized Library getLibrary(String groupId, String artifactId) {
		return libraryCacheProvider.get(groupId, artifactId);
	}

	static synchronized Set<Version> getAllVersions(String groupId, String artifactId){
		Set<Version> versions = new TreeSet<Version>();

		if(!(hasInvalidRequestChar(groupId) || hasInvalidRequestChar(artifactId))) {
			JSONArray itensJSON = getJsonArrayResponseWithRest(groupId, artifactId);
	
			for (int i = 0; i < itensJSON.length(); i++) {
				final JSONObject libJSON = itensJSON.getJSONObject(i);
				final String version = libJSON.getString("v");
				final long date = libJSON.getLong("timestamp");
				versions.add(new Version(date, version));
			}
		}
		
		return versions;
	}

	private static boolean hasInvalidRequestChar(String request) {
		return (StringUtils.isEmpty(request) ||
				request.contains("{") ||
				request.contains("?"));
	}

	static synchronized String montaRequest(String groupId, String artifactId) {
		return "/solrsearch/select?q=g:" + groupId +
				"+AND+a:" + artifactId +
				"&rows=200&wt=json&core=gav";
	}

	static synchronized JSONArray getJsonArrayResponseWithRest(String groupId, String artifactId) {
		
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			final HttpGet getRequest = new HttpGet();
			getRequest.setURI(new URI(montaRequest(groupId, artifactId)));
			getRequest.addHeader("accept", "application/json");
			
			final HttpEntity entity = httpclient.execute(target, getRequest).getEntity();
			final String json = EntityUtils.toString(entity);
			final JSONObject myResponse = new JSONObject(json).getJSONObject("response");
			
			return (JSONArray) myResponse.get("docs");

		} catch (IOException | URISyntaxException | JSONException e) {
			System.err.println(e.getMessage());
			return new JSONArray();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}
	
	public synchronized static String getLibraryVersionReleaseDate(String groupId, String artifactId, String versionNumber) {
		try {
			final Library lib = getLibrary(groupId, artifactId);
			return SIMPLE_DATE_FORMAT.format(lib.getReleaseDateOfLibraryVersionNumber(versionNumber));
		} catch (VersionNotFoundException ve) {
			return DATE_NOT_FOUND;
		}
	}

	public synchronized static String getLibraryVersionOnCommitDate(String groupId, String artifactId, String date) {
		try {
			final Library lib = getLibrary(groupId, artifactId);
			return lib.getVersionLibraryOnDate(SIMPLE_DATE_FORMAT.parse(date).getTime());
		} catch (VersionNotFoundException ve) {
			return VERSION_NOT_FOUND;
		} catch (ParseException e) {
			return "Unparseable date " + date;
		}
	}

}
