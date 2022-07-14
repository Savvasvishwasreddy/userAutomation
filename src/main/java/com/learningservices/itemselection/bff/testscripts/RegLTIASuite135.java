package com.learningservices.itemselection.bff.testscripts;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.learningservices.itemselection.bff.canvas.pages.CanvasCoursePage;
import com.learningservices.itemselection.bff.canvas.pages.CanvasDashboardPage;
import com.learningservices.itemselection.bff.canvas.pages.CanvasLoginPage;
import com.learningservices.itemselection.bff.utils.PropertyReader;
import com.learningservices.itemselection.bff.utils.RBSAPIUtils;
import com.learningservices.itemselection.bff.utils.RumbaClient;
import com.learningservices.utils.DataProviderUtils;
import com.learningservices.utils.EmailReport;
import com.learningservices.utils.Log;
import com.learningservices.utils.WebDriverFactory;

import io.restassured.response.Response;

@Listeners(EmailReport.class)
public class RegLTIASuite135 {

	private PropertyReader configProperty;
	private String webSite;
	private String caUserUsername = null;
	private String teacherUserUsername = null;
	private String teacherUserPassword =  null;
	private String studentUserUsername = null;
	private String studentUserPassword = null;
	private String password = "";
	private static final String TEACHER_ROLE = RBSAPIUtils.UMS_ROLE.Teacher.toString();

	private final static String GET_USER_PROFILE_ATTRIBUTE = "{\"operationName\":null,\"variables\":{},\"query\":\"query {\\n"
			+ " getUserProfileAttribute {\\n browseProfileWizard \\n hasLtiADomain \\n }\\n "
			+ " }\\n \"}";

	private final static String UPDATE_USER_PROFILE_ATTRIBUTE = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
			+ " updateUserProfileAttribute(\\n userProfileRequest: {userId: \\\"%s\\\"  browseProfileWizard: \\\"%s\\\" })"
			+ " {\\n responseBody \\n responseCode \\n }\\n }\\n \"}";

	private final static String SUBSCRIBE_PRODUCTS = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
			+ " subscribeProducts(\\n data:{productIds: [\\\"%s\\\"], userIds: [\\\"%s\\\"],  organizationId: \\\"%s\\\", role: \\\"%s\\\"})"
			+ " {\\n status \\n }\\n }\\n \"}";

	private final static String GET_SUBSCRIBED_PRODUCTS = "{\"operationName\":null,\"variables\":{},\"query\":\"query {\\n"
			+ " getSubscribedProducts {\\n organizationId \\n licenseId \\n licensedOrganizationId \\n licensedOrganizationDisplayName \\n programName "
			+ "\\n productId \\n productDisplayName \\n}\\n "
			+ " }\\n \"}";

	private final static String REFRESH_RBS_TOKEN = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
			+ " refreshRbsToken  {\\n message \\n access_token \\n }\\n "
			+ " }\\n \"}";

	private final static String GET_LICENSED_PRODUCTS = "{\"operationName\":null,\"variables\":{},\"query\":\"query getLicensedProducts {\\n"
			+ " getLicensedProducts  {\\n organizationId \\n licenseId \\n licensedOrganizationId \\n licensedOrganizationDisplayName \\n licensePoolType "
			+ "\\n licensePoolStatus \\n denyNewSubscription \\n programName \\n quantity \\n usedLicensesForThisOrg \\n productId \\n productDisplayName \\n "
			+ "startDate \\n endDate \\n orderedISBN \\n productShortDescription \\n productLongDescription \\n daysLeft \\n}\\n "
			+ " }\\n \"}";
	
	private final static String POST_CONTENT_RESPONSE = "{\"operationName\":\"postContentResponse\",\"variables\":{\"selectedContent\":\"[{\\\"contentId\\\":\\\"%s\\\",\\\"contentVersion\\\":%d,\\\"title\\\":\\\"%s\\\",\\\"programNames\\\":[\\\"%s\\\"],\\\"mediaType\\\":\\\"Test\\\",\\\"contentInfo\\\":{\\\"description\\\":\\\"cutscore=0\\\",\\\"materials\\\":[],\\\"keywords\\\":[],\\\"pacing\\\":\\\"\\\",\\\"author\\\":\\\"\\\",\\\"genres\\\":[],\\\"textFeatures\\\":null,\\\"contentAreas\\\":null,\\\"comprehensionSkills\\\":null,\\\"isbn\\\":\\\"\\\",\\\"notebookEntries\\\":null,\\\"activities\\\":[],\\\"__typename\\\":\\\"ContentInfo\\\"},\\\"__typename\\\":\\\"Content\\\",\\\"productId\\\":\\\"%s\\\"}]\"},\"query\":\"mutation postContentResponse($selectedContent: String!) {\\n  postContentResponse(data: {selectedContent: $selectedContent}) {\\n    status\\n    response\\n    __typename\\n  }\\n}\\n\"}";
	
