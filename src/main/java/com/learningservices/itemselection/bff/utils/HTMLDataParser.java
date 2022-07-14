package com.learningservices.itemselection.bff.utils;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.learningservices.utils.Log;

public class HTMLDataParser {
	
	private static String commonErrMsgSelector = "h1";
	private static String responseCodeSelector = "div[data-e2e-id=\"error-code-message\"]>span:nth-of-type(1)";
	private static String uniqueErrMsgSelector = "div[data-e2e-id=\"error-code-message\"]>span:nth-of-type(2)";
	private static String clientIdSelector = "form div input:nth-of-type(3)";
	private static String redirectUrlSelector = "form div input:nth-of-type(4)";
	private static String loginHintSelector = "form div input:nth-of-type(5)";
	private static String ltiMessageHintSelector = "form div input:nth-of-type(6)";
	private static String csrfTokenSelector = "form div input:nth-of-type(7)";
	private static String dlFormSelector = "form#deeplinkingResponseForm";
	private static String jwtTextboxSelector = "form>input[name=\"JWT\"]";
	
	/**
	 * To get common error message from given HTML string
	 * @param htmlString
	 * @return - common error message
	 */
	public static String getCommonErrorMessageFromScreen(String htmlString) {
		String commonErrMsg = null;
		try {
			Document html = Jsoup.parse(htmlString);
			Element codeElement = html.body().select(commonErrMsgSelector).first();
			commonErrMsg = codeElement.text().toString().trim();
		} catch (Exception e) {
			Log.message("Unable to get error message from html string. Error: " + e.getMessage());
		}
		return commonErrMsg;
	}
	
	/**
	 * To get unique error message from given HTML string
	 * @param htmlString
	 * @return - unique error message
	 */
	public static String getUniqueErrorMessageFromScreen(String htmlString) {
		String uniqueErrMsg = null;
		try {
			Document html = Jsoup.parse(htmlString);
			Element codeElement = html.body().select(uniqueErrMsgSelector).first();
			uniqueErrMsg = codeElement.text().toString().trim();
		} catch (Exception e) {
			Log.message("Unable to get error message from html string. Error: " + e.getMessage());
		}
		return uniqueErrMsg;
	}
	
	/**
	 * To get response code displayed from given HTML string
	 * @param htmlString
	 * @return - response code
	 */
	public static String getResponseCodeFromScreen(String htmlString) {
		String respCode = null;
		try {
			Document html = Jsoup.parse(htmlString);
			Element codeElement = html.body().select(responseCodeSelector).first();
			respCode = codeElement.text().toString().trim();
		} catch (Exception e) {
			Log.message("Unable to get error code from html string. Error: " + e.getMessage());
		}
		return respCode;
	}
	
	/**
	 * To get client id from given HTML string
	 * @param htmlString
	 * @return - client id
	 */
	public static String getClientIdFromPage(String htmlString) {
		String clientId = null;
		try {
			Document html = Jsoup.parse(htmlString);
			clientId = html.body().select(clientIdSelector).val();
		} catch (Exception e) {
			Log.message("Unable to get client id from html string. Error: " + e.getMessage());
		}
		return clientId;
	}
	
	/**
	 * To get redirect URL from given HTML string
	 * @param htmlString
	 * @return - redirect URL
	 */
	public static String getRedirectUrlFromPage(String htmlString) {
		String redirectUrl = null;
		try {
			Document html = Jsoup.parse(htmlString);
			redirectUrl = html.body().select(redirectUrlSelector).val();
		} catch (Exception e) {
			Log.message("Unable to get redirect URL from html string. Error: " + e.getMessage());
		}
		return redirectUrl;
	}
	
	/**
	 * To get CSRF token from given HTML string
	 * @param htmlString
	 * @return - CSRF token 
	 */
	public static String getCsrfTokenFromPage(String htmlString) {
		String csrfToken = null;
		try {
			Document html = Jsoup.parse(htmlString);
			csrfToken = html.body().select(csrfTokenSelector).val();
		} catch (Exception e) {
			Log.message("Unable to get csrf token from html string. Error: " + e.getMessage());
		}
		return csrfToken;
	}
	
