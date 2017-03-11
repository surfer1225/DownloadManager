package service;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DownloadHelper implements Runnable {
	private static final String HTTP = "http";
	private static final String FTP = "ftp";
	private static final String SFTP = "sftp";
	
	private String urlString;
	private String destination;
	
	public DownloadHelper(String urlString, String destination) {
		this.urlString = urlString;
		this.destination = destination;
	}
	
	public static void downloadMultipleFiles(List<String> urlStrings, String destDir) {
		for (String urlStr : urlStrings) {
			System.out.println((downloadFile(urlStr, destDir)?"Successful in downloading: ":"Failed in downloading")
					+ urlStr);
		}
	}
	
	//boolean type for unit test
	public static boolean downloadSingleFile(String urlStr, String destDir) {		
		return downloadFile(urlStr, destDir);
	}

	private static boolean downloadFile(String urlStr, String destDir) {
		// check the directory for existence.
		System.out.println("Downloading " + urlStr + " to " + destDir);
		boolean downloadStatus = true;
		if(!(destDir.endsWith(File.separator) || destDir.endsWith("/"))) {
			destDir += File.separator;
		}
		
		//append file name to the directory
		String fileName = urlStr.substring(urlStr.lastIndexOf(File.separator));
		
		File dstFile = new File(destDir + fileName);
		
		try {
		    switch (verifyURL(urlStr)) {
		    	case HTTP:
		    		URL url = new URL(urlStr);
		    		URLConnection urlCon = url.openConnection();
		    		if (urlCon instanceof HttpsURLConnection) {//set agent for https connection
		    			//TODO: check certification issue with HTTPS
		    			urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) "
		    					+ "AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		    			if (urlCon.getContentLengthLong() > 1024*1024*1024*100) {//TODO: to be set
		    				System.out.println("File too big to download");
		    				return false;
		    			}
		    			else {
		    				downloadStatus = HttpDownload.httpDownload(url, dstFile, true);
		    			}
		    		}
		    		else {
		    			if (urlCon.getContentLengthLong() > 1024*1024*1024*100) {
		    				System.out.println("File too big to download");
		    				return false;
		    			}
		    			else {
		    				downloadStatus = HttpDownload.httpDownload(url, dstFile, false);
		    			}
		    		}

		    		break;
		    	case FTP:
		    		downloadStatus = FTPDownload.ftpDownload(urlStr, dstFile);
		    		break;
		    	case SFTP:
		    		
		    		break;
		    	default:
		    		System.err.println("invalid url: " + urlStr);
		    		break;
		    }
		} catch (Exception e) {
		    System.err.println("Error in downloading file: " + e);
		    return false;
		}
		return downloadStatus;
	}
	
	private static String verifyURL(String url) {
		if (url.startsWith("http")) {
			return HTTP;
		}
		else if (url.startsWith("ftp")) {
			return FTP;
		}
		else if (url.startsWith("sftp")) {
			return SFTP;
		}
		else {
			return "";
		}
	}

	@Override
	public void run() {
		downloadFile(urlString, destination);
	}
}
