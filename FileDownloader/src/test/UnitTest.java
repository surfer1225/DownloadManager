package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import service.DownloadHelper;
import service.DownloadManager;

public class UnitTest {
	
	@Test
	public void testDownloadFromURL() throws IOException {
		String destDir = DownloadManager.loadProp("destination");
		String url = "http://mediaroom.agoda.com/wp-content/uploads/2014/05/Agoda-logo-small.png";
		assertTrue("Download Failed", DownloadHelper.downloadSingleFile(url, destDir));
		destDir = destDir + url.substring(url.lastIndexOf("/"));
		File file = new File(destDir);
		assertTrue("File does not exist.", file.exists());
		file.delete();
	}
	
	@Test
	public void tesHttpsDownload() throws IOException {
		String destDir = DownloadManager.loadProp("destination");
		String url = "https://cloudsecurityalliance.org/csaguide.pdf";
		assertTrue("Download Failed", DownloadHelper.downloadSingleFile(url, destDir));
		destDir = destDir + url.substring(url.lastIndexOf("/"));
		File file = new File(destDir);
		assertTrue("File does not exist.", file.exists());
		file.delete();
	}
	
	@Test
	public void testDownloadFromFTP() throws IOException {
		String destDir = DownloadManager.loadProp("destination");
		String url = "ftp://127.0.0.1/Desktop/Agoda.txt";
		assertTrue("Download Failed", DownloadHelper.downloadSingleFile(url, destDir));
		destDir = destDir + url.substring(url.lastIndexOf("/"));
		File file = new File(destDir);
		assertTrue("File does not exist.", file.exists());
		file.delete();
	}
}
