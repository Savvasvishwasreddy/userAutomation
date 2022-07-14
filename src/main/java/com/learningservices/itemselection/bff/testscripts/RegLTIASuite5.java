package com.learningservices.itemselection.bff.testscripts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.learningservices.itemselection.bff.canvas.pages.CanvasCoursePage;
import com.learningservices.itemselection.bff.canvas.pages.CanvasDashboardPage;
import com.learningservices.itemselection.bff.canvas.pages.CanvasLoginPage;
import com.learningservices.itemselection.bff.utils.EnvironmentPropertiesReader;
import com.learningservices.itemselection.bff.utils.HTMLDataParser;
import com.learningservices.itemselection.bff.utils.LTIAUtils;
import com.learningservices.itemselection.bff.utils.PropertyReader;
import com.learningservices.itemselection.bff.utils.RBSAPIUtils;
import com.learningservices.itemselection.bff.utils.RumbaClient;
import com.learningservices.utils.DataProviderUtils;
import com.learningservices.utils.EmailReport;
import com.learningservices.utils.Log;
import com.learningservices.utils.WebDriverFactory;

import io.restassured.response.Response;

@Listeners(EmailReport.class)
public class RegLTIASuite5 {
	
	private static PropertyReader configProperty;
	private static EnvironmentPropertiesReader envProperty;
	private HashMap<String, String> ltiRequestHeaders = new HashMap<String, String>();
	private HashMap<String, String> queryParam = new HashMap<String, String>();
	private static JSONObject deepLinkRequest = new JSONObject();
	private static JSONObject jsonRequestBody = new JSONObject();
	private static Response deepLinkResponse = null;
	private static String jsonPayLoad = null;
	private List<String> programNames = null;
	private String schoolName = null;
	private String password = "";
	private String webSite;

	private final static String POST_CONTENT_RESPONSE = "{\"operationName\":\"postContentResponse\",\"variables\":{\"selectedContent\":\"[{\\\"contentId\\\":\\\"%s\\\",\\\"contentVersion\\\":%d,\\\"title\\\":\\\"%s\\\",\\\"programNames\\\":[\\\"%s\\\"],\\\"mediaType\\\":\\\"Test\\\",\\\"contentInfo\\\":{\\\"description\\\":\\\"cutscore=0\\\",\\\"materials\\\":[],\\\"keywords\\\":[],\\\"pacing\\\":\\\"\\\",\\\"author\\\":\\\"\\\",\\\"genres\\\":[],\\\"textFeatures\\\":null,\\\"contentAreas\\\":null,\\\"comprehensionSkills\\\":null,\\\"isbn\\\":\\\"\\\",\\\"notebookEntries\\\":null,\\\"activities\\\":[],\\\"__typename\\\":\\\"ContentInfo\\\"},\\\"__typename\\\":\\\"Content\\\",\\\"productId\\\":\\\"%s\\\"}]\"},\"query\":\"mutation postContentResponse($selectedContent: String!) {\\n  postContentResponse(data: {selectedContent: $selectedContent}) {\\n    status\\n    response\\n    __typename\\n  }\\n}\\n\"}";
	
	@BeforeTest
	public void init(ITestContext context) {
		webSite = (System.getProperty("webSite") != null ? System.getProperty("webSite") : context.getCurrentXmlTest().getParameter("webSite")).toLowerCase();
		System.setProperty("webSite", webSite);
		configProperty = PropertyReader.getInstance();
		envProperty = EnvironmentPropertiesReader.getInstance("bff_config");
		schoolName = configProperty.getProperty("ebb.district04.school01.name");
		programNames = Arrays.asList(configProperty.getProperty("ims.platform1.product01.name"));
		password = configProperty.getProperty("DEFAULT_PASSWORD").trim();
	}

