package com.learningservices.itemselection.bff.utils;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.learningservices.utils.Log;
import com.learningservices.utils.RestAssuredAPI;

import io.restassured.response.Response;

public class RumbaClient {

	private final static String contentType = "application/soap+xml";
	private static PropertyReader configProperty = PropertyReader.getInstance();	
	private static final String ulcReadUrlProd = "https://user.rumba.pk12ls.com/UserLifeCycle/services/V3";
	private static final String ulcReadUrlNightly = "https://nightly-user.rumba.pk12ls.com/UserLifeCycle/services/V3";
	private static final String ulcReadUrlPPe = "https://ppe-user.rumba.pk12ls.com/UserLifeCycle/services/V3";
	private static final String ulcReadUrlCert = "https://cert-user.rumba.pk12ls.com/UserLifeCycle/services/V3";

	/** Gets the Rumba userId for given userName
	 *
	 * @param userName the user name
	 * @return the rumba userId
	 **/
	public static String getRumbaUserId(String userName) {
		Log.event("Getting Rumba UserId for " + userName);
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		String rumbaId = null;
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUserByUserName");
		int retryAttempt = 0;
		
		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUserByUserNameRequest>\n"
						+ "         <v3:UserName>"
						+ userName
						+ "</v3:UserName>\n"
						+ "      </v3:GetUserByUserNameRequest>\n"
						+ "   </soap:Body>\n" + "</soap:Envelope>";

		try {
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			while (response.getStatusCode() == 404 && retryAttempt < 5) {
				retryAttempt++;
				RealizeUtils.nap(5); // wait in seconds
				Log.message("Retrying after 5 sec, attempt: " + retryAttempt);
				response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			}

			if (response.getStatusCode() == 200) {
				String responseBody = response.getBody().asString();
				if (responseBody.contains("UserId>")) {
					rumbaId = responseBody.split("UserId>")[1].split("<")[0];
				} else if (responseBody.contains("FAILURE")) {
					String errorMessage = responseBody.split("exception>")[1].split("<")[0];
					Log.message(RealizeUtils.WARN_HTML_BEGIN + errorMessage + RealizeUtils.WARN_HTML_END);
				}
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get rumba id for given user name '" + userName
						+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get userId by username" + RealizeUtils.FAIL_HTML_END
					+ ", Error: " + e.getMessage());
		}
		return rumbaId;
	}

	/** Gets the UserName by user id.
	 *
	 * @param userId the user id
	 * @return the UserName by user id
	 **/
	public static String getUsernameByUserId(String userId) {
		String rumbaUserName = "";
		Log.event("Getting Rumba username for " + userId);
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUser");
		int retryAttempt = 0;
		
		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUserRequest>\n"
						+ "         <v3:UserId>"
						+ userId
						+ "</v3:UserId>\n"
						+ "      </v3:GetUserRequest>\n"
						+ "   </soap:Body>\n" + "</soap:Envelope>";

		try {
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			while (response.getStatusCode() == 404 && retryAttempt < 5) {
				retryAttempt++;
				RealizeUtils.nap(5); // wait in seconds
				Log.message("Retrying after 5 sec, attempt: " + retryAttempt);
				response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			}
			
			if (response.getStatusCode() == 200) {
				String responseBody = response.getBody().asString();
				if (responseBody.contains("UserName>")) {
					rumbaUserName = responseBody.split("UserName>")[1].split("<")[0];
				} else if (responseBody.contains("FAILURE")) {
					String errorMessage = responseBody.split("exception>")[1].split("<")[0];
					Log.message(RealizeUtils.WARN_HTML_BEGIN + errorMessage + RealizeUtils.WARN_HTML_END);
				}
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get username for given rumba userId '" + userId
						+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get username by userId" + RealizeUtils.FAIL_HTML_END
					+ ", Error: " + e.getMessage());
		}
		return rumbaUserName;
	}

