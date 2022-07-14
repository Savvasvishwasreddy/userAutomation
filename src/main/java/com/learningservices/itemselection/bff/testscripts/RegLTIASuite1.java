package com.learningservices.itemselection.bff.testscripts;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.learningservices.itemselection.bff.canvas.pages.DiscoverPage;
import com.learningservices.itemselection.bff.canvas.pages.IMSGlobalPage;
import com.learningservices.itemselection.bff.ltia.pages.SchoolSelectionPage;
import com.learningservices.itemselection.bff.utils.EnvironmentPropertiesReader;
import com.learningservices.itemselection.bff.utils.LTIAUtils;
import com.learningservices.itemselection.bff.utils.PropertyReader;
import com.learningservices.itemselection.bff.utils.RBSAPIUtils;
import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.itemselection.bff.utils.RumbaClient;
import com.learningservices.utils.DataProviderUtils;
import com.learningservices.utils.EmailReport;
import com.learningservices.utils.Log;
import com.learningservices.utils.RestAssuredAPI;
import com.learningservices.utils.WebDriverFactory;
import io.restassured.response.Response;

@Listeners(EmailReport.class)
public class RegLTIASuite1 {
	private String webSite;
	private static PropertyReader configProperty;
	private static EnvironmentPropertiesReader envProperty;
	private String password = "";
	String createdBy = "Self-reg";
	private static final String TEACHER_ROLE = RBSAPIUtils.UMS_ROLE.Teacher.toString();
	
	private final static String POST_CONTENT_RESPONSE = "{\"operationName\":\"postContentResponse\",\"variables\":{\"selectedContent\":\"[{\\\"contentId\\\":\\\"%s\\\",\\\"contentVersion\\\":%d,\\\"title\\\":\\\"%s\\\",\\\"programNames\\\":[\\\"%s\\\"],\\\"mediaType\\\":\\\"Test\\\",\\\"contentInfo\\\":{\\\"description\\\":\\\"\\\",\\\"materials\\\":[],\\\"keywords\\\":[],\\\"pacing\\\":\\\"\\\",\\\"author\\\":\\\"\\\",\\\"genres\\\":[],\\\"textFeatures\\\":null,\\\"contentAreas\\\":null,\\\"comprehensionSkills\\\":null,\\\"isbn\\\":\\\"\\\",\\\"notebookEntries\\\":null,\\\"activities\\\":[],\\\"__typename\\\":\\\"ContentInfo\\\"},\\\"__typename\\\":\\\"Content\\\",\\\"productId\\\":\\\"%s\\\"}]\"},\"query\":\"mutation postContentResponse($selectedContent: String!) {\\n  postContentResponse(data: {selectedContent: $selectedContent}) {\\n    status\\n    response\\n    __typename\\n  }\\n}\\n\"}";
	private final static String POST_MUTLIPLE_CONTENT_RESPONSE = "{\"operationName\":\"postContentResponse\",\"variables\":{\"selectedContent\":\"[{\\\"contentId\\\":\\\"%s\\\",\\\"contentVersion\\\":%d,\\\"title\\\":\\\"%s\\\",\\\"programNames\\\":[\\\"%s\\\"],\\\"mediaType\\\":\\\"Document\\\",\\\"contentInfo\\\":{\\\"description\\\":\\\"Pdf file\\\",\\\"materials\\\":[],\\\"keywords\\\":[],\\\"pacing\\\":\\\"\\\",\\\"author\\\":\\\"\\\",\\\"genres\\\":[],\\\"textFeatures\\\":null,\\\"contentAreas\\\":null,\\\"comprehensionSkills\\\":null,\\\"isbn\\\":\\\"\\\",\\\"notebookEntries\\\":null,\\\"activities\\\":[],\\\"__typename\\\":\\\"ContentInfo\\\"},\\\"__typename\\\":\\\"Content\\\",\\\"productId\\\":\\\"%s\\\"},{\\\"contentId\\\":\\\"%s\\\",\\\"contentVersion\\\":%d,\\\"title\\\":\\\"%s\\\",\\\"programNames\\\":[\\\"%s\\\"],\\\"mediaType\\\":\\\"Image\\\",\\\"contentInfo\\\":{\\\"description\\\":\\\"JPG image\\\",\\\"materials\\\":[],\\\"keywords\\\":[],\\\"pacing\\\":\\\"\\\",\\\"author\\\":\\\"\\\",\\\"genres\\\":[],\\\"textFeatures\\\":null,\\\"contentAreas\\\":null,\\\"comprehensionSkills\\\":null,\\\"isbn\\\":\\\"\\\",\\\"notebookEntries\\\":null,\\\"activities\\\":[],\\\"__typename\\\":\\\"ContentInfo\\\"},\\\"__typename\\\":\\\"Content\\\",\\\"productId\\\":\\\"%s\\\"}]\"},\"query\":\"mutation postContentResponse($selectedContent: String!) {\\n  postContentResponse(data: {selectedContent: $selectedContent}) {\\n    status\\n    response\\n    __typename\\n  }\\n}\\n\"}";

