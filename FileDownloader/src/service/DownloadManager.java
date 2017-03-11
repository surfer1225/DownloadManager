package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DownloadManager {

	public static void main(String[] args) {
		
		final String destDir = loadProperty("destination");
		
		if (destDir == null) {
			System.err.println("Problem in loading destination from property file");
		}
		
		List<String> urls = new ArrayList<String>();
		
//		urls.add("http://mediaroom.agoda.com/wp-content/uploads/2014/05/Agoda-logo-small.png");
//		urls.add("https://cloudsecurityalliance.org/csaguide.pdf");
//		urls.add("https://www.iso.org/files/live/sites/isoorg/files/archive/pdf/en/annual_report_2009.pdf");
		urls.add("ftp://127.0.0.1/Desktop/Agoda.txt");
		urls.add("ftp://127.0.0.1/Desktop/Agoda copy.txt");
//		urls.add("http://www-eu.apache.org/dist//commons/io/binaries/commons-io-2.5-bin.tar.gz");
//		urls.add("http://www.pdf995.com/samples/pdf.pdf");
//		urls.add("http://scholar.princeton.edu/sites/default/files/oversize_pdf_test_0.pdf");
		
		//for testing
		//DownloadHelper.downloadMultipleFiles(urls, destDir);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);//exact size may vary
        for (String url : urls) {
        	DownloadHelper downaloadTask = new DownloadHelper(url, destDir);
            executor.execute(downaloadTask);
        }
        executor.shutdown();
	}
	
	public static String loadProp(String prop) {
		return loadProperty(prop);
	}
	
	private static String loadProperty(String property) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			String configFile = "resources/config.properties";
			input = DownloadManager.class.getClassLoader().getResourceAsStream(configFile);

			if(input==null){
				System.err.println("Unable to find " + configFile);
				return null;
			}

			prop.load(input);
			System.out.println(prop.getProperty("destination"));
			return prop.getProperty(property);
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
		return null;
	}

}