	/**
	 * To get login hint from given HTML string
	 * @param htmlString
	 * @return - login hint
	 */
	public static String getLoginHintFromPage(String htmlString) {
		String loginHint = null;
		try {
			Document html = Jsoup.parse(htmlString);
			loginHint = html.body().select(loginHintSelector).val();
		} catch (Exception e) {
			Log.message("Unable to get login hint from html string. Error: " + e.getMessage());
		}
		return loginHint;
	}
	
	/**
	 * To get LTI message hint from given HTML string
	 * @param htmlString
	 * @return - LTI message hint 
	 */
	public static String getLtiMessageHintFromPage(String htmlString) {
		String ltiMsgHint = null;
		try {
			Document html = Jsoup.parse(htmlString);
			ltiMsgHint = html.body().select(ltiMessageHintSelector).val();
		} catch (Exception e) {
			Log.message("Unable to get LTI message  hint from html string. Error: " + e.getMessage());
		}
		return ltiMsgHint;
	}
	
	/**
	 * To verify given string is HTML or not
	 * @param inputString
	 * @return
	 */
	public static boolean verifyGivenStringIsHTML(String inputString) {
		String textOfHtmlString = Jsoup.parse(inputString).text();
		return !textOfHtmlString.equals(inputString);
	}
	
	/**
	 * To get JWT token from the DeepLinking form response
	 * @param htmlString
	 * @return - JWT token
	 */
	public static String getJWTtokenFromDeeplinkingResponseForm(String htmlString) {
		String jwtToken = null;
		try {
			Document html = Jsoup.parse(htmlString);
			Element jwtElement = html.body().select(jwtTextboxSelector).first();
			jwtToken = jwtElement.val().toString().trim();
		} catch (Exception e) {
			Log.message("Unable to get jwt Token from Deeplinking response body. Error: " + e.getMessage());
		}
		return jwtToken;
	}

	/**
	 * To get action attribute value from the DeepLinking form response
	 * @param htmlString
	 * @return - action value
	 */
	public static String getActionFromDeeplinkingResponseForm(String htmlString) {
		String actionValue = null;
		try {
			Document html = Jsoup.parse(htmlString);
			Element dlFormElement = html.body().select(dlFormSelector).first();
			actionValue = dlFormElement.attr("action").toString().trim();
		} catch (Exception e) {
			Log.message("Unable to get action value from Deeplinking response body. Error: " + e.getMessage());
		}
		return actionValue;
	}

	/**
	 * To get Kibana OAuth Token string from the given HTML string
	 * @param HTMLString
	 * @return - jsonToken
	 */
	public static JSONObject getKibanaTokenFromPageSource(String HTMLString) {
		JSONObject jsonToken = null;
		Log.event("Getting Kibana token from HTML page source");
		try {
			Document html = Jsoup.parse(HTMLString);
			Element codeElement = html.body().select("pre").first();
			jsonToken = new JSONObject(codeElement.text().toString().trim());
		} catch (Exception e) {
			Log.message("Unable to get kibana OAuth Token from html string. Error: " + e.getMessage());
		}
		return jsonToken;
	}


	/**
	 * To get URL of given link from the given HTML String and link text
	 * @param HTMLSTring, HTMLText
	 * @return - URL of given link text
	 */
	public static String getLinkURL(String HTMLSTring, String HTMLText) {
		String linkText = null;
		try {
			Document html = Jsoup.parse(HTMLSTring);
			linkText = html.body().getElementsContainingOwnText(HTMLText).attr("href");
		} catch (Exception e) {
			Log.message("Unable to get link for: '"+ HTMLText +"' from html string. Error: " + e.getMessage());
		}
		return linkText;
	}
}