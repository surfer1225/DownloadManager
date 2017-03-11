package service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import common.HttpStatusCode;

public class HttpDownload implements Runnable {
	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;//may vary
	private URL sourceForThread;
	private File destinationForThread;
	private boolean isHTTPSForThread; 
	
	HttpDownload(URL sourceForThread, File destinationForThread, boolean isHTTPSForThread) {
		this.sourceForThread = sourceForThread;
		this.destinationForThread = destinationForThread;
		this.isHTTPSForThread = isHTTPSForThread;
	}
	
	public static boolean httpDownload(URL source, File destination, boolean isHTTPS) throws IOException {
		
		//check if destination directory exists
		if (destination.getParentFile() != null
				&& !destination.getParentFile().exists()) {
			destination.getParentFile().mkdirs();
		}
		//make sure we can write to destination
		if (destination.exists() && !destination.canWrite()) {
			String message = "Unable to open " + destination + " for writing.";
			System.err.println(message);
		}
		
		HttpURLConnection urlCon = (HttpURLConnection) source.openConnection();
		if (isHTTPS) {
			urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) "
					+ "AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		}
		
		if (urlCon.getResponseCode() != HttpStatusCode.SUCCESS) {
			System.err.println("HTTP connection failed, code is: " + urlCon.getResponseCode());
			return false;
		}
		
		InputStream input = urlCon.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(input);
		
		try {
			FileOutputStream output = new FileOutputStream(destination);
			try {
				long fileSizeFromCon = urlCon.getContentLengthLong();
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int count = 0;
				int n = 0;
				long totalSize = 0;
				long current = System.currentTimeMillis();
				while (-1 != (n = bis.read(buffer))) {
					output.write(buffer, 0, n);
					count += n;
					totalSize += n;
					//flush the outputStream once it reads 2mb, size may change
					if (count > 1024 * 1024 * 2) {
						count = 0;
						System.out.println("2mb, outputStream flush() for: " + source.toString()
								.substring(source.toString().lastIndexOf("/")));
						output.flush();
					}
					
				}
				System.out.println("Time taken for downloading this file: " + (System.currentTimeMillis() - current));
				output.flush();//flush the final bytes
				
				if (totalSize != fileSizeFromCon) {
					System.err.println("Incomplete file downloaded");
					//delete the incomplete file
					if (destination.exists()) {
						destination.delete();
						System.err.println("Incompletely downloaded file deleted");
					}
					return false;
				}
			} catch (IOException ex) {
				System.err.println("error in downloading file: " + source.toString());
				return false;
			} finally {
				IOUtils.closeQuietly(output);
			}
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(bis);
		}
		
		return true;
	}
	
	@Override
	public void run() {//thread to unblock other files in the queue
		try {
			System.out.println("File a bit big, downloading in another thread");
			httpDownload(sourceForThread, destinationForThread, isHTTPSForThread);
		} catch (IOException e) {
			System.err.println("error in downloading file in thread: " + sourceForThread.toString()
					+ "\n" + e);
		}
	}
}