	@BeforeTest
	public void init(ITestContext context) {
		webSite = (System.getProperty("webSite") != null ? System.getProperty("webSite") : context.getCurrentXmlTest().getParameter("webSite")).toLowerCase();
		System.setProperty("webSite", webSite);
		configProperty = PropertyReader.getInstance();
		password = configProperty.getProperty("DEFAULT_PASSWORD").trim();

		caUserUsername = configProperty.getProperty("ebb.district04.ca.username");
		teacherUserUsername = configProperty.getProperty("bff.browseContent.teacher.username");
		teacherUserPassword = configProperty.getProperty("bff.browseContent.teacher.password");

		studentUserUsername = configProperty.getProperty("bff.browseContent.student.username");
		studentUserPassword = configProperty.getProperty("bff.browseContent.student.password");
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 1)
	public void tc010RLZ35337(String browser) throws Exception {

		Log.testCaseInfo("RLZ35337: Verify ltia item selection bff service graphql response when getting user profile attributes with valid values");

		String requestBody = "";
		boolean browseProfileWizard = false;
		boolean hasLtiADomain = false;

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			requestBody = String.format(GET_USER_PROFILE_ATTRIBUTE);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			jsonResponseBody = jsonResponseBody.getJSONObject("data").getJSONObject("getUserProfileAttribute");
			boolean returnedBrowseProfileWizard = jsonResponseBody.getBoolean("browseProfileWizard");
			boolean returnedHasLtiADomain = jsonResponseBody.getBoolean("hasLtiADomain");

			Log.assertThat(returnedBrowseProfileWizard == browseProfileWizard && returnedHasLtiADomain == hasLtiADomain,
					"<b>PASSED:</b> browseProfileWizard and hasLtiADomain returned in the response body",
					"<b>FAILED:</b> browseProfileWizard and hasLtiADomain not returned in the response body");
			
			Log.assertThat(returnedBrowseProfileWizard == browseProfileWizard,
					"<b>PASSED:</b> Value for browseProfileWizard:" + returnedBrowseProfileWizard,
					"<b>FAILED:</b> Value for  browseProfileWizard is not correct");
		
			Log.assertThat(returnedHasLtiADomain == hasLtiADomain,
					"<b>PASSED:</b> Value for hasLtiADomain:" + returnedHasLtiADomain,
					"<b>FAILED:</b> Value for  hasLtiADomain is not correct");
			
			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 2)
	public void tc020RLZ35338(String browser) throws Exception {

		Log.testCaseInfo("RLZ35338: Verify ltia item selection bff service graphql error response when getting user "
				+ "profile attributes with invalid Authorization token");

		String requestBody = "";

		try {
			
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");

			requestBody = String.format(GET_USER_PROFILE_ATTRIBUTE);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject errorObject = jsonResponseBody.getJSONObject("extensions");
			final String errorMessage = errorObject.getString("message").split("error_description\":\"")[1]; 
			final int statusCode = errorObject.getInt("code");

			Log.assertThat(errorMessage.contains("oAuth Token auth_scope GET Failed. Reason: access_token not found"), 
					"<b>PASSED:</b> Expected error message <b>" + errorMessage + "</b> is displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(statusCode == 404,
					"<b>PASSED:</b> Statusd code " + statusCode + " is verified in the Response body",
					"<b>FAILED:</b> Error code '404' is not displayed in the Response body, "
							+ "Actual response code: " + statusCode);	

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 3)
	public void tc030RLZ35339(String browser) throws Exception {

		Log.testCaseInfo("RLZ35339: Verify ltia item selection bff service graphql error response when getting user "
				+ "profile attributes without Authorization token");

		String requestBody = "";
		String accessToken = null;

		try {

			requestBody = String.format(GET_USER_PROFILE_ATTRIBUTE);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("No authorization token found"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 4)
	public void tc040RLZ35340(String browser) throws Exception {

		Log.testCaseInfo("RLZ35340: Verify ltia item selection bff service graphql error response when getting user "
				+ "profile attributes with Student Authorization token");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Student User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserUsername, studentUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for student user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + studentUserUsername + "'");

			requestBody = String.format(GET_USER_PROFILE_ATTRIBUTE);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("Insufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 5)
	public void tc050RLZ35341(String browser) throws Exception {

		Log.testCaseInfo("RLZ35341: Verify ltia item selection bff service graphql response when updating user profile attributes with valid values");

		boolean browseProfileWizard = false;
		boolean hasLtiADomain = false;
		String accessToken = "";
		String requestBody = "";
		String updateRequestBody = "";
		String updateBrowseProfileWizard = "true";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");
			
			String userId = RumbaClient.getRumbaUserId(teacherUserUsername);

			requestBody = String.format(GET_USER_PROFILE_ATTRIBUTE);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			jsonResponseBody = jsonResponseBody.getJSONObject("data").getJSONObject("getUserProfileAttribute");
			boolean returnedBrowseProfileWizard = jsonResponseBody.getBoolean("browseProfileWizard");
			boolean returnedHasLtiADomain = jsonResponseBody.getBoolean("hasLtiADomain");

			Log.assertThat(returnedBrowseProfileWizard == browseProfileWizard && returnedHasLtiADomain == hasLtiADomain,
					"<b>PASSED:</b> browseProfileWizard and hasLtiADomain returned in the response body",
					"<b>FAILED:</b> browseProfileWizard and hasLtiADomain not returned in the response body");
			
			Log.assertThat(returnedBrowseProfileWizard == browseProfileWizard,
					"<b>PASSED:</b> Value for browseProfileWizard:" + returnedBrowseProfileWizard,
					"<b>FAILED:</b> Value for  browseProfileWizard is not correct");
		
			Log.assertThat(returnedHasLtiADomain == hasLtiADomain,
					"<b>PASSED:</b> Value for hasLtiADomain:" + returnedHasLtiADomain,
					"<b>FAILED:</b> Value for  hasLtiADomain is not correct");

			Log.message("</br><b>Update user profile attribute</b>");
			updateRequestBody = String.format(UPDATE_USER_PROFILE_ATTRIBUTE, userId, updateBrowseProfileWizard);

			Response updateResponse = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, updateRequestBody);

			JSONObject jsonResponseBodyUpdate = new JSONObject(updateResponse.getBody().asString());
			jsonResponseBodyUpdate = jsonResponseBodyUpdate.getJSONObject("data").getJSONObject("updateUserProfileAttribute");

			Log.assertThat(updateResponse.getStatusCode() == 200,
					"<b>PASSED:</b>  Verified '200' status code in Update User Profile Attribute "
							+ "with valid issuerId and browseProfileWizard",
							"<b>FAILED:</b> '200' status code is not returned in Update User Profile Attribute "
									+ "with valid issuerId and browseProfileWizard");

			Log.message("</br><b>Verify get Updated user profile attribute</b>");
			response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody2 = new JSONObject(response.getBody().asString());
			jsonResponseBody2 = jsonResponseBody2.getJSONObject("data").getJSONObject("getUserProfileAttribute");
			boolean updatedProfileWizard = jsonResponseBody2.getBoolean("browseProfileWizard");
			
			Log.softAssertThat(updatedProfileWizard == true,
					"<b>PASSED:</b> Verified: browseProfileWizard value has been updated to " + updatedProfileWizard, 
					"<b>FAILED:</b> Failed: browseProfileWizard value has not been updated to " + updatedProfileWizard);
			
			Log.message("</br><b>Test cleanip: Reset user profile attribute</b>");
			updateRequestBody = String.format(UPDATE_USER_PROFILE_ATTRIBUTE, userId, "false");
			updateResponse = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, updateRequestBody);

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 6)
	public void tc060RLZ35342(String browser) throws Exception {

		Log.testCaseInfo("RLZ35342: Verify ltia item selection bff service graphql error response when updating user "
				+ "profile attributes with invalid Authorization token");

		String issuerId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String browseProfileWizard = "true";
		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");

			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_USER_PROFILE_ATTRIBUTE, issuerId, browseProfileWizard);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject errorObject = jsonResponseBody.getJSONObject("extensions");
			final String errorMessage = errorObject.getString("message").split("error_description\":\"")[1]; 
			final int statusCode = errorObject.getInt("code");

			Log.assertThat(errorMessage.contains("oAuth Token auth_scope GET Failed. Reason: access_token not found"), 
					"<b>PASSED:</b> Expected error message <b>" + errorMessage + "</b> is displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(statusCode == 404,
					"<b>PASSED:</b> Statusd code " + statusCode + " is verified in the Response body",
					"<b>FAILED:</b> Error code '404' is not displayed in the Response body, "
							+ "Actual response code: " + statusCode);

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 7)
	public void tc070RLZ35343(String browser) throws Exception {

		Log.testCaseInfo("RLZ35343: Verify ltia item selection bff service graphql error response when updating user "
				+ "profile attributes without Authorization token");

		String issuerId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String browseProfileWizard = "true";
		String requestBody = "";
		String accessToken = null;

		try {
			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_USER_PROFILE_ATTRIBUTE, issuerId, browseProfileWizard);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("No authorization token found"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 8)
	public void tc080RLZ35344(String browser) throws Exception {

		Log.testCaseInfo("RLZ35344: Verify ltia item selection bff service graphql error response when updating user "
				+ "profile attributes with Student Authorization token");

		String issuerId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String browseProfileWizard = "true";
		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Student User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserUsername, studentUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for student user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + studentUserUsername + "'");

			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_USER_PROFILE_ATTRIBUTE, issuerId, browseProfileWizard);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("Insufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 9)
	public void tc090RLZ35345(String browser) throws Exception {

		Log.testCaseInfo("RLZ35345: Verify ltia item selection bff service graphql error response when updating user "
				+ "profile attributes without required field - browseProfileWizard");

		// input file without browseProfileWizard field
		String UPDATE_GRAPHQL_BODY = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
				+ " updateUserProfileAttribute(\\n userProfileRequest: {userId: \\\"%s\\\" })"
				+ " {\\n responseBody \\n responseCode \\n }\\n }\\n \"}";

		String issuerId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_GRAPHQL_BODY, issuerId);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 
			int statusCode = response.statusCode();

			Log.assertThat(errorMessage.equals("Field UserProfileRequest.browseProfileWizard of required type String! was not provided."), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("GRAPHQL_VALIDATION_FAILED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");		
			
			Log.assertThat(statusCode == 400,
					"<b>PASSED:</b> Statusd code " + statusCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + statusCode + " is not displayed in the Response body");	

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 10)
	public void tc100RLZ35346(String browser) throws Exception {

		Log.testCaseInfo("RLZ35346 : Verify ltia item selection bff service graphql error response when updating "
				+ "user profile attributes without required field - userId");

		// input file without userId field
		String UPDATE_GRAPHQL_BODY = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
				+ " updateUserProfileAttribute(\\n userProfileRequest: {browseProfileWizard: \\\"%s\\\" })"
				+ " {\\n responseBody \\n responseCode \\n }\\n }\\n \"}";

		String browseProfileWizard = "true";
		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_GRAPHQL_BODY, browseProfileWizard);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 
			int statusCode = response.statusCode();

			Log.assertThat(errorMessage.equals("Field UserProfileRequest.userId of required type String! was not provided."), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("GRAPHQL_VALIDATION_FAILED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");	
			
			Log.assertThat(statusCode == 400,
					"<b>PASSED:</b> Statusd code " + statusCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + statusCode + " is not displayed in the Response body");	

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 11)
	public void tc110RLZ35347(String browser) throws Exception {

		Log.testCaseInfo("RLZ35347: Verify ltia item selection bff service graphql error response when updating user "
				+ "profile attributes with unknown field");

		// input file with unknown field
		String UPDATE_GRAPHQL_BODY = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
				+ " updateUserProfileAttribute(\\n userProfileRequest: {userId: \\\"%s\\\"  browseProfileWizard: \\\"%s\\\" unknown: \\\"%s\\\"})"
				+ " {\\n responseBody \\n responseCode \\n }\\n }\\n \"}";

		String issuerId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String browseProfileWizard = "true";
		String unknown = "unknown";
		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_GRAPHQL_BODY, issuerId, browseProfileWizard, unknown);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 
			int statusCode = response.statusCode();

			Log.assertThat(errorMessage.equals("Field \"unknown\" is not defined by type UserProfileRequest."), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("GRAPHQL_VALIDATION_FAILED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");	
			
			Log.assertThat(statusCode == 400,
					"<b>PASSED:</b> Statusd code " + statusCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + statusCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 12)
	public void tc120RLZ35348(String browser) throws Exception {

		Log.testCaseInfo("RLZ35348: Verify ltia item selection bff service graphql error response when updating user "
				+ "profile attributes with UserId field empty");

		String accessToken = "";
		String requestBody = "";
		String issuerId = "";
		String browseProfileWizard = "true";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Update user profile attribute</b>");
			requestBody = String.format(UPDATE_USER_PROFILE_ATTRIBUTE, issuerId, browseProfileWizard);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("The userIds cannot be empty"),
					"<b>PASSED:</b> Expected error message " + errorMessage.toString() + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage.toString());

			Log.assertThat(errorCode.equals("BAD_REQUEST"),
					"<b>PASSED:</b> Expected error code " + errorCode.toString() + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorCode.toString());

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 13)
	public void tc130RLZ35349(String browser) throws Exception {

		Log.testCaseInfo("RLZ35349: Verify graphql response when user subscribes products with valid token in header");
		
		String accessToken = "";
		String requestBody = "";
		String productId = configProperty.getProperty("ltia.product01.id");
		String role = "Teacher";
		
		String emailId = "canvas-jaguar-c462@gedu-demo-pearson.com";
		String teacherFName = "RLZ35349";
		String lastName = "Teacher";
		String schoolOrgId = configProperty.getProperty("ebb.school02.id").trim();
		String ebTeacherRumbaUserId = "ffffffff6116d114f435a4002e3d187e";
		String realizeUserName = "teachRlz35349";
		
		HashMap<String, String> userDetails = new HashMap<String, String>();
		userDetails.put("FirstName", teacherFName);
		userDetails.put("LastName", lastName);
		userDetails.put("Gender", "Male");
		userDetails.put("UserName", realizeUserName);
		userDetails.put("Password", password);
		userDetails.put("EmailAddress", emailId);
		userDetails.put("OrganizationId", schoolOrgId);
		userDetails.put("OrgRole", TEACHER_ROLE);
	
		try {
			
			// EB-Basic user Creation
			ebTeacherRumbaUserId = RumbaClient.createUser(userDetails);
			Log.message("<br>Created EB basic user (Username : '<b>" + ebTeacherRumbaUserId + "</b>' and rumba user id: '<b>" 
					+ ebTeacherRumbaUserId + "</b>') in Jedi basic school :" + schoolOrgId);
			
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(realizeUserName, password);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + realizeUserName + "'");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS, productId, ebTeacherRumbaUserId, schoolOrgId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			jsonResponseBody = jsonResponseBody.getJSONObject("data").getJSONObject("subscribeProducts");
			Object status = jsonResponseBody.get("status");
			
			Log.assertThat(status.equals("success"),
					"<b>PASSED:</b> Expected code " + status.toString() + " displayed in Response body",
					"<b>FAILED:</b> Incorrect message displayed. Message: " + status.toString());
			
			requestBody = String.format(GET_SUBSCRIBED_PRODUCTS);

			response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject data = jsonResponseBody.getJSONObject("data");
			JSONArray productList = data.getJSONArray("getSubscribedProducts");
			
			Log.assertThat(productList.length() != 0, "Pass: Response returned subscriptions", "Fail: Response did not return subscriptions");
			
			for (int i=0; i<productList.length(); i++) {
				JSONObject item = productList.getJSONObject(i);
				String returnedProductId = item.get("productId").toString();
				String name = item.get("productDisplayName").toString();
				Log.message("productDisplayName: " + name + " and productId: " + returnedProductId + " in response");
			}
			
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
			Log.message("<b><u>Data Cleanup: Delete EB basic user</u></b>");
			if (ebTeacherRumbaUserId != null && !ebTeacherRumbaUserId.isEmpty()) {
				RumbaClient.deleteUser(ebTeacherRumbaUserId);
			}
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 14)
	public void tc140RLZ35350(String browser) throws Exception {

		Log.testCaseInfo("RLZ35350: Verify graphql response when user subscribes products with invalid token in header");

		String accessToken = "";
		String requestBody = "";
		String productId = "1937923";
		String userId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String organizationId = "8a97b1a6668d0b040166c70d05bc10f1";
		String role = "Teacher";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS, productId, userId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject errorObject = jsonResponseBody.getJSONObject("extensions");
			final String errorMessage = errorObject.getString("message").split("error_description\":\"")[1]; 
			final int statusCode = errorObject.getInt("code");

			Log.assertThat(errorMessage.contains("oAuth Token auth_scope GET Failed. Reason: access_token not found"), 
					"<b>PASSED:</b> Expected error message <b>" + errorMessage + "</b> is displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(statusCode == 404,
					"<b>PASSED:</b> Statusd code " + statusCode + " is verified in the Response body",
					"<b>FAILED:</b> Error code '404' is not displayed in the Response body, "
							+ "Actual response code: " + statusCode);
			
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 15)
	public void tc150RLZ35351(String browser) throws Exception {

		Log.testCaseInfo("RLZ35351: Verify graphql response when user subscribes products without token in header");

		String accessToken = null;
		String requestBody = "";
		String productId = "1937923";
		String userId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String organizationId = "8a97b1a6668d0b040166c70d05bc10f1";
		String role = "Teacher";

		try {
			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS, productId, userId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("No authorization token found"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 16)
	public void tc160RLZ35352(String browser) throws Exception {

		Log.testCaseInfo("RLZ35352: Verify graphql response when user subscribes products with Student token in header");

		String accessToken = "";
		String requestBody = "";
		String productId = "1937923";
		String userId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String organizationId = "8a97b1a6668d0b040166c70d05bc10f1";
		String role = "Teacher";

		try {
			Log.message("</br><b>Get Access Token for the Student User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserUsername, studentUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for student user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + studentUserUsername + "'");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS, productId, userId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
			
			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("Insufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 17)
	public void tc170RLZ35353(String browser) throws Exception {

		Log.testCaseInfo("RLZ35353: Verify graphql error response when user subscribes product with missing field values- productIds ");

		// input file with no product Ids
		String SUBSCRIBE_PRODUCTS_BODY = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
				+ " subscribeProducts(\\n data:{productIds: [], userIds: [\\\"%s\\\"],  organizationId: \\\"%s\\\", role: \\\"%s\\\"})"
				+ " {\\n status \\n }\\n }\\n \"}";

		String accessToken = "";
		String requestBody = "";
		String userId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String organizationId = "8a97b1a6668d0b040166c70d05bc10f1";
		String role = "Teacher";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS_BODY, userId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code");

			Log.assertThat(errorMessage.equals("[{\"attributeError\":\"Product id list can't be empty.\",\"attributeName\":\"productIds\"}]"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("BAD_REQUEST"), 
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 18)
	public void tc180RLZ35354(String browser) throws Exception {

		Log.testCaseInfo("RLZ35354 : Verify graphql error response when user subscribes product with missing field values - userIds ");

		// input file with no user Ids
		String SUBSCRIBE_PRODUCTS_BODY = "{\"operationName\":null,\"variables\":{},\"query\":\"mutation {\\n"
				+ " subscribeProducts(\\n data:{productIds: [\\\"%s\\\"], userIds: [],  organizationId: \\\"%s\\\", role: \\\"%s\\\"})"
				+ " {\\n status \\n }\\n }\\n \"}";

		String accessToken = "";
		String requestBody = "";
		String productId = "1937923";
		String organizationId = "8a97b1a6668d0b040166c70d05bc10f1";
		String role = "Teacher";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS_BODY, productId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code");

			Log.assertThat(errorMessage.equals("[{\"attributeError\":\"User id list can't be empty.\",\"attributeName\":\"userIds\"}]"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("BAD_REQUEST"), 
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 19)
	public void tc190RLZ35355(String browser) throws Exception {

		Log.testCaseInfo("RLZ35355 : Verify graphql error response when user subscribes product with missing field values - role ");

		String accessToken = "";
		String requestBody = "";
		String productId = "1937923";
		String userId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String organizationId = "8a97b1a6668d0b040166c70d05bc10f1";
		String role = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS, productId, userId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code");

			Log.assertThat(errorMessage.equals("[{\"attributeError\":\"Role can't be blank.\",\"attributeName\":\"role\"}]"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("BAD_REQUEST"), 
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 20)
	public void tc200RLZ35356(String browser) throws Exception {

		Log.testCaseInfo("RLZ35356: Verify graphql error response when user subscribes product with missing field values - organizationId ");

		String accessToken = "";
		String requestBody = "";
		String productId = "1937923";
		String userId = "ffffffff60b2369d6f76d0002ee7b2a6";
		String organizationId = "";
		String role = "Teacher";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			Log.message("</br><b>Get Subscribed Products</b>");
			requestBody = String.format(SUBSCRIBE_PRODUCTS, productId, userId, organizationId, role);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code");

			Log.assertThat(errorMessage.equals("[{\"attributeError\":\"Organization id can't be blank.\",\"attributeName\":\"organizationId\"}]"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("BAD_REQUEST"), 
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 21)
	public void tc210RLZ35357(String browser) throws Exception {

		Log.testCaseInfo("RLZ35357: Verify graphql response when getting Subscribed Products with valid token in header");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			requestBody = String.format(GET_SUBSCRIBED_PRODUCTS);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject data = jsonResponseBody.getJSONObject("data");
			JSONArray productList = data.getJSONArray("getSubscribedProducts");

			Log.assertThat(productList.length() != 0, "Pass: Response returned subscriptions", "Fail: Response did not return subscriptions");
			
			for (int i=0; i<productList.length(); i++) {
				JSONObject item = productList.getJSONObject(i);
				String productId = item.get("productId").toString();
				String name = item.get("productDisplayName").toString();
				Log.message("productDisplayName: " + name + " and productId: " + productId + " in response");
			}

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 22)
	public void tc220RLZ35358(String browser) throws Exception {

		Log.testCaseInfo("RLZ35358: Verify graphql error response when getting Subscribed Products with invalid token in header");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");

			requestBody = String.format(GET_SUBSCRIBED_PRODUCTS);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject errorObject = jsonResponseBody.getJSONObject("extensions");
			final String errorMessage = errorObject.getString("message").split("error_description\":\"")[1]; 
			final int statusCode = errorObject.getInt("code");

			Log.assertThat(errorMessage.contains("oAuth Token auth_scope GET Failed. Reason: access_token not found"), 
					"<b>PASSED:</b> Expected error message <b>" + errorMessage + "</b> is displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(statusCode == 404,
					"<b>PASSED:</b> Statusd code " + statusCode + " is verified in the Response body",
					"<b>FAILED:</b> Error code '404' is not displayed in the Response body, "
							+ "Actual response code: " + statusCode);

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 23)
	public void tc230RLZ35359(String browser) throws Exception {

		Log.testCaseInfo("RLZ35359: Verify graphql error response when getting Subscribed Products without token in header");

		String requestBody = "";
		String accessToken = null;

		try {

			requestBody = String.format(GET_SUBSCRIBED_PRODUCTS);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("No authorization token found"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");;

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 24)
	public void tc240RLZ35360(String browser) throws Exception {

		Log.testCaseInfo("RLZ35360: Verify graphql error response when getting Subscribed Products with Student token in header");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Student User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserUsername, studentUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for student user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + studentUserUsername + "'");

			requestBody = String.format(GET_SUBSCRIBED_PRODUCTS);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("Insufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 25)
	public void tc250RLZ35361(String browser) throws Exception {

		Log.testCaseInfo("RLZ35361: Verify graphql response when Refresh RBS Token with valid values");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			requestBody = String.format(REFRESH_RBS_TOKEN);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			jsonResponseBody = jsonResponseBody.getJSONObject("data").getJSONObject("refreshRbsToken");
			Object message = jsonResponseBody.get("message");
			Log.assertThat(message.equals("Access token was successfully refreshed."),
					"<b>PASSED:</b> Expected code " + message.toString() + " displayed in Response body",
					"<b>FAILED:</b> Incorrect message displayed. Message: " + message.toString());

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 26)
	public void tc260RLZ35362(String browser) throws Exception {

		Log.testCaseInfo("RLZ35362: Verify graphql error response when Refresh RBS Token with invalid token in header");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");

			requestBody = String.format(REFRESH_RBS_TOKEN);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject errorObject = jsonResponseBody.getJSONObject("extensions");
			final String errorMessage = errorObject.getString("message").split("error_description\":\"")[1]; 
			final int statusCode = errorObject.getInt("code");

			Log.assertThat(errorMessage.contains("oAuth Token auth_scope GET Failed. Reason: access_token not found"), 
					"<b>PASSED:</b> Expected error message <b>" + errorMessage + "</b> is displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(statusCode == 404,
					"<b>PASSED:</b> Statusd code " + statusCode + " is verified in the Response body",
					"<b>FAILED:</b> Error code '404' is not displayed in the Response body, "
							+ "Actual response code: " + statusCode);

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 27)
	public void tc270RLZ35363(String browser) throws Exception {

		Log.testCaseInfo("RLZ35363: Verify graphql error response when Refresh RBS Token without token in header");

		String requestBody = "";
		String accessToken = null;

		try {
			requestBody = String.format(REFRESH_RBS_TOKEN);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("No authorization token found"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 28)
	public void tc280RLZ35364(String browser) throws Exception {

		Log.testCaseInfo("RLZ35364: Verify graphql error response when Refresh RBS Token with Student token in header");

		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Student User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserUsername, studentUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for student user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + studentUserUsername + "'");

			requestBody = String.format(REFRESH_RBS_TOKEN);

			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("Insufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 29)
	public void tc290RLZ35365(String browser) throws Exception {

		Log.testCaseInfo("RLZ35365: Verify graphql response when get LicensedProducts with valid values");

		String requestBody = "";
		String licensedOrganizationId = "8a97b1cf692b37ce016950ae243d05ec";
		String licensedOrganizationDisplayName = "JaguarBasicDistrict";
		String productId = "421023";
		String productDisplayName = "enVisionMATH Texas 2.0 Grade 2";
		String licensePoolType = "Student seat based licensing";
		String licensePoolStatus = "A";
		
		try {
			Log.message("</br><b>Get Access Token for the customer admin User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(caUserUsername, password);
			if (accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for customer admin user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + caUserUsername + "'");

			requestBody = String.format(GET_LICENSED_PRODUCTS);

			Response response = RBSAPIUtils.getBrowseContentGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject data = jsonResponseBody.getJSONObject("data");
			JSONArray productList = data.getJSONArray("getLicensedProducts");

			for (int i=0; i<productList.length(); i++) {
				JSONObject item = productList.getJSONObject(i);
				if(item.get("productId").toString().equals(productId)) {

					Log.assertThat(productList.length() != 0, "Pass: Response returned LicensedProducts", 
							"Fail: Response did not return LicensedProducts");
					Log.message("Returns " + productList.length() + " LicensedProducts");

					Log.assertThat(licensedOrganizationId.equals(item.getString("licensedOrganizationId")),
							"Verified: licensedOrganizationId is: " + licensedOrganizationId, 
							"Fail: licensedOrganizationId is not " + licensedOrganizationId);

					Log.assertThat(licensedOrganizationDisplayName.equals(item.getString("licensedOrganizationDisplayName")),
							"Verified: licensedOrganizationDisplayName is: " + licensedOrganizationDisplayName, 
							"Fail: licensedOrganizationDisplayName is not " + licensedOrganizationDisplayName);

					Log.assertThat(productId.equals(item.getString("productId")),
							"Verified: productId is: " + productId, 
							"Fail: productId is not " + productId);

					Log.assertThat(productDisplayName.equals(item.getString("productDisplayName")),
							"Verified: productDisplayName is: " + productDisplayName, 
							"Fail: productDisplayName is not " + productDisplayName);

					Log.assertThat(licensePoolType.equals(item.getString("licensePoolType")),
							"Verified: licensePoolType is: " + licensePoolType, 
							"Fail: licensePoolType is not " + licensePoolType);

					Log.assertThat(licensePoolStatus.equals(item.getString("licensePoolStatus")),
							"Verified: licensePoolStatus is: " + licensePoolStatus, 
							"Fail: licensePoolStatus is not " + licensePoolStatus);
					break;
				}
			}

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 30)
	public void tc300RLZ35366(String browser) throws Exception {

		Log.testCaseInfo("RLZ35366: Verify graphql response when get LicensedProducts with invalid token in header");

		String requestBody = "";
		String accessToken = "";

		try { 
			Log.message("</br><b>Get Access Token for the customer Admin User</b>");

			accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(caUserUsername, password);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for customer admin user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + caUserUsername + "'");

			accessToken = accessToken.concat("abc");
			Log.message("Invalid Authorization token is : <b>" + accessToken + "</b>");

			requestBody = String.format(GET_LICENSED_PRODUCTS);

			Response response = RBSAPIUtils.getBrowseContentGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("Authentication Error"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 31)
	public void tc310RLZ35367(String browser) throws Exception {

		Log.testCaseInfo("RLZ35367: Verify graphql response when get LicensedProducts without token in header ");

		String requestBody = "";
		String accessToken = null;

		try {
			requestBody = String.format(GET_LICENSED_PRODUCTS);

			Response response = RBSAPIUtils.getBrowseContentGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			final String errorMessage = errorObject.getString("message"); 
			final String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("No authorization token found"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("UNAUTHENTICATED"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}
	
	@Test(groups = { "RGHT-149290", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 32)
	public void tc320RLZ35368(String browser) throws Exception {

		Log.testCaseInfo("RLZ35368: Verify graphql response when get LicensedProducts with Teacher or Student token in header");

		String requestBody = "";
	
		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String teachAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(teachAccessToken != null && !teachAccessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + teachAccessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");
	
			requestBody = String.format(GET_LICENSED_PRODUCTS);

			Response response = RBSAPIUtils.getBrowseContentGraphQLResponse(teachAccessToken, requestBody);
			
			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONArray errors = jsonResponseBody.getJSONArray("errors");
			JSONObject errorObject = errors.getJSONObject(0);
			String errorMessage = errorObject.getString("message"); 
			String errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("InSufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");
			
			Log.message("</br><b>Get Access Token for the Student User</b>");
			String studAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(studentUserUsername, studentUserPassword);
			if(studAccessToken != null && !studAccessToken.isEmpty())
				Log.message("Access token for student user is : <b>" + studAccessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + studentUserUsername + "'");
	
			response = RBSAPIUtils.getBrowseContentGraphQLResponse(studAccessToken, requestBody);
			
			 jsonResponseBody = new JSONObject(response.getBody().asString());
			 errors = jsonResponseBody.getJSONArray("errors");
			 errorObject = errors.getJSONObject(0);
			 errorMessage = errorObject.getString("message"); 
			 errorCode = errorObject.getJSONObject("extensions").getString("code"); 

			Log.assertThat(errorMessage.equals("InSufficient Permissions"), 
					"<b>PASSED:</b> Expected error message " + errorMessage + " displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(errorCode.equals("FORBIDDEN"),
					"<b>PASSED:</b> Error code " + errorCode + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " is not displayed in the Response body");
			
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
		}
	}

	@Test(groups = { "RGHT-149290", "RGHT-153191", "Functional", "P1", "API", "R_LTIA" },
			dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 33)
	public void tc330RLZ35409(String browser) throws Exception {

		Log.testCaseInfo("RLZ35409: Verify ltia-item-selection-bff-service graphql via Postman when selection submit has backend validation error");

		final WebDriver driver = WebDriverFactory.get(browser);
		String contentResponseEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();
		
		String requestBody = "";
		JSONObject postRequestResponse = null, postRequestResponse1 = null;
		
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
			
			requestBody = String.format(POST_CONTENT_RESPONSE, "ead1c97b-4f50-3d69-9a4a-7222e1aff88c", 12, "LTIA Mathxl Homework 4-3 Mixed Review ST", "LTIA Selenium Product A", "2044737");
										
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
			postRequestResponse1 = new JSONObject(response.getBody().asString());

			Log.softAssertThat(postRequestResponse1.getJSONObject("data").getJSONObject("postContentResponse").getString("status").equals("success"),
					"<b>PASSED:</b> Verified '200' status code in POST request for deeplinking content Response",
					"<b>FAILED:</b> '200' status code is not returned in POST request for deeplinking content Response");
			
			Log.message("Performing POST Request for " + contentResponseEndpoint);
			response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);
			postRequestResponse = new JSONObject(response.getBody().asString());
			Log.assertThat(postRequestResponse.getJSONObject("data").getJSONObject("postContentResponse").getString("response").contains("500"),
					"<b>PASSED:</b> Verified '500' status code in second POST request",
					"<b>FAILED:</b> '500' status code is not returned in second POST request");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();	
			driver.quit();
		}
	}

	//new 3 test cases for RGHT-153191
	@Test(groups = { "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" }, dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 34)
	public void tc340RLZ38077(String browser) throws Exception {
		Log.testCaseInfo("RLZ38077 : Verify ltia-item-selection-bff-service graphql via Postman when selection submit with Expired token");

		String contentResponseEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();
		String requestBody = "";
		String accessToken = "VuVpNHO9n0PtHG4rUcPKDouwk9npk8wwSkhmdLrVzmxbI42qGvVCeMbq9WTy7kcuo98VgKfC9C2uDMN76foAhTofDbDltEeOuUHnfUKTzrRhmybZ1RxAKtnFtnssKVC9ltjxfi8hgOLlzFIKT97NCCBl3ok6xkVXJSUlONAOoBWEtsTNruAAu3geJKEoDQ4TgPoM7ycM9LTVtHCxhmWsfVbsnT5ocMe3HtybMxh7ozxCQ1KNd1ptzBD8vHJAf";

		try {

			Log.message("Performing POST Request for " + contentResponseEndpoint);
			requestBody = String.format(POST_CONTENT_RESPONSE, "ead1c97b-4f50-3d69-9a4a-7222e1aff88c", 12, "LTIA Mathxl Homework 4-3 Mixed Review ST", "LTIA Selenium Product A", "2044737");
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject jsonResponseBody = new JSONObject(response.getBody().asString());
			JSONObject errorObject = jsonResponseBody.getJSONObject("extensions");
			final String errorMessage = errorObject.getString("message").split("error_description\":\"")[1]; 
			final int statusCode = errorObject.getInt("code");

			Log.assertThat(errorMessage.contains("oAuth Token auth_scope GET Failed. Reason: access_token not found"), 
					"<b>PASSED:</b> Expected error message <b>" + errorMessage + "</b> is displayed in Response body",
					"<b>FAILED:</b> Incorrect error message displayed. Message: " + errorMessage);

			Log.assertThat(statusCode == 404,
					"<b>PASSED:</b> Verified '404' status code in POST request for bff graphql  with Expired token",
					"<b>FAILED:</b> '404' status code is not returned in POST request for bff graphql  with Expired token");
			
			Log.testCaseResult();
		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
		}
	}
	
	@Test(groups = { "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" }, dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 35)
	public void tc350RLZ38078(String browser) throws Exception {
		Log.testCaseInfo("RLZ38078 : Verify ltia-item-selection-bff-service graphql via Postman when selection submit with without required field");

		String POST_CONTENT_RESPONSE = "{\"operationName\":\"postContentResponse\",\"query\":\"mutation {\\r\\n  postContentResponse(data: {\\r\\n  }) {\\r\\n    status\\r\\n    response\\r\\n  }\\r\\n}\\r\\n\"}";
		String requestBody = "";
		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			requestBody = String.format(POST_CONTENT_RESPONSE);
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject postRequestResponse = new JSONObject(response.getBody().asString());
			String errorMessage = postRequestResponse.getJSONArray("errors").getJSONObject(0).getString("message").toString();
			String errorCode = postRequestResponse.getJSONArray("errors").getJSONObject(0).getJSONObject("extensions").getString("code");
			Log.assertThat(errorMessage.equals("Field PostContentRequest.selectedContent of required type String! was not provided.") && errorCode.equals("GRAPHQL_VALIDATION_FAILED"),
					"<b>PASSED:</b> Error code " + errorCode + " and message " + errorMessage + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " and message " + errorMessage + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
		}
	}
	
	@Test(groups = { "RGHT-153191", "Functional", "API", "P1",	"R_LTIA" }, dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", priority = 36)
	public void tc360RLZ38079(String browser) throws Exception {
		Log.testCaseInfo("RLZ38079 : Verify ltia-item-selection-bff-service graphql via Postman when selection submit with unknown field");

		String POST_CONTENT_RESPONSE = "{\"operationName\":\"postContentResponse\",\"query\":\"mutation {\\r\\n  postContentResponse(data: {\\r\\n    selectedContent: \\\"[{\\\\\\\"contentId\\\\\\\":\\\\\\\"929f0589-c666-37c9-b886-c04bb907c303\\\\\\\",\\\\\\\"contentVersion\\\\\\\":8,\\\\\\\"title\\\\\\\":\\\\\\\"LTIA TestNav Manual and auto scoring Assessment ST\\\\\\\",\\\\\\\"programNames\\\\\\\":[\\\\\\\"LTIA Selenium Product A\\\\\\\"],\\\\\\\"mediaType\\\\\\\":\\\\\\\"Test\\\\\\\",\\\\\\\"contentInfo\\\\\\\":{\\\\\\\"description\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"materials\\\\\\\":[],\\\\\\\"keywords\\\\\\\":[],\\\\\\\"pacing\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"author\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"genres\\\\\\\":[],\\\\\\\"textFeatures\\\\\\\":null,\\\\\\\"contentAreas\\\\\\\":null,\\\\\\\"comprehensionSkills\\\\\\\":null,\\\\\\\"isbn\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"notebookEntries\\\\\\\":null,\\\\\\\"activities\\\\\\\":[],\\\\\\\"__typename\\\\\\\":\\\\\\\"ContentInfo\\\\\\\"},\\\\\\\"__typename\\\\\\\":\\\\\\\"Content\\\\\\\",\\\\\\\"productId\\\\\\\":\\\\\\\"2044737\\\\\\\"}]\\\",\\r\\n    unknownField: \\\"\\\"\\r\\n  }) {\\r\\n    status\\r\\n    response\\r\\n  }\\r\\n}\\r\\n\"}";
		String requestBody = "";

		try {
			Log.message("</br><b>Get Access Token for the Teacher User</b>");
			String accessToken = RBSAPIUtils.getAccessTokenUsingCastGC(teacherUserUsername, teacherUserPassword);
			if(accessToken != null && !accessToken.isEmpty())
				Log.message("Access token for teacher user is : <b>" + accessToken + "</b>");
			else
				Log.fail("Unable to get user authorization from RBS for the user '" + teacherUserUsername + "'");

			requestBody = String.format(POST_CONTENT_RESPONSE);
			Response response = RBSAPIUtils.getLtiaItemSelectionGraphQLResponse(accessToken, requestBody);

			JSONObject postRequestResponse = new JSONObject(response.getBody().asString());
			String errorMessage = postRequestResponse.getJSONArray("errors").getJSONObject(0).getString("message").toString();
			String errorCode = postRequestResponse.getJSONArray("errors").getJSONObject(0).getJSONObject("extensions").getString("code");
			Log.assertThat(errorMessage.equals("Field \"unknownField\" is not defined by type PostContentRequest.") && errorCode.equals("GRAPHQL_VALIDATION_FAILED"),
					"<b>PASSED:</b> Error code " + errorCode + " and message " + errorMessage + " verified in the Response body",
					"<b>FAILED:</b> Error code " + errorCode + " and message " + errorMessage + " is not displayed in the Response body");

		} catch (Exception e) {
			Log.exception(e);
		} finally {
			Log.endTestCase();
		}
	}
}