	@BeforeTest
	public void init(ITestContext context) {
		webSite = (System.getProperty("webSite") != null ? System.getProperty("webSite") : context.getCurrentXmlTest().getParameter("webSite")).toLowerCase();
		System.setProperty("webSite", webSite);
		configProperty = PropertyReader.getInstance();
		envProperty = EnvironmentPropertiesReader.getInstance("bff_config");
		password = configProperty.getProperty("DEFAULT_PASSWORD");
	}

	@Test(groups = { "IDAM-468", "RGHT-153191", "Functional", "API", "P1",
			"R_LTIA" }, dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 16)
	public void tc170REA448629(String browser) throws Exception {

		final WebDriver driver = WebDriverFactory.get(browser);
		String browserName = RealizeUtils.getBrowerName(driver);

		Log.testCaseInfo("REA448629: Verify BFF graphql API to send multiple non-assessment"
				+ " contents selection to LTI Tool Gateway <small><b><i>[" + browserName + "]</b></i></small>");

		String environment = envProperty.getProperty("test_environment").trim();
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String ltiContentItemsField = configProperty.getProperty("lti.claim.contentItems");
		String launchEndpoint = configProperty.getProperty("lti.tool.launch.endpoint");
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String schoolName = configProperty.getProperty("ebb.district01.school01.name");
		List<String> programNames = Arrays.asList(configProperty.getProperty("ltia.product01.name"));
		String jsonFileName = "./src/main/resources/import_files/LTI-A/REA448629_" + environment + ".json";

		String deepLinkURL = "", platformName = "", issuerId = "";
		if (environment.contains("nightly")) {
			deepLinkURL = configProperty.getProperty("ims.platform8.course.deeplink");
			platformName = configProperty.getProperty("ims.platform8.name");
			issuerId = configProperty.getProperty("ims.platform8.issuerId");
		} else if (environment.contains("cert")) {
			deepLinkURL = configProperty.getProperty("ims.platform1.course.deeplink");
			platformName = configProperty.getProperty("ims.platform1.name");
			issuerId = configProperty.getProperty("ims.platform1.issuerId");
		} else if (environment.contains("ppe")) {
			deepLinkURL = configProperty.getProperty("ims.platform1.course.deeplink");
			platformName = configProperty.getProperty("ims.platform1.name");
			issuerId = configProperty.getProperty("ims.platform1.issuerId");
		}

		String userName = "", userAuth = "";
		List<String> lstProductId = new ArrayList<String>();
		List<String> lstExpectedContentId = new ArrayList<String>();
		List<String> lstExpectedContentViewerUrl = new ArrayList<String>();
		List<String> lstExpectedContentIdVersion = new ArrayList<String>();
		List<String> lstExpectedContentTitle = new ArrayList<String>();
		List<String> lstExpectedContentURL = new ArrayList<String>();
		List<String> lstExpectedThumbnailURL = new ArrayList<String>();
		List<Integer> lstExpectedThumbnailWidth = new ArrayList<Integer>();
		List<Integer> lstExpectedThumbnailHeight = new ArrayList<Integer>();

		try {

			JSONObject expectedJsonBody = RealizeUtils.getJsonFromFile(jsonFileName);
			JSONArray expectedSelectedContents = expectedJsonBody.getJSONArray(ltiContentItemsField);

			for (int item = 0; item < expectedSelectedContents.length(); item++) {
				JSONObject expectedCustom = expectedSelectedContents.getJSONObject(item).getJSONObject("custom");
				lstExpectedContentId.add(expectedCustom.get("contentId").toString().split("/")[0]);
				lstExpectedContentIdVersion.add(expectedCustom.get("contentId").toString().split("/")[1]);
				lstExpectedContentViewerUrl.add(expectedCustom.get("contentViewerUrl").toString());
				lstProductId.add(expectedCustom.get("productId").toString());
			}
			int totalContent = lstExpectedContentId.size(); 
			
			for (int item = 0; item < expectedSelectedContents.length(); item++) {
				lstExpectedContentTitle.add(expectedSelectedContents.getJSONObject(item).get("title").toString());
			}
			
			for (int item = 0; item < expectedSelectedContents.length(); item++) {
				lstExpectedContentURL.add(expectedSelectedContents.getJSONObject(item).get("url").toString());
			}
			
			for (int item = 0; item < expectedSelectedContents.length(); item++) {
				JSONObject expectedCustom = expectedSelectedContents.getJSONObject(item).getJSONObject("thumbnail");
				lstExpectedThumbnailURL.add(expectedCustom.get("url").toString());
				lstExpectedThumbnailWidth.add(expectedCustom.getInt("width"));
				lstExpectedThumbnailHeight.add(expectedCustom.getInt("height"));
			}
			
			JSONArray arrcontents = new JSONArray();
			JSONObject contentJson = new JSONObject();
			contentJson.put("contentId", lstExpectedContentId.get(0));
			contentJson.put("contentVersion", lstExpectedContentIdVersion.get(0));
			contentJson.put("productId", lstProductId.get(0));
			
			JSONObject contentJson2 = new JSONObject();
			contentJson2.put("contentId", lstExpectedContentId.get(1));
			contentJson2.put("contentVersion", lstExpectedContentIdVersion.get(1));
			contentJson2.put("productId", lstProductId.get(1));
			
			arrcontents.put(contentJson);
			arrcontents.put(contentJson2);
			
			Log.message("Platform name: '<b>" + platformName + "</b>'");
			Log.message("URL of 'Launch Deep Link Flow (OIDC)': '<b>" + deepLinkURL + "</b>'");

			driver.get(deepLinkURL);
			IMSGlobalPage imsGlobalpage = new IMSGlobalPage(driver);

			imsGlobalpage.clickSendRequestButton(true);
			imsGlobalpage.clickLaunchDeepLink(true);

			// getting JWT details before navigating Discover page
			Log.message("<br><u>External user details from JWT attributes before navigating to Discover page</u>");
			String JWTBodyRequest = imsGlobalpage.getJWTRequestBodyFromPlatform();
			JSONObject platfromJsonCode = new JSONObject(JWTBodyRequest);
			String sub = platfromJsonCode.get("sub").toString();
			Log.message("External User Id(sub) : '<b>" + sub + "</b>'");

			// getting Organization id from via LTI
			Log.message("</br>");
			String orgId = RBSAPIUtils.getOrganizationIdForIssuerId(issuerId);
			if(orgId != null && !orgId.isEmpty())
				Log.message("Organization id: '<b>" + orgId + "</b>'");
			else
				Log.fail("Unable to get Organization id from the given issuer id: " + issuerId);

			imsGlobalpage.clickPerformLaunchButton(false);

			// Select the schools in school selection page
			SchoolSelectionPage schoolSelectionPage = new SchoolSelectionPage(driver).get();
			Log.assertThat(schoolSelectionPage.isSchoolSelectionPageForTeacherLoaded(),
					"School selection page is launched successfully",
					"Test Failed: School selection page is not loaded properly", driver);

			// School selection page for course
			schoolSelectionPage.clickDownArrowInSchoolSelectionDropdownForTeacher();
			schoolSelectionPage.selectGivenSchoolsInSchoolSelectionDropdown(Arrays.asList(schoolName), true);
			schoolSelectionPage.clickDownArrowInSchoolSelectionDropdownForTeacher();
			schoolSelectionPage.clickSelectButton(true);

			RealizeUtils.nap(10);// Wait to load Content
			DiscoverPage discoverPage = new DiscoverPage(driver).get();
			discoverPage.selectProgram(programNames);
			discoverPage.clickSaveButton();

			Log.assertThat(discoverPage.verifyBrowseProgramPageLoadedOrNot(),
					"<b> PASSED: </b> Browse Program page is launched successfully after clicking Save button in Program selection page for first time user",
					"<b> FAILED: </b> Realize Discover page is not launched after clicking Save button", driver);

			discoverPage.clickBrowseAllContentButton(true);
			Log.assertThat(discoverPage.verifyDiscoverHeaderToolbar(), "Realize Discover page is launched successfully",
					"Test Failed: Realize Discover page is not loaded properly", driver);

			// creating username with MD5 Hash code
			userName = LTIAUtils.generateLTIAUsername(sub, orgId, TEACHER_ROLE);
			RBSAPIUtils.updatePasswordForGivenUser(userName, password);
			userAuth = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
			if (userAuth != null && !userAuth.isEmpty())
				Log.message("Basic authorization of teacher: <b>" + userAuth + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + userName + "'");
			
			String requestBody = String.format(POST_MUTLIPLE_CONTENT_RESPONSE, lstExpectedContentId.get(0), Integer.parseInt(lstExpectedContentIdVersion.get(0)), lstExpectedContentTitle.get(0), programNames.get(0), lstProductId.get(0), lstExpectedContentId.get(1), Integer.parseInt(lstExpectedContentIdVersion.get(1)), lstExpectedContentTitle.get(1), programNames.get(0), lstProductId.get(1), lstExpectedContentTitle.get(1));
			Response deepLinkResponse = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(userAuth, requestBody);
			
			JSONObject postRequestResponse = new JSONObject(deepLinkResponse.getBody().asString());
			Log.softAssertThat(postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("status").equals("success"),
					"</br><b>PASSED:</b> Verified: Response code 200 in POST request for deeplinking content Response when basic auth is correct",
					"<b>FAILED:</b> Failed: Response code 200 is not returned in POST request for deeplinking content Response when basic auth is correct");

			String deeplinkHtmlString = postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("response");
			String jsonPayLoad = LTIAUtils.decodeJWTtokenFromDeeplinkResponse(deeplinkHtmlString, issuerId, null, null);

			JSONObject actualResponseBody = new JSONObject(jsonPayLoad);
			Log.message("JSON Deeplinking Response: <pre>"
					+ RealizeUtils.convertJSONStringtoPrettyFormat(jsonPayLoad.toString()) + "</pre>");

			String actualTitle;
			if (actualResponseBody.has(ltiContentItemsField) && !actualResponseBody.isNull(ltiContentItemsField)) {
				JSONArray actualSelectedContents = actualResponseBody.getJSONArray(ltiContentItemsField);

				Log.assertThat(actualSelectedContents.length() == totalContent,
						"Verified: Deeplinking Json Response body have same " + totalContent + " number of contents in <b>'"
								+ ltiContentItemsField + "'</b> field.",
						"Failed: Deeplinking Json Response body does not have same number of contents in <b>'" + ltiContentItemsField
								+ "'</b> field. Expected size: " + totalContent + " , Actual size: "
								+ actualSelectedContents.length());

				// verifying title
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					final String expectedTitle = lstExpectedContentTitle.get(item);
					String contentId = lstExpectedContentId.get(item);
					int findIndex = IntStream.range(0, actualSelectedContents.length()).filter(
							i -> actualSelectedContents.getJSONObject(i).getString("title").equals(expectedTitle))
							.findFirst().orElse(-1);
					if (findIndex != -1) {
						actualTitle = actualSelectedContents.getJSONObject(findIndex).getString("title");
						Log.softAssertThat(actualTitle.equals(expectedTitle),
								"Verified: Title of the content <b>'" + contentId + "'</b> in deeplink response is matched with the content from discover page. Title value - <b>'" + expectedTitle + "'</b> ",
								"Failed: Title of the content in deeplink response is not matched with the content from discover page, "
										+ "Expected Title: " + expectedTitle + ", Actual Title: " + actualTitle);
					} else {
						Log.failsoft("Failed: Deep linking response does not have content title <b>'" + expectedTitle
								+ "'</b> in the custom attribute of <b>'" + ltiContentItemsField + "'</b>");
					}
				}

				// Verify content url for each content in Deep linking response
				String expectedContentId, expectedProductId, expectedURL, actualURL;
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					expectedContentId = lstExpectedContentId.get(item);
					expectedProductId = lstProductId.get(item);
					expectedURL = ltiBaseUrl + String.format(launchEndpoint, expectedContentId + "/" + lstExpectedContentIdVersion.get(item), expectedProductId);
					actualURL = actualSelectedContents.getJSONObject(item).getString("url");
										
					Log.softAssertThat(actualURL.equals(expectedURL),
							"Verified: Content url for the content <b>'" + expectedContentId + "'</b> in <b>" + ltiContentItemsField
									+ "</b> block has correct value - <b>" + actualURL + "</b>",
							"Failed: Content url attribute is not matched for content <b>'" + expectedContentId + "'</b> in "
									+ ltiContentItemsField + " field." + " Expected: " + expectedURL + ", Actual: "
									+ actualURL);
				}

				// Verify custom field for contentId
				String actualProductId, actualContentId;
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					String contentId = lstExpectedContentId.get(item);
					expectedProductId = lstProductId.get(item);
					final String expectedContentId1 = lstExpectedContentId.get(item) + "/" + lstExpectedContentIdVersion.get(item);
					
					final String expectedTitle = lstExpectedContentTitle.get(item);
					int findIndex = IntStream.range(0, actualSelectedContents.length()).filter(
							i -> actualSelectedContents.getJSONObject(i).getString("title").equals(expectedTitle))
							.findFirst().orElse(-1);
					
					if (findIndex != -1) {
						actualContentId = actualSelectedContents.getJSONObject(item).getJSONObject("custom").getString("contentId");
						Log.softAssertThat(actualContentId.equals(expectedContentId1),
								"Verified: Content id of the content <b>'" + contentId + "'</b> in deeplink response is matched. contentId - <b>'" + expectedContentId1 + "'</b>",
								"Faield: Content id of the content in deeplink response is not matched, "
										+ "Expected contentId: " + expectedContentId1 + ", Actual contentId: " + actualContentId);
					} else {
						Log.failsoft("Deep linking response does not have contentId <b>'" + expectedContentId1
								+ "'</b> in the custom field of <b>'" + ltiContentItemsField + "'</b>");
					}
					
					if (findIndex != -1) {
						actualProductId = actualSelectedContents.getJSONObject(item).getJSONObject("custom").getString("productId");
						Log.softAssertThat(actualProductId.equals(expectedProductId),
								"Verified: productId of the content <b>'" + contentId + "'</b> in deeplink response is matched. productId - <b>'" + expectedProductId + "'</b>",
								"Faield: productId of the content in deeplink response is not matched, "
										+ "Expected productId: " + expectedProductId + ", Actual productId: " + actualProductId);
					} else {
						Log.failsoft("Deep linking response does not have productId <b>'" + expectedProductId
								+ "'</b> in the custom field of <b>'" + ltiContentItemsField + "'</b>");
					}
				}

