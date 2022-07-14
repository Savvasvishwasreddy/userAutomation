package com.learningservices.itemselection.bff.utils;
import java.io.*;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.learningservices.utils.EnvironmentPropertiesReader;
import com.learningservices.utils.Log;

public class PropertyReader {
	private static Properties properties = new Properties();
	private static PropertyReader envProperties;

	private PropertyReader() {
		properties = loadEnvironmentProperties();
	}

	public PropertyReader(String filePath) {
		try {
			File file = new File(filePath);
			FileInputStream fileInput = new FileInputStream(file);
			properties.load(fileInput);
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	/** Load environment properties. */
	private Properties loadEnvironmentProperties() {
		Properties properties = new Properties();
		String configFile = null;
		try {
			// Load environment properties file
			if(System.getProperty("webSite") != null){
				String configFileName = null;
				String webSite = System.getProperty("webSite");
				if(webSite.toLowerCase().contains("nightly"))
					configFileName = "nightly";
				else if(webSite.toLowerCase().contains("cert"))
					configFileName = "cert";
				else if(webSite.toLowerCase().contains("ppe"))
					configFileName = "ppe";
				else if(webSite.toLowerCase().contains("savvasrealize"))
					configFileName = "prod";
				else if(webSite.toLowerCase().contains("perf"))
					configFileName = "perf";
				configFile = "bff_config/" + configFileName + ".properties";
				Log.event("Config file name:: " + configFile);
			}else{
				configFile = "bff_config/"
						+ (System.getProperty("test_environment") != null ? System.getProperty("test_environment")
								: EnvironmentPropertiesReader.getInstance().getProperty("test_environment")).toLowerCase()
						+ ".properties";
			}
			properties.load(PropertyReader.class.getClassLoader().getResourceAsStream(configFile));
			Log.event("bff_config/" + configFile + " is loaded properly");
		} catch (Exception e) {
			Log.fail("bff_config/" + configFile + " is not loaded properly!");
			e.printStackTrace();
		}
		return properties;
	}

	public static PropertyReader getInstance() {
		if (envProperties == null) {
			envProperties = new PropertyReader();
		}
		return envProperties;
	}

	public String getProperty(String key) {
		if (properties != null) {
			return properties.getProperty(key, "Key " + key + " is not present").toString();
		}
		return " properties file is blank ";
	}

	public boolean hasProperty(String key) {		
		return StringUtils.isNotBlank(properties.getProperty(key));
	}

}
