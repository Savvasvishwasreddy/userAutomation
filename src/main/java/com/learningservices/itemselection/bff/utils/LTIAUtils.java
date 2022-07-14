package com.learningservices.itemselection.bff.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.learningservices.itemselection.bff.canvas.pages.CanvasCoursePage;
import com.learningservices.itemselection.bff.canvas.pages.SchoologyCoursePage;
import com.learningservices.utils.Log;
import com.learningservices.utils.RestAssuredAPI;
import com.learningservices.utils.WebDriverFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class LTIAUtils {

	private static PropertyReader configProperty = PropertyReader.getInstance();
	private static DateTimeFormatter UTC_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC);
	private static DateTimeFormatter UTC_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
	private static DateTimeFormatter UTC_FORMAT2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").withZone(ZoneOffset.UTC);
	private static DateTimeFormatter UTC_FORMAT3 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'").withZone(ZoneOffset.UTC);
	private static final String CANVAS_ASSIGNMENT_ENDPOINT = "/courses/%s/assignments";
	public static final String ETEXT1_REALIZE_PLATFORMS = "etext1|realize";
	public static final String ETEXT1_DASH_REALIZE_PLATFORMS = "etext1|dash|realize";
	public static final String ETEXT1_PSN_DASH_REALIZE_PLATFORMS = "etext1|psn|psnplus|dash|realize";
	public static final String PENDING_MANUAL_SCORE_GRADE_COMMENTS = "Activity Completed, Pending Manual Grading";
	public static final String COMPLETED_MANUAL_SCORE_GRADE_COMMENTS = "Activity Completed, Fully Graded";
	public static final String COMPLETED_ASSIGNMENT_COMMENTS = "Completed";
	private static final String CANVAS_DEFAULT_TIMEZONE = "America/New_York";
	private static final String SCHOOLOGY_DEFAULT_TIMEZONE = "America/New_York";

	public enum LMS {
		Canvas("canvas", "Canvas"),
		Schoology("schoology", "Schoology"),
		SchoologyMT("schoology-mt", "Schoology"),
		IMSGlobal("ims-global", "IMS Global"),
		TestHarness("test-harness", "Test Harness"),
		SafariMontage("safari-montage", "Safari Montage");

		private String name;
		private String type;

		LMS(String type, String name) {
			this.type = type;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}
	}

	public enum School {
		BRETTON("Bretton", "5665305272"),
		HAYSTACK("Haystack", "5665302702"),
		KILLINGTON("Killington", "5659191151"),
		OKEMO("Okemo", "5659205053"),
		SADDLEBACK("Saddleback", "5665306014"),
		SAVVAS("Savvas", "260469334"),
		STOWE("Stowe", "5659202161"),
		STRATTON("Stratton", "5665300364"),
		SUGARLOAF("Sugarloaf", "5665301793"),
		ATTITASH("Attitash", "5689275664"),
		WATERVILLE("Waterville", "5665306846");

		private String name;
		private String code;

		School(String name, String code) {
			this.name = name;
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public String getCode() {
			return code;
		}
	}
	
	// ******************* JWT Utils ************************//

	/**
	 * To verify given JWT token is signed or not 
	 * @param jwtToken
	 * @return
	 */
	public static boolean isJWTSigned(String jwtToken) {
		try {
			return Jwts.parser().isSigned(jwtToken);
		} catch (Exception err) {
			Log.event("Error in verifying JWT token: " + err.getMessage());
			return false;
		}
	}
	
	/**
	 * To get RSA Private key from given string
	 * @param key - key string
	 * @return - RSAPrivateKey
	 * @throws Exception 
	 */
	public static RSAPrivateKey getPrivateKeyFromString(String key) throws Exception {
		String privateKeyPEM = key;
		RSAPrivateKey privKey = null;
		try {
			if (Security.getProvider("BC") == null) {
				//Note: If BC provider not added then it will throw IOException : algid parse error, not a sequence
				Log.event("Bouncy Castle Provider is not loaded, Adding BC provider..");
				Security.addProvider(new BouncyCastleProvider());
				Security.setProperty("crypto.policy", "unlimited");
			}
			privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----", "");
			privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
			privateKeyPEM = privateKeyPEM.replaceAll("\\s+", "");
			byte[] byteKey = Base64.getDecoder().decode(privateKeyPEM.getBytes(StandardCharsets.UTF_8));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);
			privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new Exception("Error in getting RSA Private key from string: " + e.getMessage());
		}
		return privKey;
	}

	/**
	 * To get RAS Private key from given file path
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKeyFromFile(String fileName) throws Exception {
		String strKeyPEM = "";
	    try {
	    	// Read key from file
	    	String line;
		    BufferedReader br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
			    strKeyPEM += line + "\n";
			}
			br.close();
		} catch (Exception e) {
			throw new Exception("Error in getting Private key from file - "+ fileName + ", Error: " + e.getMessage());
		}
		return getPrivateKeyFromString(strKeyPEM);
	}
	
	/**
	 * To get RSA Public key from given string
	 * @param key - key string
	 * @return - RSAPublicKey
	 * @throws Exception 
	 */
	public static RSAPublicKey getPublicKeyFromString(String key) throws Exception {
		String publicKeyPEM = key;
		RSAPublicKey pubKey = null;
		try {
			publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
			publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
			publicKeyPEM = publicKeyPEM.replaceAll("\\s+", "");
			byte[] byteKey = Base64.getDecoder().decode(publicKeyPEM.getBytes(StandardCharsets.UTF_8));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(byteKey));
		} catch (Exception e) {
			throw new Exception("Error in getting RSA Public key from string: " + e.getMessage());
		}
		return pubKey;
	}
	
	/**
	 * To get Claim payload in JSON from the given Java Web token string and public key
	 * @param jwtToken
	 * @param publickey
	 * @return - JSON string
	 * @throws Exception
	 */
	public static String getJsonPayloadFromToken(String jwtToken, Key publickey) throws Exception {
		JSONObject json = new JSONObject();
		Claims claims = getPayloadFromToken(jwtToken, publickey);
        try {
        	for (String key :claims.keySet()) {
            	json.put(key, claims.get(key, claims.get(key).getClass()));
            }
        } catch (Exception ex) {
        	throw new Exception("Error in parsing JWT Claims to Json: " + ex.getMessage());
        }
        return json.toString();
    }
	
	/**
	 * To get Claim payload from the given Java Web token string and public key
	 * @param jwtToken
	 * @param publickey
	 * @return - Claims object
	 * @throws Exception 
	 */
	public static Claims getPayloadFromToken(String jwtToken, Key publickey) throws Exception {
		Claims claims = null;
        try {
        	claims = Jwts.parser()
                    .setSigningKey(publickey)
                    .parseClaimsJws(jwtToken)
                    .getBody();
        } catch (Exception ex) {
        	throw new Exception("Error in JWT/JWS validation: " + ex.getMessage());
        }
        return claims;
    }
	
	/**
	 * To get JWT signature for the given Java Web token string and public key
	 * @param jwtToken
	 * @param publickey
	 * @return
	 * @throws Exception 
	 */
	public static String getSignatureFromToken(String jwtToken, Key publickey) throws Exception {
		String sign = null;
        try {
            sign = Jwts.parser()
            .setSigningKey(publickey)
            .parseClaimsJws(jwtToken).getSignature();
        } catch (Exception ex) {
        	throw new Exception("Error in getting JWT Signature: " + ex.getMessage());
        }
        return sign;
    }
	
	/**
	 * To get JWT Headers for the given Java Web token string and public key
	 * @param jwtToken
	 * @param publickey
	 * @return - HashMap<String, String> headerMap
	 * @throws Exception 
	 */
	public static HashMap<String, String> getJWTHeadersFromToken(String jwtToken, Key publickey) throws Exception {
		HashMap<String, String> headerMap = new HashMap<String, String>();
        try {
			Header<?> header = Jwts.parser().setSigningKey(publickey)
					.parseClaimsJws(jwtToken).getHeader();

        	for (String key : header.keySet()) {
        		headerMap.put(key, header.get(key).toString());
        	}
        } catch (Exception ex) {
        	throw new Exception("Error in getting JWT Header: " + ex.getMessage());
        }
        return headerMap;
    }

	/**
	 * sign the payload (request/response) with private key to create JWT token
	 * @param payload
	 * @param privatekey
	 * @return
	 */
	public static String generateJWT(String payload, Key privatekey) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
		return Jwts.builder().setPayload(payload).setHeaderParam("typ", "JWT")
				.setHeaderParam("alg", signatureAlgorithm.getValue()).signWith(privatekey, signatureAlgorithm)
				.compact();
	}

	/**
	 * sign the payload (request/response) with private key and KeyId to create JWT token
	 * @param payload
	 * @param privatekey
	 * @param keyId
	 * @return
	 */
	public static String generateJWT(String payload, Key privatekey, String keyId) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
		return Jwts.builder().setPayload(payload).setHeaderParam("typ", "JWT").setHeaderParam("kid", keyId)
				.setHeaderParam("alg", signatureAlgorithm.getValue()).signWith(privatekey, signatureAlgorithm)
				.compact();
	}

	/**
	 * To decode the JWT Token from Deep Link Response 
	 * 
	 * @param deeplinkHtmlString
	 * @param issuerId
	 * @param clientId
	 * @param deploymentId
	 * @return
	 * @throws Exception
	 */
	public static String decodeJWTtokenFromDeeplinkResponse(String deeplinkHtmlString, String issuerId, String clientId,
			String deploymentId) throws Exception {
		String payLoad = null;
		String publicKeyString = RBSAPIUtils.getToolPublicKeyForIssuerId(issuerId, clientId, deploymentId);
		RSAPublicKey publicKey = LTIAUtils.getPublicKeyFromString(publicKeyString);
		String jwtToken = HTMLDataParser.getJWTtokenFromDeeplinkingResponseForm(deeplinkHtmlString);
		try {
			payLoad=LTIAUtils.getJsonPayloadFromToken(jwtToken, publicKey);
		} catch (Exception e) {
			throw new Exception("Error in getting Payload: " + e.getMessage());
		}
		return payLoad;
	}

	// ****************** Canvas API Utils ********************* //

	/**
	 * To create new canvas user using Canvas User API
	 * 
	 * @param name       - Canvas user Full name
	 * @param loginId    - Login Id of new canvas user
	 * @param sisId      - SIS Id of new canvas user
	 * @param logAsEvent - true to print log as event
	 * @return - Canvas user Id
	 */
	public static String createCanvasUser(String name, String loginId, String sisId, boolean...logAsEvent) {
		String canvasId = null;
		String canvasBaseUrl = configProperty.getProperty("canvas.savvas.base.url").trim();
		String canvasAuthorization = configProperty.getProperty("canvas.admin.bearer.authorization").trim();
		String endPointUrl = configProperty.getProperty("canvas.users.create.api.endpoint").trim();

		HashMap<String, String> queryParam = new HashMap<String, String>();
		HashMap<String, String> canvasHeader = new HashMap<String, String>();
		canvasHeader.put("Authorization", canvasAuthorization);
		canvasHeader.put("Content-Type", "application/json");

		String requestBody = "{ \"user\": { \"name\": \"%s\", \"terms_of_use\": true, \"skip_registration\": true, \"time_zone\":\"Eastern Time (US & Canada)\" }, \"pseudonym\": {\"send_confirmation\": false, \"unique_id\": \"%s\", \"sis_user_id\": \"%s\", \"password\":\"testing123$\" } }";
		String formattedRequestBody = String.format(requestBody, name, loginId, sisId);
		JSONObject jsonRequestBody = new JSONObject(formattedRequestBody);

		Log.message("<b>Performing canvas user creation</b>: " + canvasBaseUrl + endPointUrl);
		Response postResponse = RestAssuredAPI.post(canvasBaseUrl, canvasHeader, queryParam, jsonRequestBody.toString(),
				endPointUrl);
		RealizeUtils.apiLogMessageFormatter("POST", canvasBaseUrl, endPointUrl, canvasHeader, queryParam, postResponse,
				jsonRequestBody.toString(), logAsEvent);

		if (postResponse.getStatusCode() == 200) {
			canvasId = postResponse.getBody().jsonPath().getString("id");
		} else {
			Log.message(
					postResponse.getStatusCode() + " status code returned from canvas user creation API with message "
							+ postResponse.getBody().asString());
		}
		return canvasId;
	}
	
	/**
	 * To update the email of the given canvas user Id
	 * 
	 * @param canvasId - canvas user Id
	 * @param email    - Email to update
	 * @return - true if updated successfully
	 */
	public static boolean updateCanvasUserEmail(String canvasId, String email) {
		boolean isUpdated = false;
		String canvasBaseUrl = configProperty.getProperty("canvas.savvas.base.url").trim();
		String canvasAuthorization = configProperty.getProperty("canvas.admin.bearer.authorization").trim();
		String endPointUrl = configProperty.getProperty("canvas.users.update.api.endpoint").trim();
		endPointUrl = String.format(endPointUrl, canvasId);
		
		HashMap<String, String> canvasHeader = new HashMap<String, String>();
		canvasHeader.put("Authorization", canvasAuthorization);
		Response putResponse = RestAssured.given().baseUri(canvasBaseUrl).formParam("user[email]", email).headers(canvasHeader).put(endPointUrl.trim());
		if (putResponse.getStatusCode() == 200) {
			isUpdated = true;
		} else {
			Log.message(
					putResponse.getStatusCode() + " status code returned from canvas update API with message "
							+ putResponse.getBody().asString());
		}
		return isUpdated;
	}

	// ****************** LTI-A Keyword Generator ********************* //

	/**
	 * To generate External User Id for given Sub and OrgId
	 * @param sub
	 * @param orgId
	 * @return - ltiExternalUserId
	 */
	public static String generateLTIAUserId(String sub, String orgId) {
		String ltiExternalUserId = "";
		try {
			Log.event("Generate external user id (in MD5 hash) based on sub ("+ sub + ") and Organization id ("+ orgId + ")");
			ltiExternalUserId = RealizeUtils.getMD5Hash(sub + "_" + orgId + "_lti-a-external-userid");
			ltiExternalUserId = ltiExternalUserId.toLowerCase(); //External User Id should be generated in lower case alpha numeric
			Log.message("External user id('&#60;sub&#62;_&#60;orgId&#62;_lti-a-external-userid' in MD5 hash) for sub '" + sub + "': '<b>"
					+ ltiExternalUserId + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating External User Id: " + err.getMessage()
					+ RealizeUtils.FAIL_HTML_END);
		}
		return ltiExternalUserId;
	}
	
	/**
	 * To generate LTIA UserName for given external UserId and OrgId
	 * @param sub
	 * @param orgId
	 * @return - ltiUserName
	 */
	public static String generateLTIAUsername(String sub, String orgId) {
		String ltiUserName = "";
		try {
			Log.message("</br><u>Generate User Name (in MD5 hash) based on External User Id and OrganizationId</u>");
			ltiUserName = RealizeUtils.getMD5Hash(sub + "_" + orgId + "_lti-a-rumba-username");
			ltiUserName = ltiUserName.toLowerCase(); //UserName should be generated in lower case alpha numeric
			Log.message("User Name('&#60;sub&#62;_&#60;orgId&#62;_lti-a-rumba-username' in MD5 hash) for sub '" + sub + "': '<b>"
					+ ltiUserName + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating LTI-A Username: " + err.getMessage()
					+ RealizeUtils.FAIL_HTML_END);
		}
		return ltiUserName;
	}

	/**
	 * To generate LTIA-v2 UserName for given external UserId, OrgId and Role
	 * 
	 * @param externalUserId
	 * @param orgId
	 * @param Role
	 * @return - ltiv2UserName
	 */
	public static String generateLTIAUsername(String externalUserId, String orgId, String role) {
		String ltiv2UserName = "";
		try {
			Log.message("</br><u>Generate User Name (in MD5 hash) based on External UserId, OrganizationId and Role</u>");
			ltiv2UserName = RealizeUtils.getMD5Hash(externalUserId + "_" + orgId + "_" + role + "_lti-a-v2-ae-username");
			ltiv2UserName = ltiv2UserName.toLowerCase(); // UserName should be generated in lower case alpha numeric
			Log.message("User Name('&#60;externalUserId&#62;_&#60;orgId&#62;_&#60;role&#62;_lti-a-v2-ae-username' in MD5 hash) for externalUserId '"
							+ externalUserId + "': '<b>" + ltiv2UserName + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating LTIA-v2 Username: " + err.getMessage()
					+ RealizeUtils.FAIL_HTML_END);
		}
		return ltiv2UserName;
	}

	/**
	 * To generate LTIA ClassId for given external ClassId and OrgId
	 * @param externalClassId
	 * @param orgId
	 * @return - ltiClassId
	 */
	public static String generateLTIAClassId(String externalClassId, String orgId) {
		String ltiClassId = "";
		try {
			Log.message("</br><u>Generate unique class id (in MD5 hash) based on External ClassId and OrganizationId</u>");
			ltiClassId = RealizeUtils.getMD5Hash(externalClassId + "_" + orgId + "_lti-a-external-classid");
			ltiClassId = ltiClassId.toLowerCase(); //ClassId should be generated in lower case alpha numeric
			Log.message("External class id('&#60;externalClassId&#62;_&#60;orgId&#62;_lti-a-external-classid' in MD5 hash) for contextId '["+externalClassId+"]': '<b>"
					+ ltiClassId + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating unique External ClassId: " + err.getMessage()
					+ RealizeUtils.FAIL_HTML_END);
		}
		return ltiClassId;
	}

	/**
	 * To generate LTI-Av2 classId from external class Id
	 * 
	 * @param externalClassId
	 * @param orgId
	 * @return
	 */
	public static String generateLTIAV2ClassId(String externalClassId, String orgId) {
		String ltiClassId = null;
		try {
			Log.message("</br><u>Generate unique class id (in MD5 hash) based on External ClassId and OrganizationId</u>");
			ltiClassId = externalClassId + "_" + orgId + "_lti-a-v2-external-classid";
			Log.message("External class id('&#60;externalClassId&#62;_&#60;orgId&#62;_lti-a-v2-external-classid' in MD5 hash) for contextId '["
							+ externalClassId + "]': '<b>" + ltiClassId + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating unique External ClassId: "
					+ err.getMessage() + RealizeUtils.FAIL_HTML_END);
		}
		return ltiClassId;
	}
	
	/**
	 * To generate gradable assignment id for given resource link id, org id
	 * 
	 * @param resourceLinkId
	 * @param orgId
	 * @return - gradableAssignmentId
	 */
	public static String generateGradableAssignmentId(String resourceLinkId, String orgId) {
		String gradableAssignmentId = "";
		try {
			Log.message("</br><u>Generate gradable assignment id (in MD5 hash) based on resource link id and OrganizationId </u>");
			gradableAssignmentId = RealizeUtils.getMD5Hash(orgId + "_" + resourceLinkId  + "_lti-a-v2-external-assignment");
			gradableAssignmentId = gradableAssignmentId.toLowerCase();
			Log.message("Gradable assignment id('&#60;orgId&#62;_&#60;resourceLinkId&#62;_lti-a-v2-ae-username' in MD5 hash) "
							+ ": '<b>" + gradableAssignmentId + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating LTIA-v2 external assignemnt id : " + err.getMessage()
					+ RealizeUtils.FAIL_HTML_END);
		}
		return gradableAssignmentId;
	}
	
	/**
	 * To generate non-gradable assignment id for given resource link id, contentId and contentVersion
	 * 
	 * @param resourceLinkId
	 * @param contentId
	 * @param contentVersion
	 * @return - nonGradableAssignmentId
	 */
	public static String generateNonGradableAssignmentId(String resourceLinkId, String contentId, String contentVersion) {
		String nonGradableAssignmentId = "";
		try {
			Log.message("</br><u>Generate non-gradable assignment id (in MD5 hash) based on resource link id, content id and content version </u>");
			nonGradableAssignmentId = RealizeUtils.getMD5Hash(contentId + "_" + contentVersion + "_" + resourceLinkId  + "_lti-a-v2-external-assignment");
			nonGradableAssignmentId = nonGradableAssignmentId.toLowerCase();
			Log.message("Non-gradable assignment id('&#60;contentId&#62;_&#60;contentVersion&#62;_&#60;resourceLinkId&#62;_lti-a-v2-ae-username' in MD5 hash) "
							+ ": '<b>" + nonGradableAssignmentId + "</b>'");
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Error in generating LTIA-v2 external assignemnt id : " + err.getMessage()
					+ RealizeUtils.FAIL_HTML_END);
		}
		return nonGradableAssignmentId;
	}

	/**
	 * To get canvas integrationId for the given client Id
	 * 
	 * @param clientId
	 * @return integrationId - <IssuerId>_<ClientId>_lti-a
	 */
	public static String getCanvasIntegrationId(String clientId) {
		String integrationId = null;
		String issuerId = configProperty.getProperty("canvas.savvas.issuerId").trim();
		integrationId = issuerId + "_" + clientId.trim() + "_lti-a";
		return integrationId.toLowerCase();
	}
	
	/**
	 * To get schoology integrationId for the given client Id
	 * 
	 * @param clientId
	 * @return integrationId - <IssuerId>_<ClientId>_lti-a
	 */
	public static String getSchoologyIntegrationId(String clientId) {
		String integrationId = null;
		String issuerId = configProperty.getProperty("schoology.savvas.issuerId").trim();
		integrationId = issuerId + "_" + clientId.trim() + "_lti-a";
		return integrationId.toLowerCase();
	}

	/**
	 * To get integrationId for the given IMSGlobal platforms in nightly.properties file
	 * 
	 * @param IMS_PLATFORM - platform key in nightly.properties file (e.g.: ims.platform1)
	 * @return integrationId - <IssuerId>_<ClientId>_lti-a
	 */
	public static String getIMSGlobalIntegrationId(String IMS_PLATFORM) {
		String integrationId = null;
		String issuerId = configProperty.getProperty(IMS_PLATFORM + ".issuerId").trim();
		String clientId = configProperty.getProperty(IMS_PLATFORM + ".clientId").trim();
		integrationId = issuerId + "_" + clientId + "_lti-a";
		return integrationId.toLowerCase();
	}
	
	/**
	 * To get external userId for UMS API
	 * 
	 * @param sub  - external userId from JWT
	 * @param role - user role T or S
	 * @return
	 */
	public static String getExternalUserId(String sub, String role) {
		return sub + "_" + role.toUpperCase();
	}

	// ****************** API Response Verification ********************* //

	/**
	 * To verify the UPS Response with the given attribute
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyGivenAttributesInUPSResponse(JSONObject JsonResponseBody, HashMap<String, String> attributeToVerify) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<br><u>Verifying UPS responses as below:</u>");
			String rumbaUserId = null, firstName = null, middleName = null, lastName = null;

			if (attributeToVerify.containsKey("rumbaUserId") && attributeToVerify.get("rumbaUserId") != null) {
				rumbaUserId = attributeToVerify.get("rumbaUserId");
			}

			JSONObject attributesJSONObject = new JSONObject();
			if (JsonResponseBody.has("users") && !JsonResponseBody.isNull("users")) {
				attributesJSONObject = JsonResponseBody.getJSONArray("users").getJSONObject(0).getJSONObject("attributes");
				JsonResponseBody = JsonResponseBody.getJSONArray("users").getJSONObject(0).getJSONObject("rumbaUser");
			} else if (rumbaUserId != null && !rumbaUserId.isEmpty() && JsonResponseBody.has(rumbaUserId)
					&& !JsonResponseBody.isNull(rumbaUserId)) {
				JsonResponseBody = JsonResponseBody.getJSONObject(rumbaUserId).getJSONObject("data");
			}

			if (attributeToVerify.containsKey("rumbaUserId") && attributeToVerify.get("rumbaUserId") != null) {
				String actualUserId = JsonResponseBody.get("userId").toString();
				if (actualUserId.equals(rumbaUserId)) {
					status.add(true);
					Log.message("Verified: Rumba id is matched: <b>" + actualUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Rumba id is not matched. Expected rumba user id: <b>" + rumbaUserId
							+ "</b>, Actual rumba user id: <b>" + actualUserId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("userName") && attributeToVerify.get("userName") != null) {
				String actualUserName = JsonResponseBody.get("userName").toString();
				String userName = attributeToVerify.get("userName");
				if (actualUserName.equals(userName)) {
					status.add(true);
					Log.message("Verified: User name is matched: <b>" + userName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: User name is not matched. Expected User name: <b>" + userName
							+ "</b>, Actual User name: <b>" + actualUserName + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("firstName") && attributeToVerify.get("firstName") != null) {
				String actualFirstName = JsonResponseBody.get("firstName").toString();
				firstName = attributeToVerify.get("firstName");
				if (actualFirstName.equals(firstName)) {
					status.add(true);
					Log.message("Verified: First Name is matched: <b>" + firstName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: First Name is not matched. Expected First Name: <b>" + firstName
							+ "</b>, Actual First Name: <b>" + actualFirstName + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("middleName") && attributeToVerify.get("middleName") != null) {
				String actualMiddleName = JsonResponseBody.get("middleName").toString();
				middleName = attributeToVerify.get("middleName");
				if (actualMiddleName.equals(middleName)) {
					status.add(true);
					Log.message("Verified: Middle Name is matched: <b>" + middleName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Middle Name is not matched. Expected Middle Name: <b>" + middleName
							+ "</b>, Actual Middle Name: <b>" + actualMiddleName + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("lastName") && attributeToVerify.get("lastName") != null) {
				String actualLastName = JsonResponseBody.get("lastName").toString();
				lastName = attributeToVerify.get("lastName");
				if (actualLastName.equals(lastName)) {
					status.add(true);
					Log.message("Verified: Last Name is matched: <b>" + lastName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Last Name is not matched. Expected Last Name: <b>" + lastName
							+ "</b>, Actual Last Name: <b>" + actualLastName + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("emailAddress") && attributeToVerify.get("emailAddress") != null) {
				String actualEmailId = JsonResponseBody.get("emailAddress").toString();
				String emailAddress = attributeToVerify.get("emailAddress");
				if (actualEmailId.equals(emailAddress)) {
					status.add(true);
					Log.message("Verified: Email id is matched: <b>" + emailAddress + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Email id is not matched. Expected Email id: <b>"
							+ emailAddress + "</b>, Actual Email id: <b>" + actualEmailId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("primaryOrgRole") && attributeToVerify.get("primaryOrgRole") != null) {
				String primaryOrgRole = JsonResponseBody.get("primaryOrgRole").toString();
				String expectedPrimaryOrgRole = attributeToVerify.get("primaryOrgRole");
				if (primaryOrgRole.equals(expectedPrimaryOrgRole)) {
					status.add(true);
					Log.message("Verified: primaryOrgRole is matched: <b>" + expectedPrimaryOrgRole + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: primaryOrgRole is not matched. Expected primaryOrgRole: <b>" + expectedPrimaryOrgRole
							+ "</b>, Actual primaryOrgRole: <b>" + primaryOrgRole + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("organizationId") && attributeToVerify.get("organizationId") != null) {
				List<String> expectedOrgIds = Arrays.asList(attributeToVerify.get("organizationId").split("\\|"));
				if (expectedOrgIds.size() == 1) {
					String primaryOrgId = JsonResponseBody.get("primaryOrgId").toString();
					if (primaryOrgId.equals(expectedOrgIds.get(0))) {
						status.add(true);
						Log.message("Verified: primaryOrgId is matched: <b>" + expectedOrgIds.get(0) + "</b>");
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN
								+ "Failed: primaryOrgId is not matched. Expected primaryOrgId: <b>"
								+ expectedOrgIds.get(0) + "</b>, Actual primaryOrgId: <b>" + primaryOrgId + "</b>"
								+ RealizeUtils.FAIL_HTML_END);
					}
				} else {
					List<String> actualOrgIds = new ArrayList<String>();
					JsonResponseBody.getJSONArray("primaryOrgIds").forEach(x -> actualOrgIds.add(x.toString()));
					if (RealizeUtils.compareTwoList(expectedOrgIds, actualOrgIds)) {
						status.add(true);
						Log.message("Verified: primaryOrgIds is matched: <b>[ " + String.join(", ", expectedOrgIds) + " ]</b>");
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN
								+ "Failed: primaryOrgIds is not matched. Expected primaryOrgIds: <b>[ "
								+ String.join(", ", expectedOrgIds) + " ]</b>, Actual primaryOrgIds: <b>[ "
								+ String.join(", ", actualOrgIds) + " ]</b>" + RealizeUtils.FAIL_HTML_END);
					}
				}
			}

			if (attributeToVerify.containsKey("firstName") && attributeToVerify.get("firstName") != null
					&& attributeToVerify.containsKey("middleName") && attributeToVerify.get("middleName") != null
					&& attributeToVerify.containsKey("lastName") && attributeToVerify.get("lastName") != null) {
				String fullName = JsonResponseBody.get("fullName").toString();
				if (fullName.equals((firstName + " " + middleName + " " + lastName).replaceAll("\\s+", " "))) {
					status.add(true);
					Log.message("Verified: fullName is matched: <b>" + firstName + " " + middleName + " " + lastName
							+ "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: fullName is not matched. Expected fullName: <b>"
							+ firstName + " " + middleName + " " + lastName + "</b>, Actual fullName: <b>" + fullName
							+ "</b>" + RealizeUtils.FAIL_HTML_END);
				}

				String firstAndLastName = JsonResponseBody.get("firstAndLastName").toString();
				if (firstAndLastName.equals(firstName + " " + lastName)) {
					status.add(true);
					Log.message("Verified: firstAndLastName is matched: <b>" + firstName + " " + lastName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: firstAndLastName is not matched. Expected firstAndLastName: <b>" + firstName
							+ " " + lastName + "</b>, Actual firstAndLastName: <b>" + firstAndLastName + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("userDomainId") && attributeToVerify.get("userDomainId") != null) {
				int count = 0;
				String domainId = attributeToVerify.get("userDomainId");
				String[] domainIds = { "-1", domainId };
				String[] userDomainIds = { "null", rumbaUserId };
				if (JsonResponseBody.has("userDomainType") && !JsonResponseBody.isNull("userDomainType")) {
					JSONArray userDomainTypes = JsonResponseBody.getJSONArray("userDomainType");
					for (int item = 0; item < userDomainTypes.length(); item++) {
						final String actualDomainId = userDomainTypes.getJSONObject(item).get("domainId").toString();
						final String actualUserDomainId = userDomainTypes.getJSONObject(item).get("userDomainId")
								.toString();
						for (int i = 0; i < domainIds.length; i++) {
							if (actualDomainId.equals(domainIds[i])) {
								count++;
								Log.message("Verified: domainId is matched: <b>" + domainIds[i] + "</b>");
								if (actualUserDomainId.equals(userDomainIds[i])) {
									status.add(true);
									Log.message("Verified: userDomainId is matched: <b>" + userDomainIds[i] + "</b>");
								} else {
									status.add(false);
									Log.message(RealizeUtils.FAIL_HTML_BEGIN
											+ "Failed: userDomainId is not matched. Expected rumba user id: <b>"
											+ userDomainIds[i] + "</b>, Actual userDomainId: <b>" + actualUserDomainId
											+ "</b>" + RealizeUtils.FAIL_HTML_END);
								}
							}
						}
					}
					if (count != domainIds.length) {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN
								+ "Failed: all domainId and userDomainId attributes values are not matched"
								+ RealizeUtils.FAIL_HTML_END);
					}
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: \"userDomainType\" field in deeplink is null"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("platform") && attributeToVerify.get("platform") != null) {
				String userStatus = JsonResponseBody.get("userStatus").toString();
				if (userStatus.equals("ACTIVE")) {
					status.add(true);
					Log.message("Verified: userStatus is matched: <b>ACTIVE</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: userStatus is not matched. Expected userStatus: <b>ACTIVE</b>, Actual userStatus: <b>"
							+ userStatus + "</b>" + RealizeUtils.FAIL_HTML_END);
				}

				if ((JsonResponseBody.has("attributes") && !JsonResponseBody.isNull("attributes"))
						|| !attributesJSONObject.keySet().isEmpty()) {
					if (attributesJSONObject.keySet().isEmpty())
						attributesJSONObject = JsonResponseBody.getJSONObject("attributes");

					String platform = attributesJSONObject.get("platform").toString();
					String expectedPlatform = attributeToVerify.get("platform");
					if (platform.equals(expectedPlatform)) {
						status.add(true);
						Log.message("Verified: platform is matched: <b>" + expectedPlatform + "</b>");
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN
								+ "Failed: platform is not matched. Expected platform: <b>" + expectedPlatform
								+ "</b>, Actual platform: <b>" + platform + "</b>" + RealizeUtils.FAIL_HTML_END);
					}

					String ltiAdvantageUser = attributesJSONObject.get("lti.advantage.user").toString();
					if (ltiAdvantageUser.equals("true")) {
						status.add(true);
						Log.message("Verified: lti.advantage.user is matched: <b>true</b>");
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN
								+ "Failed: lti.advantage.user is not matched. Expected lti.advantage.user: <b>true</b>, Actual lti.advantage.user: <b>"
								+ ltiAdvantageUser + "</b>" + RealizeUtils.FAIL_HTML_END);
					}
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: \"attributes\" field in deeplink is null"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("federated") && attributeToVerify.get("federated") != null) {
				String actualFederated = JsonResponseBody.get("federated").toString();
				String expectedFederated = attributeToVerify.get("federated");
				if (actualFederated.equalsIgnoreCase(expectedFederated)) {
					status.add(true);
					Log.message("Verified: federated is matched: <b>"+ actualFederated +"</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: federated is not matched. Expected federated: <b>"+ expectedFederated +"</b>, Actual federated: <b>"
							+ actualFederated + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("orgRole") && attributeToVerify.get("orgRole") != null
					&& attributeToVerify.containsKey("organizationId") && attributeToVerify.get("organizationId") != null) {
				if (JsonResponseBody.has("activeAffiliations") && !JsonResponseBody.isNull("activeAffiliations")) {
					List<String> expectedOrgIds = Arrays.asList(attributeToVerify.get("organizationId").split("\\|"));
					List<String> expectedOrgRoles = Arrays.asList(attributeToVerify.get("orgRole").split("\\|"));
					if (expectedOrgIds.size() > 1) {
						status.add(verifyActiveAffiliationsInUPSResponse(JsonResponseBody, expectedOrgIds, expectedOrgRoles));
					} else {
						JSONObject activeAffiliations = JsonResponseBody.getJSONArray("activeAffiliations").getJSONObject(0);
						String activeAffiliationStatus = activeAffiliations.get("affiliationStatus").toString();
						if (activeAffiliationStatus.equals("Confirmed")) {
							status.add(true);
							Log.message("Verified: Active affiliation status is matched: <b>Confirmed</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN
									+ "Failed: Active affiliation status is not matched. Expected active AffiliationStatus: <b>Confirmed</b>, "
									+ "Actual affiliation status: <b>" + activeAffiliationStatus + "</b>"
									+ RealizeUtils.FAIL_HTML_END);
						}

						String actualOrgRole = activeAffiliations.get("orgRole").toString();
						if (actualOrgRole.equals(expectedOrgRoles.get(0))) {
							status.add(true);
							Log.message("Verified: Active OrgRole is matched: <b>" + expectedOrgRoles.get(0) + "</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN
									+ "Failed: Active OrgRole is not matched. Expected active OrgRole: <b>"
									+ expectedOrgRoles.get(0) + "</b>, Actual active OrgRole: <b>" + actualOrgRole
									+ "</b>" + RealizeUtils.FAIL_HTML_END);
						}

						String activeOrganizationId = activeAffiliations.get("organizationId").toString();
						if (activeOrganizationId.equals(expectedOrgIds.get(0))) {
							status.add(true);
							Log.message("Verified: active OrganizationId is matched: <b>" + expectedOrgIds.get(0) + "</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN
									+ "Failed: active OrganizationId is not matched. Expected active OrganizationId: <b>"
									+ expectedOrgIds.get(0) + "</b>, Actual active OrganizationId: <b>"
									+ activeOrganizationId + "</b>" + RealizeUtils.FAIL_HTML_END);
						}

						String activeAfiliationStatus = activeAffiliations.get("afiliationStatus").toString();
						if (activeAffiliationStatus.equals("Confirmed")) {
							status.add(true);
							Log.message("Verified: Active afiliation status is matched: <b>Confirmed</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN
									+ "Failed: Active afiliation status is not matched. Expected active AfiliationStatus: <b>Confirmed</b>, "
									+ "Actual afiliation status: <b>" + activeAfiliationStatus + "</b>"
									+ RealizeUtils.FAIL_HTML_END);
						}
					}
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: \"activeAffiliations\" field in UPS response is null"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying attributes in UPS Response.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}

	/**
	 * To verify active affiliations in UPS Response
	 * 
	 * @param JsonResponseBody - UPS JSON response
	 * @param orgIds           - list of Organization Id
	 * @param orgRoles         - list of Organization Role for the respective orgId
	 * @return - true if all the given affiliation match with UPS response
	 */
	public static boolean verifyActiveAffiliationsInUPSResponse(JSONObject JsonResponseBody, List<String> orgIds,
			List<String> orgRoles) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {

			if (JsonResponseBody.has("users") && !JsonResponseBody.isNull("users")) {
				JsonResponseBody = JsonResponseBody.getJSONArray("users").getJSONObject(0).getJSONObject("rumbaUser");
			}

			if (JsonResponseBody.has("activeAffiliations") && !JsonResponseBody.isNull("activeAffiliations")) {
				JSONArray activeAffiliations = JsonResponseBody.getJSONArray("activeAffiliations");

				if (orgIds.size() != 1 && (activeAffiliations.length() != orgIds.size()
						|| activeAffiliations.length() != orgRoles.size())) {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: No. of \"activeAffiliations\" fields in UPS response is not matched with the expected. Actual: "
							+ activeAffiliations.length() + ", Expected: " + orgIds.size() + RealizeUtils.FAIL_HTML_END);
				}

				Boolean isAffiliationFound = false, isOrgFound = false;
				String actualOrgId = null, actualOrgRole = null, activeAffiliationStatus = null;
				for (int index = 0; index < orgIds.size(); index++) {
					isOrgFound = false;
					isAffiliationFound = false;
					for (int item = 0; item < activeAffiliations.length(); item++) {
						actualOrgId = activeAffiliations.getJSONObject(item).get("organizationId").toString();
						actualOrgRole = activeAffiliations.getJSONObject(item).get("orgRole").toString();
						activeAffiliationStatus = activeAffiliations.getJSONObject(item).get("affiliationStatus").toString();
						if (actualOrgId.equals(orgIds.get(index))) {
							isOrgFound = true;
							if (actualOrgRole.equals(orgRoles.get(index))) {
								isAffiliationFound = true;
								break;
							}
						}
					}

					if (isAffiliationFound) {
						Log.message("Verified: active OrganizationId is matched: <b>" + orgIds.get(index)
								+ "</b> with OrgRole <b>" + orgRoles.get(index) + "</b>");
						if (activeAffiliationStatus.equals("Confirmed")) {
							status.add(true);
							Log.message("Verified: Active affiliation status is matched: <b>Confirmed</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Active affiliation status for <b>"
									+ orgIds.get(index) + "</b> is not matched. Expected active AffiliationStatus: <b>Confirmed</b>, "
									+ "Actual affiliation status: <b>" + activeAffiliationStatus + "</b>"
									+ RealizeUtils.FAIL_HTML_END);
						}
					} else if (isOrgFound) {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: active OrganizationId is matched: <b>"
								+ orgIds.get(index) + "</b> but it is not enrolled with OrgRole <b>" + orgRoles.get(index)
								+ "</b>" + RealizeUtils.FAIL_HTML_END);
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: No Active affiliation in OrganizationId: <b>"
										+ orgIds.get(index) + "</b> found." + RealizeUtils.FAIL_HTML_END);
					}
				}
			} else {
				status.add(false);
				Log.message(RealizeUtils.FAIL_HTML_BEGIN
						+ "Failed: \"activeAffiliations\" field in UPS Response is null" + RealizeUtils.FAIL_HTML_END);
			}
		} catch (Exception e) {
			Log.message("Error in verifying activeAffiliations in UPS response. Exception: " + e.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To verify the Section Response in Roster service API with the given attribute
	 * 
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyGivenAttributesInSectionResponse(JSONObject JsonResponseBody, HashMap<String, String> attributeToVerify) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<br><u>Verifying Roster service responses as below:</u>");
			JSONObject sectionObject = JsonResponseBody.getJSONObject("data").getJSONObject("section");
			if (attributeToVerify.containsKey("sectionId") && attributeToVerify.get("sectionId") != null) {
				String actualSectionId = sectionObject.get("id").toString();
				String sectionId = attributeToVerify.get("sectionId");
				if (actualSectionId.equals(sectionId)) {
					status.add(true);
					Log.message("Verified: Section id is matched: <b>" + actualSectionId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Section Id is not matched. Expected section Id: <b>" + sectionId
							+ "</b>, Actual Section id: <b>" + actualSectionId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			JSONObject sectionInfoObject = sectionObject.getJSONObject("data").getJSONObject("sectionInfo");
			if (attributeToVerify.containsKey("sectionName") && attributeToVerify.get("sectionName") != null) {
				String actualSectionName = sectionInfoObject.get("sectionName").toString();
				String sectionName = attributeToVerify.get("sectionName");
				if (actualSectionName.equals(sectionName)) {
					status.add(true);
					Log.message("Verified: Section name is matched: <b>" + actualSectionName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Section name is not matched. Expected section name: <b>" + sectionName
							+ "</b>, Actual Section name: <b>" + actualSectionName + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("organizationId") && attributeToVerify.get("organizationId") != null) {
				String actualOrgId = sectionInfoObject.get("organizationId").toString();
				String orgId = attributeToVerify.get("organizationId");
				if (actualOrgId.equals(orgId)) {
					status.add(true);
					Log.message("Verified: Organization id is matched: <b>" + actualOrgId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Organization id is not matched. Expected Organization id: <b>" + orgId
							+ "</b>, Actual Organization id: <b>" + actualOrgId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("rosterSource") && attributeToVerify.get("rosterSource") != null) {
				String actualRosterSource = sectionInfoObject.get("rosterSource").toString();
				String rosterSource = attributeToVerify.get("rosterSource");
				if (actualRosterSource.equals(rosterSource)) {
					status.add(true);
					Log.message("Verified: Roster source is matched: <b>" + actualRosterSource + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Roster source is not matched. Expected Roster source: <b>" + rosterSource
							+ "</b>, Actual Roster source: <b>" + actualRosterSource + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("studentPiId") && attributeToVerify.get("studentPiId") != null) {
				String actualStudentPiId = sectionInfoObject.getJSONArray("students").getJSONObject(0).get("studentPiId").toString();
				String studentPiId = attributeToVerify.get("studentPiId");
				if (actualStudentPiId.equals(studentPiId)) {
					status.add(true);
					Log.message("Verified: studentPiId is matched: <b>" + actualStudentPiId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: StudentPiId is not matched. Expected studentPiId: <b>" + studentPiId
							+ "</b>, Actual studentPiId: <b>" + actualStudentPiId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("staffPiId") && attributeToVerify.get("staffPiId") != null) {
				String actualStaffId = sectionInfoObject.getJSONArray("staff").getJSONObject(0).get("staffPiId").toString();
				String staffPiId = attributeToVerify.get("staffPiId");
				if (actualStaffId.equals(staffPiId)) {
					status.add(true);
					Log.message("Verified: staffPiId is matched: <b>" + actualStaffId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: staffPiId is not matched. Expected staffPiId: <b>" + staffPiId
							+ "</b>, Actual staffPiId: <b>" + actualStaffId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
				
				String teacherOfRecord = sectionInfoObject.getJSONArray("staff").getJSONObject(0).get("teacherOfRecord").toString();
				if (teacherOfRecord.equals("false")) {
					status.add(true);
					Log.message("Verified: teacherOfRecord is <b>'false'</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "teacherOfRecord is not false, Actual: <b>"
							+ teacherOfRecord + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
				
				String teacherAssignment = sectionInfoObject.getJSONArray("staff").getJSONObject(0).get("teacherAssignment").toString();
				if (teacherAssignment.equals("Lead Teacher")) {
					status.add(true);
					Log.message("Verified: teacherAssignment is <b>'Lead Teacher'</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "teacherAssignment is not matched. Expected: Lead Teacher, Actual: <b>"
							+ teacherAssignment + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("contineo") && attributeToVerify.get("contineo") != null) {
				String actualContineo = sectionInfoObject.get("contineo").toString();
				String isContineo = attributeToVerify.get("contineo");
				if (actualContineo.equals(isContineo)) {
					status.add(true);
					Log.message("Verified: contineo is matched: <b>" + actualContineo + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: contineo is not matched. Expected: <b>" + isContineo
							+ "</b>, Actual: <b>" + actualContineo + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("productId") && attributeToVerify.get("productId") != null) {
				String[] productIds = attributeToVerify.get("productId").split("\\|");
				JSONArray sectionProductsList = sectionObject.getJSONObject("data").getJSONArray("sectionProductsAssociationList");
				
				List<String> expectedPlatforms = new ArrayList<String>();
				if (attributeToVerify.containsKey("platforms") && attributeToVerify.get("platforms") != null) {
					expectedPlatforms = Arrays.asList(attributeToVerify.get("platforms").split("\\|\\|"));
				}
				
				//Verify given product Id are listed in the response
				for (int item = 0; item < productIds.length; item++) {
					final String productId = productIds[item].toString().trim();
					int findIndex = IntStream.range(0, sectionProductsList.length()).filter(i -> sectionProductsList
							.getJSONObject(i).get("productId").toString().trim().equals(productId)).findFirst().orElse(-1);

					if (findIndex != -1) {
						status.add(true);
						Log.message("Verified: Product Id <b>" + productId + "</b> is found");
						
						//Verify platforms for each product Id is matched or not
						if (!expectedPlatforms.isEmpty() && expectedPlatforms.size() > findIndex) {
							List<String> individualPlatforms = Arrays.asList(expectedPlatforms.get(findIndex).split("\\|"));
							if (sectionProductsList.getJSONObject(findIndex).has("platforms")
									&& !sectionProductsList.getJSONObject(findIndex).isNull("platforms")) {
								JSONArray actualPlatformsJson = sectionProductsList.getJSONObject(findIndex).getJSONArray("platforms");
								List<String> actualPlatforms = Arrays.asList(actualPlatformsJson.join("|").replaceAll("\"", "").split("\\|"));

//								status.add(true);
//								Log.message(RealizeUtils.WARN_HTML_BEGIN + "Not verified: platforms " + actualPlatforms
//										+ " for productId: <b>" + productId + "</b>" + RealizeUtils.WARN_HTML_END);

								//Compare given platforms is equal to actual platform for each product Id
								if (RealizeUtils.compareTwoList(individualPlatforms, actualPlatforms)) {
									status.add(true);
									Log.message("Verified: platforms " + actualPlatforms
											+ " are matched for productId: <b>" + productId + "</b>");
								} else {
									status.add(false);
									Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Actual platforms "
											+ actualPlatforms + " are not matched with expected platforms "
											+ individualPlatforms + " for product Id '<b>" + productId + "</b>'"
											+ RealizeUtils.FAIL_HTML_END);
								}
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN
										+ "Failed: platforms field not found inside sectionProductsAssociationList for product Id "
										+ productId + RealizeUtils.FAIL_HTML_END);
							}
						}
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Product Id '<b>" + productId
								+ "</b>' is not found" + RealizeUtils.FAIL_HTML_END);
					}
				}
			}

			JSONObject systemObject = JsonResponseBody.getJSONObject("system").getJSONObject("lifecycle");
			if (attributeToVerify.containsKey("deleted") && attributeToVerify.get("deleted") != null) {
				String actualDeleted = systemObject.get("deleted").toString();
				String isDeleted = attributeToVerify.get("deleted");
				if (actualDeleted.equals(isDeleted)) {
					status.add(true);
					Log.message("Verified: deleted is <b>" + actualDeleted + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: deleted is not <b>" + isDeleted
							+ "</b>, Actual: <b>" + actualDeleted + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("createDate") && attributeToVerify.get("createDate") != null) {
				String createDate = attributeToVerify.get("createDate").toString().trim();
				String actualCreateDate = systemObject.get("createDate").toString().trim();
				ZonedDateTime expectedDateTime = ZonedDateTime.parse(createDate);
				ZonedDateTime actualDateTime = ZonedDateTime.parse(actualCreateDate, UTC_TIME_FORMAT);

				// Added this verification to verify createdDate field values by +30 or -30 seconds
				if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 30) {
					status.add(true);
					Log.message("Verified: createDate is matched: <b>" + actualCreateDate + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: createDate is not matched. Expected: <b>" + createDate
							+ "</b>, Actual: <b>" + actualCreateDate + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("modifiedDate") && attributeToVerify.get("modifiedDate") != null) {
				String modifiedDate = attributeToVerify.get("modifiedDate").toString().trim();
				String actualModifiedDate = systemObject.get("modifiedDate").toString().trim();
				ZonedDateTime expectedDateTime = ZonedDateTime.parse(modifiedDate);
				ZonedDateTime actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_TIME_FORMAT);

				// Added this verification to verify modifiedDate field values by +30 or -30 seconds
				if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 30) {
					status.add(true);
					Log.message("Verified: Modified date is matched: <b>" + actualModifiedDate + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: modifiedDate is not matched. Expected: <b>" + modifiedDate
							+ "</b>, Actual: <b>" + actualModifiedDate + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("createdBy") && attributeToVerify.get("createdBy") != null) {
				String actualCreatedBy = attributeToVerify.get("createdBy");
				String createdBy = systemObject.get("createdBy").toString();
				if (createdBy.equals(actualCreatedBy)) {
					status.add(true);
					Log.message("Verified: CreatedBy is matched: <b>" + createdBy + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: CreatedBy is not matched. Expected: <b>"
							+ actualCreatedBy + "</b>, Actual: <b>" + createdBy + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("updatedBy") && attributeToVerify.get("updatedBy") != null) {
				String actualUpdatedBy = attributeToVerify.get("updatedBy");
				String updatedBy = systemObject.get("updatedBy").toString();
				if (updatedBy.equals(actualUpdatedBy)) {
					status.add(true);
					Log.message("Verified: UpdatedBy is matched: <b>" + updatedBy + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: UpdatedBy is not matched. Expected: <b>"
							+ actualUpdatedBy + "</b>, Actual: <b>" + updatedBy + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("districtId") && attributeToVerify.get("districtId") != null) {
				String actualDistrictId = JsonResponseBody.getJSONArray("districts").getJSONObject(0).get("id").toString();
				String districtId = attributeToVerify.get("districtId");
				if (actualDistrictId.equals(districtId)) {
					status.add(true);
					Log.message("Verified: District Id is matched: <b>" + actualDistrictId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: District Id is not matched. Expected District Id: <b>" + districtId
							+ "</b>, Actual District Id: <b>" + actualDistrictId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying attributes in Section Response in Roster service API.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}

	/**
	 * To verify all the student details on the Section Response in Roster Service API
	 * 
	 * @param JsonResponseBody
	 * @param lstStudentsDetails - list of student details in hashmap
	 * @return
	 */
	public static boolean verifyStudentAttributesInSectionResponse(JSONObject JsonResponseBody, List<HashMap<String, String>> lstStudentsDetails) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			JSONObject sectionObject = JsonResponseBody.getJSONObject("data").getJSONObject("section");
			JSONArray studentObjects = sectionObject.getJSONObject("data").getJSONObject("sectionInfo")
					.getJSONArray("students");

			if (lstStudentsDetails.isEmpty()) {
				if (studentObjects.length() == lstStudentsDetails.size()) {
					status.add(true);
					Log.message("Verified: students is empty");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: students are not empty. Actual students: "
							+ studentObjects.toString() + RealizeUtils.FAIL_HTML_END);
				}
			} else {
				for (HashMap<String, String> studAttribute : lstStudentsDetails) {
					final String studentPiId = studAttribute.get("studentPiId");
					int findIndex = IntStream.range(0, studentObjects.length())
							.filter(i -> studentObjects.getJSONObject(i).getString("studentPiId").equals(studentPiId))
							.findFirst().orElse(-1);

					if (findIndex != -1) {
						status.add(true);
						Log.message("Verified: studentPiId is matched: <b>" + studentPiId + "</b>");

						if (studAttribute.containsKey("studentEnrollmentId") && studAttribute.get("studentEnrollmentId") != null) {
							String actualEnrollId = studentObjects.getJSONObject(findIndex)
									.get("studentEnrollmentId").toString();
							String expectedEnrollId = studAttribute.get("studentEnrollmentId");
							if (actualEnrollId.equals(expectedEnrollId)) {
								status.add(true);
								Log.message("Verified: Section name for student id '" + studentPiId
										+ "'  is matched: <b>" + actualEnrollId + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN
										+ "Failed: Student EnrollmentId having student id '" + studentPiId
										+ "' is not matched. Expected: <b>" + expectedEnrollId + "</b>, Actual: <b>"
										+ actualEnrollId + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}
						
						if (studAttribute.containsKey("beginDate") && studAttribute.get("beginDate") != null) {
							String actualBeginDate = studentObjects.getJSONObject(findIndex).get("beginDate").toString();
							String expectedBeginDate = studAttribute.get("beginDate").toString().trim();
							ZonedDateTime expectedDateTime = ZonedDateTime.parse(expectedBeginDate);
							ZonedDateTime actualDateTime = ZonedDateTime.parse(actualBeginDate, UTC_TIME_FORMAT);

							// Added this verification to verify beginDate field values by +30 or -30 seconds
							if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 30) {
								status.add(true);
								Log.message("Verified: Begin date for student id '" + studentPiId + "' is matched: <b>" + actualBeginDate + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Begin date for student id '"
										+ studentPiId + "' is not matched. Expected: <b>" + expectedBeginDate
										+ "</b>, Actual: <b>" + actualBeginDate + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

						if (studAttribute.containsKey("endDate") && studAttribute.get("endDate") != null) {
							String actualEndDate = studentObjects.getJSONObject(findIndex).get("endDate").toString();
							String expectedEndDate = studAttribute.get("endDate").toString().trim();
							ZonedDateTime expectedDateTime = ZonedDateTime.parse(expectedEndDate);
							ZonedDateTime actualDateTime = ZonedDateTime.parse(actualEndDate, UTC_TIME_FORMAT);

							// Added this verification to verify endDate field values by +30 or -30 seconds
							if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 30) {
								status.add(true);
								Log.message("Verified: End date for student id '" + studentPiId + "'  is matched: <b>" + actualEndDate + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: End date for student id '"
										+ studentPiId + "'  is not matched. Expected: <b>" + expectedEndDate
										+ "</b>, Actual: <b>" + actualEndDate + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: StudentPiId <b>" + studentPiId
								+ "</b> is not found. " + RealizeUtils.FAIL_HTML_END);
					}

				}
			}

		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying Student attributes in Section Response in Roster service API.."
					+ err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To verify all the staff details on the Section Response in Roster Service API
	 * 
	 * @param JsonResponseBody
	 * @param lstStaffsDetails - list of staff details in hashmap
	 * @return
	 */
	public static boolean verifyStaffAttributesInSectionResponse(JSONObject JsonResponseBody, List<HashMap<String, String>> lstStaffsDetails) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			JSONObject sectionObject = JsonResponseBody.getJSONObject("data").getJSONObject("section");
			JSONArray staffObjects = sectionObject.getJSONObject("data").getJSONObject("sectionInfo").getJSONArray("staff");
			
			if (lstStaffsDetails.isEmpty()) {
				if (staffObjects.length() == lstStaffsDetails.size()) {
					status.add(true);
					Log.message("Verified: staff is empty");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: staff is not empty. Actual staff: "
							+ staffObjects.toString() + RealizeUtils.FAIL_HTML_END);
				}
			} else {
				for (HashMap<String, String> staffAttribute : lstStaffsDetails) {
					final String staffPiId = staffAttribute.get("staffPiId");
					int findIndex = IntStream.range(0, staffObjects.length())
							.filter(i -> staffObjects.getJSONObject(i).getString("staffPiId").equals(staffPiId))
							.findFirst().orElse(-1);

					if (findIndex != -1) {
						status.add(true);
						Log.message("Verified: staffPiId is matched: <b>" + staffPiId + "</b>");

						if (staffAttribute.containsKey("staffAssignmentId") && staffAttribute.get("staffAssignmentId") != null) {
							String actualAssignmentId = staffObjects.getJSONObject(findIndex).get("staffAssignmentId").toString();
							String expectedAssignmentId = staffAttribute.get("staffAssignmentId");
							if (actualAssignmentId.equals(expectedAssignmentId)) {
								status.add(true);
								Log.message("Verified: Staff AssignmentId for staff '" + staffPiId
										+ "'  is matched: <b>" + actualAssignmentId + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN
										+ "Failed: Staff AssignmentId having staff id '" + staffPiId
										+ "' is not matched. Expected: <b>" + expectedAssignmentId + "</b>, Actual: <b>"
										+ actualAssignmentId + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}
						
						if (staffAttribute.containsKey("beginDate") && staffAttribute.get("beginDate") != null) {
							String actualBeginDate = staffObjects.getJSONObject(findIndex).get("beginDate").toString();
							String expectedBeginDate = staffAttribute.get("beginDate").toString().trim();
							ZonedDateTime expectedDateTime = ZonedDateTime.parse(expectedBeginDate);
							ZonedDateTime actualDateTime = ZonedDateTime.parse(actualBeginDate, UTC_TIME_FORMAT);

							// Added this verification to verify beginDate field values by +30 or -30 seconds
							if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 30) {
								status.add(true);
								Log.message("Verified: Begin date for staff '" + staffPiId + "' is matched: <b>" + actualBeginDate + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Begin date for staff '"
										+ staffPiId + "' is not matched. Expected: <b>" + expectedBeginDate
										+ "</b>, Actual: <b>" + actualBeginDate + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

						if (staffAttribute.containsKey("endDate") && staffAttribute.get("endDate") != null) {
							String actualEndDate = staffObjects.getJSONObject(findIndex).get("endDate").toString();
							String expectedEndDate = staffAttribute.get("endDate").toString().trim();
							ZonedDateTime expectedDateTime = ZonedDateTime.parse(expectedEndDate);
							ZonedDateTime actualDateTime = ZonedDateTime.parse(actualEndDate, UTC_TIME_FORMAT);

							// Added this verification to verify endDate field values by +30 or -30 seconds
							if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 30) {
								status.add(true);
								Log.message("Verified: End date for staff '" + staffPiId + "'  is matched: <b>" + actualEndDate + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: End date for staff '"
										+ staffPiId + "'  is not matched. Expected: <b>" + expectedEndDate
										+ "</b>, Actual: <b>" + actualEndDate + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

						if (staffAttribute.containsKey("teacherOfRecord") && staffAttribute.get("teacherOfRecord") != null) {
							String actualTeachRecord = staffObjects.getJSONObject(findIndex).get("teacherOfRecord").toString();
							String expectedTeachRecord = staffAttribute.get("teacherOfRecord");
							if (actualTeachRecord.equalsIgnoreCase(expectedTeachRecord)) {
								status.add(true);
								Log.message("Verified: teacherOfRecord for staff '" + staffPiId
										+ "'  is matched: <b>" + actualTeachRecord + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN
										+ "Failed: teacherOfRecord for staff '" + staffPiId
										+ "' is not matched. Expected: <b>" + expectedTeachRecord + "</b>, Actual: <b>"
										+ actualTeachRecord + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

						if (staffAttribute.containsKey("teacherAssignment") && staffAttribute.get("teacherAssignment") != null) {
							String actualTeacherType = staffObjects.getJSONObject(findIndex).get("teacherAssignment").toString();
							String expectedTeacherType = staffAttribute.get("teacherAssignment");
							if (actualTeacherType.equalsIgnoreCase(expectedTeacherType)) {
								status.add(true);
								Log.message("Verified: teacherAssignment for staff '" + staffPiId
										+ "'  is matched: <b>" + actualTeacherType + "</b>");
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN
										+ "Failed: teacherAssignment for staff '" + staffPiId
										+ "' is not matched. Expected: <b>" + expectedTeacherType + "</b>, Actual: <b>"
										+ actualTeacherType + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: staffPiId <b>" + staffPiId
								+ "</b> is not found. " + RealizeUtils.FAIL_HTML_END);
					}

				}
			}
		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying Staffs attributes in  Section Response in Roster service API.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To get total staff from the given Section Response JSON
	 * @param JsonResponseBody
	 * @return
	 */
	public static int getTotalStaffCountFromSectionResponse(JSONObject JsonResponseBody) {
		int count = -1;
		try {
			JSONObject sectionObject = JsonResponseBody.getJSONObject("data").getJSONObject("section");
			JSONArray staffObjects = sectionObject.getJSONObject("data").getJSONObject("sectionInfo").getJSONArray("staff");
			count = staffObjects.length();
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get total Staff count from section response. Error: "
					+ e.getMessage() + RealizeUtils.FAIL_HTML_END);
		}
		return count;
	}
	
	/**
	 * To get total Student from the given Section Response JSON
	 * @param JsonResponseBody
	 * @return
	 */
	public static int getTotalStudentCountFromSectionResponse(JSONObject JsonResponseBody) {
		int count = -1;
		try {
			JSONObject sectionObject = JsonResponseBody.getJSONObject("data").getJSONObject("section");
			JSONArray studentObjects = sectionObject.getJSONObject("data").getJSONObject("sectionInfo").getJSONArray("students");
			count = studentObjects.length();
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get total Students count from section response. Error: "
					+ e.getMessage() + RealizeUtils.FAIL_HTML_END);
		}
		return count;
	}
	
	/**
	 * To get total Product from the given Section Response JSON
	 * @param JsonResponseBody
	 * @return
	 */
	public static int getTotalProductCountFromSectionResponse(JSONObject JsonResponseBody) {
		int count = -1;
		try {
			JSONObject sectionObject = JsonResponseBody.getJSONObject("data").getJSONObject("section");
			JSONArray productObjects = sectionObject.getJSONObject("data").getJSONArray("sectionProductsAssociationList");
			count = productObjects.length();
		} catch (Exception e) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get total Students count from section response. Error: "
					+ e.getMessage() + RealizeUtils.FAIL_HTML_END);
		}
		return count;
	}
	
	/**
	 * To verify CPS response with the given attribute
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @param lstMemberDetails
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyGivenAttributesInCPSResponse(JSONObject JsonResponseBody,
			HashMap<String, String> attributeToVerify, List<HashMap<String, String>> lstMemberDetails) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<u>Verifying CPS responses as below:</u>");

			// verify platform issuer id
			if (attributeToVerify.containsKey("iss") && attributeToVerify.get("iss") != null) {
				String actualIssuerId = JsonResponseBody.get("iss").toString();
				String expectedIssuerId = attributeToVerify.get("iss");
				if (actualIssuerId.equals(expectedIssuerId)) {
					status.add(true);
					Log.message("Verified: Issuer Id (iss) is matched: <b>" + actualIssuerId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Issuer Id (iss) is not matched. Expected: <b>"
							+ expectedIssuerId + "</b>, Actual: <b>" + actualIssuerId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			// verify sub
			if (attributeToVerify.containsKey("sub") && attributeToVerify.get("sub") != null) {
				String actualSub = JsonResponseBody.get("sub").toString();
				String expectedSub = attributeToVerify.get("sub");
				if (actualSub.equals(expectedSub)) {
					status.add(true);
					Log.message("Verified: sub is matched: <b>" + actualSub + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: sub is not matched. Expected: <b>" + expectedSub
							+ "</b>, Actual: <b>" + actualSub + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("externalUserId") && attributeToVerify.get("externalUserId") != null) {
				String externalUserId = JsonResponseBody.get("externalUserId").toString();
				String expectedExternalUserId = attributeToVerify.get("externalUserId");
				if (externalUserId.equals(expectedExternalUserId)) {
					status.add(true);
					Log.message("Verified: External UserId is matched: <b>" + externalUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: External UserId is not matched, Expected: <b>"
							+ expectedExternalUserId + "</b>, Actual: <b>" + externalUserId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("toolUserId") && attributeToVerify.get("toolUserId") != null) {
				String actualToolUserId = JsonResponseBody.get("toolUserId").toString();
				String toolUserId = attributeToVerify.get("toolUserId");
				if (actualToolUserId.equals(toolUserId)) {
					status.add(true);
					Log.message("Verified: Tool UserId is matched: <b>" + actualToolUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Tool UserId is not matched, Expected: <b>"
							+ toolUserId + "</b>, Actual: <b>" + actualToolUserId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("role") && attributeToVerify.get("role") != null) {
				String actualRole = JsonResponseBody.get("role").toString();
				String role = attributeToVerify.get("role");
				if (actualRole.equals(role)) {
					status.add(true);
					Log.message("Verified: Role is matched: <b>" + actualRole + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Role is not matched, Expected: <b>" + role
							+ "</b>, Actual: <b>" + actualRole + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("productId") && attributeToVerify.get("productId") != null) {
				String actualProdId = JsonResponseBody.get("productId").toString();
				String productId = attributeToVerify.get("productId");
				if (actualProdId.equals(productId)) {
					status.add(true);
					Log.message("Verified: Product Id is matched: <b>" + actualProdId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Product Id is not matched, Expected: <b>"
							+ productId + "</b>, Actual: <b>" + actualProdId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("organizationId") && attributeToVerify.get("organizationId") != null) {
				String actualOrgId = JsonResponseBody.get("organizationId").toString();
				String orgId = attributeToVerify.get("organizationId");
				if (actualOrgId.equals(orgId)) {
					status.add(true);
					Log.message("Verified: Organization Id is matched: <b>" + actualOrgId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Organization Id is not matched, Expected: <b>"
							+ orgId + "</b>, Actual: <b>" + actualOrgId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("externalSystem") && attributeToVerify.get("externalSystem") != null) {
				String actualSystem = JsonResponseBody.get("externalSystem").toString();
				String externalSystem = attributeToVerify.get("externalSystem");
				if (actualSystem.equals(externalSystem)) {
					status.add(true);
					Log.message("Verified: External System is matched: <b>" + actualSystem + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: External System is not matched, Expected: <b>"
							+ externalSystem + "</b>, Actual: <b>" + actualSystem + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			if (attributeToVerify.containsKey("rosterId") && attributeToVerify.get("rosterId") != null) {
				String actualRosterId = JsonResponseBody.getJSONObject("roster").get("id").toString();
				String rosterId = attributeToVerify.get("rosterId");
				if (actualRosterId.equals(rosterId)) {
					status.add(true);
					Log.message("Verified: Roster Id is matched: <b>" + actualRosterId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Roster Id is not matched, Expected: <b>"
							+ rosterId + "</b>, Actual: <b>" + actualRosterId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("rosterName") && attributeToVerify.get("rosterName") != null) {
				String actualRosterName = JsonResponseBody.getJSONObject("roster").get("name").toString();
				String rosterName = attributeToVerify.get("rosterName");
				if (actualRosterName.equals(rosterName)) {
					status.add(true);
					Log.message("Verified: Roster Name is matched: <b>" + actualRosterName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Roster name is not matched, Expected: <b>"
							+ rosterName + "</b>, Actual: <b>" + actualRosterName + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("toolSectionId") && attributeToVerify.get("toolSectionId") != null) {
				String actualSectionId = JsonResponseBody.getJSONObject("roster").get("toolSectionId").toString();
				String toolSectionId = attributeToVerify.get("toolSectionId");
				if (actualSectionId.equals(toolSectionId)) {
					status.add(true);
					Log.message("Verified: Tool SectionId is matched: <b>" + actualSectionId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Tool SectionId is not matched, Expected: <b>"
							+ toolSectionId + "</b>, Actual: <b>" + actualSectionId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("externalClassId") && attributeToVerify.get("externalClassId") != null) {
				String actualClassId = JsonResponseBody.getJSONObject("roster").get("externalClassId").toString();
				String externalClassId = attributeToVerify.get("externalClassId");
				if (actualClassId.equals(externalClassId)) {
					status.add(true);
					Log.message("Verified: External ClassId is matched: <b>" + actualClassId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: External ClassId is not matched, Expected: <b>"
							+ externalClassId + "</b>, Actual: <b>" + actualClassId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("isAccountLinkingEnabled")
					&& attributeToVerify.get("isAccountLinkingEnabled") != null) {
				String actualAccLinking = JsonResponseBody.get("isAccountLinkingEnabled").toString();
				String expectedAccLinking = attributeToVerify.get("isAccountLinkingEnabled");
				if (actualAccLinking.equals(expectedAccLinking)) {
					status.add(true);
					Log.message("Verified: isAccountLinkingEnabled attribute is matched: <b>" + actualAccLinking + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: isAccountLinkingEnabled attribute is not matched. Expected: <b>"
							+ expectedAccLinking + "</b>, Actual: <b>" + actualAccLinking + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("isAutoMatchingEnabled")
					&& attributeToVerify.get("isAutoMatchingEnabled") != null) {
				String actualAutoMatching = JsonResponseBody.get("isAutoMatchingEnabled").toString();
				String expectedAutoMatching = attributeToVerify.get("isAutoMatchingEnabled");
				if (actualAutoMatching.equals(expectedAutoMatching)) {
					status.add(true);
					Log.message("Verified: isAutoMatchingEnabled attribute is matched: <b>" + actualAutoMatching + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: isAutoMatchingEnabled attribute is not matched. Expected: <b>"
							+ expectedAutoMatching + "</b>, Actual: <b>" + actualAutoMatching + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("autoMatchingAttribute")
					&& attributeToVerify.get("autoMatchingAttribute") != null) {
				String actualMatchingAttribute = JsonResponseBody.get("autoMatchingAttribute").toString();
				String expectedMatchingAttribute = attributeToVerify.get("autoMatchingAttribute");
				if (actualMatchingAttribute.equals(expectedMatchingAttribute)) {
					status.add(true);
					Log.message("Verified: autoMatchingAttribute is matched: <b>" + actualMatchingAttribute + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: autoMatchingAttribute is not matched. Expected: <b>" + expectedMatchingAttribute
							+ "</b>, Actual: <b>" + actualMatchingAttribute + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("orgSelectionType")
					&& attributeToVerify.get("orgSelectionType") != null) {
				String actualOrgSelectionType = JsonResponseBody.get("orgSelectionType").toString();
				String expectedOrgSelectionType = attributeToVerify.get("orgSelectionType");
				if (actualOrgSelectionType.equals(expectedOrgSelectionType)) {
					status.add(true);
					Log.message("Verified: orgSelectionType is matched: <b>" + actualOrgSelectionType + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: orgSelectionType is not matched. Expected: <b>" + expectedOrgSelectionType
							+ "</b>, Actual: <b>" + actualOrgSelectionType + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("isRedirectionRequired")
					&& attributeToVerify.get("isRedirectionRequired") != null) {
				String actualValue = JsonResponseBody.get("isRedirectionRequired").toString();
				String expectedValue = attributeToVerify.get("isRedirectionRequired");
				if (actualValue.equals(expectedValue)) {
					status.add(true);
					Log.message("Verified: isRedirectionRequired attribute is matched: <b>" + actualValue + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: isRedirectionRequired attribute is not matched. Expected: <b>" + expectedValue
							+ "</b>, Actual: <b>" + actualValue + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("affiliationUpdateAllowed")
					&& attributeToVerify.get("affiliationUpdateAllowed") != null) {
				String actualValue = JsonResponseBody.get("affiliationUpdateAllowed").toString();
				String expectedValue = attributeToVerify.get("affiliationUpdateAllowed");
				if (actualValue.equals(expectedValue)) {
					status.add(true);
					Log.message("Verified: affiliationUpdateAllowed attribute is matched: <b>" + actualValue + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: affiliationUpdateAllowed attribute is not matched. Expected: <b>" + expectedValue
							+ "</b>, Actual: <b>" + actualValue + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("redirectUrl") && attributeToVerify.get("redirectUrl") != null) {
				if (JsonResponseBody.has("redirectUrl") && !JsonResponseBody.getString("redirectUrl").isEmpty()) {
					String actualUrl = JsonResponseBody.get("redirectUrl").toString();
					String expectedUrl = attributeToVerify.get("redirectUrl");
					if (actualUrl.contains(expectedUrl)) {
						status.add(true);
						Log.message("Verified: redirectUrl is matched: <b>" + actualUrl + "</b>");
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: redirectUrl is not matched. Expected: <b>"
								+ expectedUrl + "</b>, Actual: <b>" + actualUrl + "</b>" + RealizeUtils.FAIL_HTML_END);
					}
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: redirectUrl attribute is not found in the response."
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("selectedOrganizations")
					&& attributeToVerify.get("selectedOrganizations") != null) {
				List<String> expectedOrgIds = Arrays.asList(attributeToVerify.get("selectedOrganizations").split("\\|"));
				if (expectedOrgIds.size() == 1) {
					String expectedOrgId = expectedOrgIds.get(0);
					if (expectedOrgId.isEmpty()) {
						if (JsonResponseBody.getJSONArray("selectedOrganizations").length() == 0) {
							status.add(true);
							Log.message("Verified: selectedOrganizations is empty</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN
									+ "Failed: selectedOrganizations is not empty. Actual selectedOrganizations: <b>"
									+ JsonResponseBody.getJSONArray("selectedOrganizations").toString() + "</b>"
									+ RealizeUtils.FAIL_HTML_END);
						}
					} else {
						if (JsonResponseBody.getJSONArray("selectedOrganizations").length() > 0
								&& JsonResponseBody.getJSONArray("selectedOrganizations").get(0).toString().equals(expectedOrgId)) {
							status.add(true);
							Log.message(
									"Verified: selectedOrganizations is matched: <b>" + expectedOrgId + "</b>");
						} else {
							status.add(false);
							Log.message(RealizeUtils.FAIL_HTML_BEGIN
									+ "Failed: selectedOrganizations is not matched. Expected selectedOrganizations: <b>"
									+ expectedOrgId + "</b>, Actual selectedOrganizations: <b>"
									+ JsonResponseBody.getJSONArray("selectedOrganizations").toString() + "</b>"
									+ RealizeUtils.FAIL_HTML_END);
						}
					}
				} else {
					List<String> actualOrgIds = new ArrayList<String>();
					JsonResponseBody.getJSONArray("selectedOrganizations").forEach(x -> actualOrgIds.add(x.toString()));
					if (RealizeUtils.compareTwoList(expectedOrgIds, actualOrgIds)) {
						status.add(true);
						Log.message("Verified: selectedOrganizations is matched: <b>[ "
								+ String.join(", ", expectedOrgIds) + " ]</b>");
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN
								+ "Failed: selectedOrganizations is not matched. Expected selectedOrganizations: <b>[ "
								+ String.join(", ", expectedOrgIds) + " ]</b>, Actual selectedOrganizations: <b>[ "
								+ String.join(", ", actualOrgIds) + " ]</b>" + RealizeUtils.FAIL_HTML_END);
					}
				}
			}

			status.add(verifyMemberDetails(JsonResponseBody, lstMemberDetails));
			
		} catch (Exception err) {
			status.add(false);
			Log.message("Unable to verify attributes in the response of Class Provisioning service API, Error: " + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To verify given members details from Response
	 * @param memberBodyJson
	 * @param lstMemberDetails
	 * @return
	 */
	private static boolean verifyMemberDetails(JSONObject memberBodyJson, List<HashMap<String, String>> lstMemberDetails) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		if (lstMemberDetails != null) {
			JSONArray membersJson = memberBodyJson.getJSONArray("members");
			if (lstMemberDetails.isEmpty()) {
				if (membersJson.length() == lstMemberDetails.size()) {
					status.add(true);
					Log.message("Verified: members is empty");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: members are not empty. Actual members: "
							+ membersJson.toString() + RealizeUtils.FAIL_HTML_END);
				}
			} else {
				for (HashMap<String, String> memberDetail : lstMemberDetails) {
					final String userId = memberDetail.get("userId");
					int findIndex = IntStream.range(0, membersJson.length())
							.filter(i -> membersJson.getJSONObject(i).getString("userId").equals(userId)).findFirst()
							.orElse(-1);

					if (findIndex != -1) {
						if (memberDetail.containsKey("externalUserId") && memberDetail.get("externalUserId") != null) {
							String externalUserId = membersJson.getJSONObject(findIndex).get("externalUserId")
									.toString();
							String expectedExternalUserId = memberDetail.get("externalUserId");
							if (externalUserId.equals(expectedExternalUserId)) {
								status.add(true);
								Log.message("Verified: External UserId <b>" + externalUserId
										+ "</b> is matched for user " + userId);
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN
										+ "Failed: External UserId is not matched for user " + userId
										+ ", Expected: <b>" + expectedExternalUserId + "</b>, Actual: <b>"
										+ externalUserId + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

						if (memberDetail.containsKey("toolUserId") && memberDetail.get("toolUserId") != null) {
							String actualToolUserId = membersJson.getJSONObject(findIndex).get("toolUserId").toString();
							String toolUserId = memberDetail.get("toolUserId");
							if (actualToolUserId.equals(toolUserId)) {
								status.add(true);
								Log.message("Verified: Tool UserId <b>" + actualToolUserId + "</b> is matched for user "
										+ userId);
							} else {
								status.add(false);
								Log.message(
										RealizeUtils.FAIL_HTML_BEGIN + "Failed: Tool UserId is not matched for user "
												+ userId + ", Expected: <b>" + toolUserId + "</b>, Actual: <b>"
												+ actualToolUserId + "</b>" + RealizeUtils.FAIL_HTML_END);
							}
						}

						if (memberDetail.containsKey("role") && memberDetail.get("role") != null) {
							String actualRole = membersJson.getJSONObject(findIndex).get("role").toString();
							String role = memberDetail.get("role");
							if (actualRole.equals(role)) {
								status.add(true);
								Log.message("Verified: Role <b>" + actualRole + "</b> is matched for user " + userId);
							} else {
								status.add(false);
								Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Role is not matched for user "
										+ userId + ", Expected: <b>" + role + "</b>, Actual: <b>" + actualRole + "</b>"
										+ RealizeUtils.FAIL_HTML_END);
							}
						}
					} else {
						status.add(false);
						Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: No member found with given userId: <b>"
								+ userId + "</b>" + RealizeUtils.FAIL_HTML_END);
					}
				}
			}
		} else {
			status.add(true);
			Log.event("Skipping members detail verification");
		}
		return RealizeUtils.isAllTrue(status);
	}

	/**
	 * To verify the LTI Tool gateway Response with the given attribute
	 * 
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyGivenAttributesInLTItoolgatewayResponse(JSONObject JsonResponseBody,
			HashMap<String, String> attributeToVerify) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<br><u>Verifying LTI Toolgateway responses as below:</u>");
			if (attributeToVerify.containsKey("issuerId") && attributeToVerify.get("issuerId") != null) {
				String actualIssuerId = JsonResponseBody.get("issuerId").toString();
				String expectedIssuerId = attributeToVerify.get("issuerId");
				if (actualIssuerId.equals(expectedIssuerId)) {
					status.add(true);
					Log.message("Verified: Issuer id is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualIssuerId + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Issuer id is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected Issuer id: <pre style=\"margin:0;display:inline\"><b>&#34;" + expectedIssuerId
							+ "&#34;</b></pre>, Actual Issuer id: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualIssuerId + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("clientId") && attributeToVerify.get("clientId") != null) {
				String actualClientId = JsonResponseBody.get("clientId").toString();
				String expectedClientId = attributeToVerify.get("clientId");
				if (actualClientId.equals(expectedClientId)) {
					status.add(true);
					Log.message("Verified: Client id is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualClientId + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: client id is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected client id: <pre style=\"margin:0;display:inline\"><b>&#34;" + expectedClientId
							+ "&#34;</b></pre>, Actual client id: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualClientId + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("integrationName") && attributeToVerify.get("integrationName") != null) {
				String actualIntegrationName = JsonResponseBody.get("integrationName").toString();
				String expectedIntegrationName = attributeToVerify.get("integrationName");
				if (actualIntegrationName.equals(expectedIntegrationName)) {
					status.add(true);
					Log.message("Verified: Integration Name is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualIntegrationName + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Integration Name is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected Integration Name: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedIntegrationName
							+ "&#34;</b></pre>, Actual Integration Name: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualIntegrationName + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("organizationName")
					&& attributeToVerify.get("organizationName") != null) {
				String actualOrganizationName = JsonResponseBody.get("organizationName").toString();
				String expectedOrganizationName = attributeToVerify.get("organizationName");
				if (actualOrganizationName.equals(expectedOrganizationName)) {
					status.add(true);
					Log.message(
							"Verified: Organization Name is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualOrganizationName + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Organization Name is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected Organization Name: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedOrganizationName
							+ "&#34;</b></pre>, Actual Organization Name: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualOrganizationName + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("rumbaDistrictId") && attributeToVerify.get("rumbaDistrictId") != null) {
				String actualOrgId = JsonResponseBody.get("rumbaDistrictId").toString();
				String expectedOrgId = attributeToVerify.get("rumbaDistrictId");
				if (actualOrgId.equals(expectedOrgId)) {
					status.add(true);
					Log.message(
							"Verified: Rumba District Id is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualOrgId + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Rumba District Id is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected rumba district Id: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedOrgId
							+ "&#34;</b></pre>, Actual rumba district Id: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualOrgId + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("platformName") && attributeToVerify.get("platformName") != null) {
				String actualPlatformName = JsonResponseBody.get("platformName").toString();
				String expectedPlatformName = attributeToVerify.get("platformName");
				if (actualPlatformName.equals(expectedPlatformName)) {
					status.add(true);
					Log.message("Verified: Platform Name is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualPlatformName + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Platform Name is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected Platform Name: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedPlatformName
							+ "&#34;</b></pre>, Actual Platform Name: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualPlatformName + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("platformType") && attributeToVerify.get("platformType") != null) {
				String actualPlatformType = JsonResponseBody.get("platformType").toString();
				String expectedPlatformType = attributeToVerify.get("platformType");
				if (actualPlatformType.equals(expectedPlatformType)) {
					status.add(true);
					Log.message("Verified: platformType is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualPlatformType + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: platformType is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected platformType: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedPlatformType
							+ "&#34;</b></pre>, Actual platformType: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualPlatformType + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("platformStatus") && attributeToVerify.get("platformStatus") != null) {
				String actualPlatformStatus = JsonResponseBody.get("platformStatus").toString();
				String expectedPlatformStatus = attributeToVerify.get("platformStatus");
				if (actualPlatformStatus.equals(expectedPlatformStatus)) {
					status.add(true);
					Log.message("Verified: Platform Status is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualPlatformStatus + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: platformStatus is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected platform status: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedPlatformStatus
							+ "&#34;</b></pre>, Actual platform status: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualPlatformStatus + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("accessTokenURL") && attributeToVerify.get("accessTokenURL") != null) {
				String actualAccessTokenURL = JsonResponseBody.get("accessTokenURL").toString();
				String expectedAccessTokenURL = attributeToVerify.get("accessTokenURL");
				if (actualAccessTokenURL.equals(expectedAccessTokenURL)) {
					status.add(true);
					Log.message("Verified: AccessToken URL is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualAccessTokenURL + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Access token URL is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected accessTokenURL: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedAccessTokenURL
							+ "&#34;</b></pre>, Actual accessTokenURL: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualAccessTokenURL + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("authorizationURL")
					&& attributeToVerify.get("authorizationURL") != null) {
				String actualAuthorizationURL = JsonResponseBody.get("authorizationURL").toString();
				String expectedAuthorizationURL = attributeToVerify.get("authorizationURL");
				if (actualAuthorizationURL.equals(expectedAuthorizationURL)) {
					status.add(true);
					Log.message(
							"Verified: Authorization URL is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualAuthorizationURL + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Authorization URL is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected Authorization URL: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedAuthorizationURL
							+ "&#34;</b></pre>, Actual Authorization URL: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualAuthorizationURL + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("platformPublicKeySetUrl")
					&& attributeToVerify.get("platformPublicKeySetUrl") != null) {
				String actualKeySetURL = JsonResponseBody.get("platformPublicKeySetUrl").toString();
				String expectedKeySetURL = attributeToVerify.get("platformPublicKeySetUrl");
				if (actualKeySetURL.equals(expectedKeySetURL)) {
					status.add(true);
					Log.message(
							"Verified: Platform Public KeySet Url is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualKeySetURL + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: Platform Public KeySet Url is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected KeySet Url: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedKeySetURL
							+ "&#34;</b></pre>, Actual KeySet Url: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualKeySetURL + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("toolPublicKey") && attributeToVerify.get("toolPublicKey") != null) {
				String actualToolPublicKey = JsonResponseBody.get("toolPublicKey").toString();
				String expectedToolPublicKey = attributeToVerify.get("toolPublicKey");
				if (actualToolPublicKey.equals(expectedToolPublicKey)) {
					status.add(true);
					Log.message("Verified: toolPublicKey is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualToolPublicKey + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: toolPublicKey is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected toolPublicKey: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedToolPublicKey
							+ "&#34;</b></pre>, Actual toolPublicKey: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualToolPublicKey + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("toolKeySetUrl") && attributeToVerify.get("toolKeySetUrl") != null) {
				String actualToolKeySetURL = JsonResponseBody.get("toolKeySetUrl").toString();
				String expectedToolKeySetURL = attributeToVerify.get("toolKeySetUrl");
				if (actualToolKeySetURL.equals(expectedToolKeySetURL)) {
					status.add(true);
					Log.message("Verified: toolKeySetUrl is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualToolKeySetURL + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: toolKeySetUrl is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected toolKeySetUrl: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedToolKeySetURL
							+ "&#34;</b></pre>, Actual toolKeySetUrl: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualToolKeySetURL + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("isAccountLinkingEnabled")
					&& attributeToVerify.get("isAccountLinkingEnabled") != null) {
				String actualIsAccountLinkingEnabled = JsonResponseBody.get("isAccountLinkingEnabled").toString();
				String expectedIsAccountLinkingEnabled = attributeToVerify.get("isAccountLinkingEnabled").toLowerCase();
				if (actualIsAccountLinkingEnabled.equals(expectedIsAccountLinkingEnabled)) {
					status.add(true);
					Log.message(
							"Verified: isAccountLinkingEnabled is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualIsAccountLinkingEnabled + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: isAccountLinkingEnabled is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected isAccountLinkingEnabled: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedIsAccountLinkingEnabled
							+ "&#34;</b></pre>, Actual isAccountLinkingEnabled: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualIsAccountLinkingEnabled + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("isAutoMatchingEnabled")
					&& attributeToVerify.get("isAutoMatchingEnabled") != null) {
				String actualIsAutoMatchingEnabled = JsonResponseBody.get("isAutoMatchingEnabled").toString();
				String expectedIsAutoMatchingEnabled = attributeToVerify.get("isAutoMatchingEnabled").toLowerCase();
				if (actualIsAutoMatchingEnabled.equals(expectedIsAutoMatchingEnabled)) {
					status.add(true);
					Log.message(
							"Verified: isAutoMatchingEnabled is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualIsAutoMatchingEnabled + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: isAutoMatchingEnabled is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected isAutoMatchingEnabled: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedIsAutoMatchingEnabled
							+ "&#34;</b></pre>, Actual isAutoMatchingEnabled: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualIsAutoMatchingEnabled + "&#34;</b></pre>");
				}
			}

			if (attributeToVerify.containsKey("autoMatchingAttribute")
					&& attributeToVerify.get("autoMatchingAttribute") != null) {
				String actualAutoMatchingAttribute = JsonResponseBody.get("autoMatchingAttribute").toString();
				String expectedLisLinkingAttribute = attributeToVerify.get("autoMatchingAttribute").toLowerCase();
				if (actualAutoMatchingAttribute.equals(expectedLisLinkingAttribute)) {
					status.add(true);
					Log.message(
							"Verified: autoMatchingAttribute is matched: <pre style=\"margin:0;display:inline\"><b>&#34;"
									+ actualAutoMatchingAttribute + "&#34;</b></pre>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: autoMatchingAttribute is not matched. "
							+ RealizeUtils.FAIL_HTML_END
							+ " Expected autoMatchingAttribute: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ expectedLisLinkingAttribute
							+ "&#34;</b></pre>, Actual autoMatchingAttribute: <pre style=\"margin:0;display:inline\"><b>&#34;"
							+ actualAutoMatchingAttribute + "&#34;</b></pre>");
				}
			}

		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying attributes in LTI Toolgateway Response.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}

	/**
	 * To get subscribed program Names for the given user using license service API
	 * 
	 * @param orgId   - organization Id
	 * @param rumbaId - Rumba user Id
	 * @return - List of programs that the users subscribed
	 */
	public static List<String> getSubscribedProgramsForGivenUser(String orgId, String rumbaId) {
		List<String> programsSubscribed = new ArrayList<String>();
		Response response = RBSAPIUtils.getUserSubscriptions(orgId, rumbaId);
		try {
			if (response.getStatusCode() == 200) {
				JSONArray arrResponsejson = new JSONArray(response.getBody().asString());
				for (int i = 0; i < arrResponsejson.length(); i++) {
					if (arrResponsejson.getJSONObject(i).has("productDisplayName")) {
						String productName = arrResponsejson.getJSONObject(i).getString("productDisplayName");
						if (!programsSubscribed.contains(productName)) {
							programsSubscribed.add(productName);
						}
					} else {
						Log.event("'productDisplayName' field not found in item response");
					}
				}
			} else {
				throw new Exception("User Subscription API returned with error code: " + response.getStatusCode());
			}
		} catch (Exception err) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Unable to get subscribed products for given rumba user. Error:"
					+ RealizeUtils.FAIL_HTML_END + err.getMessage());
		}
		return programsSubscribed;
	}

	/**
	 * To verify given product names are added to given user
	 * 
	 * @param orgId        - organization Id
	 * @param rumbaId      - Rumba user Id
	 * @param programNames - Program names to verify
	 * @return true, if given programs are subscribed to user
	 */
	public static boolean verifyGivenProductsAreAddedToUser(String orgId, String rumbaId, List<String> programNames) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		List<String> programsSubscribed =  getSubscribedProgramsForGivenUser(orgId, rumbaId);
		for (String program : programNames) {
			if (programsSubscribed.contains(program)) {
				status.add(true);
				Log.event(program + " is subscribed to user " + rumbaId);
			} else {
				status.add(false);
				Log.event(program + " is not subscribed to user " + rumbaId);
			}
		}
		if (programNames.size() != programsSubscribed.size()) {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN
					+ "Number of subscribed programs are not matched. Expected program count: " + programNames.size()
					+ ", Actual program count: " + programsSubscribed.size() + RealizeUtils.FAIL_HTML_END);
			status.add(false);
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To get jwks details for the given keyId from the jwks url
	 * @param keySetUrl
	 * @param keyId
	 * @return HashMap<String, String>
	 * @throws Exception
	 */
	public static HashMap<String, String> getGivenKeyDetailsFromKeySetUrl(String keySetUrl, String keyId) throws Exception {
		HashMap<String, String> keyMap = new HashMap<String, String>();
		try {
			JSONObject jwksJson = RealizeUtils.getJsonFromURL(keySetUrl);
			
			int jsonIndex = -1;
			if (keyId != null && !keyId.isEmpty() ) {
				jsonIndex = IntStream.range(0, jwksJson.getJSONArray("keys").length())
					.filter(i -> jwksJson.getJSONArray("keys").getJSONObject(i)
							.getString("kid").equals(keyId)).findFirst().orElse(-1);
			} else {
				jsonIndex = 0;
				Log.event("No keyId given in the argument, hence getting the first keyset");
			}
			
			if (jsonIndex == -1)
				throw new Exception("Unable to find given KeyId '" + keyId + "' from the keyset URL: " + keySetUrl);

			JSONObject jsonKey = jwksJson.getJSONArray("keys").getJSONObject(jsonIndex);
			for (String key : jsonKey.keySet()) {
				keyMap.put(key, jsonKey.getString(key));
			}
		} catch (Exception err) {
			throw new Exception("Error in getting jwks details: " + err.getMessage());
		}
		return keyMap;
	}
	
	/**
	 * To get the modified JSONObject for LTIA Assignment API
	 * 
	 * @param jsonObject
	 * @param teacherUuid
	 * @param contentUuid
	 * @param contentVersion
	 * @param assignmentName
	 * @param startDate
	 * @param dueDate
	 * @param classId
	 * @return modified JSONObject
	 */
	public static JSONObject getModifiedJSONForLTIAAssignmentAPI(JSONObject jsonObject, String teacherUuid, String contentUuid, String contentVersion, String assignmentName, String startDate, String dueDate, String classId) {		
		JSONObject assignmentDetails = jsonObject.getJSONObject("assignment");
		JSONObject classDetails = jsonObject.getJSONArray("newAssignedTo").getJSONObject(0);
		assignmentDetails.put("teacherUuid", teacherUuid);
		assignmentDetails.put("itemUuid", contentUuid);
		assignmentDetails.put("itemVersion", contentVersion);
		assignmentDetails.put("title", assignmentName);
		assignmentDetails.put("dueDate", dueDate);
		assignmentDetails.put("startDate", startDate);
		classDetails.put("classUuid", classId);
		return jsonObject;
	}
	
	/**
	 * To get class provisioning service payload for the given attributes
	 * @param attributeToModify
	 * @param lstMemberDetails
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getClassProvisioningServicePayload(HashMap<String, String> attributeToModify,
			List<HashMap<String, String>> lstMemberDetails) throws Exception {

		String resourceLinkField = configProperty.getProperty("lti.claim.resourceLink").trim();
		String customClaimField = configProperty.getProperty("lti.claim.custom").trim();
		String jsonFileName = "./src/main/resources/import_files/LTI-A/apsPayload.json";
		JSONObject apsPayload = RealizeUtils.getJsonFromFile(jsonFileName);

		if (attributeToModify.containsKey("platformId") && attributeToModify.get("platformId") != null) {
			apsPayload = new JSONObject(apsPayload.toString().replaceAll("PLATFORM_ID", attributeToModify.get("platformId")));
		}

		if (attributeToModify.containsKey("contextsId") && attributeToModify.get("contextsId") != null) {
			apsPayload = new JSONObject(apsPayload.toString().replaceAll("CONTEXTS_ID", attributeToModify.get("contextsId")));
		}

		JSONObject resourceLinkJson = apsPayload.getJSONObject("resourceLinkLaunchRequest");
		if (attributeToModify.containsKey("resourceId") && attributeToModify.get("resourceId") != null) {
			resourceLinkJson.getJSONObject(resourceLinkField).put("id", attributeToModify.get("resourceId"));
		}
		
		if (attributeToModify.containsKey("resourceTitle") && attributeToModify.get("resourceTitle") != null) {
			resourceLinkJson.getJSONObject(resourceLinkField).put("title", attributeToModify.get("resourceTitle"));
		}
		
		if (attributeToModify.containsKey("contentId") && attributeToModify.get("contentId") != null) {
			resourceLinkJson.getJSONObject(customClaimField).put("contentId", attributeToModify.get("contentId"));
		}
		
		if (attributeToModify.containsKey("productId") && attributeToModify.get("productId") != null) {
			resourceLinkJson.getJSONObject(customClaimField).put("productId", attributeToModify.get("productId"));
		}
		
		JSONObject provisioningDataJson = apsPayload.getJSONObject("provisioningData");
		if (attributeToModify.containsKey("userId") && attributeToModify.get("userId") != null) {
			provisioningDataJson.put("toolUserId", attributeToModify.get("userId"));
		}
		
		if (attributeToModify.containsKey("sectionId") && attributeToModify.get("sectionId") != null) {
			provisioningDataJson.getJSONObject("roster").put("toolSectionId", attributeToModify.get("sectionId"));
		}

		if (attributeToModify.containsKey("externalClassId") && attributeToModify.get("externalClassId") != null) {
			provisioningDataJson.getJSONObject("roster").put("externalClassId",
					attributeToModify.get("externalClassId"));
		}

		if (attributeToModify.containsKey("orgId") && attributeToModify.get("orgId") != null) {
			provisioningDataJson.put("organizationId", attributeToModify.get("orgId"));
		}

		JSONArray membersJsonArray = new JSONArray();
		if (!lstMemberDetails.isEmpty()) {
			for (HashMap<String, String> memberAttribute : lstMemberDetails) {
				JSONObject memberJson = new JSONObject();
				
				if (memberAttribute.containsKey("userId") && memberAttribute.get("userId") != null) {
					memberJson.put("userId", memberAttribute.get("userId"));
				}
				
				if (memberAttribute.containsKey("toolUserId") && memberAttribute.get("toolUserId") != null) {
					memberJson.put("toolUserId", memberAttribute.get("toolUserId"));
				}
				
				if (memberAttribute.containsKey("role") && memberAttribute.get("role") != null) {
					memberJson.put("role", memberAttribute.get("role"));
				}
				membersJsonArray.put(memberJson);
			}
		}
		provisioningDataJson.put("members", membersJsonArray);
		
		return apsPayload;
	}
	
	/**
	 * To get JSONObject from the given file
	 * @param fileName
	 * @return - JSONObject
	 * @throws Exception
	 */
	public static JSONObject getJsonFromFile(String fileName) throws Exception {
		JSONObject jsonData = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(fileName));
			org.json.simple.JSONObject jsonRequest = (org.json.simple.JSONObject) obj;
			jsonData = new JSONObject(jsonRequest.toJSONString());
		} catch (Exception e) {
			throw new Exception("Error while reading json file " + e.getMessage());
		}
	    return jsonData;
	}

	/**
	 * To verify the ResourceLinkLaunchRequest in APS Response with the given attribute
	 * 
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyResourceLinkLaunchRequestInAPSResponse(JSONObject JsonResponseBody,
			HashMap<String, String> attributeToVerify) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<br><u>Verifying ResourceLinkLaunchRequest attributes in APS responses as below:</u>");
			
			int thresholdTime = 10;	//in Seconds
			JsonResponseBody = JsonResponseBody.getJSONObject("resourceLinkLaunchRequest");
			if (attributeToVerify.containsKey("rumbaUserId") && attributeToVerify.get("rumbaUserId") != null) {
				String actualUserId = JsonResponseBody.get("rumbaUserId").toString();
				String expectedUserId = attributeToVerify.get("rumbaUserId").trim();
				if (actualUserId.equals(expectedUserId)) {
					status.add(true);
					Log.message("Verified: Rumba id is matched: <b>" + actualUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Rumba id is not matched. Expected rumba user id: <b>" + expectedUserId
							+ "</b>, Actual rumba user id: <b>" + actualUserId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("role") && attributeToVerify.get("role") != null) {
				String actualRole = JsonResponseBody.get("rumbaRole").toString();
				String expectedRole = attributeToVerify.get("role").trim();
				if (actualRole.equals(expectedRole)) {
					status.add(true);
					Log.message("Verified: Rumba role is matched: <b>" + actualRole + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Rumba role is not matched. Expected rumba role: <b>" + expectedRole
							+ "</b>, Actual rumba role: <b>" + actualRole + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("issuerId") && attributeToVerify.get("issuerId") != null) {
				String actualIssuerId = JsonResponseBody.get("iss").toString();
				String expectedIssuerId = attributeToVerify.get("issuerId").trim();
				if (actualIssuerId.equals(expectedIssuerId)) {
					status.add(true);
					Log.message("Verified: IssuerId is matched: <b>" + actualIssuerId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: IssuerId is not matched. Expected IssuerId: <b>" + expectedIssuerId
							+ "</b>, Actual IssuerId: <b>" + actualIssuerId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("clientId") && attributeToVerify.get("clientId") != null) {
				String actualClientId = JsonResponseBody.getJSONArray("aud").getString(0);
				String expectedClientId = attributeToVerify.get("clientId").trim();
				if (actualClientId.equals(expectedClientId)) {
					status.add(true);
					Log.message("Verified: ClientId is matched: <b>" + actualClientId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: ClientId not matched. Expected ClientId: <b>" + expectedClientId
							+ "</b>, Actual ClientId: <b>" + actualClientId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			// Verify iat value is having given value within threshold
			if (attributeToVerify.containsKey("iat") && attributeToVerify.get("iat") != null) {
				long expected_iat = new BigDecimal(attributeToVerify.get("iat").toString()).longValue();
				long actual_iat = new BigDecimal(JsonResponseBody.get("iat").toString()).longValue();
				if (Math.abs(expected_iat - actual_iat) <= thresholdTime) {
					status.add(true);
					Log.message("Verified: 'iat' is matched with epoch time when user loads assignment");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: 'iat' is not matched, Expected: <b>"
							+ expected_iat + ", Actual: <b>" + actual_iat + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			String messageTypeProperty = configProperty.getProperty("lti.claim.messageType").trim();
			String actualMsgType = JsonResponseBody.get(messageTypeProperty).toString();
			if (actualMsgType.equals("LtiResourceLinkRequest")) {
				status.add(true);
				Log.message("Verified: " + messageTypeProperty + " is <b>LtiResourceLinkRequest</b>");
			} else {
				status.add(false);
				Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: " + messageTypeProperty
						+ " is <b>LtiResourceLinkRequest</b>, Actual: <b>" + actualMsgType + "</b>"
						+ RealizeUtils.FAIL_HTML_END);
			}
			
			if (attributeToVerify.containsKey("orgId") && attributeToVerify.get("orgId") != null) {
				String actualOrgId = JsonResponseBody.get("organizationId").toString();
				String expectedOrgId = attributeToVerify.get("orgId").trim();
				if (actualOrgId.equals(expectedOrgId)) {
					status.add(true);
					Log.message("Verified: organizationId is matched: <b>" + actualOrgId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: organizationId is not matched. Expected organizationId: <b>" + expectedOrgId
							+ "</b>, Actual organizationId: <b>" + actualOrgId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("sub") && attributeToVerify.get("sub") != null) {
				String actualSub = JsonResponseBody.get("sub").toString();
				String expectedSub = attributeToVerify.get("sub").trim();
				if (actualSub.equals(expectedSub)) {
					status.add(true);
					Log.message("Verified: sub is matched: <b>" + actualSub + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: sub is not matched. Expected sub: <b>" + expectedSub
							+ "</b>, Actual sub: <b>" + actualSub + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("contextId") && attributeToVerify.get("contextId") != null) {
				String contextProperty = configProperty.getProperty("lti.claim.context").trim();
				String actualContextId = JsonResponseBody.getJSONObject(contextProperty).get("id").toString();
				String expectedContextId = attributeToVerify.get("contextId").trim();
				if (actualContextId.equals(expectedContextId)) {
					status.add(true);
					Log.message("Verified: Id in " + contextProperty + " is matched: <b>" + actualContextId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Id in " + contextProperty + " is not matched. Expected contextId: <b>" + expectedContextId
							+ "</b>, Actual contextId: <b>" + actualContextId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("contextTitle") && attributeToVerify.get("contextTitle") != null) {
				String contextProperty = configProperty.getProperty("lti.claim.context").trim();
				String actualContextTitle = JsonResponseBody.getJSONObject(contextProperty).get("title").toString();
				String expectedContextTitle = attributeToVerify.get("contextTitle").trim();
				if (actualContextTitle.equals(expectedContextTitle)) {
					status.add(true);
					Log.message("Verified: title in " + contextProperty + " is matched: <b>" + actualContextTitle + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: title in " + contextProperty + " is not matched. Expected contextTitle: <b>" + expectedContextTitle
							+ "</b>, Actual contextTitle: <b>" + actualContextTitle + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("contentId") && attributeToVerify.get("contentId") != null &&
					attributeToVerify.containsKey("productId") && attributeToVerify.get("productId") != null) {
				String expectedContentId = attributeToVerify.get("contentId").trim();
				String expectedProductId = attributeToVerify.get("productId").trim();
				
				
				String customProperty = configProperty.getProperty("lti.claim.custom").trim();
				String actualContentId = JsonResponseBody.getJSONObject(customProperty).get("contentId").toString();
				if (actualContentId.equals(expectedContentId)) {
					status.add(true);
					Log.message("Verified: contentId in " + customProperty + " is matched: <b>" + actualContentId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: contentId in " + customProperty + " is not matched. Expected contentId: <b>" + expectedContentId
							+ "</b>, Actual contentId: <b>" + actualContentId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
				
				String actualProductId = JsonResponseBody.getJSONObject(customProperty).get("productId").toString();
				if (actualProductId.equals(expectedProductId)) {
					status.add(true);
					Log.message("Verified: productId in " + customProperty + " is matched: <b>" + actualProductId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: productId in " + customProperty + " is not matched. Expected productId: <b>" + expectedProductId
							+ "</b>, Actual productId: <b>" + actualProductId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}

				
				String targetLinkProperty = configProperty.getProperty("lti.claim.targetLinkUri").trim();
				String launchEndpoint = configProperty.getProperty("lti.tool.launch.endpoint").trim();
				String ltiBaseUrl = configProperty.getProperty("lti.tool.gateway.url").trim();
				String expectedTargetLinkUri = ltiBaseUrl + String.format(launchEndpoint, expectedContentId, expectedProductId);
				String actualTargetLinkUri = JsonResponseBody.get(targetLinkProperty).toString();
				if (actualTargetLinkUri.equals(expectedTargetLinkUri)) {
					status.add(true);
					Log.message("Verified: " + targetLinkProperty + " is matched: <b>" + actualTargetLinkUri + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: " + targetLinkProperty
							+ " is not matched. Expected: <b>" + expectedTargetLinkUri + "</b>, Actual: <b>"
							+ actualTargetLinkUri + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			

			if (attributeToVerify.containsKey("courseId") && attributeToVerify.get("courseId") != null) {
				String canvasBaseUrl = configProperty.getProperty("canvas.savvas.base.url").trim();
				String launchPresentProperty = configProperty.getProperty("lti.claim.launchPresentation").trim();
				String canvasAssignmentUrl = canvasBaseUrl + CANVAS_ASSIGNMENT_ENDPOINT;
				String actualReturnUrl = JsonResponseBody.getJSONObject(launchPresentProperty).get("return_url").toString();
				String expectedReturnUrl = String.format(canvasAssignmentUrl, attributeToVerify.get("courseId").trim());
				if (actualReturnUrl.equals(expectedReturnUrl)) {
					status.add(true);
					Log.message("Verified: return_url in " + launchPresentProperty + " is matched: <b>" + actualReturnUrl + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: return_url in " + launchPresentProperty + " is not matched. Expected contextId: <b>" + expectedReturnUrl
							+ "</b>, Actual contextId: <b>" + actualReturnUrl + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("resourceTitle") && attributeToVerify.get("resourceTitle") != null) {
				String resourceLinkProperty = configProperty.getProperty("lti.claim.resourceLink").trim();
				String actualResourceTitle = JsonResponseBody.getJSONObject(resourceLinkProperty).get("title").toString();
				String expectedResourceTitle = attributeToVerify.get("resourceTitle").trim();
				if (actualResourceTitle.equals(expectedResourceTitle)) {
					status.add(true);
					Log.message("Verified: resource title in " + resourceLinkProperty + " is matched: <b>" + actualResourceTitle + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: resource title in " + resourceLinkProperty + " is not matched. Expected: <b>" + expectedResourceTitle
							+ "</b>, Actual: <b>" + actualResourceTitle + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying ResourceLinkLaunchRequest attributes in APS Response.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To verify the ProvisioningData in APS Response with the given attribute
	 * 
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyProvisioningDataInAPSResponse(JSONObject JsonResponseBody,
			HashMap<String, String> attributeToVerify, List<HashMap<String, String>> lstMemberDetails) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<br><u>Verifying provisioning data attributes in APS responses as below:</u>");
			JsonResponseBody = JsonResponseBody.getJSONObject("provisioningData");
			
			if (attributeToVerify.containsKey("issuerId") && attributeToVerify.get("issuerId") != null) {
				String actualIssuerId = JsonResponseBody.get("iss").toString();
				String expectedIssuerId = attributeToVerify.get("issuerId").trim();
				if (actualIssuerId.equals(expectedIssuerId)) {
					status.add(true);
					Log.message("Verified: IssuerId is matched: <b>" + actualIssuerId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: IssuerId is not matched. Expected IssuerId: <b>" + expectedIssuerId
							+ "</b>, Actual IssuerId: <b>" + actualIssuerId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("sub") && attributeToVerify.get("sub") != null) {
				String actualSub = JsonResponseBody.get("sub").toString();
				String expectedSub = attributeToVerify.get("sub").trim();
				if (actualSub.equals(expectedSub)) {
					status.add(true);
					Log.message("Verified: sub is matched: <b>" + actualSub + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: sub is not matched. Expected sub: <b>" + expectedSub
							+ "</b>, Actual sub: <b>" + actualSub + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("role") && attributeToVerify.get("role") != null) {
				String actualRole = JsonResponseBody.get("role").toString();
				String expectedRole = attributeToVerify.get("role").trim();
				if (actualRole.equals(expectedRole)) {
					status.add(true);
					Log.message("Verified: Role is matched: <b>" + actualRole + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Role is not matched. Expected rumba role: <b>" + expectedRole
							+ "</b>, Actual role: <b>" + actualRole + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("rumbaUserId") && attributeToVerify.get("rumbaUserId") != null) {
				String actualUserId = JsonResponseBody.get("toolUserId").toString();
				String expectedUserId = attributeToVerify.get("rumbaUserId").trim();
				if (actualUserId.equals(expectedUserId)) {
					status.add(true);
					Log.message("Verified: toolUserId is matched: <b>" + actualUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: toolUserId is not matched. Expected: <b>"
							+ expectedUserId + "</b>, Actual: <b>" + actualUserId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("rosterId") && attributeToVerify.get("rosterId") != null) {
				String actualRosterId = JsonResponseBody.getJSONObject("roster").get("id").toString();
				String expectedRosterId = attributeToVerify.get("rosterId").trim();
				if (actualRosterId.equals(expectedRosterId)) {
					status.add(true);
					Log.message("Verified: rosterId is matched: <b>" + actualRosterId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: rosterId is not matched. Expected: <b>"
							+ expectedRosterId + "</b>, Actual: <b>" + actualRosterId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("rosterName") && attributeToVerify.get("rosterName") != null) {
				String actualRosterName = JsonResponseBody.getJSONObject("roster").get("name").toString();
				String expectedRosterName = attributeToVerify.get("rosterName").trim();
				if (actualRosterName.equals(expectedRosterName)) {
					status.add(true);
					Log.message("Verified: rosterName is matched: <b>" + actualRosterName + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: rosterName is not matched. Expected: <b>"
							+ expectedRosterName + "</b>, Actual: <b>" + actualRosterName + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("classId") && attributeToVerify.get("classId") != null) {
				String actualClassId = JsonResponseBody.getJSONObject("roster").get("toolSectionId").toString();
				String expectedClassId = attributeToVerify.get("classId").trim();
				if (actualClassId.equals(expectedClassId)) {
					status.add(true);
					Log.message("Verified: toolSectionId is matched: <b>" + actualClassId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: toolSectionId is not matched. Expected: <b>"
							+ expectedClassId + "</b>, Actual: <b>" + actualClassId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("externalClassId") && attributeToVerify.get("externalClassId") != null) {
				String actualClassId = JsonResponseBody.getJSONObject("roster").get("externalClassId").toString();
				String expectedClassId = attributeToVerify.get("externalClassId").trim();
				if (actualClassId.equals(expectedClassId)) {
					status.add(true);
					Log.message("Verified: externalClassId is matched: <b>" + actualClassId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: externalClassId is not matched. Expected: <b>"
							+ expectedClassId + "</b>, Actual: <b>" + actualClassId + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("orgId") && attributeToVerify.get("orgId") != null) {
				String actualOrgId = JsonResponseBody.get("organizationId").toString();
				String expectedOrgId = attributeToVerify.get("orgId").trim();
				if (actualOrgId.equals(expectedOrgId)) {
					status.add(true);
					Log.message("Verified: organizationId is matched: <b>" + actualOrgId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: organizationId is not matched. Expected organizationId: <b>" + expectedOrgId
							+ "</b>, Actual organizationId: <b>" + actualOrgId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("externalSystem") && attributeToVerify.get("externalSystem") != null) {
				String actualSystem = JsonResponseBody.get("externalSystem").toString();
				String externalSystem = attributeToVerify.get("externalSystem");
				if (actualSystem.equals(externalSystem)) {
					status.add(true);
					Log.message("Verified: External System is matched: <b>" + actualSystem + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: External System is not matched, Expected: <b>"
							+ externalSystem + "</b>, Actual: <b>" + actualSystem + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("staffPiId") && attributeToVerify.get("staffPiId") != null) {
				JSONObject existingTeacherJson = JsonResponseBody.getJSONObject("roster").getJSONArray("existingTeacherIds").getJSONObject(0);
				String actualStaffId = existingTeacherJson.get("staffPiId").toString();
				String staffPiId = attributeToVerify.get("staffPiId");
				if (actualStaffId.equals(staffPiId)) {
					status.add(true);
					Log.message("Verified: staffPiId is matched: <b>" + actualStaffId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: staffPiId is not matched. Expected staffPiId: <b>" + staffPiId
							+ "</b>, Actual staffPiId: <b>" + actualStaffId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
				
				String teacherOfRecord = existingTeacherJson.get("teacherOfRecord").toString();
				if (teacherOfRecord.equals("false")) {
					status.add(true);
					Log.message("Verified: teacherOfRecord is <b>'false'</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "teacherOfRecord is not false, Actual: <b>"
							+ teacherOfRecord + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
				
				String teacherAssignment = existingTeacherJson.get("teacherAssignment").toString();
				if (teacherAssignment.equals("Lead Teacher")) {
					status.add(true);
					Log.message("Verified: teacherAssignment is <b>'Lead Teacher'</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "teacherAssignment is not matched. Expected: Lead Teacher, Actual: <b>"
							+ teacherAssignment + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			status.add(verifyMemberDetails(JsonResponseBody, lstMemberDetails));
		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying provisioning data attributes in APS Response.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
	
	/**
	 * To verify the user mapping in UMS Response with the given attribute
	 * 
	 * @param JsonResponseBody
	 * @param attributeToVerify
	 * @return - true if all the attributes are matched else return false
	 */
	public static boolean verifyGivenAttributesInUserMappingResponse(JSONObject JsonResponseBody, HashMap<String, String> attributeToVerify) {
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			Log.message("<br><u>Verifying user mapping responses as below:</u>");
			if (attributeToVerify.containsKey("userId") && attributeToVerify.get("userId") != null) {
				String actualUserId = JsonResponseBody.get("userId").toString();
				String userId = attributeToVerify.get("userId");
				if (actualUserId.equals(userId)) {
					status.add(true);
					Log.message("Verified: User id is matched: <b>" + actualUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: User id is not matched. Expected User id: <b>" + userId
							+ "</b>, Actual User id: <b>" + actualUserId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("userRole") && attributeToVerify.get("userRole") != null) {
				String actualUserRole = JsonResponseBody.get("userRole").toString();
				String userRole = attributeToVerify.get("userRole");
				if (actualUserRole.equalsIgnoreCase(userRole)) {
					status.add(true);
					Log.message("Verified: User role is matched: <b>" + actualUserRole + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: User role is not matched. Expected User role: <b>" + userRole
							+ "</b>, Actual User role: <b>" + actualUserRole + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("integrationId") && attributeToVerify.get("integrationId") != null) {
				String actualIntegrationId = JsonResponseBody.get("integrationId").toString();
				String integrationId = attributeToVerify.get("integrationId");
				if (actualIntegrationId.equals(integrationId)) {
					status.add(true);
					Log.message("Verified: Integration Id is matched: <b>" + actualIntegrationId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Integration Id is not matched. Expected Integration Id: <b>" + integrationId
							+ "</b>, Actual Integration Id: <b>" + actualIntegrationId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("integrationType") && attributeToVerify.get("integrationType") != null) {
				String actualIntegrationType = JsonResponseBody.get("integrationType").toString();
				String integrationType = attributeToVerify.get("integrationType");
				if (actualIntegrationType.equals(integrationType)) {
					status.add(true);
					Log.message("Verified: Integration Type is matched: <b>" + actualIntegrationType + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Integration Typeis not matched. Expected Integration Type: <b>" + integrationType
							+ "</b>, Actual Integration Type: <b>" + actualIntegrationType + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("externalUserId") && attributeToVerify.get("externalUserId") != null) {
				String actualExternalUserId = JsonResponseBody.get("externalUserId").toString();
				String externalUserId = attributeToVerify.get("externalUserId");
				if (actualExternalUserId.equalsIgnoreCase(externalUserId)) {
					status.add(true);
					Log.message("Verified: External UserId is matched: <b>" + actualExternalUserId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: External UserId is not matched. Expected External UserId: <b>" + externalUserId
							+ "</b>, Actual External UserId: <b>" + actualExternalUserId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("externalSisId") && attributeToVerify.get("externalSisId") != null) {
				String actualExternalSisId = JsonResponseBody.get("externalSisId").toString();
				String externalSisId = attributeToVerify.get("externalSisId");
				if (actualExternalSisId.equalsIgnoreCase(externalSisId)) {
					status.add(true);
					Log.message("Verified: External SisId is matched: <b>" + actualExternalSisId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: External SisId is not matched. Expected External SisId: <b>" + externalSisId
							+ "</b>, Actual External SisId: <b>" + actualExternalSisId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
			
			if (attributeToVerify.containsKey("userFederatedId") && attributeToVerify.get("userFederatedId") != null) {
				String actualFederatedId = JsonResponseBody.get("userFederatedId").toString();
				String expectedFederatedId = attributeToVerify.get("userFederatedId");
				if (actualFederatedId.equalsIgnoreCase(expectedFederatedId)) {
					status.add(true);
					Log.message("Verified: Federated Id is matched: <b>" + actualFederatedId + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: Federated Id is not matched. Expected Federated Id: <b>" + expectedFederatedId
							+ "</b>, Actual Federated Id: <b>" + actualFederatedId + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}

			if (attributeToVerify.containsKey("modifiedDate") && attributeToVerify.get("modifiedDate") != null) {
				ZonedDateTime expectedDateTime = null, actualDateTime = null;
				String expectedModifiedDate = attributeToVerify.get("modifiedDate").toString().trim();
				String actualModifiedDate = JsonResponseBody.get("modifiedDate").toString().trim();

				try {
					actualDateTime = ZonedDateTime.ofInstant(Instant.parse(actualModifiedDate), ZoneId.of("UTC"));
				} catch (Exception e1) {
					Log.event("Error while parsing UMS modified date. Exception: " + e1.getMessage());
					int length = actualModifiedDate.substring(actualModifiedDate.indexOf(".") + 1).replaceAll("Z", "").length();
					if (length == 3)
						actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_FORMAT);
					else if (length == 6)
						actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_FORMAT2);
					else if (length == 9)
						actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_FORMAT3);
					else
						Log.event("UMS modified date is not in ISO-8601 instant format with 0, 3, 6 or 9 digit nano-of-second."
										+ " Date: " + actualModifiedDate);
				}

				try {
					expectedDateTime = ZonedDateTime.ofInstant(Instant.parse(expectedModifiedDate), ZoneId.of("UTC"));
				} catch (Exception e2) {
					Log.event("Error while parsing given modified date. Exception: " + e2.getMessage());
					int length = actualModifiedDate.substring(expectedModifiedDate.indexOf(".") + 1).replaceAll("Z", "").length();
					if (length == 3)
						expectedDateTime = ZonedDateTime.parse(expectedModifiedDate, UTC_FORMAT);
					else if (length == 6)
						expectedDateTime = ZonedDateTime.parse(expectedModifiedDate, UTC_FORMAT2);
					else if (length == 9)
						expectedDateTime = ZonedDateTime.parse(expectedModifiedDate, UTC_FORMAT3);
					else
						Log.event("Expected modified date is not in ISO-8601 instant format with 0, 3, 6 or 9 digit nano-of-second."
										+ " Date: " + expectedModifiedDate);
				}

				// Added this verification to verify modifiedDate field values by +120 or -120 seconds
				if (Math.abs(Duration.between(expectedDateTime, actualDateTime).getSeconds()) <= 210) {
					status.add(true);
					Log.message("Verified: Modified date is matched: <b>" + actualModifiedDate + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed: modifiedDate is not matched. Expected: <b>"
							+ expectedModifiedDate + "</b>, Actual: <b>" + actualModifiedDate + "</b>"
							+ RealizeUtils.FAIL_HTML_END);
				}
			} 
			
			if (attributeToVerify.containsKey("updatedBy") && attributeToVerify.get("updatedBy") != null) {
				String actualupdatedBy = JsonResponseBody.get("updatedBy").toString();
				String updatedBy = attributeToVerify.get("updatedBy");
				if (actualupdatedBy.equals(updatedBy)) {
					status.add(true);
					Log.message("Verified: UpdatedBy is matched: <b>" + actualupdatedBy + "</b>");
				} else {
					status.add(false);
					Log.message(RealizeUtils.FAIL_HTML_BEGIN
							+ "Failed: UpdatedBy is not matched. Expected UpdatedBy: <b>" + updatedBy
							+ "</b>, Actual UpdatedBy: <b>" + actualupdatedBy + "</b>" + RealizeUtils.FAIL_HTML_END);
				}
			}
		} catch (Exception err) {
			status.add(false);
			Log.message("Error in verifying attributes in UMS mapping Response.." + err.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}

	/**
	 * To get content Id from the given content url
	 * @param contentUrl
	 * @return
	 */
	public static String getContentIdFromUrl(String contentUrl) {
		return contentUrl.split("content/")[1].split("\\?")[0];
	}
	
	/**
	 * To get product Id from the given content url
	 * @param contentUrl
	 * @return
	 */
	public static String getProductIdFromUrl(String contentUrl) {
		return contentUrl.split("\\?")[1].split("=")[1];
	}
	
	public static boolean verifyBlankPageLoadedForGivenUrl(WebDriver driver, String urlToLaunch) {
		boolean status = false;
		driver.get(urlToLaunch);
		(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(500))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
				.withMessage("Page document is not loading")).until(WebDriverFactory.documentLoad);
		if (driver.findElement(By.tagName("body")).getText().toString().trim().equals("")) {
			status = true;
			Log.event(urlToLaunch + " is loaded with blank page");
		} else {
			Log.event(urlToLaunch + " is not loaded with blank page");
		}
		return status;
	}

	/**
	 * To verify given assignment dates are same
	 * 
	 * @param canvasDate      - date from canvas assignment
	 * @param canvasTimeZone  - IANA TimeZone of canvas
	 * @param realizeDate     - date from realize
	 * @param realizeTimeZone - IANA TimeZone of realize
	 * @return - true if both dates are same
	 */
	public static boolean verifyAssignmentDatesAreSame(String canvasDate, String canvasTimeZone, String realizeDate,
			String realizeTimeZone) {
		boolean status = false;
		Log.event("Comparing assignment date from canvas with realize:");
		try {
			long timeoutInSeconds = 90;
			Instant canvasDateTime = null;
			ZoneId canvasZone = ZoneId.of(canvasTimeZone);
			if (canvasDate.contains(":")) {
				canvasDateTime = ZonedDateTime
						.parse(canvasDate, CanvasCoursePage.ASSIGNMENT_DATETIME_FORMAT.withZone(canvasZone)).toInstant();
			} else {
				canvasDateTime = LocalDate.parse(canvasDate, CanvasCoursePage.ASSIGNMENT_DATE_FORMAT)
						.atStartOfDay(canvasZone).toInstant();
			}

			// Convert given dates to UTC time and save it as Epoch time
			Instant realizeDateTime = RealizeUtils.parseAssignmentDate(realizeDate, realizeTimeZone);
			Long timeDifference = Math.abs(Duration.between(canvasDateTime, realizeDateTime).getSeconds());
			if (timeDifference < timeoutInSeconds) {
				status = true;
			} else {
				// To fix timezone difference
				if (canvasTimeZone.equalsIgnoreCase("UTC")) {
					canvasZone = ZoneId.of(CANVAS_DEFAULT_TIMEZONE);
				}

				if (canvasZone.getRules().isDaylightSavings(canvasDateTime)) {
					timeoutInSeconds = Math.abs(canvasZone.getRules().getDaylightSavings(canvasDateTime).getSeconds());
				}
				// Verify both dates are almost same after DST
				status = (Math.abs(Duration.between(canvasDateTime, realizeDateTime).getSeconds()) <= timeoutInSeconds);
			}
			Log.event("Canvas TimeZone: " + canvasTimeZone + ", Realize TimeZone: " + realizeTimeZone);
			Log.event("Canvas Time: " + canvasDateTime + ", Realize Time: " + realizeDateTime);
		} catch (Exception e) {
			Log.message("Error in comparing assignment dates: " + e.getMessage());
		}
		return status;
	}

	/**
	 * To verify given assignment dates are same
	 * 
	 * @param schoologyDate      - date from schoology assignment
	 * @param schoologyTimeZone  - IANA TimeZone of canvas
	 * @param realizeDate     - date from realize
	 * @param realizeTimeZone - IANA TimeZone of realize
	 * @return - true if both dates are same
	 */
	public static boolean verifyAssignmentDatesAreSameInSchoology(String schoologyDate, String schoologyTimeZone,
			String realizeDate, String realizeTimeZone) {
		boolean status = false;
		Log.event("Comparing assignment date from schoology with realize:");
		try {
			long timeoutInSeconds = 90;
			ZoneId schoologyZone = ZoneId.of(schoologyTimeZone);
			Instant schoologyDateTime = ZonedDateTime
					.parse(schoologyDate, SchoologyCoursePage.ASSIGNMENT_DATETIME_FORMAT.withZone(schoologyZone))
					.toInstant();

			// Convert given dates to UTC time and save it as Epoch time
			Instant realizeDateTime = RealizeUtils.parseAssignmentDate(realizeDate, realizeTimeZone);
			Long timeDifference = Math.abs(Duration.between(schoologyDateTime, realizeDateTime).getSeconds());
			if (timeDifference < timeoutInSeconds) {
				status = true;
			} else {
				// To fix timezone difference
				if (schoologyTimeZone.equalsIgnoreCase("UTC")) {
					schoologyZone = ZoneId.of(SCHOOLOGY_DEFAULT_TIMEZONE);
				}

				if (schoologyZone.getRules().isDaylightSavings(schoologyDateTime)) {
					timeoutInSeconds = Math
							.abs(schoologyZone.getRules().getDaylightSavings(schoologyDateTime).getSeconds());
				}
				// Verify both dates are almost same after DST
				status = (Math.abs(Duration.between(schoologyDateTime, realizeDateTime).getSeconds()) <= timeoutInSeconds);
			}
			Log.event("Schoology TimeZone: " + schoologyTimeZone + ", Realize TimeZone: " + realizeTimeZone);
			Log.event("Schoology Time: " + schoologyDateTime + ", Realize Time: " + realizeDateTime);
		} catch (Exception e) {
			Log.message("Error in comparing assignment dates: " + e.getMessage());
		}
		return status;
	}
	
	/**
	 * To add given month to given dates
	 * 
	 * @param schoologyDate - Schoology date or date/time
	 * @param timeZone      - TimeZone of given schoology date
	 * @param noOfMonths    - the months to add, may be negative
	 * @return - updated date in given timezone
	 */
	public static String addMonthsToGivenDateInSchoology(String schoologyDate, String timeZone, int noOfMonths) {
		String modifiedDate = "";
		try {
			ZoneId timeZoneId = ZoneId.of(timeZone);
			DateTimeFormatter DATETIME_FORMAT = SchoologyCoursePage.ASSIGNMENT_DATETIME_FORMAT;
			Instant schoologyDateTime = ZonedDateTime.parse(schoologyDate, DATETIME_FORMAT.withZone(timeZoneId)).toInstant();

			// Add the given month
			modifiedDate = schoologyDateTime.atZone(timeZoneId).plusMonths(noOfMonths).format(DATETIME_FORMAT);
		} catch (Exception e) {
			Log.message("Error in adding a month to given dates: " + e.getMessage());
		}
		return modifiedDate;
	}

	/**
	 * To add given month to given dates
	 * 
	 * @param canvasDate - Canvas date or date/time
	 * @param timeZone   - TimeZone of given canvas date
	 * @param noOfMonths - the months to add, may be negative
	 * @return - updated date in given timezone
	 */
	public static String addMonthsToGivenDate(String canvasDate, String timeZone, int noOfMonths) {
		String modifiedDate = "";
		try {
			Instant canvasDateTime = null;
			ZoneId timeZoneId = ZoneId.of(timeZone);
			DateTimeFormatter DATETIME_FORMAT = null;
			if (canvasDate.contains(":")) {
				DATETIME_FORMAT = CanvasCoursePage.ASSIGNMENT_DATETIME_FORMAT;
				canvasDateTime = ZonedDateTime.parse(canvasDate, DATETIME_FORMAT.withZone(timeZoneId)).toInstant();
			} else {
				DATETIME_FORMAT = CanvasCoursePage.ASSIGNMENT_DATE_FORMAT;
				canvasDateTime = LocalDate.parse(canvasDate, DATETIME_FORMAT).atStartOfDay(timeZoneId).toInstant();
			}

			// Add the given month
			modifiedDate = canvasDateTime.atZone(timeZoneId).plusMonths(noOfMonths).format(DATETIME_FORMAT);
		} catch (Exception e) {
			Log.message("Error in adding a month to given dates: " + e.getMessage());
		}
		return modifiedDate;
	}

	/**
	 * To change given time to canvas timezone
	 * 
	 * @param responseDate   - Realize response date with local (UTC) format
	 * @param isConvertToUTC - true for UTC conversion
	 * @return - dateTime in MST / UTC timezone
	 */
	public static String changeDateToCanvasTimeFormat(String responseDate, Boolean isConvertToUTC) {
		String dateString = "";
		try {
			DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
					.withZone(ZoneOffset.UTC);
			DateTimeFormatter dateFormat = CanvasCoursePage.ASSIGNMENT_DATETIME_FORMAT;
			dateFormat = (isConvertToUTC) ? dateFormat.withZone(ZoneOffset.UTC)
					: dateFormat.withZone(ZoneId.of(CanvasCoursePage.CANVAS_TIMEZONE));

			dateString = ZonedDateTime.parse(responseDate, input).format(dateFormat);
		} catch (Exception e) {
			Log.message("Error in changing given date to canvas format: " + e.getMessage());
		}
		return dateString;
	}

	/**
	 * To verify given modified date for existing user is not changed recently
	 * 
	 * @param JsonResponseBody
	 * @param modifiedDate
	 * @return - true if mapping not modified recently
	 */
	public static boolean verifyUMSModifiedDateAreNotRecentlyUpdated(JSONObject JsonResponseBody) {
		Log.event("Verifying Modified date in UMS is not changed recently");
		boolean status = false;
		ZonedDateTime currentDateTime = null, actualDateTime = null;
		String actualModifiedDate = JsonResponseBody.get("modifiedDate").toString().trim();
		String currentTime = Instant.now().toString();

		try {
			currentDateTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
		} catch (Exception e2) {
			Log.event("Error while parsing current DateTime. Exception: " + e2.getMessage());
			int length = actualModifiedDate.substring(currentTime.indexOf(".") + 1).replaceAll("Z", "").length();
			if (length == 3)
				currentDateTime = ZonedDateTime.parse(currentTime, UTC_FORMAT);
			else if (length == 6)
				currentDateTime = ZonedDateTime.parse(currentTime, UTC_FORMAT2);
			else if (length == 9)
				currentDateTime = ZonedDateTime.parse(currentTime, UTC_FORMAT3);
			else
				Log.event("Current date time is not in ISO-8601 instant format with 0, 3, 6 or 9 digit nano-of-second."
						+ " Date: " + currentTime);
		}

		try {
			actualDateTime = ZonedDateTime.ofInstant(Instant.parse(actualModifiedDate), ZoneId.of("UTC"));
		} catch (Exception e1) {
			Log.event("Error while parsing UMS modified date. Exception: " + e1.getMessage());
			int length = actualModifiedDate.substring(actualModifiedDate.indexOf(".") + 1).replaceAll("Z", "").length();
			if (length == 3)
				actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_FORMAT);
			else if (length == 6)
				actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_FORMAT2);
			else if (length == 9)
				actualDateTime = ZonedDateTime.parse(actualModifiedDate, UTC_FORMAT3);
			else
				Log.event("UMS modified date is not in ISO-8601 instant format with 0, 3, 6 or 9 digit nano-of-second."
						+ " Date: " + actualModifiedDate);
		}

		// Added this verification to verify modifiedDate field values by 6hrs
		if (Math.abs(Duration.between(actualDateTime, currentDateTime).toHours()) >= 6) {
			status = true;
			Log.message("Modified date is matched: <b>" + actualModifiedDate + "</b>");
		} else {
			Log.message(RealizeUtils.FAIL_HTML_BEGIN
					+ "Failed: modifiedDate is recently changed. Expected time is 6hrs before <b>" + currentTime
					+ "</b>, Actual: <b>" + actualModifiedDate + "</b>" + RealizeUtils.FAIL_HTML_END);
		}
		return status;
	}

	/**
	 * To reset browse.profile.wizard flag for given user to false using UPS API
	 * 
	 * @param userId - A&E userId
	 * @return true if browse.profile.wizard flag set to false
	 */
	public static boolean resetBrowseProfileWizard(String userId) {
		Boolean isReset = false;
		Log.event("Reset <b>browse.profile.wizard</b> attribute to false for user: " + userId);
		try {
			JSONObject profileJson = new JSONObject("{\"browse.profile.wizard\": \"false\"}");
			isReset = RBSAPIUtils.updateUserAttributesUsingRBSToken(profileJson.toString(), userId);
		} catch (Exception e) {
			Log.message("Unable to reset browse.profile.wizard flag. Error: " + e.getMessage());
		}
		return isReset;
	}

	/**
	 * To remove the given email Id in rumba users if available
	 * 
	 * @param email - email Id of the user
	 */
	public static void removeEmailIdInRumbaUsersIfAvailable(String email) {
		Log.event("Remove given email Id in rumba Users if available: " + email);
		try {
			int MAX_LOOP = 5;
			List<String> rumbaIds = RumbaClient.getUserIdByEmail(email, false);
			while(rumbaIds != null && !rumbaIds.isEmpty() && MAX_LOOP > 0) {
				MAX_LOOP--;
				for (String rumbaId : rumbaIds) {
					String emailId = RumbaClient.getEmailIdByEmailAddress(rumbaId, email);
					if (RumbaClient.deleteEmailForUser(rumbaId, emailId)) {
						Log.message("Removed email '" + email + "' from the user " + rumbaId);
					}
				}
				rumbaIds = RumbaClient.getUserIdByEmail(email, false);
			}
		} catch (Exception e) {
			Log.message("Unable to get rumba users id for the given email: " + e.getMessage());
		}
	}
	
	/**
	 * To converts the byte in to CMAC string using the cipherKey
	 * 
	 * @param dataBytesToDigest - The data as a byte for which CMAC has to be generated
	 * @param cipherkey - The cipherkey which is used for generating the CMAC for the data
	 * @return  - the Authorization token as a String
	 */
	public static String generateCMAC(byte[] dataBytesToDigest, String cipherkey) {
		if (dataBytesToDigest == null || cipherkey == null) {
			return null;
		}
		byte[] cipherkeyByte = cipherkey.getBytes();
		byte[] macOutputByte = new byte[16];

		CMac macProvider = new CMac(new AESEngine(), 128);
		macProvider.init(new KeyParameter(cipherkeyByte));
		macProvider.update(dataBytesToDigest, 0, dataBytesToDigest.length);
		macProvider.doFinal(macOutputByte, 0);
		return new String(Hex.encodeHex(macOutputByte));
	}
	
	/**
	 * To generate Prospero Authtoken
	 * 
	 * @param dataToDigest The data for which CMAC has to be generated.
	 * @param timeStamp    The timeStamp which is concatenated as part of Auth Token creation
	 * @return  - the Authorization token as a String
	 */
	public static String generateProsperoAuthtoken(String dataToDigest, String timeStamp) {
		String principalId = configProperty.getProperty("ems.prospero.principal.id").trim();
		String cipherkey = configProperty.getProperty("ems.prospero.cipher.key").trim();
		String cipherBasedMAC = generateCMAC(dataToDigest.getBytes(), cipherkey);
		return principalId + "|" + timeStamp + "|" + cipherBasedMAC;
	}
}
