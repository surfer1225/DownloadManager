package service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


public class FTPDownload {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static String username = "";
	private static String pwd = "";
	
	public static void setUserNamePwd() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			String configFile = "resources/config.properties";
			input = FTPDownload.class.getClassLoader().getResourceAsStream(configFile);

			if(input==null){
				System.err.println("Unable to find " + configFile);
				return;
			}

			prop.load(input);
			username = prop.getProperty("username");
			pwd = prop.getProperty("pwd");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean ftpDownload(String url, File destination) {
		if (username == null || username.isEmpty() || pwd == null || pwd.isEmpty()) {
			System.out.println("Setting password for FTP connection");
			setUserNamePwd();//TODO, store in cache
		}
		final String serverAddress = ((String[]) url.split("/"))[2];//TODO: add check for this url validity
		//new ftp client
		FTPClient ftpClient = new FTPClient();
		OutputStream outputStream = null;
		InputStream inputStream = null;
		try {
			//try to connect
			ftpClient.connect(serverAddress);
			//login to server
			if(!ftpClient.login(username, pwd)) {
				System.out.println("log in unsuccessful");
				ftpClient.logout();
				return false;
			}
			int reply = ftpClient.getReplyCode();
			//FTPReply stores a set of constants for FTP reply codes.
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				return false;
			}

			//using InputStream retrieveFileStream(String)
			String remoteFile = url.substring(url.indexOf(serverAddress + "/") + 1 + serverAddress.length());
			
			FTPFile file = ftpClient.mlistFile(remoteFile);
			long size = file.getSize();
			
			outputStream = new BufferedOutputStream(new FileOutputStream(destination));			
			inputStream = ftpClient.retrieveFileStream(remoteFile);
			
			System.out.println("Downloading from FTP server: " + url);
			byte[] bytesArray = new byte[DEFAULT_BUFFER_SIZE];
			int bytesRead = -1;
			int totalSize = 0;
			while ((bytesRead = inputStream.read(bytesArray)) != -1) {
				outputStream.write(bytesArray, 0, bytesRead);
				totalSize += bytesRead;
			}
			
			if (totalSize != size) {
				System.err.println("Incomplete FTP download");
				if (destination.exists()) {
					destination.delete();
					System.err.println("Incompletely downloaded file deleted");
					return false;
				}
			}

			if (ftpClient.completePendingCommand()) {
				System.out.println("FTP File " + remoteFile + " has been downloaded successfully.");
			}
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
		} catch (IOException ioe) {
			System.err.println("Error in downloading FTP data" + ioe.toString());
			return false;
		} finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
			if(ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch(IOException ioe) {
					System.err.println("Error in disconnecting FTP: " + ioe.toString());
				}
			}
		}
		return true;
	}
}