				// Verify thumbnail field for content
				String actualThumbnailURL;
				int actualThumbnailWidth, actualThumbnailHeight;
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					String contentId = lstExpectedContentId.get(item);
					final String expectedThumbnailURL = lstExpectedThumbnailURL.get(item);
					final int expectedThumbnailWidth = lstExpectedThumbnailWidth.get(item);
					final int expectedThumbnailHeight = lstExpectedThumbnailHeight.get(item);
									
					final String expectedTitle = lstExpectedContentTitle.get(item);
					int findIndex = IntStream.range(0, actualSelectedContents.length()).filter(
							i -> actualSelectedContents.getJSONObject(i).getString("title").equals(expectedTitle))
							.findFirst().orElse(-1);
					
					if (findIndex != -1) {
						actualThumbnailURL = actualSelectedContents.getJSONObject(findIndex).getJSONObject("thumbnail").getString("url");
						Log.softAssertThat(actualThumbnailURL.equals(expectedThumbnailURL),
								"Verified: Thumbnail URL of the content <b>'" + contentId + "'</b> in deeplink response is matched. url - <b>'" + expectedThumbnailURL + "'</b>",
								"Faield: Thumbnail URL of the content in deeplink response is not matched, "
										+ "Expected URL: " + expectedThumbnailURL + ", Actual URL: " + actualThumbnailURL);
					} else {
						Log.failsoft("Deep linking response does not have url <b>'" + expectedThumbnailURL
								+ "'</b> in the Thumbnail field of <b>'" + ltiContentItemsField + "'</b>");
					}
					
