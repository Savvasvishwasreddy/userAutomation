package com.learningservices.itemselection.mfe.utils;

import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;

import com.learningservices.googleapi.GoogleDrive;
import com.learningservices.utils.EnvironmentPropertiesReader;
import com.learningservices.utils.Log;

public class GDriveUtils {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	public static String webSite;
	public static String testDataWorkbookName;

	@BeforeSuite(alwaysRun=true)
	public void downloadGSheet(ITestContext context) throws Exception {
		webSite = (System.getenv("webSite") != null ? System.getenv("webSite") 
				: context.getCurrentXmlTest().getParameter("webSite"));
		if(webSite.contains("nightly") || webSite.contains("dev")) {
			System.setProperty("executionEnvironment", "nightly");
		}else {
			System.setProperty("executionEnvironment", "prod");
		}
		
		testDataWorkbookName = "itemselectiontestdata_"+System.getProperty("executionEnvironment");
		
		if (configProperty.getProperty("readFromGoogleSheet").equalsIgnoreCase("true")) {
			GoogleDrive.downloadFileFromGDrive(testDataWorkbookName);
		}
		testDataWorkbookName = "itemselectiontestdata_"+System.getProperty("executionEnvironment")+".xlsx";
		
		if (System.getProperty("RUNENV") == null) {
			System.setProperty("RUNENV", "NIGHTLY"); // default to run on nightly
		}
		Log.message("Environment to run: " + System.getProperty("RUNENV"));
	}
}