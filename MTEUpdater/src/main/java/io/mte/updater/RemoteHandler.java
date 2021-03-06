package io.mte.updater;

import java.net.URI;
import java.net.URL;

public class RemoteHandler {

	public final static String RELEASE_FILENAME = "MTE-Release.7z";
	
	public static class Link {
	
		/* These URL definitions should probably never change */
		private static final URL github = constructURL("https://github.com");
		private static final URL ghusercontent = constructURL("https://raw.githubusercontent.com");
		
		/* These relative paths are used to construct URL's */
		private static final String repoPath = "Tyler799/Morrowind-2019";
		//private static final String updaterBranch = "updater";
		private static final String masterBranch = "master";
		private static final String comparePath = "compare";
		private static final String releasePath = "releases/download";
		
		/* Append this to compare links to activate rich diff display */
		private static final String richDiff = "?short_path=4a4f391#diff-4a4f391a7396ba51c9ba42372b55d34e";
		
		public static final URL repository = constructURL(github, repoPath);
		public static final URL commitCompare = constructURL(github, repoPath, comparePath);
		public static final URL versionFile = constructURL(ghusercontent, repoPath, masterBranch, VersionFile.Type.MTE.getName());
		public static final URL releasesPage = constructURL(github, repoPath, "download"); 
		public static final URL mwseDevVersion = constructURL("https://nullcascade.com/mwse/version_dev");
		
		private static URL constructURL(URL url, String...paths)  {
			return constructURL(url.toString() + "/" + String.join("/", paths));
		}
		private static URL constructURL(URL url, String path)  {
			return constructURL(url.toString() + "/" + path);
		}
		
		private static URL constructURL(String url) {
			
			try {
				return new URL(url);
			} catch (java.net.MalformedURLException e) {
				Logger.print(Logger.Level.ERROR, e, "%s is not a valid URL format", url.toString());
				return null;
			}
		}
	}
	
	/**
	 * Create a hyperlink to a direct comparison between two commits made in the
	 * guide repository. It's recommended to use richDiff and displayCommits.
	 * 
	 * @param commit1 SHA of base commit to compare against
	 * @param commit2 SHA of comparing commit
	 * @param richDiff Make the comparison display more user friendly
	 * @param range View default comparison for the given commit range
	 * @return URI wrapped url or {@code null} if an exception was thrown
	 */
	 static URI getGithubCompareLink(String commit1, String commit2, boolean richDiff, boolean range) {
		
		URL compareUrl = Link.constructURL(Link.commitCompare, commit1 +
				(range ? "..." : "..") + commit2 + (richDiff ? Link.richDiff : ""));

		// Apply URI wrapper to string
		try {
			return new java.net.URI(compareUrl.toString());
		} catch (java.net.URISyntaxException e) {
			Logger.error("URL string violates RFC 2396!", e);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Open a webpage with the provided hyperlink using the default user browser
	 * 
	 * @param url web address we want to browse
	 * @return {@code true} if the operation was successful, {@code false} otherwise
	 */
	static boolean browseWebpage(URI url) {
		
		try {
			java.awt.Desktop.getDesktop();
			if (java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop.getDesktop().browse(url);
				return true;
			} else {
				Logger.error("Desktop class is not suppored on this platform");
				return false;
			}
		} catch (Exception e) {
			if (e instanceof java.io.IOException)
				Logger.error("Unable to open web browser, default browser is not found or it failed to launch", e);
			else if (e instanceof SecurityException)
				Logger.error("Unable to open web browser, security manager denied permission", e);
			return false;
		}
	}
	/** 
	 * Download latest project release from Github
	 * @param tag number used to find the latest release
	 */
	static boolean downloadLatestRelease(float tag) {

		try {
			FileHandler fh = FileHandler.get();
			URL releaseLink = Link.constructURL(Link.repository, Link.releasePath, "v" + tag, RELEASE_FILENAME);
			if (fh.downloadUsingStream(releaseLink, RELEASE_FILENAME)) {
				fh.registerTempFile(new java.io.File(RELEASE_FILENAME));
				return true;
			}
			return false;
		} 
		catch (java.io.IOException e) {
			Logger.error("Unable to download repository files!", e);
			return false;
		}
	}
	/** Returns the latest nightly build version of MWSE. */
	static String getMwseDevVersion() {
		return downloadStringLine(Link.mwseDevVersion);
	}
	/** 
	 * Reads a text file from url and returns the first line as string. 
	 * @param url web location of the text file to read
	 * @return {@code null} if an error occurred
	 */
	static String downloadStringLine(URL url) {
		
		try {
			java.io.InputStreamReader stream = new java.io.InputStreamReader(url.openStream());
			java.io.BufferedReader reader = new java.io.BufferedReader(stream);
			return reader.readLine();
		}
		catch (java.io.IOException e) {
			Logger.print(Logger.Level.ERROR, e, "Unable to download string from %s", url.toString());
			return null;
		}
	}
}