	/** Gets the Rumba UserID by user email ID
	 *
	 * @param emailID  The user email id
	 * @param optional If parameter available(true/false) then it will not retry to find userId when status code is 200 but userId is empty. 
	 *                 If parameter not available then it will retry to find userId when status code is 200 but userId is empty.
	 * @return List of Rumba user id registered with given email id
	 * @throws Exception 
	 **/
	public static List<String> getUserIdByEmail(String emailID, Boolean... optional) throws Exception {
		Log.message("<u>Getting rumba user id by given user email id in Rumba:</u> <b>'" + emailID + "'</b>");
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUsersByEmail");
		int retryAttemptWhenStatusNot200 = 0;
		int retryAttemptWhenUserIdIsEmpty = 0;
		boolean status = false;
		String[] userIds;
		
		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUsersByEmailRequest>\n"
						+ "         <v3:Email>"
						+ emailID
						+ "</v3:Email>\n"
						+ "      </v3:GetUsersByEmailRequest>\n" 
						+ "   </soap:Body>\n" + "</soap:Envelope>";

		ArrayList<String> resultIds = null;
		try {
			resultIds = new ArrayList<>();
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			
			// if status code is 404 or 500, then retry 5 times
			while (response.getStatusCode() == 404 || response.getStatusCode() == 500 && retryAttemptWhenStatusNot200 < 5) {
				retryAttemptWhenStatusNot200++;
				RealizeUtils.nap(5); // wait in seconds
				Log.message("Status code: " + response.getStatusCode() + " while getting userId from Rumba . Retrying after 5 sec, attempt: " + retryAttemptWhenStatusNot200);
				response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			}
			
			// if status code is 200, optional parameter is not available and resultIds is empty, then retry 3 times
			while(optional.length == 0 && resultIds.size() == 0 && retryAttemptWhenUserIdIsEmpty < 3) {
				retryAttemptWhenUserIdIsEmpty ++;
				if (response.getStatusCode() == 200) {
					String responseBody = response.getBody().asString();
					if (responseBody.contains("Users>")) {
						userIds = responseBody.split("UserId>");
						for (String id : userIds) {
							if (!(id.endsWith("</ns6:") && id.length() == 38)) {
								continue;
							} else {
								resultIds.add(id.substring(0, id.length() - 6));
								status = true;
							}
						}
					} else if (responseBody.contains("FAILURE")) {
						String errorMessage = responseBody.split("exception>")[1].split("<")[0];
						Log.message(RealizeUtils.WARN_HTML_BEGIN + errorMessage + RealizeUtils.WARN_HTML_END);
					}
				}
				if(resultIds.size() == 0) {
					RealizeUtils.nap(10); // wait in seconds
					Log.message("Status code: " + response.getStatusCode() + " & userId is empty while getting userId from Rumba. Retrying after 10 sec, attempt: " + retryAttemptWhenUserIdIsEmpty);
					response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
				}
			}
			
			// if status code is 200 and optional parameter is available, then no retry
			if (optional.length > 0 && response.getStatusCode() == 200) {
				userIds = response.getBody().asString().split("UserId>");
				for (String id : userIds) {
					if (!(id.endsWith("</ns6:") && id.length() == 38)) {
						continue;
					} else {
						resultIds.add(id.substring(0, id.length() - 6));
						status = true;
					}
				}
			} 
			
			// if user id not found
			if (!status || resultIds.size() == 0){
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get userId associated with given email '" + emailID
						+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			throw new Exception("Unable to get userId. Error: " + e.getMessage());
		}
		return resultIds;
	}

	/** Gets all the userID affiliated to given organization
	 * @param orgId - organization id
	 * @param roleType - user role type
	 * @return - List of userIds
	 **/
	public static List<String> getUserIdByAffiliation(String orgId, String roleType) {
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUsersByAffiliation");

		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUsersByAffiliationRequest>\n"
						+ "         <v3:OrganizationId>"
						+ orgId
						+ "</v3:OrganizationId>\n"
                     + " <v3:OrgRole>"
                     + roleType
                     + "</v3:OrgRole>\n"
                     + "      </v3:GetUsersByAffiliationRequest>\n" 
                     + "   </soap:Body>\n" + "</soap:Envelope>";

		ArrayList<String> resultIds = new ArrayList<>();
		Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
		if (response.getStatusCode() == 200) {
			String[] userIds = response.getBody().asString().split("UserId>");
			for (String id : userIds) {
				if (!(id.endsWith("</ns6:") && id.length() == 38)) {
					continue;
				} else {
					resultIds.add(id.substring(0, id.length() - 6));
				}
			}
		} else {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get userId affiliated to given organization '" + orgId
					+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
		}
		return resultIds;
	}
	
	/** Get organization ID affiliated for the given userName 
	 * @param userName the user name
	 * @return - List of OrgIds
	 **/
	public static List<String> getAffiliatedOrgIdByUserName(String userName) {
		ArrayList<String> orgIds = new ArrayList<>();
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUserByUserName");

		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUserByUserNameRequest>\n"
						+ "         <v3:UserName>"
						+ userName
						+ "</v3:UserName>\n"
						+ "      </v3:GetUserByUserNameRequest>\n"
						+ "   </soap:Body>\n" + "</soap:Envelope>";

		Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
		if (response.getStatusCode() == 200) {
			String responseBody = response.getBody().asString();
			if (responseBody.contains("OrganizationId>")) {
				String[] userIds = responseBody.split("OrganizationId>");
				for(String id : userIds){
					if(!(id.endsWith("</ns6:") && id.length() == 38)){
						continue;
					}else{
						orgIds.add(id.substring(0, id.length()- 6));
					}
				}
			} else if (responseBody.contains("FAILURE")) {
				String errorMessage = responseBody.split("exception>")[1].split("<")[0];
				Log.message(RealizeUtils.WARN_HTML_BEGIN + errorMessage + RealizeUtils.WARN_HTML_END);
			}
		} else {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get Organization Id for given Username '" + userName
					+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
		}
		return orgIds;
	}

	/** To get all the licensed subscriptions for a given organization
	 * @param orgId
	 * @param status
	 * @return List
	 * @throws Exception
	 */
	public static List<String> getLicensedProducts(String orgId, String status) throws Exception {
		String baseURL = configProperty.getProperty("rumba.licensedproduct.baseurl");
		String endPoint = configProperty.getProperty("rumba.licensedproduct.endpoint");
		List<String> programList = new ArrayList<String>();
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("action", "GetLicensedProduct");
		String request = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v2=\"http://licensedproduct.rws.pearson.com/doc/V2/\">\r\n" 
				+ "   <soap:Header/>"
				+ "   <soap:Body>\r\n" 
				+ "     <v2:GetLicensedProductRequestElement>\r\n"
				+ "        <v2:GetLicensedProduct>\r\n" 
				+ "           <v2:OrganizationId>%s</v2:OrganizationId>\r\n" 
				+ "           <v2:QualifyingLicensePool>RootAndParents</v2:QualifyingLicensePool>\r\n"
				+ "           <v2:Status>%s</v2:Status>\r\n" 
				+ "         </v2:GetLicensedProduct>\r\n" 
				+ "     </v2:GetLicensedProductRequestElement>\r\n" 
				+ "  </soap:Body>\r\n" 
				+ "</soap:Envelope>";

		request = String.format(request, orgId, status);
		Response response = RestAssuredAPI.post(baseURL, headers, queryParam, request, endPoint);
		if (response.getStatusCode() == 200) {
			String xml = response.getBody().asString();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			NodeList nodes = document.getElementsByTagName("LicensedProduct");
			for (int i = 0; i < nodes.getLength(); i++) {
				String program = document.getElementsByTagName("ProductDisplayName").item(i).getTextContent().trim();
				programList.add(program);
			}
		} else {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get licensed products for given organization '" + orgId
					+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
		}
		return programList;
	}

	/** Gets all the Subscribed Products and Resources for the given Rumba user id
	 * @param userId - rumba user id
	 * @return - List of subscribed product name
	 * @throws Exception
	 **/
	public static List<String> getUserSubscriptionByUserId(String userId) throws Exception {
		String slcUrl = configProperty.getProperty("rumba.slc.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetSubscribedProductsAndResources");
		List<String> lstSubscriptions = new ArrayList<String>();
		
		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:ns=\"http://subscriptionevent.rws.pearson.com/doc/2009/06/01/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <ns:GetSubscribedProductsAndResourcesRequestElement>\n"
						+ "         <ns:GetSubscribedProductsAndResourcesByUserIdType>"
						+ "         	<ns:UserId>"+userId+"</ns:UserId>\n"
						+ "		    </ns:GetSubscribedProductsAndResourcesByUserIdType>\n"
						+ "      </ns:GetSubscribedProductsAndResourcesRequestElement>\n" 
                     	+ "   </soap:Body>\n"
                + "</soap:Envelope>";

		Response response = RestAssuredAPI.post(slcUrl, headers, queryParam, request, "");
		if (response.getStatusCode() == 200) {
			String xml = response.getBody().asString();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			NodeList nodes = document.getElementsByTagName("ns10:Product");
			for (int i = 0; i < nodes.getLength(); i++) {
				String program = document.getElementsByTagName("ns10:ProductName").item(i).getTextContent().trim();
				lstSubscriptions.add(program);
			}
		} else {
			Log.message(RealizeUtils.WARN_HTML_BEGIN + "</br>Unable to get user subscription for the given userId '" + userId
					+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.WARN_HTML_END);
		}
		return lstSubscriptions;
	}

	/**
	 * To get the internal emailId for the user having given email Address
	 * 
	 * @param userId       - A&E UserId
	 * @param emailAddress - Email Address
	 * @return - Internal EmailId
	 * @throws Exception
	 */
	public static String getEmailIdByEmailAddress(String userId, String emailAddress) throws Exception {
		String emailId = null;
		Boolean isFound = false;
		Log.event("Getting EmailId for " + userId + " having email address " + emailAddress);
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUser");
		int retryAttempt = 0;

		if (emailAddress == null || emailAddress.isEmpty() || !emailAddress.contains("@")) {
			throw new Exception("Given Email address is empty or invalid");
		}

		String request =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUserRequest>\n"
						+ "         <v3:UserId>"
						+ userId
						+ "</v3:UserId>\n"
						+ "      </v3:GetUserRequest>\n"
						+ "   </soap:Body>\n" + "</soap:Envelope>";
		try {
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			while (response.getStatusCode() == 404 && retryAttempt < 5) {
				retryAttempt++;
				RealizeUtils.nap(5); // wait in seconds
				Log.message("Retrying after 5 sec, attempt: " + retryAttempt);
				response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			}

			if (response.getStatusCode() == 200) {
				String responseBody = response.getBody().asString();
				if (responseBody.contains("EmailInfo>")) {
					String[] emailInfos = responseBody.split("EmailInfo>");
					for (String emailInfo : emailInfos) {
						if (emailInfo.contains("EmailId>") && emailInfo.contains("EmailAddress>")
								&& emailInfo.contains(emailAddress)) {
							isFound = true;
							emailId = emailInfo.split("EmailId>")[1].split("<")[0];
							break;
						}
					}

					if (!isFound) {
						throw new Exception("User '" + userId + "' does not have email address " + emailAddress);
					}
				} else if (responseBody.contains("FAILURE")) {
					String errorMessage = responseBody.split("exception>")[1].split("<")[0];
					Log.message(RealizeUtils.WARN_HTML_BEGIN + errorMessage + RealizeUtils.WARN_HTML_END);
				}
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get emailId for given rumba userId '"
						+ userId + "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get emailId by userId" + RealizeUtils.FAIL_HTML_END
					+ ", Error: " + e.getMessage());
		}
		return emailId;
	}

	/**
	 * To update given user password
	 * @param userId - Rumba user Id
	 * @param password - password to reset
	 * @return - true if password gets reset
	 * @throws Exception
	 */
	public static boolean updateUserPassword(String userId, String password) throws Exception {
		boolean isUpdated = false;
		String ruleSet = "DomainUser";
		Log.event("Reset Rumba User password for " + userId);
		String ulcReadUrl = configProperty.getProperty("rumba.ulc.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "UpdateUser");

		String request = 
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\r\n"
				+ "   <soap:Header/>"
				+ "   <soap:Body>\r\n"
				+ "      <v3:UpdateUserRequest CreatedBy=\"?\">\r\n"
				+ "         <v3:User>\r\n"
				+ "            <v3:UserId>"+userId+"</v3:UserId>\r\n"
				+ "            <v3:Password>"+password+"</v3:Password>\r\n"
				+ "            <v3:BusinessRuleSet>"+ruleSet+"</v3:BusinessRuleSet>\r\n"
				+ "         </v3:User>\r\n"
				+ "      </v3:UpdateUserRequest>\r\n"
				+ "   </soap:Body>\r\n"
				+ "</soap:Envelope>";

		try {
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			if (response.getStatusCode() == 200) {
				if (response.getBody().asString().contains("SUCCESS")) {
					isUpdated = true;
				} else {
					Log.event("Unable to reset user password, Actual response: " + response.getBody().asString());
				}
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to updated password for the given userId '" + userId
						+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to updated password for given userId"
					+ RealizeUtils.FAIL_HTML_END + ", Error: " + e.getMessage());
		}
		return isUpdated;
	}
	
	/**
	 * Gets the user Suspend Status.
	 *
	 * @param userName
	 *            the user name
	 * @return the user Suspend Status
	 * @throws Exception
	 *             the exception
	 */
	public static String getUserSuspendStatus(String userName, String webSite) throws Exception {	
		String ulcReadUrl = null;
		if (webSite.contains("nightly")) {
			ulcReadUrl = ulcReadUrlNightly;
		} else if (webSite.contains("cert")) {
			ulcReadUrl = ulcReadUrlCert;
		} else if (webSite.contains("ppe")) {
			ulcReadUrl  = ulcReadUrlPPe;
		} else if (webSite.contains("savvasrealize")) {
			ulcReadUrl = ulcReadUrlProd;
		}
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/soap+xml");
		headers.put("charset", "utf-8");
		headers.put("action", "GetUserByUserName");

		String request = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
				+ "   <soap:Header>" + "   </soap:Header>" + "   <soap:Body>\n"
				+ "      <v3:GetUserByUserNameRequest>\n" + "         <v3:UserName>" + userName + "</v3:UserName>\n"
				+ "         <!--Optional:-->\n" + "         <v3:DomainId>-1</v3:DomainId>\n"
				+ "      </v3:GetUserByUserNameRequest>\n" + "   </soap:Body>\n" + "</soap:Envelope>";
		
		request = String.format(request, "");
		Response response = RestAssuredAPI.POST(ulcReadUrl, headers, "", "", contentType, request, ulcReadUrl);		
		String status = response.getBody().asString().split("UserStatus>")[1].split("<")[0];
		return status;
	}

	/**
	 * To update the user Suspend Status.
	 *
	 * @param userId
	 * @param status - 'Suspended' or 'Active'
	 * @return true if status got updated successfully
	 * @throws Exception
	 */
   public static boolean updateUserSuspendStatus(String userId, String status, String webSite) throws Exception {	 
	   boolean isUpdated = false;
	   String ulcReadUrl = null;
	 		if (webSite.contains("nightly")) {
	 			ulcReadUrl = ulcReadUrlNightly;
	 		} else if (webSite.contains("cert")) {
	 			ulcReadUrl = ulcReadUrlCert;
	 		} else if (webSite.contains("ppe")) {
	 			ulcReadUrl  = ulcReadUrlPPe;
	 		} else if (webSite.contains("savvasrealize")) {
	 			ulcReadUrl = ulcReadUrlProd;
	 		}
       Map<String, String> headers = new HashMap<String, String>();
       headers.put("Content-Type", "application/soap+xml");
       headers.put("charset", "utf-8");
       headers.put("action", "UpdateUser");
       
       String request =
               "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
                       + "   <soap:Header>"
                       + "   </soap:Header>"
                       + "   <soap:Body>\n"
                       + "      <v3:UpdateUserRequest CreatedBy=\"?\">\n"
                       + "<v3:User>\n"
                       + " <v3:UserId>"
                       + userId
                       + "</v3:UserId>\n"
                       + "<v3:UserStatus>"
                       + status
                       + "</v3:UserStatus>\n "
                       + "<v3:BusinessRuleSet>DomainUser</v3:BusinessRuleSet>\n"
                       + " </v3:User>\n"
                       + "   </v3:UpdateUserRequest>\n" + " </soap:Body>\n" + " </soap:Envelope>";       
       request = String.format(request, "");       
       Response response = RestAssuredAPI.POST(ulcReadUrl, headers, "", "", contentType, request, ulcReadUrl);
       if (response.getStatusCode() == 200) {
			if (response.getBody().asString().contains("SUCCESS")) {
				isUpdated = true;
				Log.message("Successfully updated the '"+status+"' for the userId '"+userId+"'" );
			} else {
				Log.event("Unable to update Status:'" + status + "' for the userId:'" + userId + "', Actual response: " + response.getBody().asString());
			}
		} else {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to update Status:'" + status + "'  for the given userId '" + userId
					+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
		}
		return isUpdated;
	}

	/**
	 * To create user using CreateUser SOAP call
	 * 
	 * @param userDetails - HashMap of user Details
	 * @return userId - A&E userId
	 * @throws Exception
	 */
	public static String createUser(HashMap<String, String> userDetails) throws Exception {
		String userId = null;
		Log.event("Creating new user using UserLifeCycle SOAP Call");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timeStamp = dateFormat.format(new Date());
		StringBuilder soapRequest = new StringBuilder();
		String ulcReadUrl = configProperty.getProperty("rumba.ulc.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "CreateUser");

		soapRequest.append(
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\r\n"
				+ "   <soap:Header/>"
				+ "   <soap:Body>\r\n"
				+ "      <v3:CreateUserRequest CreatedBy=\"Self-reg\" SendEmailNotification=\"false\">\r\n"
				+ "         <v3:User>\r\n");

		if (userDetails.containsKey("FirstName") && userDetails.get("FirstName") != null
				&& !userDetails.get("FirstName").isEmpty()) {
			soapRequest.append("	<v3:FirstName>" + userDetails.get("FirstName").trim() + "</v3:FirstName>\r\n");
		} else {
			throw new Exception("FirstName is expected and it is empty");
		}

		if (userDetails.containsKey("LastName") && userDetails.get("LastName") != null
				&& !userDetails.get("LastName").isEmpty()) {
			soapRequest.append("	<v3:LastName>" + userDetails.get("LastName").trim() + "</v3:LastName>\r\n");
		} else {
			throw new Exception("LastName is expected and it is empty");
		}
		
		if (userDetails.containsKey("Gender") && userDetails.get("Gender") != null
				&& (userDetails.get("Gender").equals("Male") || userDetails.get("Gender").equals("Female"))) {
			soapRequest.append("	<v3:Gender>" + userDetails.get("Gender").trim() + "</v3:Gender>\r\n");
		} else {
			soapRequest.append("	<v3:Gender>Unspecified</v3:Gender>\r\n");
		}
		
		if (userDetails.containsKey("UserName") && userDetails.get("UserName") != null
				&& !userDetails.get("UserName").isEmpty()) {
			soapRequest.append("	<v3:UserName>" + userDetails.get("UserName").trim() + "</v3:UserName>\r\n");
		} else {
			throw new Exception("UserName is expected and it is empty");
		}
		
		if (userDetails.containsKey("Password") && userDetails.get("Password") != null
				&& !userDetails.get("Password").isEmpty()) {
			soapRequest.append("	<v3:Password>" + userDetails.get("Password").trim() + "</v3:Password>\r\n");
		} else {
			throw new Exception("Password is expected and it is empty");
		}

		soapRequest.append("	<v3:EncryptionType>SHA</v3:EncryptionType>\r\n"
				+ "            <v3:AuthenticationType>SSO</v3:AuthenticationType>\r\n"
				+ "            <v3:BusinessRuleSet>CG</v3:BusinessRuleSet>\r\n");

		if (userDetails.containsKey("EmailAddress") && userDetails.get("EmailAddress") != null
				&& !userDetails.get("EmailAddress").isEmpty()) {
			soapRequest.append(
					"		<v3:EmailInfo>\r\n"
					+ "            <v3:IsPrimary>true</v3:IsPrimary>\r\n"
					+ "            <v3:EmailAddress>" + userDetails.get("EmailAddress").trim() + "</v3:EmailAddress>\r\n"
					+ "            <v3:Status>Active</v3:Status>\r\n"
					+ "		</v3:EmailInfo>\r\n");
		} else {
			throw new Exception("EmailAddress is expected and it is empty");
		}
		
		if (userDetails.containsKey("OrganizationId") && userDetails.get("OrganizationId") != null
				&& !userDetails.get("OrganizationId").isEmpty()) {
			if (userDetails.containsKey("OrgRole") && userDetails.get("OrgRole") != null
					&& (userDetails.get("OrgRole").equals("T") || userDetails.get("OrgRole").equals("S")
							|| userDetails.get("OrgRole").equals("CA"))) {
				soapRequest.append("	<v3:AffiliationInfo>\r\n"
								+ "            <v3:AffiliationType>User</v3:AffiliationType>\r\n"
								+ "            <v3:AffiliationStatus>Confirmed</v3:AffiliationStatus>\r\n"
								+ "            <v3:OrgRole>" + userDetails.get("OrgRole").trim() + "</v3:OrgRole>\r\n"
								+ "            <v3:OrganizationId>" + userDetails.get("OrganizationId").trim() + "</v3:OrganizationId>\r\n"
								+ "		</v3:AffiliationInfo>");
			} else {
				throw new Exception(
						"OrgRole is expected and it is empty or invalid. Actual: " + userDetails.get("OrgRole"));
			}
		} else {
			throw new Exception("OrganizationId is expected and it is empty");
		}

		soapRequest.append("<v3:ConsentInfo>\r\n"
				+ "            <v3:PolicyDesc>License Agreement</v3:PolicyDesc>\r\n"
				+ "            <v3:ConsentMethod>Self</v3:ConsentMethod>\r\n"
				+ "            <v3:PolicyCategory>Terms of Use</v3:PolicyCategory>\r\n"
				+ "            <v3:ConsentFlag>true</v3:ConsentFlag>\r\n"
				+ "            <v3:ConsentDate>" + timeStamp + "</v3:ConsentDate>\r\n"
				+ "         </v3:ConsentInfo>\r\n"
				+ "       </v3:User>\r\n"
				+ "     </v3:CreateUserRequest>\r\n"
				+ "  </soap:Body>\r\n"
				+ "</soap:Envelope>");

		try {
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, soapRequest.toString(), "");
			if (response.getStatusCode() == 200) {
				String responseBody = response.getBody().asString();
				if (responseBody.contains("SUCCESS")) {
					userId = responseBody.split("returnValue>")[1].split("<")[0];
				} else {
					Log.event("Unable to create user, Actual response: " + responseBody);
				}
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to create user. Actual status code: "
						+ response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to create a user" + RealizeUtils.FAIL_HTML_END
					+ ", Error: " + e.getMessage());
		}
		return userId;
	}
   
	/**
	 * To delete the user in A&E
	 *
	 * @param userId
	 * @throws Exception
	 */
	public static void deleteUser(String userID) throws Exception {
        Log.message("===Deleting user : " + userID + " in Radmin=====");
		String deleteUrl = configProperty.getProperty("rumba.delete.user.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept-Charset", "utf-8");

		String requestBody = "{" + "\"userIds\":" + "[" + "\"" + userID + "\"" + "]}";
		Response response = RestAssuredAPI.post(deleteUrl, headers, queryParam, requestBody, "");
		if (response.getStatusCode() == 200) {
			if (response.getBody().asString().contains("Success")) {
				Log.message("UserId <b>" + userID + "</b> is deleted successfully from RAdmin");
			} else {
				Log.message("Unable to delete user : " + userID + "from Radmin." + " Actual response is: "
						+ response.getBody().asString());
			}
		} else {
			throw new Exception("Unable to delete user. Error: " + response.getStatusCode() + " : "
					+ response.getBody().asString());
		}
	}
	
	/**
	 * To delete the existing affiliation and add new organization for given user id
	 * 
	 * @param userId
	 * 			- Rumba user id
	 * @param updateOrgid
	 * 			- Organization id
	 * @return
	 * 			- true, if updated the organization id in Rumba 
	 * @throws Exception
	 */
	public static boolean deleteAndUpdateAffiliationIdByUserId(String userId, String updateOrgid) throws Exception {
		boolean isUpdated = false;
		String affiliationId = null, orgId = null;
		String encryptionType = "SHA";
		String businessRuleSet = "DomainUser";
		String[] operationType = {"Delete", "Create"};
		String affiliationStatus = "Confirmed";
		String OrgRole = "T";
		String affiliationType = "User";
		Log.event("Getting Rumba Affiliation Id for " + userId);
		String ulcReadUrl = configProperty.getProperty("rumba.ulcread.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "GetUser");
		
		String getUserRequest =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\n"
						+ "      <v3:GetUserRequest>\n"
						+ "         <v3:UserId>"
						+ userId
						+ "</v3:UserId>\n"
						+ "      </v3:GetUserRequest>\n"
						+ "   </soap:Body>\n" + "</soap:Envelope>";

		Response getUserResponse = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, getUserRequest, "");
		if (getUserResponse.getStatusCode() == 200) {
			String responseBody = getUserResponse.getBody().asString();
			if (responseBody.contains("AffiliationId>") && responseBody.contains("OrganizationId>")) {
				affiliationId = responseBody.split("AffiliationId>")[1].replace("</ns6:", "");
				orgId = responseBody.split("OrganizationId>")[1].replace("</ns6:", "");
			} else if (responseBody.contains("FAILURE")) {
				String errorMessage = responseBody.split("exception>")[1].split("<")[0];
				Log.message(RealizeUtils.WARN_HTML_BEGIN + errorMessage + RealizeUtils.WARN_HTML_END);
			}
		} else {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to get Organization Id for given rumba user id '" + userId
					+ "'. Actual status code: " + getUserResponse.getStatusCode() + RealizeUtils.FAIL_HTML_END);
		}
		
		headers.clear();
		String ulcUrl = configProperty.getProperty("rumba.ulc.url");
		Log.message("===Deleting Affiliation Id : " + affiliationId+ " in Radmin=====");
		Log.message("===Updating Organization Id : " + updateOrgid+ " in Radmin=====");
		headers.put("Content-Type", contentType);
		headers.put("charset", "UTF-8");
		headers.put("action", "UpdateUser");
		headers.put("Host", "nightly-user.rumba.pk12ls.com");
		
		String deleteAndUpdaterequestBody =
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\r\n"
						+ "   <soap:Header/>"
						+ "   <soap:Body>\r\n"
						+ "      <v3:UpdateUserRequest CreatedBy=\"?\">\r\n"
						+ "         <v3:User>\r\n"
						+ "				<v3:UserId>" + userId + "</v3:UserId>\n"
						+ "				<v3:EncryptionType>" + encryptionType + "</v3:EncryptionType>\n"
						+ "				<v3:BusinessRuleSet>" + businessRuleSet + "</v3:BusinessRuleSet>\n"
						+ " 			<v3:AffiliationInfo>\n"
						+ "					<v3:OperationType>" + operationType[0]+ "</v3:OperationType>\n"
						+ " 				<v3:AffiliationId>" + affiliationId + "</v3:AffiliationId>\n"
						+ " 				<v3:AffiliationType>" + affiliationType + "</v3:AffiliationType>\n"
						+ "					<v3:AffiliationStatus>" + affiliationStatus + "</v3:AffiliationStatus>\n"
						+ "					<v3:OrgRole>" + OrgRole + "</v3:OrgRole>\n"
						+ "					<v3:OrganizationId>" + orgId + "</v3:OrganizationId>\n"
						+ "				</v3:AffiliationInfo>\n"
						+ " 			<v3:AffiliationInfo>\n"
						+ "					<v3:OperationType>" + operationType[1]+ "</v3:OperationType>\n"
						+ " 				<v3:AffiliationType>" + affiliationType + "</v3:AffiliationType>\n"
						+ "					<v3:AffiliationStatus>" + affiliationStatus + "</v3:AffiliationStatus>\n"
						+ "					<v3:OrgRole>" + OrgRole + "</v3:OrgRole>\n"
						+ "					<v3:OrganizationId>" + updateOrgid + "</v3:OrganizationId>\n"
						+ "				</v3:AffiliationInfo>\n"
						+ " 		</v3:User>\r\n"
						+ "   </v3:UpdateUserRequest>\r\n" 
						+ " </soap:Body>\r\n" 
						+ " </soap:Envelope>";  

		Response deleteAndUpdateresponse = RestAssuredAPI.post(ulcUrl, headers, queryParam, deleteAndUpdaterequestBody, "");
		if (deleteAndUpdateresponse.getStatusCode() == 200) {
			if (deleteAndUpdateresponse.getBody().asString().contains("SUCCESS")) {
				isUpdated = true;
				Log.message("AffiliationId " + affiliationId + " deleted successfully and updated the organization " + updateOrgid + " successfully from RAdmin");
			} else {
				Log.message("Unable to delete AffiliationId : " + affiliationId + "and updated the organization " + updateOrgid + " from RAdmin"+ " Actual response is: "
						+ deleteAndUpdateresponse.getBody().asString());
			}
		} else {
			Log.message(deleteAndUpdateresponse.getStatusCode() + " : " + deleteAndUpdateresponse.getBody().asString());
		}

		return isUpdated;
	}

	/**
	 * To delete Email Address for the given user
	 * 
	 * @param userId  - A&E UserId
	 * @param emailId - Internal Email Id
	 * @return
	 */
	public static boolean deleteEmailForUser(String userId, String emailId) {
		boolean isDeleted = false;
		String ruleSet = "DomainUser";
		Log.event("Delete email address for user " + userId);
		String ulcReadUrl = configProperty.getProperty("rumba.ulc.url");
		Map<String, String> queryParam = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", contentType);
		headers.put("charset", "utf-8");
		headers.put("action", "UpdateUser");

		String request = 
				"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v3=\"http://user.rws.pearson.com/doc/V3/\">\r\n"
				+ "   <soap:Header/>"
				+ "   <soap:Body>\r\n"
				+ "      <v3:UpdateUserRequest CreatedBy=\"?\">\r\n"
				+ "         <v3:User>\r\n"
				+ "            <v3:UserId>"+userId+"</v3:UserId>\r\n"
				+ "            <v3:BusinessRuleSet>"+ruleSet+"</v3:BusinessRuleSet>\r\n"
				+ "            <v3:EmailInfo>\r\n"
				+ "            		<v3:OperationType>Delete</v3:OperationType>\r\n"
                + "            		<v3:EmailId>"+emailId+"</v3:EmailId>\r\n"
                + "            		<v3:IsPrimary>false</v3:IsPrimary>\r\n"
                + "            		<v3:EmailAddress>?</v3:EmailAddress>\r\n"
                + "            </v3:EmailInfo>\r\n"
				+ "         </v3:User>\r\n"
				+ "      </v3:UpdateUserRequest>\r\n"
				+ "   </soap:Body>\r\n"
				+ "</soap:Envelope>";

		try {
			Response response = RestAssuredAPI.post(ulcReadUrl, headers, queryParam, request, "");
			if (response.getStatusCode() == 200) {
				if (response.getBody().asString().contains("SUCCESS") || response.getBody().asString().contains("200")) {
					isDeleted = true;
				} else {
					Log.event("Unable to delete email for user, Actual response: " + response.getBody().asString());
				}
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "</br>Unable to delete email for the given userId '" + userId
						+ "'. Actual status code: " + response.getStatusCode() + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to delete email for given userId"
					+ RealizeUtils.FAIL_HTML_END + ", Error: " + e.getMessage());
		}
		return isDeleted;
	}
	
	/**
	 * To Unsubscribe a product for a given user id
	 * 
	 * @param userDetails - HashMap of user Details
	 * @return boolean - true, if the product was removed successfully
	 * @throws Exception
	 */
	public static boolean unsubscribeProductForUserId(HashMap<String, String> userDetails, boolean... logAsEvent) throws Exception {
		boolean isUpdated = false;
		String rumbaSlcUrl = configProperty.getProperty("rumba.slc.base.url").trim();
		String rumbaUnsubscribeProductEndPoint = configProperty.getProperty("rumba.unsubscribeproduct.endpoint").trim();

		HashMap<String, String> requestHeaders = RBSAPIUtils.getJsonRequestHeaders();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("SubscribeType", "Institutional Licensing");
		queryParams.put("CreatedBy", "System");
		queryParams.put("OrganizationId", userDetails.get("OrganizationId"));
		queryParams.put("ModelType", "Student Seat based");
		queryParams.put("LearningContextRole", userDetails.get("LearningContextRole"));
		queryParams.put("ProductId", userDetails.get("ProductId"));
		queryParams.put("UserId", userDetails.get("rumbaUserId"));

		Log.message("<b>Performing GET Request :</b> " + rumbaSlcUrl + rumbaUnsubscribeProductEndPoint);
		Response response = RestAssuredAPI.get(rumbaSlcUrl, requestHeaders, queryParams, rumbaUnsubscribeProductEndPoint);
		RealizeUtils.apiLogMessageFormatter("GET", rumbaSlcUrl, rumbaUnsubscribeProductEndPoint, requestHeaders, queryParams,
				response, "", logAsEvent);

		if (response.getStatusCode() == 200) {
			if (response.getBody().asString().contains("successfully")) {
				isUpdated = true;
				Log.message("Unsubscribed the product : " + userDetails.get("ProductId") + " for this UserId " + userDetails.get("rumbaUserId"));
			} else {
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to remove the subscription : " + userDetails.get("ProductId") + "for this userId " + 
						userDetails.get("rumbaUserId") +  " Actual response is: " + response.getBody().asString() + RealizeUtils.FAIL_HTML_BEGIN);
			}
		} else {
			throw new Exception(response.getStatusCode() + " : " + response.getBody().asString());
		}
		return isUpdated;
	}
}
