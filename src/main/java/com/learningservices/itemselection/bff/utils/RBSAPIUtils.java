package com.learningservices.itemselection.bff.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningservices.utils.Log;
import com.learningservices.utils.RestAssuredAPI;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.response.Response;

public class RBSAPIUtils {
	
	public static Response postResponse;
	private static final String JSON_CONTENT_TYPE = "application/json";
	private static final String JSON_CONTENT_TYPE_SCHOOLOGY = "application/vnd.ims.lti-nrps.v2.membershipcontainer+json";
	private static PropertyReader configProperty = PropertyReader.getInstance();
	public static final String emsExternalUserMappingEndpoint = "/external-service/api/externalusermappings";
	public static final String emsExternalUserIdEndpoint = "/external-service/api/externalusermappings/externalUserId";
	public static final String emsExternalClassMappingSearchEndpoint = "/api/externalclassmappings/search";
	public static final String emsAssignmentMappingEndpoint = "/external-service/api/assignmentmappings/%s";
	public static final String emsAssignmentMappingDeletionEndpoint = "/external-service/api/assignmentmappings/bulk?externalSystem=%s";
    public static final String emsAssignmentMappingCreationEndpoint = "/external-service/api/assignmentmappings/bulk";
	public static final String apsResourceClaimEndpoint = "/assignmentprovisioning-service/v1/claims/%s";
	private final static String BROWSE_CONTENT_GRAPHQL_BODY = "{\"operationName\":null,\"variables\":{},\"query\":\"query {\\n  " +
            "getSearchResult(searchInput: { searchKeyword: \\\"%s\\\", pageNumber: %d, pageSize: %d } ) {totalAvailable\\n contents {\\n    contentId\\n    contentVersion\\n    title\\n}\\n}\\n}\\n\"}";

	public static enum UMS_ROLE {
		Teacher("T"),
		Student("S");

		private String role;

		UMS_ROLE(String role) {
			this.role = role;
		}

		@Override
		public String toString() {
			return role;
		}
	}

	public static enum orgSelectionType {
		UserCreation("user-creation"),
		ClassCreation("class-creation"),
		Invalid("invalid-creation");

		private String selectionType;

		orgSelectionType(String type) {
			this.selectionType = type;
		}

		@Override
		public String toString() {
			return selectionType;
		}
	}
	
	public static enum AutoMatchingAttribute {
		NONE("none"),
		SIS("sis"),
		FEDERATED("federated");

		private String attribute;

		AutoMatchingAttribute(String attribute) {
			this.attribute = attribute;
		}

		@Override
		public String toString() {
			return attribute;
		}
	}

	/**
	 * To get the default Request Headers with application/json as content-type and accept headers
	 * @return
	 */
	public static HashMap<String, String> getJsonRequestHeaders() {
		HashMap<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("accept", JSON_CONTENT_TYPE);
		requestHeaders.put("content-type", JSON_CONTENT_TYPE);
		return requestHeaders;
	}
	
	/**
	 * To get the default Request Headers with application/json as content-type and accept headers
	 * @return
	 */
	public static HashMap<String, String> getJsonRequestHeadersForSchoology() {
		HashMap<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("accept", JSON_CONTENT_TYPE_SCHOOLOGY);
		requestHeaders.put("content-type", JSON_CONTENT_TYPE);
		return requestHeaders;
	}

	/**
     * To get given tag value from the given link
     * @param tag
     * @param link
     * @return
     */
    private static String getTag(String tag, String link) {
        String valueToReturn = null;
        Pattern pattern = Pattern.compile(tag+"=(.*?);");
        Matcher matcher = pattern.matcher(link);
        if(matcher.find()) {
            valueToReturn = matcher.group(1);
        }
        return valueToReturn;
    }

	/**
	 * Get the jessionID and failover value for any realize user[Admin/Teacher/Student] from any environment 
	 * 
	 * @param userName can be teacher / student/ admin – Example: realize_pa_user
	 * @param password - user password
	 * @return jsessionID and failover valve
	 */
    public static String getJSessionID(String userName, String password) {
    	String realizeURL = configProperty.getProperty("webSite").trim();
    	String serviceURL = realizeURL + "/j_spring_cas_security_check";
    	String rumbaLoginURL = configProperty.getProperty("rumba.sso.login.url").trim();
    	
        // Query Params and Headers - Declaration
        Map<String, String> inputData1Login = new HashMap<String, String>();
        Map<String, String> inputData2Auth1 = new HashMap<String, String>();
        Map<String, String> inputData2Auth2 = new HashMap<String, String>();

        Map<String, String> headers1Login = new HashMap<String, String>();
        Map<String, String> headers1Auth1 = new HashMap<String, String>();
        Map<String, String> headers1Auth2 = new HashMap<String, String>();

        // Query Params for Login
        inputData1Login.put("profile", "realize");
        inputData1Login.put("k12int", "true");
        inputData1Login.put("service",  serviceURL);

        // Query Params for Auth1
        inputData2Auth1.put("execution", "e1s1");
        inputData2Auth1.put("profile", "realize");
        inputData2Auth1.put("k12int", "true");
        inputData2Auth1.put("service", serviceURL);

        // Making Login GET Call
        Response respLogin = RestAssuredAPI.get(rumbaLoginURL, headers1Login, inputData1Login, "");
        
        // Extracting Response Cookies - JSESSION and BIGipServerrumba-int-cluster-01
        Map<String, String> cookies = respLogin.cookies();
        StringBuffer cookieHeader = new StringBuffer();
        for (String cookie : cookies.keySet()) {
            cookieHeader.append(cookie + "=" + cookies.get(cookie) + ";");
        }

        // Setting Request header cookie for Auth1 POST Call
        headers1Auth1.put("Cookie", cookieHeader.toString());
        headers1Auth1.put("Content-Type", "application/x-www-form-urlencoded");
        String LoginPayload = "username=" + userName + "&password=" + password + "&_eventId=submit";

        // Making Auth1 POST Call
        Response respAuth1 = RestAssuredAPI.post(rumbaLoginURL, headers1Auth1, inputData2Auth1, LoginPayload, "");
        
        // Extracting CASTGC for Auth 2 POST call
        String castgc = respAuth1.getCookie("CASTGC");

        // Extracting Location for Auth 2 POST call
        String baseURLforAuth2 = respAuth1.getHeader("Location").split("\\?")[0].split(".com")[0] + ".com";
        String endPointURLforAuth2 = respAuth1.getHeader("Location").split("\\?")[0].split("\\.com")[1];
        String ticketParam = respAuth1.getHeader("Location").split("\\?")[1].split("=")[1];
        inputData2Auth2.put("ticket", ticketParam);

        // Setting Query params for Auth2 POST Call
        headers1Auth2.put("Cookie", cookieHeader.toString() + "CASTGC=" + castgc);

        // Setting Request Cookies for Auth2 POST Call
        headers1Auth2.put("Content-Type", "application/x-www-form-urlencoded");

        Response respAuth2 = RestAssuredAPI.post(baseURLforAuth2, headers1Auth2, inputData2Auth2, LoginPayload,
                endPointURLforAuth2);
        Log.event(">>>> Auth 2 " + userName + " Cookies: \nJSESSIONID="
                + respAuth2.getCookies().get("JSESSIONID") + ";__failover=" + respAuth2.getCookies().get("__failover"));
        return "JSESSIONID=" + respAuth2.getCookies().get("JSESSIONID") + ";__failover="
                + respAuth2.getCookies().get("__failover");
    }

    /**
     * Get the given user's cookie as {@link HashMap}
     * 
     * @param userName user login name (Teacher,Student,Admin)
     * @param password - user password
     * @return JsessionID and failover {@link Cookie} value
     */
    public static Map<String, String> getCookieMap(String userName, String password) {
        String sessionID = getJSessionID(userName, password) + ";";
        return Arrays.stream(new String[] { "JSESSIONID", "__failover" }).collect(Collectors.toMap(Function.identity(), tag -> getTag(tag, sessionID)));
    }

	//********************** Auth Gateway Service ******************************//
	
	/**
     * Construct RBS token body to use in POST call
     *
     * @param scope
     * @param userId
     * @param clientId
     * @param grantType
     * @return RBS token body as string
     */
    private static String createRbsTokenBodyString(String scope, String userId, String clientId, String grantType) {
        final String body = new JSONObject()
                .put("scope", scope)
                .put("userId", userId)
                .put("clientId", clientId)
                .put("grant_type", grantType).toString();
        return body;
    } 
    
    /**
     * Construct sapi login body to use in POST call
     *
     * @param username
     * @param password
     * @return SAPI login body
     */
    private static String createSapiLoginBodyString(String username, String password) {
        final String body = new JSONObject()
                .put("username", username)
                .put("password", password).toString();
        return body;
    }
    
    /**
     * Construct sapi v2 login body to use in POST call
     * @param username
     * @param password
     * @return SAPI login v2 body
     */
    private static String createSapiLoginV2BodyString(String username, String password) {
        final String body = new JSONObject()
                .put("userName", username)
                .put("password", password).toString();
        return body;
    }

    /**
     * Make POST request to /sapi/account/login to get SAPI Login response
     *
     * @param username
     * @param password
     * @return Response
     */
    public static Response getSAPILoginResponseUsingAuthGateway(String username, String password) {
    	String authBaseUrl = configProperty.getProperty("rbs.sapi.base.url").trim();
    	String loginEndPointUrl = configProperty.getProperty("rbs.sapi.login.endpoint").trim();
        HashMap<String, String> loginRequestHeaders = getJsonRequestHeaders();
        HashMap<String, String> queryParametersMap = new HashMap<>();
        final String castgcTokenBody = createSapiLoginBodyString(username, password);

		Log.event("Performing POST Request for " + authBaseUrl + loginEndPointUrl);
		Response sapiLoginResponse = RestAssuredAPI.post(authBaseUrl, loginRequestHeaders, queryParametersMap,
				castgcTokenBody, loginEndPointUrl);

		if (sapiLoginResponse.getStatusCode() == 200) {
			Log.event("POST Response from sapi login gateway: " + sapiLoginResponse.getBody().asString());
		} else {
			Log.message(sapiLoginResponse.getStatusCode() + " status code returned from sapi login gateway with message "
							+ sapiLoginResponse.getBody().asString());
		}
        return sapiLoginResponse;
    }
    
    /**
     * Make POST request to /sapi/oauth/token to get RBS access token created using casTGC for authorization
     *
     * @param userName
     * @param password
     * @param clientId
     * @return Access token
     */
	public static String getAccessTokenUsingCastGCForSAS(String userName, String password, String clientId) {
		String accessToken = "";
		String castgcToken = "", rumbaUserId = "";
		String authBaseUrl = configProperty.getProperty("rbs.sapi.base.url").trim();
		String tokenEndPointUrl = configProperty.getProperty("rbs.sapi.sas.token.endpoint").trim();
		String scope = configProperty.getProperty("rbs.token.scope").trim();
		String grantType = configProperty.getProperty("rbs.token.castgc.grantType").trim(); // custom_castgc grant_type if castgc token being passed in header
		HashMap<String, String> rbsTokenRequestHeaders = getJsonRequestHeaders();
		HashMap<String, String> sapiQueryParam = new HashMap<>();

		Response sapiLoginResponse = getSAPILoginResponseUsingAuthGateway(userName, password);
		if (sapiLoginResponse.getStatusCode() == 200) {
			castgcToken = sapiLoginResponse.jsonPath().getString("cookies.CASTGC");
			rumbaUserId = sapiLoginResponse.jsonPath().getString("identityId");
			rbsTokenRequestHeaders.put("castgc", castgcToken );
			Log.event("CasTGC token: " + castgcToken);
			final String rbsTokenBody = createRbsTokenBodyString(scope, rumbaUserId, clientId, grantType);

			Log.event("Performing POST Request for " + authBaseUrl + tokenEndPointUrl);
			Response sapiTokenResponse = RestAssuredAPI.post(authBaseUrl, rbsTokenRequestHeaders, sapiQueryParam,
					rbsTokenBody, tokenEndPointUrl);

			if (sapiTokenResponse.getStatusCode() == 200) {
				Log.event("POST Response from sapi auth token gateway: " + sapiTokenResponse.getBody().asString());
				JSONObject postObject = new JSONObject(sapiTokenResponse.getBody().asString());
				accessToken = postObject.getString("access_token");
				Log.event("RBS Access token: " + accessToken);
			} else {
				Log.message(sapiTokenResponse.getStatusCode()
						+ " status code returned from sapi auth token gateway with message "
						+ sapiTokenResponse.getBody().asString());
			}
		}
		return accessToken;
	}
	
	/**
     * To get Access Token for the given user name and password using CasTGC token using auth gateway
     * @param userName
     * @param password
     * @return - Access Token
     */
    public static String getAccessTokenUsingCastGCForSAS(String userName, String password) {
    	String rrClientId = configProperty.getProperty("rbs.token.rr.clientId").trim();
        return getAccessTokenUsingCastGCForSAS(userName, password, rrClientId);
        
    }
    