					if (findIndex != -1) {
						actualThumbnailWidth = actualSelectedContents.getJSONObject(findIndex).getJSONObject("thumbnail").getInt("width");
						Log.softAssertThat(actualThumbnailWidth == expectedThumbnailWidth,
								"Verified: Thumbnail width of the content <b>'" + contentId + "'</b> in deeplink response is matched. width - <b>'" + expectedThumbnailWidth + "'</b>",
								"Faield: Thumbnail width of the content in deeplink response is not matched, "
										+ "Expected URL: " + expectedThumbnailWidth + ", Actual URL: " + actualThumbnailWidth);
					} else {
						Log.failsoft("Deep linking response does not have width <b>'" + expectedThumbnailWidth
								+ "'</b> in the Thumbnail field of <b>'" + ltiContentItemsField + "'</b>");
					}
					
					if (findIndex != -1) {
						actualThumbnailHeight = actualSelectedContents.getJSONObject(findIndex).getJSONObject("thumbnail").getInt("height");
						Log.softAssertThat(actualThumbnailHeight == expectedThumbnailHeight,
								"Verified: Thumbnail height of the content <b>'" + contentId + "'</b> in deeplink response is matched. height - <b>'" + expectedThumbnailHeight + "'</b>",
								"Faield: Thumbnail height of the content in deeplink response is not matched, "
										+ "Expected URL: " + expectedThumbnailHeight + ", Actual URL: " + actualThumbnailHeight);
					} else {
						Log.failsoft("Deep linking response does not have height <b>'" + expectedThumbnailHeight
								+ "'</b> in the Thumbnail field of <b>'" + ltiContentItemsField + "'</b>");
					}
				}
				
			} else {
				Log.failsoft("Deep linking response does not have <b>'" + ltiContentItemsField + "'</b> attribute");
			}
			
			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
			driver.quit();
		}
	}
		
	@Test(groups = { "IDAM-468", "RGHT-153191", "Functional", "API", "P1",
			"R_LTIA" }, dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 17)
	public void tc180REA448630(String browser) throws Exception {

		final WebDriver driver = WebDriverFactory.get(browser);
		String browserName = RealizeUtils.getBrowerName(driver);
		Log.testCaseInfo("REA448630: Verify BFF graphql API to send single assessment content selection"
				+ " to LTI Tool Gateway <small><b><i>[" + browserName + "]</b></i></small>");

		String environment = envProperty.getProperty("test_environment").trim();
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String ltiContentItemsField = configProperty.getProperty("lti.claim.contentItems");
		String launchEndpoint = configProperty.getProperty("lti.tool.launch.endpoint");
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String schoolName = configProperty.getProperty("ebb.district01.school01.name");
		List<String> programNames = Arrays.asList(configProperty.getProperty("ltia.product01.name"));
		String jsonFileName = "./src/main/resources/import_files/LTI-A/REA448630_" + environment + ".json";

		String platformName = "", deepLinkURL = "", issuerId = "";
		if (environment.contains("nightly")) {
			deepLinkURL = configProperty.getProperty("ims.platform8.course.deeplink");
			platformName = configProperty.getProperty("ims.platform8.name");
			issuerId = configProperty.getProperty("ims.platform8.issuerId");
		} else {
			deepLinkURL = configProperty.getProperty("ims.platform1.course.deeplink");
			platformName = configProperty.getProperty("ims.platform1.name");
			issuerId = configProperty.getProperty("ims.platform1.issuerId");
		}

		List<String> lstProductId = new ArrayList<String>();
		List<String> lstExpectedContentId = new ArrayList<String>();
		List<String> lstExpectedContentViewerUrl = new ArrayList<String>();
		List<String> lstExpectedContentIdVersion = new ArrayList<String>();
		List<String> lstExpectedContentTitle = new ArrayList<String>();
		List<String> lstExpectedContentURL = new ArrayList<String>();
		List<String> lstExpectedThumbnailURL = new ArrayList<String>();
		List<Integer> lstExpectedThumbnailWidth = new ArrayList<Integer>();
		List<Integer> lstExpectedThumbnailHeight = new ArrayList<Integer>();
		String userAuth = "", userName = "";
		int expectedNumberOfContents = 1;

		try {

			JSONObject expectedJsonBody = RealizeUtils.getJsonFromFile(jsonFileName);
			JSONArray expectedSelectedContents = expectedJsonBody.getJSONArray(ltiContentItemsField);
		
			for (int item = 0; item < expectedNumberOfContents; item++) {
				JSONObject expectedCustom = expectedSelectedContents.getJSONObject(item).getJSONObject("custom");
				lstExpectedContentId.add(expectedCustom.get("contentId").toString().split("/")[0]);
				lstExpectedContentIdVersion.add(expectedCustom.get("contentId").toString().split("/")[1]);
				lstExpectedContentViewerUrl.add(expectedCustom.get("contentViewerUrl").toString());
				lstProductId.add(expectedCustom.get("productId").toString());
			}
			int totalContent = lstExpectedContentId.size(); 
			
			for (int item = 0; item < expectedNumberOfContents; item++) {
				lstExpectedContentTitle.add(expectedSelectedContents.getJSONObject(item).get("title").toString());
			}
			
			for (int item = 0; item < expectedNumberOfContents; item++) {
				lstExpectedContentURL.add(expectedSelectedContents.getJSONObject(item).get("url").toString());
			}
			
			for (int item = 0; item < expectedNumberOfContents; item++) {
				JSONObject expectedCustom = expectedSelectedContents.getJSONObject(item).getJSONObject("thumbnail");
				lstExpectedThumbnailURL.add(expectedCustom.get("url").toString());
				lstExpectedThumbnailWidth.add(expectedCustom.getInt("width"));
				lstExpectedThumbnailHeight.add(expectedCustom.getInt("height"));
			}

			JSONArray arrcontents = new JSONArray();
			JSONObject contentJson = new JSONObject();
			contentJson.put("contentId", lstExpectedContentId.get(0));
			contentJson.put("contentVersion", lstExpectedContentIdVersion.get(0));
			contentJson.put("productId", lstProductId.get(0));
			arrcontents.put(contentJson);
			
			Log.message("Platform name: '<b>" + platformName + "</b>'");
			Log.message("URL of 'Launch Deep Link Flow (OIDC)': '<b>" + deepLinkURL + "</b>'");

			driver.get(deepLinkURL);
			IMSGlobalPage imsGlobalpage = new IMSGlobalPage(driver);

			imsGlobalpage.clickSendRequestButton(true);
			imsGlobalpage.clickLaunchDeepLink(true);

			// getting JWT details before navigating Discover page
			Log.message("<br><u>External user details from JWT attributes before navigating to Discover page</u>");
			String JWTBodyRequest = imsGlobalpage.getJWTRequestBodyFromPlatform();
			JSONObject platfromJsonCode = new JSONObject(JWTBodyRequest);
			String externalUserId = platfromJsonCode.get("sub").toString();
			Log.message("External User Id(sub) : '<b>" + externalUserId + "</b>'");

			// getting Organization id from via LTI
			Log.message("</br>");
			String orgId = RBSAPIUtils.getOrganizationIdForIssuerId(issuerId);
			if(orgId != null && !orgId.isEmpty())
				Log.message("Organization id: '<b>" + orgId + "</b>'");
			else
				Log.fail("Unable to get Organization id from the given issuer id: " + issuerId);

			imsGlobalpage.clickPerformLaunchButton(false);

			// Select the schools in school selection page
			SchoolSelectionPage schoolSelectionPage = new SchoolSelectionPage(driver).get();
			Log.assertThat(schoolSelectionPage.isSchoolSelectionPageForTeacherLoaded(),
					"School selection page is launched successfully",
					"Test Failed: School selection page is not loaded properly", driver);

			// School selection page for course
			schoolSelectionPage.clickDownArrowInSchoolSelectionDropdownForTeacher();
			schoolSelectionPage.selectGivenSchoolsInSchoolSelectionDropdown(Arrays.asList(schoolName), true);
			schoolSelectionPage.clickDownArrowInSchoolSelectionDropdownForTeacher();
			schoolSelectionPage.clickSelectButton(true);

			RealizeUtils.nap(10);// Wait to load Content
			DiscoverPage discoverPage = new DiscoverPage(driver).get();
			discoverPage.selectProgram(programNames);
			discoverPage.clickSaveButton();

			Log.assertThat(discoverPage.verifyBrowseProgramPageLoadedOrNot(),
					"<b> PASSED: </b> Browse Program page is launched successfully after clicking Save button in Program selection page for first time user",
					"<b> FAILED: </b> Realize Discover page is not launched after clicking Save button", driver);

			discoverPage.clickBrowseAllContentButton(true);
			Log.assertThat(discoverPage.verifyDiscoverHeaderToolbar(), "Realize Discover page is launched successfully",
					"Test Failed: Realize Discover page is not loaded properly", driver);

			// creating username with MD5 Hash code
			userName = LTIAUtils.generateLTIAUsername(externalUserId, orgId, TEACHER_ROLE);
			String userId = RumbaClient.getRumbaUserId(userName);
			if (userId != null && userId.length() > 0) {
				Log.message("Able to get rumba userId from Rumba: " + userId);
			} else {
				Log.fail("Unable to get rumba userId for username " + userName);
			}

			if (RumbaClient.updateUserPassword(userId, password))
				Log.message("Unable to update password for userId " + userId);

			userAuth = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
			Log.message("Bearer authorization of teacher: <b>" + userAuth + "</b>");
			
			String requestBody = String.format(POST_CONTENT_RESPONSE, lstExpectedContentId.get(0), Integer.parseInt(lstExpectedContentIdVersion.get(0)), lstExpectedContentTitle.get(0), programNames.get(0), lstProductId.get(0));
			Response deepLinkResponse = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(userAuth, requestBody);

			JSONObject postRequestResponse = new JSONObject(deepLinkResponse.getBody().asString());
			Log.softAssertThat(postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("status").equals("success"),
					"</br><b>PASSED:</b> Verified: Response code 200 in POST request for deeplinking content Response when basic auth is correct",
					"<b>FAILED:</b> Failed: Response code 200 is not returned in POST request for deeplinking content Response when basic auth is correct");

			String deeplinkHtmlString = postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("response");
			String jsonPayLoad = LTIAUtils.decodeJWTtokenFromDeeplinkResponse(deeplinkHtmlString, issuerId, null, null);

			JSONObject actualResponseBody = new JSONObject(jsonPayLoad);
			Log.message("JSON Deeplinking Response: <pre>"
					+ RealizeUtils.convertJSONStringtoPrettyFormat(jsonPayLoad.toString()) + "</pre>");

			String actualTitle;
			if (actualResponseBody.has(ltiContentItemsField) && !actualResponseBody.isNull(ltiContentItemsField)) {
				JSONArray actualSelectedContents = actualResponseBody.getJSONArray(ltiContentItemsField);

				Log.assertThat(actualSelectedContents.length() == totalContent,
						"Verified: Deeplinking Json Response body have same " + totalContent + " number of contents in <b>'"
								+ ltiContentItemsField + "'</b> field.",
						"Failed: Deeplinking Json Response body does not have same number of contents in <b>'" + ltiContentItemsField
								+ "'</b> field. Expected size: " + totalContent + " , Actual size: "
								+ actualSelectedContents.length());

				// verifying title
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					final String expectedTitle = lstExpectedContentTitle.get(item);
					String contentId = lstExpectedContentId.get(item);
					int findIndex = IntStream.range(0, actualSelectedContents.length()).filter(
							i -> actualSelectedContents.getJSONObject(i).getString("title").equals(expectedTitle))
							.findFirst().orElse(-1);
					if (findIndex != -1) {
						actualTitle = actualSelectedContents.getJSONObject(findIndex).getString("title");
						Log.softAssertThat(actualTitle.equals(expectedTitle),
								"Verified: Title of the content <b>'" + contentId + "'</b> in deeplink response is matched with the content from discover page. Title value - <b>'" + expectedTitle + "'</b> ",
								"Failed: Title of the content in deeplink response is not matched with the content from discover page, "
										+ "Expected Title: " + expectedTitle + ", Actual Title: " + actualTitle);
					} else {
						Log.failsoft("Failed: Deep linking response does not have content title <b>'" + expectedTitle
								+ "'</b> in the custom attribute of <b>'" + ltiContentItemsField + "'</b>");
					}
				}

				// Verify content url for each content in Deep linking response
				String expectedContentId, expectedProductId, expectedURL, actualURL;
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					expectedContentId = lstExpectedContentId.get(item);
					expectedProductId = lstProductId.get(item);
					expectedURL = ltiBaseUrl + String.format(launchEndpoint, expectedContentId + "/" + lstExpectedContentIdVersion.get(item), expectedProductId);
					actualURL = actualSelectedContents.getJSONObject(item).getString("url");
										
					Log.softAssertThat(actualURL.equals(expectedURL),
							"Verified: Content url for the content <b>'" + expectedContentId + "'</b> in <b>" + ltiContentItemsField
									+ "</b> block has correct value - <b>" + actualURL + "</b>",
							"Failed: Content url attribute is not matched for content <b>'" + expectedContentId + "'</b> in "
									+ ltiContentItemsField + " field." + " Expected: " + expectedURL + ", Actual: "
									+ actualURL);
				}

				// Verify custom field for contentId
				String actualproductId, actualContentId;
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					String contentId = lstExpectedContentId.get(item);
					expectedProductId = lstProductId.get(item);
					final String expectedContentId1 = lstExpectedContentId.get(item) + "/" + lstExpectedContentIdVersion.get(item);
					
					final String expectedTitle = lstExpectedContentTitle.get(item);
					int findIndex = IntStream.range(0, actualSelectedContents.length()).filter(
							i -> actualSelectedContents.getJSONObject(i).getString("title").equals(expectedTitle))
							.findFirst().orElse(-1);
					
					if (findIndex != -1) {
						actualContentId = actualSelectedContents.getJSONObject(item).getJSONObject("custom").getString("contentId");
						Log.softAssertThat(actualContentId.equals(expectedContentId1),
								"Verified: Content id of the content <b>'" + contentId + "'</b> in deeplink response is matched. contentId - <b>'" + expectedContentId1 + "'</b>",
								"Faield: Content id of the content in deeplink response is not matched, "
										+ "Expected contentId: " + expectedContentId1 + ", Actual contentId: " + actualContentId);
					} else {
						Log.failsoft("Deep linking response does not have contentId <b>'" + expectedContentId1
								+ "'</b> in the custom field of <b>'" + ltiContentItemsField + "'</b>");
					}
					
					if (findIndex != -1) {
						actualproductId = actualSelectedContents.getJSONObject(item).getJSONObject("custom").getString("productId");
						Log.softAssertThat(actualproductId.equals(expectedProductId),
								"Verified: Product Id of the content <b>'" + contentId + "'</b> in deeplink response is matched. productId - <b>'" + expectedProductId + "'</b>",
								"Faield: Product Id of the content in deeplink response is not matched, "
										+ "Expected productId: " + expectedProductId + ", Actual productId: " + actualproductId);
					} else {
						Log.failsoft("Deep linking response does not have productId <b>'" + expectedProductId
								+ "'</b> in the custom field of <b>'" + ltiContentItemsField + "'</b>");
					}
				}

				// Verify thumbnail field for content
				String actualThumbnailURL;
				int actualThumbnailWidth, actualThumbnailHeight;
				for (int item = 0; item < lstExpectedContentId.size(); item++) {
					String contentId = lstExpectedContentId.get(item);
					final String expectedThumbnailURL = lstExpectedThumbnailURL.get(item);
					final int expectedThumbnailWidth = lstExpectedThumbnailWidth.get(item);
					final int expectedThumbnailHeight = lstExpectedThumbnailHeight.get(item);
									
					final String expectedTitle = lstExpectedContentTitle.get(item);
					int findIndex = IntStream.range(0, actualSelectedContents.length()).filter(
							i -> actualSelectedContents.getJSONObject(i).getString("title").equals(expectedTitle))
							.findFirst().orElse(-1);
					
					if (findIndex != -1) {
						actualThumbnailURL = actualSelectedContents.getJSONObject(findIndex).getJSONObject("thumbnail").getString("url");
						Log.softAssertThat(actualThumbnailURL.equals(expectedThumbnailURL),
								"Verified: Thumbnail URL of the content <b>'" + contentId + "'</b> in deeplink response is matched. url - <b>'" + expectedThumbnailURL + "'</b>",
								"Faield: Thumbnail URL of the content in deeplink response is not matched, "
										+ "Expected URL: " + expectedThumbnailURL + ", Actual URL: " + actualThumbnailURL);
					} else {
						Log.failsoft("Deep linking response does not have url <b>'" + expectedThumbnailURL
								+ "'</b> in the Thumbnail field of <b>'" + ltiContentItemsField + "'</b>");
					}
					
					if (findIndex != -1) {
						actualThumbnailWidth = actualSelectedContents.getJSONObject(findIndex).getJSONObject("thumbnail").getInt("width");
						Log.softAssertThat(actualThumbnailWidth == expectedThumbnailWidth,
								"Verified: Thumbnail width of the content <b>'" + contentId + "'</b> in deeplink response is matched. width - <b>'" + expectedThumbnailWidth + "'</b>",
								"Faield: Thumbnail width of the content in deeplink response is not matched, "
										+ "Expected URL: " + expectedThumbnailWidth + ", Actual URL: " + actualThumbnailWidth);
					} else {
						Log.failsoft("Deep linking response does not have width <b>'" + expectedThumbnailWidth
								+ "'</b> in the Thumbnail field of <b>'" + ltiContentItemsField + "'</b>");
					}
					
					if (findIndex != -1) {
						actualThumbnailHeight = actualSelectedContents.getJSONObject(findIndex).getJSONObject("thumbnail").getInt("height");
						Log.softAssertThat(actualThumbnailHeight == expectedThumbnailHeight,
								"Verified: Thumbnail height of the content <b>'" + contentId + "'</b> in deeplink response is matched. height - <b>'" + expectedThumbnailHeight + "'</b>",
								"Faield: Thumbnail height of the content in deeplink response is not matched, "
										+ "Expected URL: " + expectedThumbnailHeight + ", Actual URL: " + actualThumbnailHeight);
					} else {
						Log.failsoft("Deep linking response does not have height <b>'" + expectedThumbnailHeight
								+ "'</b> in the Thumbnail field of <b>'" + ltiContentItemsField + "'</b>");
					}
				}
				
			} else {
				Log.failsoft("Deep linking response does not have <b>'" + ltiContentItemsField + "'</b> attribute");
			}

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
			driver.quit();
		}
	}

}