	@Test(groups = { "IDAM-360", "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 1)
	public void tc010REA447001(String browser) throws Exception {
		Log.testCaseInfo("REA447001 : Verify ltia-item-selection-bff-service contentResponse via Postman when selection submit with valid values");
		
		final WebDriver driver = WebDriverFactory.get(browser);
		String contentResponseEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();
		
		String requestBody = "";
		String teachLogin = "Teacher BFFtest";
		String teacherRumbaId = "ffffffff6116a7c66948d2002fb00371";
		String canvasLoginUrl = configProperty.getProperty("canvas.savvas.login.url");
		String externalToolName = configProperty.getProperty("canvas.savvas.ltia.externalTool");

		try {
			
			Log.message("</br><b>Login as Teacher user to import content from Discover page</b>");
			CanvasLoginPage canvasLoginPage = new CanvasLoginPage(driver, canvasLoginUrl).get();
			CanvasDashboardPage canvasDashboardPage = canvasLoginPage.loginToCanvas(teachLogin, password, true);
			Log.assertThat(canvasDashboardPage.verifyCanvasDashboardPage(true), 
					"Canvas page is launched successfully",
					"Test Failed: Canvas page is not loaded properly", driver);
			
			canvasDashboardPage.sideNav.navigateToCoursesPage("BFFtest Course", true);
			CanvasCoursePage canvasCoursePage = new CanvasCoursePage(driver).get();
			canvasCoursePage.clickAddContentToModule("LTIA Module", true);
			canvasCoursePage.selectExternalToolResource(true);
			canvasCoursePage.launchExternalTool(externalToolName, true);
			
			if (!RumbaClient.updateUserPassword(teacherRumbaId, password)) {
				Log.fail("Not able to reset user '" + teacherRumbaId + "' password");
			}
			String teacherUserName = RumbaClient.getUsernameByUserId(teacherRumbaId);
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserName, password);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for user is : <b>" + accessToken + "</b>");
			
			Log.message("Performing POST Request for " + contentResponseEndpoint);
			requestBody = String.format(POST_CONTENT_RESPONSE, "ead1c97b-4f50-3d69-9a4a-7222e1aff88c", 12, "LTIA Mathxl Homework 4-3 Mixed Review ST", "LTIA Selenium Product A", "2044737");
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
										
			JSONObject postRequestResponse = new JSONObject(response.getBody().asString());
			Log.softAssertThat(postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("status").equals("success"),
					"<b>PASSED:</b> Verified '200' status code in POST request for deeplinking content Response",
					"<b>FAILED:</b> '200' status code is not returned in POST request for deeplinking content Response");
			
			String deeplinkResponseBody = postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("response");
			HTMLDataParser.verifyGivenStringIsHTML(deeplinkResponseBody);
			String JWT = HTMLDataParser.getJWTtokenFromDeeplinkingResponseForm(deeplinkResponseBody);
			Log.softAssertThat(LTIAUtils.isJWTSigned(JWT),
					"JWT input field from HTML response contains encoded JWT token", 
					"JWT input field from HTML response doesn't contains encode JWT token. Actual value: " + JWT);

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			Log.endTestCase();
			driver.quit();
		}
	}

	@Test(groups = { "IDAM-360", "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 2)
	public void tc020REA447002(String browser) throws Exception {
		Log.testCaseInfo("REA447002 : Verify ltia-item-selection-bff-service contentResponse via Postman when selection submit with invalid token");
		final WebDriver driver = WebDriverFactory.get(browser);

		String contentResponseEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();		
		
		String requestBody = "";
		String teachLogin = "Teacher BFFtest";
		String teacherRumbaId = "ffffffff6116a7c66948d2002fb00371";
		String canvasLoginUrl = configProperty.getProperty("canvas.savvas.login.url");
		String externalToolName = configProperty.getProperty("canvas.savvas.ltia.externalTool");

		try {
			
			Log.message("</br><b>Login as Teacher user to import content from Discover page</b>");
			CanvasLoginPage canvasLoginPage = new CanvasLoginPage(driver, canvasLoginUrl).get();
			CanvasDashboardPage canvasDashboardPage = canvasLoginPage.loginToCanvas(teachLogin, password, true);
			Log.assertThat(canvasDashboardPage.verifyCanvasDashboardPage(true), 
					"Canvas page is launched successfully",
					"Test Failed: Canvas page is not loaded properly", driver);
			
			canvasDashboardPage.sideNav.navigateToCoursesPage("BFFtest Course", true);
			CanvasCoursePage canvasCoursePage = new CanvasCoursePage(driver).get();
			canvasCoursePage.clickAddContentToModule("LTIA Module", true);
			canvasCoursePage.selectExternalToolResource(true);
			canvasCoursePage.launchExternalTool(externalToolName, true);
			
			if (!RumbaClient.updateUserPassword(teacherRumbaId, password)) {
				Log.fail("Not able to reset user '" + teacherRumbaId + "' password");
			}
			
			String teacherUserName = RumbaClient.getUsernameByUserId(teacherRumbaId);
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserName, password);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for user is : <b>" + accessToken + "</b>");
			
			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");
			
			Log.message("Performing POST Request for " + contentResponseEndpoint);
			requestBody = String.format(POST_CONTENT_RESPONSE, "ead1c97b-4f50-3d69-9a4a-7222e1aff88c", 12, "LTIA Mathxl Homework 4-3 Mixed Review ST", "LTIA Selenium Product A", "2044737");
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
										
			JSONObject postRequestResponse = new JSONObject(response.getBody().asString());
			String errorMessage = postRequestResponse.getJSONObject("extensions").get("message").toString();
			String errorCode = postRequestResponse.getJSONObject("extensions").get("code").toString();
			Log.softAssertThat(errorCode.equals("404") && errorMessage.equals("404 - {\"error_description\":\"oAuth Token auth_scope GET Failed. Reason: access_token not found\",\"status\":404,\"error\":\"not_found\"}"),
					"<b>PASSED:</b> Verified '" + errorMessage + "' message in POST request for deeplinking content Response with incorrect basic auth",
					"<b>FAILED:</b> '" + errorMessage + "' message is not returned in POST request for deeplinking content Response with incorrect basic auth");

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
			driver.quit();
		}
	}
	
	@Test(groups = { "IDAM-360", "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 3)
	public void tc030REA447003(String browser) throws Exception {
		Log.testCaseInfo("REA447003 : Verify ltia-item-selection-bff-service graphql via Postman when selection submit without token");
		
		String contentResponseEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();
		String requestBody = "";
		String accessToken = null;
		
		try {
		
			Log.message("Performing POST Request for " + contentResponseEndpoint);
			requestBody = String.format(POST_CONTENT_RESPONSE, "ead1c97b-4f50-3d69-9a4a-7222e1aff88c", 12, "LTIA Mathxl Homework 4-3 Mixed Review ST", "LTIA Selenium Product A", "2044737");
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
			
			JSONObject postRequestResponse = new JSONObject(response.getBody().asString());
			String errorMessage = postRequestResponse.getJSONArray("errors").getJSONObject(0).getString("message").toString();
			String errorCode = postRequestResponse.getJSONArray("errors").getJSONObject(0).getJSONObject("extensions").getString("code");
			Log.softAssertThat(errorMessage.equals("No authorization token found") && errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Verified '" + errorCode + "' status code and '" + errorMessage + "' message in POST request for deeplinking content Response with no basic auth",
					"<b>FAILED:</b> '" + errorCode + "' status code and '" + errorMessage + "' message  is not returned in POST request for deeplinking content Response with no basic auth");

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
		}
	}
	
	@Test(groups = { "IDAM-360", "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 4)
	public void tc040REA447004(String browser) throws Exception {
		Log.testCaseInfo("REA447004 : Verify ltia-item-selection-bff-service graphql via Postman when selection submit with Student token");
	
		final WebDriver driver = WebDriverFactory.get(browser);
		String contentResponseEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();
		
		String teacherLogin = "Teacher BFFtest ";
		String studentRumbaId = "ffffffff6116c615434a25002ec3d719";
		String canvasLoginUrl = configProperty.getProperty("canvas.savvas.login.url");
//		String externalToolName = configProperty.getProperty("canvas.savvas.ltia.externalTool");
		String contentName = "LTIA Prod PDF with Tools ST";
		String requestBody = "";

		try {
			
			Log.message("</br><b>Login as Teacher user to import content from Discover page</b>");
			CanvasLoginPage canvasLoginPage = new CanvasLoginPage(driver, canvasLoginUrl).get();
			CanvasDashboardPage canvasDashboardPage = canvasLoginPage.loginToCanvas(teacherLogin, password, true);
			Log.assertThat(canvasDashboardPage.verifyCanvasDashboardPage(true), 
					"Canvas page is launched successfully",
					"Test Failed: Canvas page is not loaded properly", driver);
			
			canvasDashboardPage.sideNav.navigateToCoursesPage("BFFtest Course", true);
			CanvasCoursePage canvasCoursePage = new CanvasCoursePage(driver).get();
			canvasCoursePage.launchContent(contentName, true);
			
			if (!RumbaClient.updateUserPassword(studentRumbaId, password)) {
				Log.fail("Not able to reset user '" + studentRumbaId + "' password");
			}
			String studentUserName = RumbaClient.getUsernameByUserId(studentRumbaId);
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserName, password);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for user is : <b>" + accessToken + "</b>");
			
			
			Log.message("Performing POST Request for " + contentResponseEndpoint);
			requestBody = String.format(POST_CONTENT_RESPONSE, "ead1c97b-4f50-3d69-9a4a-7222e1aff88c", 12, "LTIA Mathxl Homework 4-3 Mixed Review ST", "LTIA Selenium Product A", "2044737");
										
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
			
			JSONObject postRequestResponse = new JSONObject(response.getBody().asString());
			String errorMessage = postRequestResponse.getJSONArray("errors").getJSONObject(0).getString("message").toString();
			String errorCode = postRequestResponse.getJSONArray("errors").getJSONObject(0).getJSONObject("extensions").getString("code").toString();
			Log.softAssertThat(errorCode.equals("FORBIDDEN") && errorMessage.equals("Insufficient Permissions"),
					"<b>PASSED:</b> Verified '" + errorCode + "' status code and '" + errorMessage + "' message in POST request for deeplinking content Response with student token",
					"<b>FAILED:</b> '" + errorCode + "' status code and '" + errorMessage + "' message  is not returned in POST request for deeplinking content Response with student token");
			
			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
			driver.quit();
		}
	}

}