    /**
     * Make POST request to /sapi/account/v2/login to get response to extract castgc Token from it
     *
     * @param username
     * @param password
     * @return castgc Token
     */
	public static Response getSAPILoginResponseUsingAuthGatewayV2(String username, String password) {
		String authBaseUrl = configProperty.getProperty("rbs.sapi.base.url").trim();
		String loginEndPointUrl = configProperty.getProperty("rbs.sapi.login-v2.endpoint").trim();
		HashMap<String, String> loginRequestHeaders = getJsonRequestHeaders();
		HashMap<String, String> queryParametersMap = new HashMap<>();
		final String sapiLoginV2Body = createSapiLoginV2BodyString(username, password);

		Log.event("Performing POST Request for " + authBaseUrl + loginEndPointUrl);
		RestAssured.config = RestAssured.config()
				.encoderConfig(new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
		Response sapiLoginResponse = RestAssuredAPI.post(authBaseUrl, loginRequestHeaders, queryParametersMap,
				sapiLoginV2Body, loginEndPointUrl);
		Log.event("POST Response from sapi login v2 gateway: " + sapiLoginResponse.getBody().asString());

		return sapiLoginResponse;
	}
    
	/**
     * Make POST request to /sapi/oauth/token to get RBS access token created using casTGC for authorization
     *
     * @param userName
     * @param password
     * @param clientId
     * @return Access token
     */
	public static String getAccessTokenUsingCastGC(String userName, String password, String clientId) {
		String accessToken = "";
		String castgcToken = "", rumbaUserId = "";
		String authBaseUrl = configProperty.getProperty("rbs.sapi.base.url").trim();
		String tokenEndPointUrl = configProperty.getProperty("rbs.sapi.token.endpoint").trim();
		String scope = configProperty.getProperty("rbs.token.scope").trim();
		String grantType = configProperty.getProperty("rbs.token.castgc.grantType").trim(); // custom_castgc grant_type if castgc token being passed in header
		HashMap<String, String> rbsTokenRequestHeaders = getJsonRequestHeaders();
		HashMap<String, String> sapiQueryParam = new HashMap<>();

		Response sapiLoginResponse = getSAPILoginResponseUsingAuthGatewayV2(userName, password);
		if (sapiLoginResponse.getStatusCode() == 200) {
			castgcToken = sapiLoginResponse.jsonPath().getString("data.castgc");
			rumbaUserId = sapiLoginResponse.jsonPath().getString("data.identityId");

			Log.event("CasTGC token: " + castgcToken);
			rbsTokenRequestHeaders.put("castgc", castgcToken);
			final String rbsTokenBody = createRbsTokenBodyString(scope, rumbaUserId, clientId, grantType);

			Log.event("Performing POST Request for " + authBaseUrl + tokenEndPointUrl);
			Response sapiTokenResponse = RestAssuredAPI.post(authBaseUrl, rbsTokenRequestHeaders, sapiQueryParam,
					rbsTokenBody, tokenEndPointUrl);

			if (sapiTokenResponse.getStatusCode() == 200) {
				Log.event("POST Response from sapi auth token gateway: " + sapiTokenResponse.getBody().asString());
				JSONObject postObject = new JSONObject(sapiTokenResponse.getBody().asString());
				accessToken = postObject.getString("access_token");
				Log.event("RBS Access token: " + accessToken);
			} else {
				Log.message(sapiTokenResponse.getStatusCode()
						+ " status code returned from sapi auth token gateway with message "
						+ sapiTokenResponse.getBody().asString());
			}
		} else {
			Log.message(
					sapiLoginResponse.getStatusCode() + " status code returned from sapi login v2 gateway with message "
							+ sapiLoginResponse.getBody().asString());
		}

		return accessToken;
	}
	
    /**
     * To get Access Token for the given user name and password using CasTGC token using auth gateway
     * @param userName
     * @param password
     * @return - Access Token
     */
    public static String getAccessTokenUsingCastGC(String userName, String password) {
    	String rrClientId = configProperty.getProperty("rbs.token.rr.clientId").trim();
        return getAccessTokenUsingCastGC(userName, password, rrClientId);
        
    }

    /** Getting the access token for the given user name using basic authorization
     * 
     * @param userName
     * @param clientId
     * @param basicAuth
     * @return - Access Token
     * @throws Exception
     */
    public static String getAccessTokenUsingBasicAuth(String userName, String basicAuth, String clientId) {
    	String accessToken = "";
    	String authBaseUrl = configProperty.getProperty("rbs.sapi.base.url").trim();
    	String tokenEndPointUrl = configProperty.getProperty("rbs.sapi.token.endpoint").trim();
    	String scope = configProperty.getProperty("rbs.token.scope").trim();
    	String basicAuthCredential = configProperty.getProperty("rbs.client.basic.authorization").trim();
    	//client_credentials grant_type, if clientId and clientSecret is being passed in header and this is the default grant type
    	String grantType = configProperty.getProperty("rbs.token.default.grantType").trim(); 

    	String rumbaUserId = RumbaClient.getRumbaUserId(userName);
		if (rumbaUserId == null || rumbaUserId.isEmpty()) {
			Log.event("Unable to get rumba userId, so trying again");
			rumbaUserId = RumbaClient.getRumbaUserId(userName);
		}
		
    	HashMap<String, String> rbsTokenRequestHeaders = getJsonRequestHeaders();
    	HashMap<String, String> queryParametersMap = new HashMap<>();
    	rbsTokenRequestHeaders.put("Authorization", basicAuthCredential);
    	final String rbsTokenBody = createRbsTokenBodyString(scope, rumbaUserId, clientId, grantType);
    	
    	Log.event("Performing POST Request for " + authBaseUrl + tokenEndPointUrl);
		Response sapiTokenResponse = RestAssuredAPI.post(authBaseUrl, rbsTokenRequestHeaders, queryParametersMap,
				rbsTokenBody, tokenEndPointUrl);

		if (sapiTokenResponse.getStatusCode() == 200) {
			Log.event("POST Response from sapi auth token gateway: " + sapiTokenResponse.getBody().asString());
			JSONObject postObject = new JSONObject(sapiTokenResponse.getBody().asString());
			accessToken = postObject.getString("access_token");
		} else {
			Log.message(sapiTokenResponse.getStatusCode() + " status code returned from sapi auth token gateway with message "
					+ sapiTokenResponse.getBody().asString());
		}

        return accessToken;
    }
    
    /**
     * To get Access token for the given userName using realize basic authorization with realize client id
     * @param userName
     * @return - Access token
     */
    public static String getAccessTokenUsingBasicAuth(String userName) {
    	String realizeClientId = configProperty.getProperty("rbs.token.realize.clientId").trim();
    	String realizeBasicAuth = configProperty.getProperty("rbs.client.basic.authorization").trim();
    	return getAccessTokenUsingBasicAuth(userName, realizeBasicAuth, realizeClientId);
    }
    
    /**
	 * To get RBS Access token using realize rbs token endpoint
	 * @param userName - Rumba username
	 * @param password - user password
	 * @param userScope - user scope - teacher/student
	 * @return - Access token
	 */
	public static String getRBSAccessToken(String userName, String password, String userScope) {
		String accToken = "";
		String endPointUrl = "/rest/rbs/tokens";
		String webSite = configProperty.getProperty("webSite").trim();
    	Map<String, String> headers = getJsonRequestHeaders();
    	HashMap<String, String> queryParams = new HashMap<String, String>();
		Map<String, String> cookies = getCookieMap(userName, password);
		String failoverValue = cookies.get("__failover");
		headers.put("Cookie", "__failover=" + failoverValue);
		headers.put("Referer", RealizeUtils.REFERER_URL);

		String rumbaUserId = RumbaClient.getRumbaUserId(userName);
		if (rumbaUserId == null || rumbaUserId.isEmpty()) {
			Log.event("Unable to get rumba userId, so trying again");
			rumbaUserId = RumbaClient.getRumbaUserId(userName);
		}

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("userId", rumbaUserId);
		jsonBody.put("scope", userScope);

		Response rbsResponse = RestAssuredAPI.post(webSite, headers, queryParams, jsonBody.toString(), endPointUrl);
		if (rbsResponse.getStatusCode() == 200) {
			accToken = rbsResponse.getBody().jsonPath().getString("id");
		} else {
			Log.message(rbsResponse.getStatusCode() + " status code returned from rbs token endpoint with message "
					+ rbsResponse.getBody().asString());
		}
		return accToken;
	}

	//********************** External Mapping Service ******************************//
	
	/**
	 * To get Rumba user id from given external user id and external system
	 * @param externalUserId
	 * @param externalSystem
	 * @return - Rumba UserId
	 */
	public static String getRumbaUserIdForExternalUserID(String externalUserId, String externalSystem) {
		String rumbaUserId = "";
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim();
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();

		HashMap<String, String> emsRequestHeaders = getJsonRequestHeaders();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("externalUserId", externalUserId);
		HashMap<String, String> emsQueryParam = new HashMap<String, String>();
		emsQueryParam.put("externalSystem", externalSystem);

		Log.message("Performing Get Request for " + emsBaseUrl+emsExternalUserIdEndpoint);
		Response emsResponse = RestAssuredAPI.get(emsBaseUrl, emsRequestHeaders, emsQueryParam, emsExternalUserIdEndpoint);

		if (emsResponse.getStatusCode() == 200) {
			Log.event("Get Response from EMS: " + emsResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(emsResponse.getBody().asString());
			rumbaUserId = responseBody.getJSONArray("data").getJSONObject(0).getString("userId");
		} else {
			Log.event(emsResponse.getStatusCode() + " status code returned from EMS with message " + emsResponse.getBody().asString());
		}
		return rumbaUserId;
	}
	
	/**
	 * To get External user id from given Rumba user id and external system
	 * @param rumbaUserId
	 * @param externalSystem
	 * @return - external UserId
	 */
	public static String getExternalUserIDForRumbaUserId(String rumbaUserId, String externalSystem) {
		String externalUserId = "";
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim();
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();

		HashMap<String, String> emsRequestHeaders = new HashMap<String, String>();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("userId", rumbaUserId);
		emsRequestHeaders.put("accept", JSON_CONTENT_TYPE);
		HashMap<String, String> emsQueryParam = new HashMap<String, String>();
		emsQueryParam.put("externalSystem", externalSystem);

		Log.message("Performing Get Request for " + emsBaseUrl+emsExternalUserMappingEndpoint);
		Response emsResponse = RestAssuredAPI.get(emsBaseUrl, emsRequestHeaders, emsQueryParam, emsExternalUserMappingEndpoint);

		if (emsResponse.getStatusCode() == 200) {
			Log.event("Get Response from EMS: " + emsResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(emsResponse.getBody().asString());
			externalUserId = responseBody.getJSONObject("data").getString("externalUserId");
		} else {
			Log.event(emsResponse.getStatusCode() + " status code returned from EMS with message " + emsResponse.getBody().asString());
		}
		return externalUserId;
	}
	
	/**
	 * To get EMS class mapping
	 * 
	 * @param externalClassId
	 * @return Response
	 */
	public static Response getClassMappingResponse(String externalClassId, String externalSystem) {
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim() + "/external-service";
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		String endPointUrl = emsExternalClassMappingSearchEndpoint;
		
		HashMap<String, String> defaultHeader = new HashMap<String, String>();
		HashMap<String, String> emsRequestHeaders = new HashMap<String, String>();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("Content-Type", JSON_CONTENT_TYPE);

		String requestBody = "{ \"externalSystem\": \"%s\", \"ids\": [ \"%s\" ], \"searchType\": \"EXTERNALCLASSID\" }";
		String formattedRequestBody = String.format(requestBody, externalSystem, externalClassId);
		
		JSONObject jsonRequestBody = new JSONObject(formattedRequestBody);
		
		Log.message("<b>Performing POST Request for EMS Class mapping</b>: " + emsBaseUrl + endPointUrl);
		Response postResponse = RestAssuredAPI.post(emsBaseUrl, emsRequestHeaders, defaultHeader, jsonRequestBody.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", emsBaseUrl, endPointUrl, emsRequestHeaders, defaultHeader, postResponse, jsonRequestBody.toString());
		
		return postResponse;
	}
	
	/**
	 * To get CMS Class Id for the given external class Id
	 * @param externalClassId
	 * @param externalSystem
	 * @return
	 */
	public static String getCMSClassIdForExternalClassId(String externalClassId, String externalSystem) {
		String classId = "";
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim() + "/external-service";
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		String endPointUrl = emsExternalClassMappingSearchEndpoint;

		HashMap<String, String> defaultHeader = new HashMap<String, String>();
		HashMap<String, String> emsRequestHeaders = new HashMap<String, String>();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("Content-Type", JSON_CONTENT_TYPE);

		String requestBody = "{ \"externalSystem\": \"%s\", \"ids\": [ \"%s\" ], \"searchType\": \"EXTERNALCLASSID\" }";
		String formattedRequestBody = String.format(requestBody, externalSystem, externalClassId);
		JSONObject jsonRequestBody = new JSONObject(formattedRequestBody);

		Response postResponse = RestAssuredAPI.post(emsBaseUrl, emsRequestHeaders, defaultHeader, jsonRequestBody.toString(), endPointUrl);
		if (postResponse.getStatusCode() == 200) {
			Log.event("Get Response from EMS: " + postResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(postResponse.getBody().asString());
			classId = responseBody.getJSONObject("data").getJSONObject(externalClassId).get("classId").toString();
		} else {
			Log.event(postResponse.getStatusCode() + " status code returned from EMS with message "
					+ postResponse.getBody().asString());
		}
		return classId;
	}
	
	/**
	 * To get External Assignment Mapping Response from given assignment Id and external system
	 * @param assignmentId
	 * @param externalSystem
	 * @return - Response
	 */
	public static Response getExternalAssignmentMappingResponse(String assignmentId, String externalSystem) {
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim();
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		String emsEndpoint = String.format(emsAssignmentMappingEndpoint, assignmentId);

		HashMap<String, String> emsRequestHeaders = getJsonRequestHeaders();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("assignmentId", assignmentId);
		HashMap<String, String> emsQueryParam = new HashMap<String, String>();
		emsQueryParam.put("externalSystem", externalSystem);

		Log.message("Performing Get Request for " + emsBaseUrl+emsEndpoint);
		Response emsResponse = RestAssuredAPI.get(emsBaseUrl, emsRequestHeaders, emsQueryParam, emsEndpoint);
		RealizeUtils.apiLogMessageFormatter("GET", emsBaseUrl, emsEndpoint, emsRequestHeaders, emsQueryParam, emsResponse, "");
		return emsResponse;
	}
	
	/**
	 * To delete External Assignment Mapping for given assignment Id and external system
	 * @param assignmentId
	 * @param classId
	 * @return - True - if assignment mapping deleted otherwise return false
	 */
	public static boolean deleteAssignmentMapping(String assignmentId, String classId, String externalSystem, boolean...logAsEvent) {
		boolean isDeleted = false;
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim();
		String emsEndPoint = String.format(emsAssignmentMappingDeletionEndpoint, externalSystem);

		HashMap<String, String> emsRequestHeaders = new HashMap<String, String>();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("Content-Type", JSON_CONTENT_TYPE);
		
		HashMap<String, String> queryParams = new HashMap<String, String>();
		//queryParams.put("externalSystem", externalSystem);
		
		String requestBody = "{ \"%s\": [\"%s\"] }";
		String formattedRequestBody = String.format(requestBody, classId, assignmentId);
		JSONObject jsonRequestBody = new JSONObject(formattedRequestBody);

		Log.event("<b>Performing DELETE Request for Assignment Mapping</b>: " + emsBaseUrl + emsEndPoint);
		
		Response deleteResponse = RestAssured.given().baseUri(emsBaseUrl).headers(emsRequestHeaders)
				.queryParams(queryParams).body(jsonRequestBody.toString()).delete(emsEndPoint);
		
		
		RealizeUtils.apiLogMessageFormatter("DELETE", emsBaseUrl, emsEndPoint, emsRequestHeaders, queryParams, deleteResponse,
				jsonRequestBody.toString().replaceAll("\\\\/", "\\/"), logAsEvent);
		
		if (deleteResponse.getStatusCode() == 202) {
			Log.event("Response from Assignment service: " + deleteResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(deleteResponse.getBody().asString());
			if (responseBody.getBoolean("success")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from EMS API with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}
	
	/**
	 * To create External Assignment Mapping for given assignment mapping request
	 * @param assignmentMappingRequestBody
	 * @return - Response
	 */
	public static Response createAssignmentMapping(String assignmentMappingRequestBody) {
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim();
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		String endPointUrl = emsAssignmentMappingCreationEndpoint;

		HashMap<String, String> defaultHeader = new HashMap<String, String>();
		HashMap<String, String> emsRequestHeaders = new HashMap<String, String>();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("Content-Type", JSON_CONTENT_TYPE);

		JSONObject jsonRequestBody = new JSONObject(assignmentMappingRequestBody);

		Log.message("<b>Performing POST Request for EMS Assignment mapping</b>: " + emsBaseUrl + endPointUrl);
		Response postResponse = RestAssuredAPI.post(emsBaseUrl, emsRequestHeaders, defaultHeader, jsonRequestBody.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", emsBaseUrl, endPointUrl, emsRequestHeaders, defaultHeader, postResponse, jsonRequestBody.toString());
		return postResponse;
	}
	
	/**

	 * To get External Assignment Mapping Response from given Realize assignment Id and external system
	 * @param externalAssignmentId
	 * @param externalSystem
	 * @return - Response
	 */
	public static Response getExternalAssignmentMappingResponseByExternalId(String externalAssignmentId, String externalSystem) {
		String emsBaseUrl = configProperty.getProperty("ems.base.url").trim();
		String emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		String emsEndpoint = configProperty.getProperty("ems.assignmentmappingbyexternalid.endpoint").trim();

		HashMap<String, String> emsRequestHeaders = getJsonRequestHeaders();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		HashMap<String, String> emsQueryParam = new HashMap<String, String>();
		emsQueryParam.put("externalSystem", externalSystem);
		emsQueryParam.put("externalAssignmentId", externalAssignmentId);
		

		Log.message("Performing Get Request for " + emsBaseUrl+emsEndpoint);
		Response emsResponse = RestAssuredAPI.get(emsBaseUrl, emsRequestHeaders, emsQueryParam, emsEndpoint);
		RealizeUtils.apiLogMessageFormatter("GET", emsBaseUrl, emsEndpoint, emsRequestHeaders, emsQueryParam, emsResponse, "");
		return emsResponse;
	}

	/**
	 *  To remove class mapping for LTIA class using EMS API
	 * @param eventDetails HashMap<String, String> - List of all optional fields for CMS class delete event
	 * @param timeStamp
	 * @return - Response
	 */
	public static Response listenForCMSClassDeleteEventAsLTIA(HashMap<String, String> eventDetails, String timeStamp, boolean... logAsEvent) throws Exception {
		String emsBaseURL = configProperty.getProperty("ems.base.url").trim();
		String emsEndPoint = configProperty.getProperty("ems.listenForCMSClassDeleteEventAsLTIA.endpoint").trim();
		HashMap<String, String> emsRequestHeaders = new HashMap<String, String>();

		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("MESSAGE-TYPE", "publish");
		queryParams.put("MESSAGE-ID", eventDetails.get("messageID"));
		queryParams.put("DELIVERY-ATTEMPT-ID", eventDetails.get("deliveryAttemptID"));
		queryParams.put("PAYLOAD", eventDetails.get("payLoad"));
		queryParams.put("PAYLOAD-CONTENT-TYPE", "application/json");
		queryParams.put("AUTHORIZATION", eventDetails.get("authorization"));
		queryParams.put("AUTHORIZATION-DELIMITER", eventDetails.get("authorizationDelimiter"));

		Log.message("<b>Performing POST Request :</b> " + emsBaseURL + emsEndPoint);
		Response response = RestAssuredAPI.post(emsBaseURL, emsRequestHeaders, queryParams,"", emsEndPoint);
		RealizeUtils.apiLogMessageFormatter("POST", emsBaseURL, emsEndPoint, emsRequestHeaders, queryParams,
				response, "", logAsEvent);

		return response;
	}
	
	/**
	 *  To get assignment type by assignment id
	 * @param assignment id
	 * @param logAsEvent
	 * @return - Response
	 */
	public static Response getAssignmentDetailsResponse(String assignmentId, boolean... logAsEvent) throws Exception {
		String rbsBaseURL = configProperty.getProperty("rbs.assignment.base.url").trim();
		String rbsAuthorization = configProperty.getProperty("roster.basic.authorization").trim();
		String rbsEndPoint = configProperty.getProperty("rbs.assignment.end.url").trim();


		HashMap<String, String> rbsRequestHeaders = getJsonRequestHeaders();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		rbsRequestHeaders.put("Authorization", rbsAuthorization);
		rbsRequestHeaders.put("Accept", "application/json");		
		rbsEndPoint = String.format(rbsEndPoint, assignmentId);
		
		Log.message("<b>Performing GET Request for assignment</b>: " + rbsBaseURL + rbsEndPoint);
		Response response = RestAssuredAPI.get(rbsBaseURL, rbsRequestHeaders, queryParams, rbsEndPoint);
		RealizeUtils.apiLogMessageFormatter("GET", rbsBaseURL, rbsEndPoint, rbsRequestHeaders, queryParams, response, "", logAsEvent);

		return response;
	}
	

	//********************** LTI Tool Gateway Service ******************************//
	
	/**
	 * To get LTI Platform configuration response from LTI tool gateway api
	 * @param issuerId
	 * @param clientId
	 * @return Response
	 */
	public static Response getPlatformConfigurationResponse(String issuerId, String clientId) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();
		
		HashMap<String, String> ltiRequestHeaders = new HashMap<String, String>();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		ltiRequestHeaders.put("accept", JSON_CONTENT_TYPE);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();
		ltiQueryParam.put("issuerId", issuerId);
		if (clientId != null) {
			ltiQueryParam.put("clientId", clientId);
		}

		Log.message("Performing Get Request for " + ltiBaseUrl+endPointUrl);
		Response ltiResponse = RestAssuredAPI.get(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam, endPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam, ltiResponse, "");
		return ltiResponse;
	}
	
	/**
	 * To get multi tentant Platform configuration response from LTI tool gateway api
	 * @param issuerId
	 * @param clientId
	 * @param deploymentId
	 * @return Response
	 */
	public static Response getMultiTentantPlatformConfigurationResponse(String issuerId, String clientId, String deploymentId) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();
		
		HashMap<String, String> ltiRequestHeaders = new HashMap<String, String>();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		ltiRequestHeaders.put("accept", JSON_CONTENT_TYPE);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();
		ltiQueryParam.put("issuerId", issuerId);
		ltiQueryParam.put("clientId", clientId);
		ltiQueryParam.put("deploymentId", deploymentId);

		Log.message("Performing Get Request for " + ltiBaseUrl+endPointUrl);
		Response ltiResponse = RestAssuredAPI.get(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam, endPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam, ltiResponse, "");
		return ltiResponse;
	}

	/**
	 * To create new Platform configuration with the given platform details using LTI tool gateway API
	 * @param platformDetails - HashMap<String, String>
	 * @return - Response
	 */
	public static Response createPlatformConfiguration(HashMap<String, String> platformDetails) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();
		
		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();
		JSONObject ltiPayload = RealizeUtils.getJsonFromMap(platformDetails);

		if (ltiPayload.has("isAccountLinkingEnabled")) {
			if (ltiPayload.get("isAccountLinkingEnabled").toString().equalsIgnoreCase("true"))
				ltiPayload.put("isAccountLinkingEnabled", true);
			else if (ltiPayload.get("isAccountLinkingEnabled").toString().equalsIgnoreCase("false"))
				ltiPayload.put("isAccountLinkingEnabled", false);
		}

		if (ltiPayload.has("isAutoMatchingEnabled")) {
			if (ltiPayload.get("isAutoMatchingEnabled").toString().equalsIgnoreCase("true"))
				ltiPayload.put("isAutoMatchingEnabled", true);
			else if (ltiPayload.get("isAutoMatchingEnabled").toString().equalsIgnoreCase("false"))
				ltiPayload.put("isAutoMatchingEnabled", false);
		}

		Log.message("Performing POST Request for " + ltiBaseUrl+endPointUrl);
		Response ltiResponse = RestAssuredAPI.post(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam, ltiPayload.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam, ltiResponse, ltiPayload.toString());
		return ltiResponse;
	}

	/**
	 * To update given Platform configuration with given platform details using LTI tool gateway API
	 * @param issuerId - Platform Issuer Id
	 * @param clientId - client Id (optional)
	 * @param newtoolkeys - 'true' would generate new tool public/private keys during update. Defaults to 'false' if not available.
	 * @param platformHmap - HashMap<String, String>
	 * @return - Response
	 */
	public static Response updatePlatformConfiguration(String issuerId, String clientId, String newtoolkeys, HashMap<String, String> platformHmap, String... deploymentId) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();

		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);

		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();
		ltiQueryParam.put("issuerId", issuerId);
		if (clientId != null)
			ltiQueryParam.put("clientId", clientId.toString().trim());
		if (deploymentId != null && deploymentId.length > 0) 
			ltiQueryParam.put("deploymentId", deploymentId[0].toString().trim());
		if (newtoolkeys != null && !newtoolkeys.isEmpty())
			ltiQueryParam.put("newtoolkeys", newtoolkeys.toString().toLowerCase().trim());

		JSONObject ltiPayload = RealizeUtils.getJsonFromMap(platformHmap);
		if (ltiPayload.has("isLisLinkingRequired")
                && ltiPayload.get("isLisLinkingRequired").toString().equalsIgnoreCase("true")) {
            ltiPayload.put("isLisLinkingRequired", true);
        } else if (ltiPayload.has("isLisLinkingRequired")
                && ltiPayload.get("isLisLinkingRequired").toString().equalsIgnoreCase("false")) {
            ltiPayload.put("isLisLinkingRequired", false);
        }

		Log.message("Performing PUT Request for " + ltiBaseUrl + endPointUrl);
		Response ltiResponse = RestAssured.given().baseUri(ltiBaseUrl).headers(ltiRequestHeaders)
				.queryParams(ltiQueryParam).body(ltiPayload.toString()).put(endPointUrl.trim());
		RealizeUtils.apiLogMessageFormatter("PUT", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam,
				ltiResponse, ltiPayload.toString());
		return ltiResponse;
	}

	/**
	 * To delete LTI-A Platform configuration for given Issuer Id and Client Id
	 * 
	 * @param issuerId - issuer Id
	 * @param clientId - client Id, null if it is optional
	 * @return - true if deleted the Platform configuration
	 */
	public static boolean deletePlatformConfiguration(String issuerId, String clientId, boolean...logAsEvent) {
		boolean isDeleted = false;
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		headers.put("Authorization", ltiAuthorization);
		headers.put("Accept", JSON_CONTENT_TYPE);
		
		queryParams.put("issuerId", issuerId);
		if (clientId != null) {
			queryParams.put("clientId", clientId);
		}

		Log.event("<b>Performing DELETE Request for </b>: " + ltiBaseUrl + endPointUrl);
		Response deleteResponse = RestAssured.given().baseUri(ltiBaseUrl).headers(headers).queryParams(queryParams)
				.delete(endPointUrl);
		RealizeUtils.apiLogMessageFormatter("DELETE", ltiBaseUrl, endPointUrl, headers, queryParams, deleteResponse, "",
				logAsEvent);
		if (deleteResponse.getStatusCode() == 200) {
			if (deleteResponse.getBody().asString().trim().equals("SUCCESS")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from LTI tool gateway with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}
	
	/**
	 * To delete multi tentantPlatform configuration for given Issuer Id Client Id and Deployment Id
	 * 
	 * @param issuerId - issuer Id
	 * @param clientId - client Id, 
	 * @param deploymentId - deployment Id, 
	 * @return - true if deleted the Platform configuration
	 */
	public static boolean deleteMultiTenantPlatformConfiguration(String issuerId, String clientId, String deploymentId, boolean...logAsEvent) {
		boolean isDeleted = false;
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		headers.put("Authorization", ltiAuthorization);
		headers.put("Accept", JSON_CONTENT_TYPE);
		
		queryParams.put("issuerId", issuerId);
		queryParams.put("clientId", clientId);
		queryParams.put("deploymentId", deploymentId);


		Log.event("<b>Performing DELETE Request for </b>: " + ltiBaseUrl + endPointUrl);
		Response deleteResponse = RestAssured.given().baseUri(ltiBaseUrl).headers(headers).queryParams(queryParams)
				.delete(endPointUrl);
		RealizeUtils.apiLogMessageFormatter("DELETE", ltiBaseUrl, endPointUrl, headers, queryParams, deleteResponse, "",
				logAsEvent);
		if (deleteResponse.getStatusCode() == 200) {
			if (deleteResponse.getBody().asString().trim().equals("SUCCESS")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from LTI tool gateway with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}

	/**
	 * To get given platform details for the given IssuerId, clientId and
	 * deploymentId using LTI tool gateway api
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentId
	 * @param jsonKey
	 * @return - value of given json key
	 */
	private static String getPlatformDetails(String issuerId, String clientId, String deploymentId, String jsonKey) {
		String jsonValue = "";
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.platformConfig.endpoint").trim();
		
		HashMap<String, String> ltiRequestHeaders = new HashMap<String, String>();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		ltiRequestHeaders.put("accept", JSON_CONTENT_TYPE);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();
		ltiQueryParam.put("issuerId", issuerId);
		if (clientId != null && !clientId.isEmpty()) {
			ltiQueryParam.put("clientId", clientId);
		}
		if (deploymentId != null && !deploymentId.isEmpty()) {
			ltiQueryParam.put("deploymentId", deploymentId.toString().trim());
		}

		Log.message("Performing Get Request for " + ltiBaseUrl + endPointUrl);
		Response ltiResponse = RestAssuredAPI.get(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam, endPointUrl);

		if (ltiResponse.getStatusCode() == 200) {
			Log.event("Get Response from LTI tool gateway: " + ltiResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(ltiResponse.getBody().asString());
			jsonValue = responseBody.getString(jsonKey);
		} else {
			Log.message(ltiResponse.getStatusCode() + " status code returned from LTI-A with message "
					+ ltiResponse.getBody().asString());
		}
		return jsonValue;
	}
	
	/**
	 * To get the DeploymentId for the given platform Issuer Id, clientId and deploymentId
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentIds
	 * @return
	 */
	public static String getDeploymentIdForIssuerId(String issuerId, String clientId, String... deploymentIds) {
		String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
		return getPlatformDetails(issuerId, clientId, deploymentId, "deploymentId");
	}
	
	/**
	 * To get the Integration Name for the given platform Issuer Id, clientId and deploymentId
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentIds
	 * @return
	 */
	public static String getIntegrationName(String issuerId, String clientId, String... deploymentIds) {
		String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
		return getPlatformDetails(issuerId, clientId, deploymentId, "integrationName");
	}

	/**
	 * To get the platform Name for the given Issuer Id, clientId and deploymentId
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentIds
	 * @return
	 */
	public static String getPlatformName(String issuerId, String clientId, String... deploymentIds) {
		String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
		return getPlatformDetails(issuerId, clientId, deploymentId, "platformName");
	}

	/**
	 * To get the rumbaDistrictId for the given platform Issuer Id
	 * @param issuerId
	 * @return
	 */
	public static String getOrganizationIdForIssuerId(String issuerId) {
		String clientId = null, deploymentId = null;
		return getPlatformDetails(issuerId, clientId, deploymentId, "rumbaDistrictId");
	}
	
	/**
	 * To get the rumbaDistrictId for the given platform Issuer Id, clientId and deploymentId
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentIds
	 * @return
	 */
	public static String getOrganizationIdForIssuerId(String issuerId, String clientId, String... deploymentIds) {
		String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
		return getPlatformDetails(issuerId, clientId, deploymentId, "rumbaDistrictId");
	}

	/**
	 * To get platform public key for the given platform Issuer Id, clientId and deploymentId
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentIds
	 * @return
	 */
	public static String getPlatformPublicKeyForIssuerId(String issuerId, String clientId, String... deploymentIds) {
		String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
		return getPlatformDetails(issuerId, clientId, deploymentId, "platformPublicKey");
	}
	
	/**
	 * To get tool public key mapped for the given platform Issuer Id, clientId and deploymentId
	 * 
	 * @param issuerId
	 * @param clientId
	 * @param deploymentIds
	 * @return
	 */
	public static String getToolPublicKeyForIssuerId(String issuerId, String clientId, String... deploymentIds) {
		String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
		return getPlatformDetails(issuerId, clientId, deploymentId, "toolPublicKey");
	}

	/**
     * To get the text of Tool key set URL
     * 
     * @param issuerId
     * @param clientId
     * @param deploymentIds
     * @return - Tool key set URL
     */
    public static String getToolKeySetUrl(String issuerId, String clientId, String... deploymentIds) {
    	String deploymentId = null;
		if (deploymentIds != null && deploymentIds.length > 0) {
			deploymentId = deploymentIds[0];
		}
        return getPlatformDetails(issuerId, clientId, deploymentId, "toolKeySetUrl");
    }

	/**
	 * To get Access Token Response for the given Issuer Id and scopes using LTI tool gateway API
	 * 
	 * @param issuerId   - Issuer Id of a LTIA Platform
	 * @param clientId   - client Id
	 * @param scope      - IMS Global scopes, each scope should be separated by || (double pipe) symbol
	 * @param logAsEvent
	 * @return
	 */
	public static Response getPlatformAccessTokenResponse(String issuerId, String clientId, String scope,
			boolean... logAsEvent) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.accessToken.endpoint").trim();

		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();

		String[] arrScopes = scope.split("\\|\\|");
		JSONObject jsonRequestBody = new JSONObject();
		JSONArray scopeJson = new JSONArray(arrScopes);
		jsonRequestBody.put("platformIssuerId", issuerId);
		jsonRequestBody.put("scope", scopeJson);
		if (clientId != null) {
			jsonRequestBody.put("platformClientId", clientId.toString().trim());
		}

		Log.message("Performing POST Request for " + ltiBaseUrl + endPointUrl);
		Response ltiResponse = RestAssuredAPI.post(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam,
				jsonRequestBody.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam,
				ltiResponse, jsonRequestBody.toString(), logAsEvent);
		return ltiResponse;
	}
	
	/**
	 * To get Access Token String for the given Issuer Id and scopes using LTI tool gateway API
	 * 
	 * @param issuerId   - Issuer Id of a LTIA Platform
	 * @param clientId   - client Id
	 * @param scope      - IMS Global scopes, each scope should be separated by || (double pipe) symbol
	 * @param logAsEvent - true if you want to print the response in Log.event
	 * @return String - Access token
	 */
	public static String getPlatformAccessToken(String issuerId, String clientId, String scope, boolean... logAsEvent) {
		String access_token = "";
		Response ltiResponse = getPlatformAccessTokenResponse(issuerId, clientId, scope, logAsEvent);
		if (ltiResponse.getStatusCode() == 200) {
			JSONObject responseBody = new JSONObject(ltiResponse.getBody().asString());
			access_token = responseBody.getString("access_token");
		} else {
			Log.message(ltiResponse.getStatusCode() + " status code returned from platform Access token"
					+ " endpoint with message " + ltiResponse.getBody().asString());
		}
		return access_token;
	}
	
	/**
	 * To get Access Token String for the given Issuer Id and scopes using LTI tool gateway API
	 * 
	 * @param issuerId   - Issuer Id of a LTIA Platform
	 * @param clientId   - client Id
	 * @param scope      - IMS Global scopes, each scope should be separated by || (double pipe) symbol
	 * @param logAsEvent - true if you want to print the response in Log.event
	 * @return String - Access token
	 */
	public static String getPlatformAccessTokenForSchoology(String issuerId, String clientId, String deploymentId, String scope, boolean... logAsEvent) {
		String access_token = "";
		Response ltiResponse = getPlatformAccessTokenResponseForSchoology(issuerId, clientId, deploymentId, scope, logAsEvent);
		if (ltiResponse.getStatusCode() == 200) {
			JSONObject responseBody = new JSONObject(ltiResponse.getBody().asString());
			access_token = responseBody.getString("access_token");
		} else {
			Log.message(ltiResponse.getStatusCode() + " status code returned from platform Access token"
					+ " endpoint with message " + ltiResponse.getBody().asString());
		}
		return access_token;
	}
	
	/**
	 * To get Access Token Response for the given Issuer Id and scopes using LTI tool gateway API
	 * 
	 * @param issuerId   - Issuer Id of a LTIA Platform
	 * @param clientId   - client Id
	 * @param scope      - IMS Global scopes, each scope should be separated by || (double pipe) symbol
	 * @param logAsEvent
	 * @return
	 */
	public static Response getPlatformAccessTokenResponseForSchoology(String issuerId, String clientId, String deploymentId, String scope,
			boolean... logAsEvent) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.accessToken.endpoint").trim();

		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();

		String[] arrScopes = scope.split("\\|\\|");
		JSONObject jsonRequestBody = new JSONObject();
		JSONArray scopeJson = new JSONArray(arrScopes);
		jsonRequestBody.put("platformIssuerId", issuerId);
		jsonRequestBody.put("scope", scopeJson);
		jsonRequestBody.put("platformDeploymentId", deploymentId);
		jsonRequestBody.put("platformClientId", clientId);

		Log.message("Performing POST Request for " + ltiBaseUrl + endPointUrl);
		Response ltiResponse = RestAssuredAPI.post(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam,
				jsonRequestBody.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam,
				ltiResponse, jsonRequestBody.toString(), logAsEvent);
		return ltiResponse;
	}

	/**
	 * To get org Selection Response for the given cachekey and authorization
	 * 
	 * @param authorization - API Authorization
	 * @param cacheKey      - external cache key
	 * @param type          - user-selection or class-selection enum
	 * @param selectedOrgs  - list of organization id
	 * @return
	 */
	public static Response getOrgSelectionResponse(String authorization, String cacheKey, orgSelectionType type,
			List<String> selectedOrgs) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.orgSelectionResponse.endpoint").trim();

		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", authorization);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();

		JSONObject jsonRequestBody = new JSONObject();
		JSONArray orgsJson = new JSONArray(selectedOrgs);
		jsonRequestBody.put("cacheKey", cacheKey);
		jsonRequestBody.put("selectedOrganizations", orgsJson);
		jsonRequestBody.put("orgSelectionType", type.toString());

		Log.message("Performing POST Request for " + ltiBaseUrl + endPointUrl);
		Response ltiResponse = RestAssuredAPI.post(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam,
				jsonRequestBody.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam,
				ltiResponse, jsonRequestBody.toString(), false);
		return ltiResponse;
	}

	/**
	 * To get org Selection Response of user for the given cachekey
	 * 
	 * @param cacheKey     - external cache key
	 * @param selectedOrgs - list of organization id
	 * @return
	 */
	public static Response getOrgSelectionResponseForUser(String cacheKey, List<String> selectedOrgs) {
		String ltiAuthorization = configProperty.getProperty("lti.tool.basic.authorization").trim();
		return getOrgSelectionResponse(ltiAuthorization, cacheKey, orgSelectionType.UserCreation, selectedOrgs);
	}

	/**
	 * To get org Selection Response of class for the given cachekey
	 * 
	 * @param cacheKey     - external cache key
	 * @param selectedOrgs - list of organization id
	 * @return
	 */
	public static Response getOrgSelectionResponseForClass(String cacheKey, List<String> selectedOrg) {
		String ltiAuthorization = configProperty.getProperty("lti.tool.basic.authorization").trim();
		return getOrgSelectionResponse(ltiAuthorization, cacheKey, orgSelectionType.ClassCreation, selectedOrg);
	}

	/**
	 * To get account Login Response for the given cachekey and authorization
	 * 
	 * @param authorization - API Authorization
	 * @param cacheKey      - external cache key
	 * @param userId        - A&E userId
	 * @return
	 */
	public static Response getAccountLoginResponse(String authorization, String cacheKey, String userId) {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.accountLoginResponse.endpoint").trim();

		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", authorization);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();

		JSONObject jsonRequestBody = new JSONObject();
		jsonRequestBody.put("cacheKey", cacheKey);
		if (userId != null && !userId.isEmpty())
			jsonRequestBody.put("userId", userId);

		Log.message("Performing POST Request for " + ltiBaseUrl + endPointUrl);
		Response ltiResponse = RestAssuredAPI.post(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam,
				jsonRequestBody.toString(), endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam,
				ltiResponse, jsonRequestBody.toString(), false);
		return ltiResponse;
	}

	/**
	 * To get account Login Response of user for the given cachekey
	 * 
	 * @param cacheKey - cache key sent from A&E
	 * @param userId   - A&E userId
	 * @return
	 */
	public static Response getAccountLoginResponse(String cacheKey, String userId) {
		String ltiAuthorization = configProperty.getProperty("lti.tool.basic.authorization").trim();
		return getAccountLoginResponse(ltiAuthorization, cacheKey, userId);
	}
	
	/**
	 * To delete tool gateway service redis cache
	 * 
	 * @return - Response
	 */
	public static Response flushRedisCacheForLTIA() {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.flushRedisCache.endpoint").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		
		HashMap<String, String> ltiRequestHeaders = getJsonRequestHeaders();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();

		Log.message("Performing POST Request for " + ltiBaseUrl+endPointUrl);
		Response ltiResponse = RestAssuredAPI.post(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam, "", endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam, ltiResponse, "");
		return ltiResponse;
	}
	
	/**
	 * To get integrated platforms configuration details
	 * 
	 * @return - Response
	 */
	public static Response getLMSConfigurationDetails() {
		String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
		String ltiAuthorization = configProperty.getProperty("lti.tool.admin.authorization").trim();
		String endPointUrl = configProperty.getProperty("lti.tool.lmsConfigurations.endpoint").trim();

		HashMap<String, String> ltiRequestHeaders = new HashMap<String, String>();
		ltiRequestHeaders.put("Authorization", ltiAuthorization);
		ltiRequestHeaders.put("Content-Type", JSON_CONTENT_TYPE);
		HashMap<String, String> ltiQueryParam = new HashMap<String, String>();

		Log.message("Performing Get Request for " + ltiBaseUrl+endPointUrl);
		Response ltiResponse = RestAssuredAPI.get(ltiBaseUrl, ltiRequestHeaders, ltiQueryParam, endPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", ltiBaseUrl, endPointUrl, ltiRequestHeaders, ltiQueryParam, ltiResponse,	"");

		return ltiResponse;
	}

	// ********************** User Profile Service ******************************//

	/**
	 * To get the user profile JSON response from UPS using self RBS token of given rumba user
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the second argument
	 * 
	 * @param rumbaUserId - Rumba userId of the user
	 * @return
	 * @throws Exception
	 */
	public static Response getUserProfileResponseUsingRBSToken(String rumbaUserId) throws Exception {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return getUserProfileResponseUsingRBSToken(rumbaUserId, rbsAccessToken);
	}

	/**
	 * To get the user profile JSON response from UPS using RBS token
	 * 
	 * @param rumbaUserId    - Rumba UserId
	 * @param rbsAccessToken - RBS Access token
	 * @return Response
	 */
	public static Response getUserProfileResponseUsingRBSToken(String rumbaUserId, String rbsAccessToken) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = "/ups/api/v1/users/";

		String upsAuthorization = "Bearer " + rbsAccessToken;
		Map<String, String> queryParam = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", JSON_CONTENT_TYPE);
		headers.put("userIds", rumbaUserId);
		headers.put("Authorization", upsAuthorization);

		Log.message("Performing Get Request for " + upsBaseUrl+upsEndPointUrl);
		Response upsResponse = RestAssuredAPI.get(upsBaseUrl, headers, queryParam, upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", upsBaseUrl, upsEndPointUrl, headers,	queryParam, upsResponse, "");
		return upsResponse;
	}
	
	/**
	 * To get Bulk user JSON response by userIds from UPS using RBS token
	 * 
	 * @param bulkUserIds    - List of userIds
	 * @param adminUserId    - RumbaId of Customer Admin which the users belongs
	 * @param rbsAccessToken - RBS Access token of the Customer Admin
	 * @return
	 */
	public static Response getBulkUserProfileResponseUsingRBSToken(List<String> bulkUserIds, String adminUserId, String rbsAccessToken) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = "/ups/api/v1/user/bulk/userids";
		String upsAuthorization = "";
		if (rbsAccessToken.length() > 0) {
			upsAuthorization = "Bearer " + rbsAccessToken;
		} else {
			upsAuthorization = configProperty.getProperty("ups.basic.authorization");
		}

		Map<String, String> queryParam = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", JSON_CONTENT_TYPE);
		headers.put("listOfUserIds", String.join(",", bulkUserIds));
		headers.put("userIds", adminUserId);
		headers.put("Authorization", upsAuthorization);

		Log.message("Performing Get Request for " + upsBaseUrl+upsEndPointUrl);
		Response upsResponse = RestAssuredAPI.get(upsBaseUrl, headers, queryParam, upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", upsBaseUrl, upsEndPointUrl, headers,	queryParam, upsResponse, "");
		return upsResponse;
	}

	/**
	 * To get the user profile JSON response from UPS API v2 by using Basic Authorization
	 * 
	 * @param rumbaUserId
	 * @return
	 */
	public static Response getUserProfileResponse(String rumbaUserId) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsAuthorization = configProperty.getProperty("ups.basic.authorization");
		String upsEndPointUrl = "/ups/api/v2/user";

		Map<String, String> queryParam = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json;charset=UTF-8");
		headers.put("userId", rumbaUserId);
		headers.put("userIds", rumbaUserId);
		headers.put("Authorization", upsAuthorization);

		Log.message("Performing Get Request for " + upsBaseUrl+upsEndPointUrl);
		Response upsResponse = RestAssuredAPI.get(upsBaseUrl, headers, queryParam, upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", upsBaseUrl, upsEndPointUrl, headers,	queryParam, upsResponse, "");
		return upsResponse;
	}

	/**
	 * To create new rumba users and get its JSON response from UPS using RBS token
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the third argument
	 * 
	 * @param rumbaUserId - Rumba UserId of Customer Admin
	 * @param userDetails - User account details for creating new rumba user
	 * @return Response
	 * @throws Exception
	 */
	public static Response createRumbaUserUsingRBSToken(String rumbaUserId, String userDetails) throws Exception {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return createRumbaUserUsingRBSToken(rumbaUserId, userDetails, rbsAccessToken);
	}

	/**
	 * To create new rumba users and get its JSON response from UPS using RBS token
	 * 
	 * @param rumbaUserId    - Rumba UserId of Customer Admin
	 * @param userDetails    - User account details for creating new rumba user
	 * @param rbsAccessToken - RBS Access token of Customer Admin
	 * @return Response
	 */
	public static Response createRumbaUserUsingRBSToken(String rumbaUserId, String userDetails, String rbsAccessToken) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = "/ups/api/v1/user";

		String upsAuthorization = "Bearer " + rbsAccessToken;
		Map<String, String> upsQueryParam = new HashMap<String, String>();
		HashMap<String, String> upsRequestHeaders = getJsonRequestHeaders();
		upsRequestHeaders.put("userIds", rumbaUserId);
		upsRequestHeaders.put("Authorization", upsAuthorization);

		Log.message("</br><u>Performing POST Request to create rumba user via UPS API using RBS Access token</u>");
		Response upsResponse = RestAssuredAPI.post(upsBaseUrl, upsRequestHeaders, upsQueryParam, userDetails.toString(),
				upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", upsBaseUrl, upsEndPointUrl, upsRequestHeaders, upsQueryParam,
				upsResponse, userDetails.toString());
		return upsResponse;
	}

	/**
	 * To update Rumba user and get its JSON response from UPS using RBS token
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the third argument
	 * 
	 * @param rumbaUserId - Rumba UserId of Customer Admin
	 * @param userDetails - User account details to update for the rumba user
	 * @return Response
	 * @throws Exception
	 */
	public static Response updateRumbaUserUsingRBSToken(String rumbaUserId, String userDetails) throws Exception {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return updateRumbaUserUsingRBSToken(rumbaUserId, userDetails, rbsAccessToken);
	}

	/**
	 * To update rumba user and get its JSON response from UPS using RBS token
	 * 
	 * @param rumbaUserId    - Rumba UserId of Customer Admin
	 * @param userDetails    - User account details to update for the rumba user
	 * @param rbsAccessToken - RBS Access token of Customer Admin
	 * @return Response
	 */
	public static Response updateRumbaUserUsingRBSToken(String rumbaUserId, String userDetails, String rbsAccessToken) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = "/ups/api/v1/user";

		String upsAuthorization = "Bearer " + rbsAccessToken;
		Map<String, String> upsQueryParam = new HashMap<String, String>();
		HashMap<String, String> upsRequestHeaders = new HashMap<String, String>();
		upsRequestHeaders.put("Accept", JSON_CONTENT_TYPE);
		upsRequestHeaders.put("userId", rumbaUserId);
		upsRequestHeaders.put("userIds", rumbaUserId);
		upsRequestHeaders.put("Authorization", upsAuthorization);

		Log.message("</br><u>Performing PUT Request to update rumba user via UPS API using RBS Access token</u>");
		Response upsResponse = RestAssuredAPI.PUT(upsBaseUrl, upsRequestHeaders, "", "", JSON_CONTENT_TYPE,
				userDetails.toString(), upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("PUT", upsBaseUrl, upsEndPointUrl, upsRequestHeaders, upsQueryParam,
				upsResponse, userDetails.toString());
		return upsResponse;
	}

	/**
	 * To get response of given user profile attributes for the given Rumba user Id using given authorization
	 * 
	 * @param attributeKeys    - list of attributes or null if you want to return all the attributes for the given user
	 * @param rumbaUserId      - Rumba UserId
	 * @param upsAuthorization - Basic Authorization or Bearer token
	 * @param logAsEvent       - true if you want to print the response in Log.event
	 * @return Response
	 */
	public static Response getUserAttributesResponse(List<String> attributeKeys, String rumbaUserId, String upsAuthorization, boolean...logAsEvent) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = configProperty.getProperty("ups.users.attributes.endpoint");

		HashMap<String, String> queryParam = new HashMap<String, String>();
		if (attributeKeys != null && !attributeKeys.isEmpty())
			queryParam.put("keys", String.join(",", attributeKeys));
		
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", JSON_CONTENT_TYPE);
		headers.put("userIds", rumbaUserId);
		headers.put("Authorization", upsAuthorization);

		if (logAsEvent.length > 0 && logAsEvent[0]) {
			Log.event("Performing Get Request for " + upsBaseUrl + upsEndPointUrl);
		} else {
			Log.message("Performing Get Request for " + upsBaseUrl + upsEndPointUrl);
		}
		Response upsResponse = RestAssuredAPI.get(upsBaseUrl, headers, queryParam, upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", upsBaseUrl, upsEndPointUrl, headers,	queryParam, upsResponse, "", logAsEvent);
		return upsResponse;
	}
	
	/**
	 * To get given user profile attributes for the given Rumba user Id using Basic Authentication
	 * 
	 * @param attributeKeys  - list of attributes or null if you want to return all the attributes for the given user
	 * @param rumbaUserId    - Rumba UserId
	 * @return - HashMap<String, String> of attributes values
	 */
	public static HashMap<String, String> getUserAttributes(List<String> attributeKeys, String rumbaUserId) {
		String basicAuthorization = configProperty.getProperty("ups.basic.authorization");
		Response upsResponse = getUserAttributesResponse(attributeKeys, rumbaUserId, basicAuthorization, true);
		HashMap<String, String> profileAttributes = new HashMap<String, String>();
		try {
			if (upsResponse.getStatusCode() == 200) {
				final JSONObject jsonProfileResponse = new JSONArray(upsResponse.getBody().asString()).getJSONObject(0).getJSONObject(rumbaUserId);
				if (attributeKeys != null && !attributeKeys.isEmpty()) {
					for (String key : attributeKeys) {
						if (jsonProfileResponse.has(key) && !jsonProfileResponse.isNull(key)) {
							profileAttributes.put(key, jsonProfileResponse.getString(key));
						} else {
							profileAttributes.put(key, null);
						}
					}
				} else {
					ObjectMapper mapper = new ObjectMapper();
					TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>(){};
					profileAttributes = mapper.readValue(jsonProfileResponse.toString(), typeRef);
				}
			} else {
				Log.message(upsResponse.getStatusCode() + " status code returned from UPS with message "
						+ upsResponse.getBody().asString());
			}
		} catch (Exception err) {
			Log.message("Error in getting user attribute from UPS API using Basic Auth. Error: " + err.getMessage());
		}
		return profileAttributes;
	}
	
	/**
	 * To get given user profile attributes for the given Rumba user Id using RBS Access token 
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the third argument
	 * 
	 * @param attributeKeys  - list of attributes or null if you want to return all the attributes for the given user
	 * @param rumbaUserId    - Rumba UserId
	 * @return - HashMap<String, String> of attributes values
	 * @throws Exception
	 */
	public static HashMap<String, String> getUserAttributesUsingRBSToken(List<String> attributeKeys, String rumbaUserId) throws Exception {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return getUserAttributesUsingRBSToken(attributeKeys, rumbaUserId, rbsAccessToken);
	}
	
	/**
	 * To get given user profile attributes for the given Rumba user Id using RBS Access token
	 * 
	 * @param attributeKeys  - list of attributes or null if you want to return all the attributes for the given user
	 * @param rumbaUserId    - Rumba UserId
	 * @param rbsAccessToken - RBS Access token of the given rumba user
	 * @return - HashMap<String, String> of attributes values
	 */
	public static HashMap<String, String> getUserAttributesUsingRBSToken(List<String> attributeKeys, String rumbaUserId, String rbsAccessToken) {
		String rbsuthorization = "Bearer " + rbsAccessToken;
		Response upsResponse = getUserAttributesResponse(attributeKeys, rumbaUserId, rbsuthorization, true);
		HashMap<String, String> profileAttributes = new HashMap<String, String>();
		try {
			if (upsResponse.getStatusCode() == 200) {
				final JSONObject jsonProfileResponse = new JSONArray(upsResponse.getBody().asString()).getJSONObject(0).getJSONObject(rumbaUserId);
				if (attributeKeys != null && !attributeKeys.isEmpty()) {
					for (String key : attributeKeys) {
						if (jsonProfileResponse.has(key) && !jsonProfileResponse.isNull(key)) {
							profileAttributes.put(key, jsonProfileResponse.getString(key));
						} else {
							profileAttributes.put(key, null);
						}
					}
				} else {
					ObjectMapper mapper = new ObjectMapper();
					TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>(){};
					profileAttributes = mapper.readValue(jsonProfileResponse.toString(), typeRef);
				}
			} else {
				Log.message(upsResponse.getStatusCode() + " status code returned from UPS with message "
						+ upsResponse.getBody().asString());
			}
		} catch (Exception err) {
			Log.message("Error in getting user attribute from UPS API using RBS Access token. Error: " + err.getMessage());
		}
		return profileAttributes;
	}
	
	/**
	 * To get Upsert response of given user profile attributes for the given Rumba user Id using given authorization
	 * 
	 * @param attributeKeys    - list of attributes or null if you want to return all the attributes for the given user
	 * @param rumbaUserId      - Rumba UserId
	 * @param upsAuthorization - Basic Authorization or Bearer token
	 * @param logAsEvent       - true if you want to print the response in Log.event
	 * @return Response
	 */
	public static Response getUpdateAttributeResponse(String profileJson, String rumbaUserId, String upsAuthorization, boolean...logAsEvent) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = configProperty.getProperty("ups.users.attributes.endpoint");
		
		Map<String, String> queryParam = new HashMap<String, String>();
		HashMap<String, String> headers = getJsonRequestHeaders();
		headers.put("userIds", rumbaUserId);
		headers.put("Authorization", upsAuthorization);

		JSONObject postUpdateBody = new JSONObject(profileJson);
		if (logAsEvent.length > 0 && logAsEvent[0]) {
			Log.event("Performing POST Request for " + upsBaseUrl + upsEndPointUrl);
		} else {
			Log.message("Performing POST Request for " + upsBaseUrl + upsEndPointUrl);
		}
		Response upsResponse = RestAssuredAPI.post(upsBaseUrl, headers, queryParam, postUpdateBody.toString(), upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", upsBaseUrl, upsEndPointUrl, headers, queryParam, upsResponse, postUpdateBody.toString(), logAsEvent);
		return upsResponse;
	}
	
	/**
	 * To Upsert given user profile attributes for the given Rumba user Id using Basic Authentication
	 * 
	 * @param profileJson - JSON string containing attribute value pairs
	 * @param rumbaUserId - Rumba UserId
	 * @return - true if given attributes get updated
	 */
	public static boolean updateUserAttributes(String profileJson, String rumbaUserId) {
		boolean isUpdated = false;
		String basicAuthorization = configProperty.getProperty("ups.basic.authorization");
		Response upsResponse = getUpdateAttributeResponse(profileJson, rumbaUserId, basicAuthorization, true);
		if (upsResponse.getStatusCode() == 200) {
			isUpdated = upsResponse.getBody().as(Boolean.class);
		} else {
			Log.message(upsResponse.getStatusCode() + " status code returned from UPS with message "
					+ upsResponse.getBody().asString());
		}
		return isUpdated;
	}
	
	/**
	 * To Upsert given user profile attributes for the given Rumba user Id using RBS Access token
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the third argument
	 * 
	 * @param profileJson - JSON string containing attribute value pairs
	 * @param rumbaUserId - Rumba UserId
	 * @return - true if given attributes get updated
	 * @throws Exception
	 */
	public static boolean updateUserAttributesUsingRBSToken(String profileJson, String rumbaUserId) throws Exception {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return updateUserAttributesUsingRBSToken(profileJson, rumbaUserId, rbsAccessToken);
	}
	
	/**
	 * To Upsert given user profile attributes for the given Rumba user Id using RBS Access token
	 * 
	 * @param profileJson    - JSON string containing attribute value pairs
	 * @param rumbaUserId    - Rumba UserId
	 * @param rbsAccessToken - RBS Access token of the given rumba user
	 * @return - true if given attributes get updated
	 */
	public static boolean updateUserAttributesUsingRBSToken(String profileJson, String rumbaUserId, String rbsAccessToken) {
		boolean isUpdated = false;
		String rbsAuthorization = "Bearer " + rbsAccessToken;
		Response upsResponse = getUpdateAttributeResponse(profileJson, rumbaUserId, rbsAuthorization, false);
		if (upsResponse.getStatusCode() == 200) {
			isUpdated = upsResponse.getBody().as(Boolean.class);
		} else {
			Log.message(upsResponse.getStatusCode() + " status code returned from UPS with message "
					+ upsResponse.getBody().asString());
		}
		return isUpdated;
	}
	
	/**
	 * To get delete response of given attributes from the given Rumba user using given authorization
	 * 
	 * @param attributeKeys    - list of attributes to delete
	 * @param rumbaUserId      - Rumba UserId
	 * @param upsAuthorization - Basic Authorization or Bearer token
	 * @param logAsEvent       - true if you want to print the response in Log.event
	 * @return Response
	 */
	public static Response getDeleteAttributeResponse(List<String> attributeKeys, String rumbaUserId, String upsAuthorization, boolean...logAsEvent) {
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = configProperty.getProperty("ups.users.attributes.endpoint");

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("accept", JSON_CONTENT_TYPE);
		headers.put("userIds", rumbaUserId);
		headers.put("Authorization", upsAuthorization);

		HashMap<String, String> queryParam = new HashMap<String, String>();
		if (attributeKeys != null && !attributeKeys.isEmpty()) {
			queryParam.put("key", String.join(",", attributeKeys));
		}
		
		if (logAsEvent.length > 0 && logAsEvent[0]) {
			Log.event("Performing DELETE Request for " + upsBaseUrl + upsEndPointUrl);
		} else {
			Log.message("Performing DELETE Request for " + upsBaseUrl + upsEndPointUrl);
		}
		Response upsResponse = RestAssured.given().baseUri(upsBaseUrl).headers(headers).queryParams(queryParam).delete(upsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("DELETE", upsBaseUrl, upsEndPointUrl, headers, queryParam, upsResponse, "", logAsEvent);
		return upsResponse;
	}
	
	/**
	 * To delete given attributes from the given Rumba user using Basic Authentication
	 * 
	 * @param attributeKeys - list of attributes to delete
	 * @param rumbaUserId   - Rumba UserId
	 * @return - true if given attributes get deleted
	 */
	public static boolean deleteUserAttributes(List<String> attributeKeys, String rumbaUserId) {
		boolean isdeleted = false;
		String basicAuthorization = configProperty.getProperty("ups.basic.authorization");
		Response upsResponse = getDeleteAttributeResponse(attributeKeys, rumbaUserId, basicAuthorization, true);
		if (upsResponse.getStatusCode() == 200) {
			isdeleted = upsResponse.getBody().as(Boolean.class);
		} else {
			Log.message(upsResponse.getStatusCode() + " status code returned from UPS with message "
					+ upsResponse.getBody().asString());
		}
		return isdeleted;
	}
	
	/**
	 * To delete given attributes from the given Rumba user using RBS Access token
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the third argument
	 * 
	 * @param attributeKeys - list of attributes to delete
	 * @param rumbaUserId   - Rumba UserId
	 * @return - true if given attributes get deleted
	 * @throws Exception
	 */
	public static boolean deleteUserAttributesUsingRBSToken(List<String> attributeKeys, String rumbaUserId) throws Exception {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return deleteUserAttributesUsingRBSToken(attributeKeys, rumbaUserId, rbsAccessToken);
	}
	
	/**
	 * To delete given attributes from the given Rumba user using RBS Access token
	 * 
	 * @param attributeKeys - list of attributes to delete
	 * @param rumbaUserId   - Rumba UserId
	 * @param rbsAccessToken - RBS Access token of the given rumba user
	 * @return - true if given attributes get deleted
	 */
	public static boolean deleteUserAttributesUsingRBSToken(List<String> attributeKeys, String rumbaUserId, String rbsAccessToken) {
		boolean isdeleted = false;
		String rbsAuthorization = "Bearer " + rbsAccessToken;
		Response upsResponse = getDeleteAttributeResponse(attributeKeys, rumbaUserId, rbsAuthorization, true);
		if (upsResponse.getStatusCode() == 200) {
			isdeleted = upsResponse.getBody().as(Boolean.class);
		} else {
			Log.message(upsResponse.getStatusCode() + " status code returned from UPS with message "
					+ upsResponse.getBody().asString());
		}
		return isdeleted;
	}
	
	/**
	 * To verify rumba user id is existing in Rumba via UPS using RBS token
	 * Note: This method will only work if the given rumba user has default password.
	 * 
	 * @param rumbaUserId
	 * @return - true, if rumba user id exist
	 */
	public static boolean isRumbaUserIDExistingInRumbaViaUPS(String rumbaUserId) {
		boolean status = false;
		String actualRumbaUserId = "";
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken != null && !rbsAccessToken.isEmpty()) {
			Response upsResponse = getUserProfileResponseUsingRBSToken(rumbaUserId, rbsAccessToken);
			if (upsResponse.getStatusCode() == 200) {
				JSONObject responseBody = new JSONObject(upsResponse.getBody().asString());
				Log.event("Response body from UPS: <pre>" + responseBody.toString(4) + "</pre>");
				actualRumbaUserId = responseBody.getJSONArray("users").getJSONObject(0).getJSONObject("rumbaUser").getString("userId");
				if (actualRumbaUserId.equals(rumbaUserId)) {
					Log.event("Rumba user id '" + rumbaUserId + "' is matched.");
					status = true;
				} else {
					Log.event("Rumba user id is not matched. Expected: '" + rumbaUserId + "', Actual: '"
							+ actualRumbaUserId + "'");
					status = false;
				}
			} else {
				Log.event(upsResponse.getStatusCode() + " status code returned from UPS with message "
						+ upsResponse.getBody().asString());
				status = false;
			}
		}
		return status;
	}

	/**
	 * To verify rumba user id is existing in Rumba via UPS v2 API using Basic Auth
	 * 
	 * @param rumbaUserId
	 * @return - true, if rumba user id exist
	 */
	public static boolean isRumbaUserIDExistingInRumbaViaUPSV2(String rumbaUserId) {
		boolean status = false;
		String actualRumbaUserId = "";
		Response upsResponse = getUserProfileResponse(rumbaUserId);
		if (upsResponse.getStatusCode() == 200) {
			JSONObject responseBody = new JSONObject(upsResponse.getBody().asString());
			Log.event("Response body from UPS: <pre>" + responseBody.toString(4) + "</pre>");
			actualRumbaUserId = responseBody.getString("userId");
			if (actualRumbaUserId.equals(rumbaUserId)) {
				Log.event("Rumba user id '" + rumbaUserId + "' is matched.");
				status = true;
			} else {
				Log.event("Rumba user id is not matched. Expected: '" + rumbaUserId + "', Actual: '" + actualRumbaUserId + "'");
				status = false;
			}
		} else {
			Log.event(upsResponse.getStatusCode() + " status code returned from UPS with message "
					+ upsResponse.getBody().asString());
			status = false;
		}
		return status;
	}

	/**
	 * To verify given username is existing in Rumba via UPS using customer admin credentials
	 * 
	 * @param userName   - Username to check
	 * @param caUserName - Customer Admin username
	 * @param caPassword - Customer Admin password
	 * @return - true if given username exists in rumba
	 * @throws Exception
	 */
	public static boolean isUsernameExistsInRumbaViaUPS(String userName, String caUserName, String caPassword) throws Exception {
		String rumbaUserId = RumbaClient.getRumbaUserId(caUserName);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(caUserName, caPassword);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			throw new Exception("Unable to get RBS Access token using CasTGC for '" + caUserName + "' having password " + caPassword);
		}
		return isUsernameExistsInRumbaUsingRBSToken(userName, rumbaUserId, rbsAccessToken);
	}
	
	/**
	 * To verify given username is existing in Rumba via UPS using RBS access token of Customer admin
	 * 
	 * @param userName       - Username to check
	 * @param caRumbaUserId  - Customer Admin rumba userId
	 * @param rbsAccessToken - RBS access token of customer admin
	 * @return - true if given username exists in rumba
	 */
	public static boolean isUsernameExistsInRumbaUsingRBSToken(String userName, String caRumbaUserId, String rbsAccessToken) {
		boolean isExists = false;
		String upsBaseUrl = configProperty.getProperty("ups.base.url");
		String upsEndPointUrl = "/ups/api/v1/user/check-username-availability";

		String upsAuthorization = "Bearer " + rbsAccessToken;
		Map<String, String> queryParam = new HashMap<String, String>();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", JSON_CONTENT_TYPE);
		headers.put("userName", userName);
		headers.put("userId", caRumbaUserId);
		headers.put("userIds", caRumbaUserId);
		headers.put("Authorization", upsAuthorization);

		Log.event("Performing Get Request for " + upsBaseUrl+upsEndPointUrl);
		Response upsResponse = RestAssuredAPI.get(upsBaseUrl, headers, queryParam, upsEndPointUrl);
		if (upsResponse.getStatusCode() == 200) {
			JSONObject responseBody = new JSONObject(upsResponse.getBody().asString());
			isExists = !responseBody.getBoolean(userName);
		} else {
			Log.event(upsResponse.getStatusCode() + " status code returned from UPS with message "
					+ upsResponse.getBody().asString());
		}
		return isExists;
	}
	
	/**
	 * To update given password for given user
	 * @param userName
	 * @param password
	 * @return - true if password updated successfully else return false
	 */
	public static boolean updatePasswordForGivenUser(String userName, String password) {
		boolean status = false;
		String upsBaseUrl = configProperty.getProperty("ups.base.url").trim();
		String upsAuthorization = configProperty.getProperty("ups.basic.authorization").trim();
		String endPointUrl = "/ups/api/v2/user";
		
		String rumbaUserId = RumbaClient.getRumbaUserId(userName);
		if (rumbaUserId == null || rumbaUserId.isEmpty()) {
			Log.event("Unable to get rumba userId, so trying again");
			rumbaUserId = RumbaClient.getRumbaUserId(userName);
		}
		
		HashMap<String, String> v2UserHeaders = getJsonRequestHeaders();
		HashMap<String, String> cookies = new HashMap<String, String>();
		v2UserHeaders.put("userId", rumbaUserId);
		v2UserHeaders.put("userIds", rumbaUserId);
		v2UserHeaders.put("Authorization", upsAuthorization);
		
		JSONObject userDetails = new JSONObject();
		JSONArray jsonOrgIds = new JSONArray();
		List<String> orgIds = RumbaClient.getAffiliatedOrgIdByUserName(userName);
		for (String orgId : orgIds)
			jsonOrgIds.put(orgId);
		
		userDetails.put("organizationIds", jsonOrgIds);
		userDetails.put("password", password);
		
		String payLoad = userDetails.toString();
		Response updateResponse = RestAssuredAPI.PUT(upsBaseUrl, v2UserHeaders, payLoad, endPointUrl, cookies);
		if (updateResponse.getStatusCode() == 200) {
			status = true;
			JSONObject createdRumbaJson = new JSONObject(updateResponse.getBody().asString());
			Log.event("</br><b>Response Body::</b><pre>" + createdRumbaJson.toString(4) + "</pre>");
		} else {
			status = false;
			Log.event(updateResponse.getStatusCode() + " status code returned from UPS with message "
					+ updateResponse.getBody().asString());
		}
		return status;
	}
	
	// ********************** Browse Content BFF Service ******************************//
	
	/**
	 * To get GraphQl Response for the given payload from given endPoint in Browse Content BFF service
	 * @param accessToken
	 * @param graphqlPayload
	 * @param bffEndPointUrl
	 * @return - GraphQl Response
	 */
	public static Response getBrowseContentGraphQLResponse(String accessToken, String graphqlPayload, String bffEndPointUrl) {
		Response graphQlResponse = null;
		Log.event("Getting Browser Content GraphQl Response from BFF service");
		String bffBaseURL = configProperty.getProperty("bff.browseContent.base.url").trim();
		HashMap<String, String> queryParametersMap = new HashMap<>();
		Map<String, String> bffRequestHeaders = getJsonRequestHeaders();
		if (accessToken != null) {
			bffRequestHeaders.put("Authorization", "Bearer " + accessToken);
        }

		graphQlResponse = RestAssuredAPI.post(bffBaseURL, bffRequestHeaders, queryParametersMap, graphqlPayload,
				bffEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", bffBaseURL, bffEndPointUrl, bffRequestHeaders, queryParametersMap,
				graphQlResponse, graphqlPayload);

		return graphQlResponse;
	}
	
	/**
	 * To get GraphQl Response for the given payload from graphQL endPoint in Browse Content BFF service
	 * @param accessToken
	 * @param graphqlPayload
	 * @return
	 */
	public static Response getBrowseContentGraphQLResponse(String accessToken, String graphqlPayload) {
		String graphQlEndpoint = configProperty.getProperty("bff.browseContent.graphql.endpoint").trim();
		return getBrowseContentGraphQLResponse(accessToken, graphqlPayload, graphQlEndpoint);
	}
	
	/**
	 * To get GraphQl Response for the given payload from graphQL endPoint in Browse Content BFF service
	 * @param accessToken
	 * @param graphqlPayload
	 * @return
	 */
	public static Response getBFFPlatformConfigGraphQLResponse(String accessToken, String graphqlPayload) {
		Response graphQlResponse = null;
		String graphQlEndpoint = configProperty.getProperty("ltia.platformConfig.bff.graphql.endpoint").trim();
		Log.event("Getting GraphQl Response for BFF Platform Configuration");
		String baseURL = configProperty.getProperty("ltia.platformConfig.bff.base.url").trim();
		HashMap<String, String> queryParametersMap = new HashMap<>();
		HashMap<String, String> bffRequestHeaders = getJsonRequestHeaders();
		if (accessToken != null) {
			bffRequestHeaders.put("authorization", "Bearer " + accessToken);
        }

		graphQlResponse = RestAssuredAPI.post(baseURL, bffRequestHeaders, queryParametersMap, graphqlPayload,
				graphQlEndpoint);
		RealizeUtils.apiLogMessageFormatter("POST", baseURL, graphQlEndpoint, bffRequestHeaders, queryParametersMap,
				graphQlResponse, graphqlPayload);

		return graphQlResponse;
	}
	
	/**
	 * To get post request from graphql
	 * @param accessToken
	 * 			- String accessToken
	 * @param payLoad
	 * 			- String body
	 * @param bffEndPoint
	 * @return postResponse
	 * @throws Exception 
	 */
	public static Response graphqlPostRequests(String accessToken,String payLoad,String bffEndPoint) throws Exception {
		Map<String, String> headers = getJsonRequestHeaders();
		headers.put("Authorization","Bearer " +accessToken.trim());
		String bffBaseURL = configProperty.getProperty("bff.browseContent.base.url").trim();		
		try {
			postResponse = RestAssuredAPI.POST(bffBaseURL, headers, "", "", JSON_CONTENT_TYPE, payLoad, bffEndPoint);
			RealizeUtils.apiLogMessageFormatter("POST", bffBaseURL, "", headers, null, postResponse, payLoad);	
		} catch (Exception e) {
			Log.exception(e);
		}
		return postResponse;
	}

	/**
	 * To get the total search result count from search query in browser content
	 * search page
	 * 
	 * @param searchKeyword
	 *            - keyword for which the search results are queried
	 * @param rbsToken
	 *            - authorization token
	 * @return totalCount
	 */
	public static int getTotalResultCountInBrowseContentSearchPage(String searchKeyword, String rbsToken) {
		int totalCount = -1;
		Log.event("Getting the total search result count in browse content search page");
		String jsonReqBody = String.format(BROWSE_CONTENT_GRAPHQL_BODY, searchKeyword, 1, 50);
		Response browseContentResponse = RBSAPIUtils.getBrowseContentGraphQLResponse(rbsToken, jsonReqBody);

		if (browseContentResponse.getStatusCode() == 200) {
			Log.event(browseContentResponse.getStatusCode()
					+ " status code returned from browse content search query with message "
					+ browseContentResponse.getBody().asString());
			JSONObject postObject = new JSONObject(browseContentResponse.getBody().asString());
			totalCount = postObject.getJSONObject("data").getJSONObject("getSearchResult").getInt("totalAvailable");			
		} else {
			Log.message(browseContentResponse.getStatusCode()
					+ " status code returned from browse content search query with message "
					+ browseContentResponse.getBody().asString());			
		}
		return totalCount;
	}

	/**
	 * To get content details from search query in browser content search page
	 * 
	 * @param rbsToken - authorization token
	 * @param searchKeyword - keyword for which the search results are queried
	 * @param pageNo - pagination number
	 * @param pageSize - number of contents
	 * @return JSONArray - contents
	 */
	public static JSONArray getContentDetailsInBrowseContentSearchPage(String rbsToken, String searchKeyword, int pageNo, int pageSize) {
		JSONArray jsonContents = new JSONArray();
		Log.event("Getting content details in json from browse content search page");
		String jsonReqBody = String.format(BROWSE_CONTENT_GRAPHQL_BODY, searchKeyword, pageNo, pageSize);
		Log.message("<!--");
		Response browseContentResponse = RBSAPIUtils.getBrowseContentGraphQLResponse(rbsToken, jsonReqBody);
		Log.message("--><div class=\"test-message\">&emsp;");
		if (browseContentResponse.getStatusCode() == 200) {
			Log.event(browseContentResponse.getStatusCode()
					+ " status code returned from browse content search query with message "
					+ browseContentResponse.getBody().asString());
			JSONObject postObject = new JSONObject(browseContentResponse.getBody().asString());
			jsonContents = postObject.getJSONObject("data").getJSONObject("getSearchResult").getJSONArray("contents");
		} else {
			Log.message(browseContentResponse.getStatusCode()
					+ " status code returned from browse content search query with message "
					+ browseContentResponse.getBody().asString());			
		}
		return jsonContents;
	}

	/**
	 * To get first 50 content details with No Search keyword from first page in browser content search page
	 * @param rbsToken - authorization token
	 * @return JSONArray - contents
	 */
	public static JSONArray getContentDetailsInBrowseContentSearchPage(String rbsToken) {
		return getContentDetailsInBrowseContentSearchPage(rbsToken, "", 1, 50);
	}

	
	// ******************** License Service **************************** //
	
	/**
	 * To perform GET user subscriptions request
	 * 
	 * @param orgId
	 * @param userId
	 * @return licenseResponse
	 */
	public static Response getUserSubscriptions(String orgId, String userId) {
		String licenseBaseUrl = configProperty.getProperty("license.base.url");
		String licenseEndPoint = configProperty.getProperty("license.getProduct.endpoint");
		String licenseAuthorization = configProperty.getProperty("license.basic.authorization");
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		
		headers.put("Authorization", licenseAuthorization);
		headers.put("Accept", "application/json");
		licenseEndPoint = String.format(licenseEndPoint, userId, orgId);
		
		Log.message("<b>Performing Get Request for User subscriptions</b>: " + licenseBaseUrl + licenseEndPoint);
		Response licenseResponse = RestAssuredAPI.get(licenseBaseUrl, headers, queryParams, licenseEndPoint); 
		RealizeUtils.apiLogMessageFormatter("GET", licenseBaseUrl, licenseEndPoint, headers, queryParams, licenseResponse, "");
		return licenseResponse;
	}
	
	/**
	 * To perform Update user subscriptions using POST request
	 * 
	 * @param requestBody
	 * @return licenseResponse
	 * @throws Exception
	 */
	public static Response postUserSubscriptions(String requestBody) throws Exception {
		String licenseBaseUrl = configProperty.getProperty("license.base.url");
		String licenseEndPoint = configProperty.getProperty("license.subscribe.endpoint");
		String licenseAuthorization = configProperty.getProperty("license.basic.authorization");
		String contentType = "application/json";
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		
		headers.put("Authorization", licenseAuthorization);
		headers.put("accept", contentType);
		
		Log.message("<b>Performing Post Request for User subscriptions</b>: " + licenseBaseUrl + licenseEndPoint);
		Response licenseResponse = RestAssuredAPI.POST(licenseBaseUrl, headers, "", "", contentType, requestBody, licenseEndPoint);
		RealizeUtils.apiLogMessageFormatter("POST", licenseBaseUrl, licenseEndPoint, headers, queryParams, licenseResponse, requestBody);
		return licenseResponse;
	}

	// ******************** Roster services API *****************************//

	/**
	 * To get section details in CMS format by section id using self RBS token of
	 * the given user
	 * Note: This method will only work if the given rumba user has default password.
	 * 		 If the given user don't have default password, please generate RBS Access token and pass the token in the third argument
	 * 
	 * @param sectionId   - section id of a class
	 * @param rumbaUserId - User Rumba ID (Required when using RBS Auth)
	 * @return Response
	 */
	public static Response getSectionDetailsUsingRBSToken(String sectionId, String rumbaUserId, boolean...logAsEvent) {
		String password = configProperty.getProperty("DEFAULT_PASSWORD");
		String userName = RumbaClient.getUsernameByUserId(rumbaUserId);
		String rbsAccessToken = RBSAPIUtils.getAccessTokenUsingCastGC(userName, password);
		if (rbsAccessToken == null || rbsAccessToken.isEmpty()) {
			Log.fail("Unable to get RBS Access token using CasTGC for '" + userName + "' having password " + password);
		}
		return getSectionDetailsUsingRBSToken(sectionId, rumbaUserId, rbsAccessToken, logAsEvent);
	}
	
	/**
	 * To get section details in CMS format by section id using RBS token
	 * 
	 * @param sectionId      - section id of a class
	 * @param rumbaUserId    - User Rumba ID (Required when using RBS Auth)
	 * @param rbsAccessToken - RBS Access token
	 * @return Response
	 */
	public static Response getSectionDetailsUsingRBSToken(String sectionId, String rumbaUserId, String rbsAccessToken, boolean...logAsEvent) {
		String rosterBaseUrl = configProperty.getProperty("roster.base.url");
		String rosterEndPoint = configProperty.getProperty("roster.cms.section.endpoint");

		String rosterAuthorization = "Bearer " + rbsAccessToken;
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();

		headers.put("Authorization", rosterAuthorization);
		headers.put("userId", rumbaUserId);
		headers.put("Accept", "application/json");
		rosterEndPoint = String.format(rosterEndPoint, sectionId);

		Log.message("<b>Performing GET Request for section</b>: " + rosterBaseUrl + rosterEndPoint);
		Response rosterResponse = RestAssuredAPI.get(rosterBaseUrl, headers, queryParams, rosterEndPoint);
		RealizeUtils.apiLogMessageFormatter("GET", rosterBaseUrl, rosterEndPoint, headers, queryParams, rosterResponse, "", logAsEvent);
		return rosterResponse;
	}

	/**
	 * To get section details in CMS format by section id
	 * 
	 * @param sectionId - section id of a class
	 * @return Response
	 */
	public static Response getSectionDetails(String sectionId) {
		String rosterBaseUrl = configProperty.getProperty("roster.base.url");
		String rosterEndPoint = configProperty.getProperty("roster.cms.section.endpoint");
		String rosterAuthorization = configProperty.getProperty("roster.basic.authorization");
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();

		headers.put("Authorization", rosterAuthorization);
		headers.put("Accept", "application/json");
		rosterEndPoint = String.format(rosterEndPoint, sectionId);

		Log.message("<b>Performing GET Request for section</b>: " + rosterBaseUrl + rosterEndPoint);
		Response rosterResponse = RestAssuredAPI.get(rosterBaseUrl, headers, queryParams, rosterEndPoint);
		RealizeUtils.apiLogMessageFormatter("GET", rosterBaseUrl, rosterEndPoint, headers, queryParams, rosterResponse,	"");
		return rosterResponse;
	}

	/**
	 * To get list of CMS class Id or section Id based on given Rumba user Id using RBS Access token
	 * @param rumbaUserId - Rumba user Id
	 * @return list of section Id
	 */
	public static List<String> getSectionIdsBasedonUserId(String rumbaUserId) {
		List<String> sectionIds = new ArrayList<String>();
		Response sectionResponse = getSectionDetailsUsingRBSToken("", rumbaUserId, true);
		if (sectionResponse.getStatusCode() == 200) {
			sectionIds = sectionResponse.getBody().jsonPath().getList("data.section.id");
		} else {
			Log.message(sectionResponse.getStatusCode() + " status code returned from roster section endpoint with message "
					+ sectionResponse.getBody().asString());
		}
		return sectionIds;
	}
	
	/**
	 * To get the section Id for the given class for the given user
	 * 
	 * @param className   - Class Name
	 * @param rumbaUserId - A&E userId
	 * @return - sectionId
	 */
	public static String getSectionIdForGivenClassName(String className, String rumbaUserId) {
		String sectionId = null;
		List<String> sectionIds = new ArrayList<String>();
		List<String> sectionNames = new ArrayList<String>();
		Response sectionResponse = getSectionDetailsUsingRBSToken("", rumbaUserId, true);
		if (sectionResponse.getStatusCode() == 200) {
			sectionIds = sectionResponse.getBody().jsonPath().getList("data.section.id");
			sectionNames = sectionResponse.getBody().jsonPath().getList("data.section.data.sectionInfo.sectionName");
			int index = sectionNames.indexOf(className);
			if (index != -1) {
				sectionId = sectionIds.get(index);
			}
		} else {
			Log.message(sectionResponse.getStatusCode() + " status code returned from roster section endpoint with message "
					+ sectionResponse.getBody().asString());
		}
		return sectionId;
	}
	
	/**
	 * To soft delete given section Id for given rumba user using Roster Service API
	 * @param sectionId
	 * @param rumbaUserId
	 * @return - true if deleted the given section
	 */
	public static boolean deleteGivenSectionId(String sectionId, String rumbaUserId) {
		boolean isDeleted = false;
		String rosterBaseUrl = configProperty.getProperty("roster.base.url");
		String rosterEndPoint = configProperty.getProperty("roster.cms.section.endpoint");
		String basicAuthorization = configProperty.getProperty("roster.basic.authorization").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();

		headers.put("Authorization", basicAuthorization);
		headers.put("userId", rumbaUserId);
		headers.put("Accept", "application/json");
		rosterEndPoint = String.format(rosterEndPoint, sectionId);

		Log.event("<b>Performing DELETE Request for section</b>: " + rosterBaseUrl + rosterEndPoint);
		Response rosterResponse = RestAssured.given().baseUri(rosterBaseUrl).headers(headers).queryParams(queryParams)
				.delete(rosterEndPoint);
		if (rosterResponse.getStatusCode() == 200) {
			Log.event("Response from Roster service: " + rosterResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(rosterResponse.getBody().asString());
			if (responseBody.getString("status").equalsIgnoreCase("Success")) {
				isDeleted = true;
			}
		} else {
			Log.event(rosterResponse.getStatusCode() + " status code returned from roster service with message "
					+ rosterResponse.getBody().asString());
		}
		return isDeleted;
	}

	/**
	 * To remove given student userId from the given section using Roster Service API
	 * 
	 * @param sectionId    - Class Id
	 * @param rumbaUserIds - List of student A&E Id
	 * @return - true if given students are deleted from the given section
	 */
	public static boolean deleteGivenStudentsFromClass(String sectionId, List<String> rumbaUserIds) {
		boolean isDeleted = false;
		String rosterBaseUrl = configProperty.getProperty("roster.base.url");
		String rosterEndPoint = configProperty.getProperty("roster.cms.students.endpoint");
		String basicAuthorization = configProperty.getProperty("roster.basic.authorization").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();

		headers.put("Authorization", basicAuthorization);
		headers.put("Accept", "application/json");
		rosterEndPoint = String.format(rosterEndPoint, sectionId);
		
		String bodyTemplate = "{\"data\":{\"section\":{\"id\":\"%s\",\"data\":{\"sectionInfo\":{\"students\":[]}}}},\"system\":{\"lifecycle\":{\"updatedBy\":\"%s\"}}}";
		String studJson = "{\"studentPiId\":\"%s\",\"deleted\":true}";
		bodyTemplate = String.format(bodyTemplate, sectionId, rumbaUserIds.get(0));
		JSONObject jsonBody = new JSONObject(bodyTemplate);
		JSONArray studs = jsonBody.getJSONObject("data").getJSONObject("section").getJSONObject("data")
				.getJSONObject("sectionInfo").getJSONArray("students");
		
		for (String id : rumbaUserIds) {
			JSONObject jsonStud = new JSONObject(String.format(studJson, id));
			studs.put(jsonStud);
		}
		Log.message(jsonBody.toString(4));
		Log.event("<b>Performing DELETE Request for section</b>: " + rosterBaseUrl + rosterEndPoint);
		Response rosterResponse = RestAssured.given().baseUri(rosterBaseUrl).headers(headers).queryParams(queryParams).body(jsonBody.toString())
				.delete(rosterEndPoint);
		if (rosterResponse.getStatusCode() == 200) {
			Log.event("Response from Roster service: " + rosterResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(rosterResponse.getBody().asString());
			if (responseBody.getString("status").equalsIgnoreCase("Success")) {
				isDeleted = true;
			}
		} else {
			Log.event(rosterResponse.getStatusCode() + " status code returned from roster service with message "
					+ rosterResponse.getBody().asString());
		}
		return isDeleted;
	}
	
	/**
	 * To remove given staff userIds from the given section using Roster Service API
	 * 
	 * @param sectionId    - Class Id
	 * @param rumbaUserIds - List of staff A&E Id
	 * @return - true if given staffs are deleted from the given section
	 */
	public static boolean deleteGivenStaffsFromClass(String sectionId, List<String> rumbaUserIds) {
		boolean isDeleted = false;
		String rosterBaseUrl = configProperty.getProperty("roster.base.url");
		String rosterEndPoint = configProperty.getProperty("roster.cms.staffs.endpoint");
		String basicAuthorization = configProperty.getProperty("roster.basic.authorization").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();

		headers.put("Authorization", basicAuthorization);
		headers.put("Accept", "application/json");
		rosterEndPoint = String.format(rosterEndPoint, sectionId);
		
		String bodyTemplate = "{\"data\":{\"section\":{\"id\":\"%s\",\"data\":{\"sectionInfo\":{\"staff\":[]}}}},\"system\":{\"lifecycle\":{\"updatedBy\":\"%s\"}}}";
		String staffJson = "{\"staffPiId\":\"%s\",\"deleted\":true}";
		bodyTemplate = String.format(bodyTemplate, sectionId, rumbaUserIds.get(0));
		JSONObject jsonBody = new JSONObject(bodyTemplate);
		JSONArray staffs = jsonBody.getJSONObject("data").getJSONObject("section").getJSONObject("data")
				.getJSONObject("sectionInfo").getJSONArray("staff");
		
		for (String id : rumbaUserIds) {
			JSONObject jsonStaff = new JSONObject(String.format(staffJson, id));
			staffs.put(jsonStaff);
		}
		Log.message(jsonBody.toString(4));
		Log.event("<b>Performing DELETE Request for section</b>: " + rosterBaseUrl + rosterEndPoint);
		Response rosterResponse = RestAssured.given().baseUri(rosterBaseUrl).headers(headers).queryParams(queryParams).body(jsonBody.toString())
				.delete(rosterEndPoint);
		if (rosterResponse.getStatusCode() == 200) {
			Log.event("Response from Roster service: " + rosterResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(rosterResponse.getBody().asString());
			if (responseBody.getString("status").equalsIgnoreCase("Success")) {
				isDeleted = true;
			}
		} else {
			Log.event(rosterResponse.getStatusCode() + " status code returned from roster service with message "
					+ rosterResponse.getBody().asString());
		}
		return isDeleted;
	}
	
	/**
	 * To add the student userIds to given section using Roster Service API
	 * 
	 * @param sectionId    - Class Id
	 * @param rumbaUserIds - List of student A&E Id
	 * @return - true if given students are added to given section
	 */
	public static boolean addGivenStudentsToClass(String sectionId, List<String> rumbaUserIds) {
		boolean isAdded = false;
		String rosterBaseUrl = configProperty.getProperty("roster.base.url");
		String rosterEndPoint = configProperty.getProperty("roster.cms.students.endpoint");
		String basicAuthorization = configProperty.getProperty("roster.basic.authorization").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();

		headers.put("Authorization", basicAuthorization);
		headers.put("Accept", "application/json");
		rosterEndPoint = String.format(rosterEndPoint, sectionId);
		
		String bodyTemplate = "{\"data\":{\"section\":{\"id\":\"%s\",\"data\":{\"sectionInfo\":{\"students\":[]}}}},\"system\":{\"lifecycle\":{\"updatedBy\":\"%s\"}}}";
		String studJson = "{\"studentPiId\":\"%s\"}";
		bodyTemplate = String.format(bodyTemplate, sectionId, rumbaUserIds.get(0));
		JSONObject jsonBody = new JSONObject(bodyTemplate);
		JSONArray studs = jsonBody.getJSONObject("data").getJSONObject("section").getJSONObject("data")
				.getJSONObject("sectionInfo").getJSONArray("students");
		
		for (String id : rumbaUserIds) {
			JSONObject jsonStud = new JSONObject(String.format(studJson, id));
			studs.put(jsonStud);
		}
		Log.message(jsonBody.toString(4));
		Log.event("<b>Performing PATCH Request for section</b>: " + rosterBaseUrl + rosterEndPoint);
		Response rosterResponse = RestAssured.given().baseUri(rosterBaseUrl).headers(headers).queryParams(queryParams).body(jsonBody.toString()).patch(rosterEndPoint);
		if (rosterResponse.getStatusCode() == 200) {
			Log.event("Response from Roster service: " + rosterResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(rosterResponse.getBody().asString());
			if (responseBody.getString("status").equalsIgnoreCase("Success")) {
				isAdded = true;
			}
		} else {
			Log.event(rosterResponse.getStatusCode() + " status code returned from roster service with message " + rosterResponse.getBody().asString());
		}
		return isAdded;
	}
	
	// ******************** Assignment services API *****************************//

	/**
	 * To get the oAuth Access token for the assignment service
	 * 
	 * @return
	 */
	public static String getAssignmentServiceAccessToken() {
		String accessToken = null;
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String tokenEndPoint = configProperty.getProperty("assignmentService.token.endpoint");
		String basicAuthorization = configProperty.getProperty("assignmentService.basic.authorization").trim();
		String grantType = configProperty.getProperty("assignmentService.token.default.grantType").trim();
		
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", basicAuthorization);
		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("grant_type", grantType);
		
		Response postResponse = RestAssuredAPI.post(assignmentServiceBaseUrl, headers, queryParams, "",	tokenEndPoint);
		if (postResponse.getStatusCode() == 200) {
			JSONObject jsonResponse = new JSONObject(postResponse.getBody().asString());
			accessToken = jsonResponse.getString("access_token");
		} else {
			Log.message(postResponse.getStatusCode()
					+ " status code returned from assignment service token endpoint with message "
					+ postResponse.getBody().asString());
		}
		return accessToken;
	}

	/**
	 * To delete all the assignments from the given classId using Assignment Service API
	 * 
	 * @param classId - sectionId of the class
	 * @return - true if deleted successfully
	 */
	public static boolean deleteAllAssignmentsInClass(String classId) {
		boolean isDeleted = false;
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String assignmentEndPoint = "/assignments";

		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		String accessToken = getAssignmentServiceAccessToken();

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json");
		queryParams.put("classId", classId);
		
		Log.event("<b>Performing DELETE Request for class</b>: " + assignmentServiceBaseUrl + assignmentEndPoint);
		Response deleteResponse = RestAssured.given().baseUri(assignmentServiceBaseUrl).headers(headers)
				.queryParams(queryParams).delete(assignmentEndPoint);
		if (deleteResponse.getStatusCode() == 200) {
			Log.event("Response from Assignment service: " + deleteResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(deleteResponse.getBody().asString());
			if (responseBody.getBoolean("success")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from assignment service API with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}

	/**
	 * To delete given AssignmentId using Assignment Service API
	 * 
	 * @param assignmentId - assignmentId of an class Assignment
	 * @return - true if deleted successfully
	 */
	public static boolean deleteGivenAssignment(String assignmentId) {
		boolean isDeleted = false;
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String assignmentEndPoint = "/assignments/" + assignmentId + "/userAssignments";

		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		String accessToken = getAssignmentServiceAccessToken();

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json");

		Log.event("<b>Performing DELETE Request for given assignment</b>: " + assignmentServiceBaseUrl
				+ assignmentEndPoint);
		Response deleteResponse = RestAssured.given().baseUri(assignmentServiceBaseUrl).headers(headers)
				.queryParams(queryParams).delete(assignmentEndPoint);
		if (deleteResponse.getStatusCode() == 200) {
			Log.event("Response from Assignment service: " + deleteResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(deleteResponse.getBody().asString());
			if (responseBody.getBoolean("success")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from assignment service API with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}

	/**
	 * To delete given user AssignmentId using Assignment Service API
	 * 
	 * @param userAssignmentId - user AssignmentId of a student
	 * @return - true if deleted successfully
	 */
	public static boolean deleteStudentAssignment(String userAssignmentId) {
		boolean isDeleted = false;
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String assignmentEndPoint = "/assignments/userAssignments/" + userAssignmentId;

		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		String accessToken = getAssignmentServiceAccessToken();

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json");
		
		Log.event("<b>Performing DELETE Request for student assignment</b>: " + assignmentServiceBaseUrl + assignmentEndPoint);
		Response deleteResponse = RestAssured.given().baseUri(assignmentServiceBaseUrl).headers(headers)
				.queryParams(queryParams).delete(assignmentEndPoint);
		if (deleteResponse.getStatusCode() == 200) {
			Log.event("Response from Assignment service: " + deleteResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(deleteResponse.getBody().asString());
			if (responseBody.getBoolean("success")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from assignment service API with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}

	/**
	 * To hide all the given assignments from the given classId using Assignment Service API
	 * 
	 * @param classId        - classId
	 * @param teacherRumbaId - teacher rumbaId
	 * @param assignmentIds  - List of assignmentId to be hidden
	 * @return - true if all the assignments are hidden
	 * 
	 */
	public static boolean hideAssignmentsInClass(String classId, String teacherRumbaId, List<String> assignmentIds) {
		boolean isHidden = false;
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String assignmentEndPoint = "/classes/%s/assignments/status";

		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		String accessToken = getAssignmentServiceAccessToken();

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", JSON_CONTENT_TYPE);
		headers.put("Content-Type", JSON_CONTENT_TYPE);
		queryParams.put("status", "INACTIVE");
		queryParams.put("userUuid", teacherRumbaId);
		queryParams.put("role", "teacher");
		assignmentEndPoint = String.format(assignmentEndPoint, classId);
		String jsonBody = new JSONArray(assignmentIds).toString();

		Log.event("<b>Performing PUT Request for class</b>: " + assignmentServiceBaseUrl + assignmentEndPoint);
		Response putResponse = RestAssured.given().baseUri(assignmentServiceBaseUrl).headers(headers)
				.queryParams(queryParams).body(jsonBody).put(assignmentEndPoint);
		if (putResponse.getStatusCode() == 200) {
			Log.event("Response from Assignment service: " + putResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(putResponse.getBody().asString());
			if (responseBody.getBoolean("success")) {
				isHidden = true;
			}
		} else {
			Log.event(putResponse.getStatusCode() + " status code returned from assignment service API with message "
					+ putResponse.getBody().asString());
		}
		return isHidden;
	}

	/**
	 * To get assignmentId for the given assignment name in given class
	 * 
	 * @param classId - sectionId of the class
	 * @param assignmentName - Assignment name
	 * @param isHidden - Whether the assignment is hidden or not
	 */
	public static String getAssignmentIdForGivenAssignment(String classId, String assignmentName, String isHidden) {
		String assignmentId = null;
		List<String> asignmentIds = new ArrayList<String>();
		List<String> assignmentNames = new ArrayList<String>();
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String assignmentEndPoint = "/classes/%s/assignmentsSummary";

		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		if (isHidden != null) {
			queryParams.put("active", isHidden.trim().toLowerCase());
		}

		String accessToken = getAssignmentServiceAccessToken();
		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json");
		assignmentEndPoint = String.format(assignmentEndPoint, classId);

		Log.event("<b>Performing Get Request for class</b>: " + assignmentServiceBaseUrl + assignmentEndPoint);
		Response getResponse = RestAssuredAPI.get(assignmentServiceBaseUrl, headers, queryParams, assignmentEndPoint);
		if (getResponse.getStatusCode() == 200) {
			assignmentNames = getResponse.getBody().jsonPath().getList("data.assignmentTitle");
			asignmentIds = getResponse.getBody().jsonPath().getList("data.assignmentId");
			int index = assignmentNames.indexOf(assignmentName);
			if (index != -1) {
				assignmentId = asignmentIds.get(index);
			}
		} else {
			Log.event(getResponse.getStatusCode() + " status code returned from assignment service API with message "
					+ getResponse.getBody().asString());
		}
		return assignmentId;
	}

	/**
	 * To get Assignment Summary api response for given assignment id
	 * 
	 * @param classId - sectionId of the class
	 * @param assignmentId - Assignment Id
	 */
	public static Response getAssignmentSummaryResponse(String classId, String assignmentId, boolean... logAsEvent) {
		String assignmentServiceBaseUrl = configProperty.getProperty("assignmentService.base.url");
		String assignmentEndPoint = "/classes/%s/assignments/%s/assignmentSummary";

		HashMap<String, String> headers = new HashMap<String, String>();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		String accessToken = getAssignmentServiceAccessToken();
		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("Accept", "application/json");
		assignmentEndPoint = String.format(assignmentEndPoint, classId, assignmentId);

		Log.event("<b>Performing Get Request for assignment</b>: " + assignmentServiceBaseUrl + assignmentEndPoint);
		Response assignmentResponse = RestAssuredAPI.get(assignmentServiceBaseUrl, headers, queryParams,
				assignmentEndPoint);
		RealizeUtils.apiLogMessageFormatter("GET", assignmentServiceBaseUrl, assignmentEndPoint, headers, queryParams,
				assignmentResponse, "", logAsEvent);
		return assignmentResponse;
	}

	// ******************** Class Provisioning Service *****************************//

	/**
	 * To get LTIA Class provisioning service response
	 * 
	 * @param organizationId             - organization Id
	 * @param requestBody                - LTIA request json string
	 * @param isAccountLinkingEnabled    - true if Account Linking required or not
	 * @param isAutoMatchingEnabled      - true if Auto Matching is enabled
	 * @param attributeName              - Name of LIS Linking attribute
	 * @param autoMatchingAttributeValue - Value of LIS Linking attribute
	 * @param selectionType              - orgSelectionType (user-creation | class-creation)
	 * @param orgIds                     - List of OrganizationId
	 * @return Response
	 */
	public static Response getClassProvisioningResponse(String organizationId, String requestBody,
			Boolean isAccountLinkingEnabled, Boolean isAutoMatchingEnabled, AutoMatchingAttribute attributeName,
			String autoMatchingAttributeValue, orgSelectionType selectionType, List<String> orgIds) {
		String cpsBaseUrl = configProperty.getProperty("cps.base.url");
		String cpsAuthorization = configProperty.getProperty("cps.basic.authorization");
		String cpsEndPointUrl = "/classprovisioning-service/v1/provision/classes";

		HashMap<String, String> queryParametersMap = new HashMap<>();
		Map<String, String> cpsRequestHeaders = getJsonRequestHeaders();
		cpsRequestHeaders.put("Authorization", cpsAuthorization);
		cpsRequestHeaders.put("organizationId", organizationId);
		cpsRequestHeaders.put("isAccountLinkingEnabled", Boolean.toString(isAccountLinkingEnabled));
		cpsRequestHeaders.put("isAutoMatchingEnabled", Boolean.toString(isAutoMatchingEnabled));
		cpsRequestHeaders.put("autoMatchingAttribute", attributeName.toString());
		if (autoMatchingAttributeValue != null && autoMatchingAttributeValue.length() > 0) {
			switch(attributeName) {
			 case SIS :
				 cpsRequestHeaders.put("sisId", autoMatchingAttributeValue);
				 break;
			 case FEDERATED :
				 cpsRequestHeaders.put("federatedId", autoMatchingAttributeValue);
				 break;
			 case NONE :
				 break;
			}
		}

		if (selectionType != null) {
			cpsRequestHeaders.put("orgSelectionType", selectionType.toString());
		} else {
			cpsRequestHeaders.put("orgSelectionType", "");
		}

		if (orgIds != null && orgIds.size() > 0) {
			cpsRequestHeaders.put("selectedOrganizations", String.join(",", orgIds));
		} else {
			cpsRequestHeaders.put("selectedOrganizations", "");
		}

		Log.message("<b>Performing POST Request for class Provisioning service</b>: " + cpsBaseUrl + cpsEndPointUrl);
		Response postResponse = RestAssuredAPI.post(cpsBaseUrl, cpsRequestHeaders, queryParametersMap, requestBody,	cpsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", cpsBaseUrl, cpsEndPointUrl, cpsRequestHeaders, queryParametersMap, postResponse, requestBody);
		return postResponse;
	}
	
	// ******************** Assignment Provisioning Service ************************//
	
	/**
	 * To return a new or existing assignment using POST request
	 * 
	 * @param requestBody
	 * @return assignmentResponse
	 * @throws Exception
	 */
	public static Response postProvisionAssignment(String requestBody) throws Exception {
		String assignmentBaseUrl = configProperty.getProperty("aps.base.url");
		String assignmentEndPoint = configProperty.getProperty("aps.provision.endpoint");
		String assignmentAuthorization = configProperty.getProperty("aps.basic.authorization");
		
		HashMap<String, String> headers = getJsonRequestHeaders();
		HashMap<String, String> queryParams = new HashMap<String, String>();
		headers.put("Authorization", assignmentAuthorization);
		
		Log.message("<b>Performing Post request for Assignment Provisioning Service</b>: " + assignmentBaseUrl + assignmentEndPoint);
		Response assignmentResponse = RestAssuredAPI.post(assignmentBaseUrl, headers, queryParams, requestBody, assignmentEndPoint);
		RealizeUtils.apiLogMessageFormatter("POST", assignmentBaseUrl, assignmentEndPoint, headers, queryParams, assignmentResponse, requestBody);
		return assignmentResponse;
	}

	/**
	 * To get assignment provisioning service response of platform’s names and role
	 * service API
	 * 
	 * @param userAssignmentId
	 *            - organization Id
	 * @return Response
	 */
	public static Response getResourceLinkClaimResponse(String userAssignmentId) {
		String apsBaseUrl = configProperty.getProperty("aps.base.url");
		String apsAuthorization = configProperty.getProperty("aps.basic.authorization");
		String apsEndPointUrl = String.format(apsResourceClaimEndpoint, userAssignmentId);

		HashMap<String, String> apsQueryParam = new HashMap<String, String>();
		Map<String, String> apsRequestHeaders = getJsonRequestHeaders();
		apsRequestHeaders.put("Authorization", apsAuthorization);

		Log.message("Performing Get Request for " + apsBaseUrl + apsEndPointUrl);
		Response apsResponse = RestAssuredAPI.get(apsBaseUrl, apsRequestHeaders, apsQueryParam, apsEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("GET", apsBaseUrl, apsEndPointUrl, apsRequestHeaders, apsQueryParam,
				apsResponse, "");
		return apsResponse;
	}

	/**
	 * * To get Rumba user id from given external gc user id and external system
	 * 
	 * @param gcUserId
	 * 
	 * @param externalSystem
	 * 
	 * @return - Rumba UserId
	 */
	public static String getRumbaUserIdForExternalEmailId(String ExternalEmailId, String externalSystem,
			String... authType) {
		String rumbaUserId = "";
		String emsAuthorization = "";
		if (authType.length > 0) {
			emsAuthorization = configProperty.getProperty("ems.bearer.authorization").trim();
		} else {
			emsAuthorization = configProperty.getProperty("ems.basic.authorization").trim();
		}

		HashMap<String, String> emsRequestHeaders = getJsonRequestHeaders();
		emsRequestHeaders.put("Authorization", emsAuthorization);
		emsRequestHeaders.put("emailId", ExternalEmailId);
		HashMap<String, String> emsQueryParam = new HashMap<String, String>();
		emsQueryParam.put("externalSystem", externalSystem);

		Log.message("Performing Get Request for " + configProperty.getProperty("ems.base.url").trim()
				+ configProperty.getProperty("ems.externalemailid.endpoint"));
		Response emsResponse = RestAssuredAPI.get(configProperty.getProperty("ems.base.url").trim(), emsRequestHeaders,
				emsQueryParam, configProperty.getProperty("ems.externalemailid.endpoint"));

		if (emsResponse.getStatusCode() == 200) {
			Log.event("Get Response from EMS: " + emsResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(emsResponse.getBody().asString());
			rumbaUserId = responseBody.getJSONArray("data").getJSONObject(0).getString("userId");
		} else {
			Log.event(emsResponse.getStatusCode() + " status code returned from EMS with message "
					+ emsResponse.getBody().asString());
		}
		return rumbaUserId;
	}

	/**
	 * 
	 * 
	 * /** To get Rumba user id from given external gc user id and external system
	 * 
	 * @param gcUserId
	 * @param externalSystem
	 * @return - Rumba UserId
	 */
	public static String getCurrentUserDomainId(String organizationId, String... authType) {
		String userDomainId = "";
		String orgAuthorization = "";
		// Basic cmVhbGl6ZTp0ZXN0aW5nMTIzJA==
		Log.message("<br>-inside" + organizationId);
		if (authType.length > 0) {
			orgAuthorization = configProperty.getProperty("org.bearer.authorization").trim();
		} else {
			orgAuthorization = configProperty.getProperty("org.basic.authorization").trim();
		}

		Log.message("orgAuthorization " + orgAuthorization);

		HashMap<String, String> orgRequestHeaders = getJsonRequestHeaders();
		orgRequestHeaders.put("Authorization", orgAuthorization);
		HashMap<String, String> orgQueryParam = new HashMap<String, String>();

		Log.message("Performing Get Request for " + configProperty.getProperty("org.base.url")
				+ configProperty.getProperty("org.domain.endpoint"));
		String orgSrerviceUrl = configProperty.getProperty("org.base.url")
				+ configProperty.getProperty("org.domain.endpoint");
		orgSrerviceUrl = orgSrerviceUrl.replaceAll("organizationId", organizationId);
		Log.message("orgSrerviceUrl  " + orgSrerviceUrl);
		Log.message("orgRequestHeaders  " + orgRequestHeaders);
		Log.message("orgQueryParam  " + orgQueryParam);
		Response orgResponse = RestAssuredAPI.get(orgSrerviceUrl, orgRequestHeaders, orgQueryParam, "");

		if (orgResponse.getStatusCode() == 200) {
			Log.event("Get Response from EMS: " + orgResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(orgResponse.getBody().asString());
			userDomainId = responseBody.getString("domainId");
		} else {
			Log.event(orgResponse.getStatusCode() + " status code returned from EMS with message "
					+ orgResponse.getBody().asString());
		}
		return userDomainId;
	}
	
	/**
	 * * To get Response body from given external gc user id and external system
	 * 
	 * @param ExternalEmailId
	 * 
	 * @param externalSystem
	 * 
	 * @param userId
	 * 
	 * @param password
	 * 
	 * @return - response
	 */
	public static Response getResponseFromExternalEmailId(String ExternalEmailId, StringBuilder externalSystem,
			String userId, String password) {
		HashMap<String, String> emsRequestHeaders = getJsonRequestHeaders();
		emsRequestHeaders.put("Content-Type", "application/json");
		emsRequestHeaders.put("emailId", ExternalEmailId);
		HashMap<String, String> emsQueryParam = new HashMap<String, String>();
		emsQueryParam.put("externalSyste", externalSystem.toString());

		Log.message("Performing Get Request for " + configProperty.getProperty("css.base.url").trim()
				+ configProperty.getProperty("css.externalemailid.endpoint"));
		Response response = RestAssuredAPI.GET(configProperty.getProperty("css.base.url").trim(), emsRequestHeaders,
				userId, password, "application/json",
				configProperty.getProperty("css.externalemailid.endpoint") + externalSystem);
		RealizeUtils.apiLogMessageFormatter("GET", configProperty.getProperty("css.base.url").trim(),
				configProperty.getProperty("css.externalemailid.endpoint"), emsRequestHeaders, emsQueryParam, response,
				"");
		return response;
	}	

	// ******************** User Mapping Service ************************//

	/**
	 * To create template for UMS Get call query parameter
	 * 
	 * @param integrationId  - <Issuer>_<ClientId>_lti-a
	 * @param externalUserId - <external id in JWT>_<role> (null if optional)
	 * @param sisId          - external source Id (null if optional)
	 * @param fedId          - Federated Id (null if optional)
	 * @return query param in HashMap<String, String>
	 */
	public static HashMap<String, String> getUMSQueryParameters(String integrationId, String externalUserId,
			String sisId, String fedId) {
		final HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("integrationId", integrationId);
		queryParams.put("integrationType", "lti-a");
		if (externalUserId != null)
			queryParams.put("externalUserId", externalUserId);
		if (sisId != null)
			queryParams.put("externalSisId", sisId);
		if (fedId != null)
			queryParams.put("userFederatedId", fedId);
		return queryParams;
	}

	/**
	 * To create template for UMS POST request body
	 * 
	 * @param userId         - A&E userId
	 * @param orgId          - organization Id
	 * @param userRole       - t or s
	 * @param integrationId  - <EdCloud tenant id>
	 * @param externalUserId - <EdCloud user Pif id>
	 * @param sisId          - SIS Id (Optional)
	 * @param federatedId    - Federated Id (Optional)
	 * @param email          - email Id (Optional)
	 * @return - request body in JSON string with indentation
	 */
	public static String getUMSRequestBody(String userId, String orgId, String userRole, String integrationId,
			String externalUserId, String sisId, String federatedId, String email) {
		JSONObject umsBody = new JSONObject();
		umsBody.put("userId", userId);
		umsBody.put("organizationId", orgId);
		umsBody.put("userRole", userRole);
		umsBody.put("integrationType", "edcloud");

		if (integrationId != null && !integrationId.isEmpty()) {
			umsBody.put("integrationId", integrationId);
		} else {
			umsBody.put("integrationId", "null");
		}
		
		if (externalUserId != null && !externalUserId.isEmpty()) {
			umsBody.put("externalUserId", externalUserId);
		} else {
			umsBody.put("externalUserId", "null");
		}
		
		if (sisId != null && !sisId.isEmpty()) {
			umsBody.put("externalSisId", sisId);
		} else {
			umsBody.put("externalSisId", "null");
		}
		
		if (federatedId != null && !federatedId.isEmpty()) {
			umsBody.put("userFederatedId", federatedId);
		} else {
			umsBody.put("userFederatedId", "null");
		}
		
		if (email != null && !email.isEmpty()) {
			umsBody.put("emailId", email);
		} else {
			umsBody.put("emailId", "null");
		}
		
		return umsBody.toString(4);
	}

	/**
	 * To get the UMS Response for getting user mapping using GET call
	 * 
	 * @param orgId          - organization Id
	 * @param role           - t or s
	 * @param umsQueryParams
	 * @param logAsEvent     - true to print as event
	 * @return
	 */
	public static Response getUserMappingResponse(String orgId, String role, HashMap<String, String> umsQueryParams,
			boolean... logAsEvent) {
		String umsAuthorization = configProperty.getProperty("ums.basic.authorization").trim();
		String umsBaseUrl = configProperty.getProperty("ums.base.url").trim();
		String umsEndPoint = configProperty.getProperty("ums.get.endpoint").trim();
		umsEndPoint = String.format(umsEndPoint, orgId, role);
		
		HashMap<String, String> umsRequestHeaders = getJsonRequestHeaders();
		umsRequestHeaders.put("Authorization", umsAuthorization);

		Log.message("<b>Performing GET Request for UMS API:</b> " + umsBaseUrl + umsEndPoint);
		Response umsResponse = RestAssuredAPI.get(umsBaseUrl, umsRequestHeaders, umsQueryParams, umsEndPoint);
		RealizeUtils.apiLogMessageFormatter("GET", umsBaseUrl, umsEndPoint, umsRequestHeaders, umsQueryParams,
				umsResponse, "", logAsEvent);
		return umsResponse;
	}

	/**
	 * To get the UMS Response for given external SIS Id using GET call
	 * 
	 * @param orgId         - organization Id
	 * @param role          - t or s
	 * @param integrationId - <Issuer>_<ClientId>_lti-a
	 * @param sisId         - external SIS Id
	 * @param logAsEvent    - true to print as event
	 * @return
	 */
	public static Response getUserMappingResponseBySISId(String orgId, String role, String integrationId, String sisId,
			boolean... logAsEvent) {
		HashMap<String, String> qParam = getUMSQueryParameters(integrationId, null, sisId, null);
		return getUserMappingResponse(orgId, role, qParam, logAsEvent);
	}

	/**
	 * To get the UMS Response for given external User Id using GET call
	 * 
	 * @param orgId          - organization Id
	 * @param role           - t or s
	 * @param integrationId  - <Issuer>_<ClientId>_lti-a
	 * @param externalUserId - external UserId
	 * @param logAsEvent     - true to print as event
	 * @return
	 */
	public static Response getUserMappingResponseByExternalUserId(String orgId, String role, String integrationId,
			String externalUserId, boolean... logAsEvent) {
		HashMap<String, String> qParam = getUMSQueryParameters(integrationId, externalUserId, null, null);
		return getUserMappingResponse(orgId, role, qParam, logAsEvent);
	}

	/**
	 * To get the UMS Response for given Federated Id using GET call
	 * 
	 * @param orgId         - organization Id
	 * @param role          - t or s
	 * @param integrationId - <Issuer>_<ClientId>_lti-a
	 * @param fedId         - Federated Id
	 * @param logAsEvent    - true to print as event
	 * @return
	 */
	public static Response getUserMappingResponseByFederatedId(String orgId, String role, String integrationId,
			String fedId, boolean... logAsEvent) {
		HashMap<String, String> qParam = getUMSQueryParameters(integrationId, null, null, fedId);
		return getUserMappingResponse(orgId, role, qParam, logAsEvent);
	}

	/**
	 * To get the UMS Response for creating new mapping using POST call
	 * 
	 * @param requestBody
	 * @return
	 */
	public static Response createUserMapping(String requestBody) {
		String umsAuthorization = configProperty.getProperty("ums.basic.authorization").trim();
		String umsBaseUrl = configProperty.getProperty("ums.base.url").trim();
		String umsEndPoint = configProperty.getProperty("ums.post.endpoint").trim();

		HashMap<String, String> umsRequestHeaders = getJsonRequestHeaders();
		umsRequestHeaders.put("Authorization", umsAuthorization);
		HashMap<String, String> umsQueryParams = new HashMap<String, String>();

		Log.message("<b>Performing POST request for UMS API:</b> " + umsBaseUrl + umsEndPoint);
		Response umsResponse = RestAssuredAPI.post(umsBaseUrl, umsRequestHeaders, umsQueryParams, requestBody,
				umsEndPoint);
		RealizeUtils.apiLogMessageFormatter("POST", umsBaseUrl, umsEndPoint, umsRequestHeaders, umsQueryParams,
				umsResponse, requestBody);
		return umsResponse;
	}

	/**
	 * To get the A&E userId of the given sisId from UMS mapping
	 * 
	 * @param orgId         - organization Id
	 * @param role          - t or s
	 * @param integrationId - <Issuer>_<ClientId>_lti-a
	 * @param sisId         - external SIS Id
	 * @return - A&E userId
	 */
	public static String getUserIdFromUserMappingBySISId(String orgId, String role, String integrationId,
			String sisId) {
		String userId = null;
		Response umsResponse = getUserMappingResponseBySISId(orgId, role, integrationId, sisId, true);
		if (umsResponse.getStatusCode() == 200) {
			userId = umsResponse.jsonPath().getString("userId");
		} else {
			Log.event(umsResponse.getStatusCode() + " status code returned from UMS with message "
					+ umsResponse.getBody().asString());
		}
		return userId;
	}

	/**
	 * To get the A&E userId of the given externalUserId from UMS mapping
	 * 
	 * @param orgId          - organization Id
	 * @param role           - t or s
	 * @param integrationId  - <Issuer>_<ClientId>_lti-a
	 * @param externalUserId - external UserId
	 * @return - A&E userId
	 */
	public static String getUserIdFromUserMappingByExternalUserId(String orgId, String role, String integrationId,
			String externalUserId) {
		String userId = null;
		Response umsResponse = getUserMappingResponseByExternalUserId(orgId, role, integrationId, externalUserId, true);
		if (umsResponse.getStatusCode() == 200) {
			userId = umsResponse.jsonPath().getString("userId");
		} else {
			Log.event(umsResponse.getStatusCode() + " status code returned from UMS with message "
					+ umsResponse.getBody().asString());
		}
		return userId;
	}

	/**
	 * To delete User mapping for given organization Id, user role, user Id and Integration Id
	 * 
	 * @param orgId         - organizationId
	 * @param role          - t or s
	 * @param userId        - A&E userId
	 * @param integrationId - Integration Id (optional)
	 * @return - true if deleted successfully
	 */
	public static boolean deleteUserMapping(String orgId, String role, String userId, String integrationId, boolean...logAsEvent) {
		boolean isDeleted = false;
		String umsAuthorization = configProperty.getProperty("ums.basic.authorization").trim();
		String umsBaseUrl = configProperty.getProperty("ums.base.url").trim();
		String umsEndPoint = configProperty.getProperty("ums.delete.endpoint").trim();
		umsEndPoint = String.format(umsEndPoint, orgId, role, userId);

		HashMap<String, String> umsRequestHeaders = getJsonRequestHeaders();
		umsRequestHeaders.put("Authorization", umsAuthorization);
		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("deletedBy", userId);
		if (integrationId != null) {
			queryParams = getUMSQueryParameters(integrationId, null, null, null);
		}

		Log.event("<b>Performing DELETE Request for User Mapping</b>: " + umsBaseUrl + umsEndPoint);
		Response deleteResponse = RestAssured.given().baseUri(umsBaseUrl).headers(umsRequestHeaders)
				.queryParams(queryParams).delete(umsEndPoint);
		
		RealizeUtils.apiLogMessageFormatter("DELETE", umsBaseUrl, umsEndPoint, umsRequestHeaders, queryParams, deleteResponse,
				"", logAsEvent);
		if (deleteResponse.getStatusCode() == 200) {
			Log.event("Response from Assignment service: " + deleteResponse.getBody().asString());
			JSONObject responseBody = new JSONObject(deleteResponse.getBody().asString());
			if (responseBody.getString("message").equalsIgnoreCase("UserMapping deleted successfully")) {
				isDeleted = true;
			}
		} else {
			Log.event(deleteResponse.getStatusCode() + " status code returned from UMS API with message "
					+ deleteResponse.getBody().asString());
		}
		return isDeleted;
	}

	/**
	 * To delete User mapping for given organization Id, role and userId
	 * 
	 * @param orgId         - organizationId
	 * @param role          - t or s
	 * @param userId        - A&E userId
	 * @return - true if deleted successfully
	 */
	public static boolean deleteUserMapping(String orgId, String role, String userId) {
		return deleteUserMapping(orgId, role, userId, null);
	}

    /**
     * To delete User mapping for given user Id, Integration Id and Integration Type
     * 
     * @param userId          - Rumba userId
     * @param integrationId   - Integration Id
     * @param integrationType - Integration Type
     * @param logAsEvent      - true if log it in Event
     * @return - true if deleted successfully
     */
    public static boolean deleteUserMappingByUserId(String userId, String integrationId, String integrationType, boolean...logAsEvent) {
        boolean isDeleted = false;
        String umsAuthorization = configProperty.getProperty("ums.basic.authorization").trim();
        String umsBaseUrl = configProperty.getProperty("ums.base.url").trim();
        String umsEndPoint = configProperty.getProperty("ums.delete.userid.endpoint").trim();
        umsEndPoint = String.format(umsEndPoint, userId);

        HashMap<String, String> umsRequestHeaders = getJsonRequestHeaders();
        umsRequestHeaders.put("Authorization", umsAuthorization);
        HashMap<String, String> queryParams = new HashMap<String, String>();
        queryParams = getUMSQueryParameters(integrationId, null, null, null);
		queryParams.put("deletedBy", userId);
        if (integrationType == null) {
            queryParams.put("integrationType", integrationType);
        }

        Log.event("<b>Performing DELETE Request for User Mapping</b>: " + umsBaseUrl + umsEndPoint);
        Response deleteResponse = RestAssured.given().baseUri(umsBaseUrl).headers(umsRequestHeaders)
                .queryParams(queryParams).delete(umsEndPoint);
        
        RealizeUtils.apiLogMessageFormatter("DELETE", umsBaseUrl, umsEndPoint, umsRequestHeaders, queryParams, deleteResponse,
                "", logAsEvent);
        if (deleteResponse.getStatusCode() == 200) {
            Log.event("Response from Assignment service: " + deleteResponse.getBody().asString());
            JSONObject responseBody = new JSONObject(deleteResponse.getBody().asString());
            if (responseBody.getString("message").equalsIgnoreCase("UserMapping deleted successfully")) {
                isDeleted = true;
            }
        } else {
            Log.event(deleteResponse.getStatusCode() + " status code returned from UMS API with message "
                    + deleteResponse.getBody().asString());
        }
        return isDeleted;
    }

	// ******************** School Selection Service ************************//
	
	/**
	 * To get GraphQl Response for the given payload from graphQL endPoint in school selection BFF service
	 * @param accessToken
	 * @param graphqlPayload
	 * @return
	 */
	public static Response getSchoolSelectionGraphQLResponse(String accessToken, String graphqlPayload) {
		Response graphQlResponse = null;
		Log.event("Getting GraphQl Response for BFF school selection");
		String graphQlEndpoint = configProperty.getProperty("ltia.schoolSelection.bff.graphql.endpoint").trim();
		String baseURL = configProperty.getProperty("ltia.schoolSelection.bff.base.url").trim();
		HashMap<String, String> queryParametersMap = new HashMap<>();
		HashMap<String, String> bffRequestHeaders = getJsonRequestHeaders();
		if (accessToken != null) {
			bffRequestHeaders.put("authorization", "Bearer " + accessToken);
        }

		graphQlResponse = RestAssuredAPI.post(baseURL, bffRequestHeaders, queryParametersMap, graphqlPayload,
				graphQlEndpoint);
		RealizeUtils.apiLogMessageFormatter("POST", baseURL, graphQlEndpoint, bffRequestHeaders, queryParametersMap,
				graphQlResponse, graphqlPayload);

		return graphQlResponse;
	}

    // ******************** Item Selection Service ************************//
    
    /**
	 * To get GraphQl Response for the given payload from graphQL endPoint in Ltia Item selection BFF service
	 * @param accessToken
	 * @param graphqlPayload
	 * @return
	 */
    public static Response getLtiaItemSelectionGraphQLResponse(String accessToken, String graphqlPayload) {
    	Response graphQlResponse = null;
    	Log.event("Getting GraphQl Response for BFF Ltia Item selection");
    	String graphQlEndpoint = configProperty.getProperty("ltia.itemSelection.bff.graphql.endpoint").trim();
		String baseURL = configProperty.getProperty("ltia.itemSelection.bff.base.url").trim();
		
		HashMap<String, String> queryParametersMap = new HashMap<>();
		HashMap<String, String> bffRequestHeaders = getJsonRequestHeaders();
		if (accessToken != null) {
			bffRequestHeaders.put("authorization", "Bearer " + accessToken);
        }

		graphQlResponse = RestAssuredAPI.post(baseURL, bffRequestHeaders, queryParametersMap, graphqlPayload,
				graphQlEndpoint);
		RealizeUtils.apiLogMessageFormatter("POST", baseURL, graphQlEndpoint, bffRequestHeaders, queryParametersMap,
				graphQlResponse, graphqlPayload);

		return graphQlResponse;
    }

    // ******************** Account Linking Service ************************//

	/**
	 * To get Request Data response for given cacheKey from Account Linking Service
	 * 
	 * @param cacheKey   - CacheKey from A&E
	 * @param logAsEvent
	 * @return
	 */
    public static Response getRequestDataResponse(String cacheKey, boolean...logAsEvent) {
    	Log.event("Getting getRequestData response in Account Linking Service graphql");
		String bffBaseURL = configProperty.getProperty("accountLinking.bff.base.url").trim();
		String bffEndPointUrl = configProperty.getProperty("accountLinking.bff.graphql.endpoint").trim();
		HashMap<String, String> queryParametersMap = new HashMap<>();
		Map<String, String> bffRequestHeaders = getJsonRequestHeaders();
		bffRequestHeaders.put("Authorization", "Bearer null");
		String graphqlPayload = "{\"query\": \"query{\\n  getRequestData(cacheKey: \\\"" + cacheKey + "\\\"){\\n  "
				+ "name\\n    userRole\\n    orgId\\n    integrationId\\n    integrationType\\n  "
				+ "callbackUrl\\n    externalCacheKey\\n    externalUserId\\n  }\\n}\"}";

		Response graphQlResponse = RestAssuredAPI.post(bffBaseURL, bffRequestHeaders, queryParametersMap, graphqlPayload,
				bffEndPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", bffBaseURL, bffEndPointUrl, bffRequestHeaders, queryParametersMap,
				graphQlResponse, graphqlPayload, logAsEvent);
		return graphQlResponse;
    }
    
	/**
	 * To get external Cache Key for given cache key from Account Linking Service
	 * 
	 * @param cacheKey - CacheKey from A&E
	 * @return externalCacheKey
	 */
    public static String getExternalCacheKeyFromRequestData(String cacheKey) {
    	String externalCacheKey = null;
    	Response graphqlResponse = getRequestDataResponse(cacheKey, true);
    	if (graphqlResponse.getStatusCode() == 200) {
    		externalCacheKey = graphqlResponse.jsonPath().getString("data.getRequestData.externalCacheKey");
		} else {
			Log.event(graphqlResponse.getStatusCode()
					+ " status codes returned from Account Linking Service graphql with message "
					+ graphqlResponse.getBody().asString());
		}
    	return externalCacheKey;
	}
    
    /**
     * Css delete assignment post call.
     *
     * @param studenUsertId the studen usert id
     * @param studentExternalId the student external id
     */
    public static void cssDeleteAssignmentPostCall(String studenUsertId, String studentExternalId) {
		String body = "{\n" 
				+ "  \"externalUserId\": \"" + studentExternalId + "\",\n" 
				+ "  \"userId\": \"" + studenUsertId + "\"\n" 
				+ "}\n";
		String authorization = configProperty.getProperty("roster.basic.authorization").trim();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", authorization);
		headers.put("content-type", JSON_CONTENT_TYPE);
		headers.put("accept", JSON_CONTENT_TYPE);
		Response cssResp = RestAssuredAPI.post(configProperty.getProperty("css.base.url").trim(), headers, new HashMap<String, String>(), body, configProperty.getProperty("css.post.delete.assignment").trim());
		RealizeUtils.apiLogMessageFormatter("POST", configProperty.getProperty("css.base.url"), configProperty.getProperty("css.post.delete.assignment").trim(), headers, new HashMap<String, String>(),
				cssResp, body, false);
	}
}

