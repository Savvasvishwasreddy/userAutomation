package com.learningservices.itemselection.bff.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.SkipException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.learningservices.utils.Log;
import com.learningservices.utils.StopWatch;
import com.learningservices.utils.Utils;
import com.learningservices.utils.WebDriverFactory;

import io.restassured.response.Response;

public class RealizeUtils {

	private static String responseFileFolderPath;
	private static PropertyReader envSpecificConfigProperty = PropertyReader.getInstance();
	public static final String REFERER_URL = envSpecificConfigProperty.getProperty("api.referer.url");
	private static AtomicInteger responseIndex = new AtomicInteger(0);
	public static final String FAIL_HTML_BEGIN = "<font color=\"red\"><strong>";
	public static final String FAIL_HTML_END = "</strong></font> ";
	public static final String WARN_HTML_BEGIN = "<font color=\"orange\"><strong>";
	public static final String WARN_HTML_END = "</strong></font> ";
	public static final String INFO_HTML_BEGIN = "<font color=\"green\">";
	public static final String INFO_HTML_END = "</font> ";
	public static int realizeMaxElementWait = 3;
	public static int realizeMinElementWait = 2;
	public static boolean useAPIToCreateSetup = false;
	public static boolean contentPlayerFrame = false;
	public static ExpectedCondition<Boolean> realizeLoad;
	public static ExpectedCondition<Boolean> dashLoad;
	public static ExpectedCondition<Boolean> nbcBannerLoad;
	public static ExpectedCondition<Boolean> discoverPageLoad;
	public static ExpectedCondition<Boolean> accountLinkPageLoad;
	public static ExpectedCondition<Boolean> salesToolLoad;
	public static ExpectedCondition<Boolean> schoologyLoad;
	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance("bff_config");
	private static int maxJsonFileLength = 500;
	static String cssSpinner = "div[id*='Loading'][class='']>i[class*='icon-spinner'], [class*='loading'],[class*='Loading']:not([class*='hide']),[id*='Loading']:not([class*='hide']), li[class*='ng-animate'],div[class='success-message'],[class*='success']:not([class*='hide']),div[id='progressDialog'],div:not([class*='withSidebarOpen']) ~ *[show-sidebar],[id='progressing'][aria-hidden='false'],[class*='icon-spinner'],[class*='la-ball-spin-clockwise'],cel-loading-spinner.loading-spinner,cel-loading-spinner.spinner,cel-loading-spinner.loader,div[class^='PSPDFKit-Loading-Indicator PSPDFKit']";
	static String dashCssSpinner = "div[class*='temp_PopWrapper'], div[id='circle'], img[id*='solorSearchLoader'], div[class*='popup_box']";  
	public static String MOUSE_HOVER_JS = "var evObj = document.createEvent('MouseEvents');" + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" + "arguments[0].dispatchEvent(evObj);";    

	private static By allSpinners = By.cssSelector(cssSpinner);
	private static By allDashSpinners = By.cssSelector(dashCssSpinner);
	private static By allDiscoverLoader = By.cssSelector("ion-content p.loading,cel-loading-spinner.spinner,cel-loading-spinner.loader");
	private static By allNBCScrollPanels = By.cssSelector("div#page_setup div.carousel-container");
	private static By allSalesToolSpinners = By.cssSelector("*.loading-screen-icon");
	private static By allSchoologyLoader = By.cssSelector("div[class*='CardsDropdownPanel-loading'],.active-loader,.loading-overlay,div#popups-loading");




	static {
		realizeLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				// To wait click events to trigger
				try {
					Thread.sleep(250);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				spinners = driver.findElements(allSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		dashLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allDashSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		nbcBannerLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> carousels = driver.findElements(allNBCScrollPanels);
				for (WebElement spinner : carousels) {
					try {
						if (!spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		discoverPageLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allDiscoverLoader);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		accountLinkPageLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		salesToolLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allSalesToolSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		schoologyLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allSchoologyLoader);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};

		realizeMaxElementWait = configProperty.getProperty("maxElementWait") != null ? Integer.valueOf(configProperty.getProperty("maxElementWait")) : realizeMaxElementWait;
		realizeMinElementWait = configProperty.getProperty("minElementWait") != null ? Integer.valueOf(configProperty.getProperty("minElementWait")) : realizeMinElementWait;
		useAPIToCreateSetup = configProperty.hasProperty("useAPIToCreateSetup") && configProperty.getProperty("useAPIToCreateSetup") != null? configProperty.getProperty("useAPIToCreateSetup").equalsIgnoreCase("true") : false;
		contentPlayerFrame = configProperty.hasProperty("contentPlayerFrame") && configProperty.getProperty("contentPlayerFrame") != null? configProperty.getProperty("contentPlayerFrame").equalsIgnoreCase("true") : false;

		File responseFolder = new File(Reporter.getCurrentTestResult().getTestContext().getOutputDirectory());
		responseFileFolderPath = responseFolder.getParent() + File.separator +"Response"+ File.separator;
		responseFolder = new File(responseFileFolderPath);
	}
	/**
	 * Nap using Thread.sleep in seconds
	 *
	 * @param seconds
	 */
	public static void nap(double seconds) {
		try {
			Thread.sleep(Math.round(seconds * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * To convert the given map into JSONObject
	 * @param map - key value pair
	 * @return - JSONObject
	 */
	public static JSONObject getJsonFromMap(Map<String, String> map) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		JSONObject jsonData = new JSONObject(gson.toJson(map));
		return jsonData;
	}


	/**
	 * Convert a JSON string to pretty print version *
	 * 
	 * @param jsonString
	 *            - JSON String
	 * @return pretty formatted String
	 */
	public static String convertJSONStringtoPrettyFormat(String jsonString) {
		JsonParser parser = new JsonParser();
		String prettyJson = jsonString;
		try {
			if (jsonString.startsWith("[")) {
				JsonArray json = parser.parse(jsonString).getAsJsonArray();
				Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
				prettyJson = gson.toJson(json);
			} else {
				JsonObject json = parser.parse(jsonString).getAsJsonObject();
				Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
				prettyJson = gson.toJson(json);
			}
		} catch (Exception e) {
			Log.event("Error while pretty print given string " +  e.getMessage());
		}
		return StringEscapeUtils.escapeHtml4(prettyJson);
	}

	/**
	 * To get response body written in a given file and shown as a hyper-link
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static String getResponseInHyperLink(Response response) throws Exception {
		String extension = null, filetype = null;
		String contentType = response.getContentType();
		if (contentType.contains("json")) {
			extension = ".json"; filetype = "json";
		} else if (contentType.contains("html")) {
			extension = ".txt";	//HTML will render in browser
			filetype = "html";
		} else if (contentType.contains("xml")) {
			extension = ".xml"; filetype = "xml";
		} else {
			extension = ".txt"; filetype = "txt";
		}

		String inputFilePath = Reporter.getCurrentTestResult().getName() + "_" + responseIndex.incrementAndGet() + extension;
		String reponseFileLink = "<a href=\"." + File.separator + "Response" + File.separator + inputFilePath
				+ "\" target=\"_blank\" >["+filetype.toUpperCase()+"Response]</a>";
		try {
			File destFile = new File(responseFileFolderPath + inputFilePath);
			destFile.getParentFile().mkdirs();
			FileWriter fw = new FileWriter(destFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			if (contentType.contains("json"))
				bw.write(StringEscapeUtils.unescapeHtml4(convertJSONStringtoPrettyFormat(response.getBody().asString())));
			else
				bw.write(response.getBody().asString());
			bw.close();
		} catch (IOException e) {
			Log.event("Failed to create "+filetype+" file");
		}
		return String.format(reponseFileLink, inputFilePath);
	}


	/**
	 * To get the response body directly or as a hyper-link based on the string
	 * length
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static String getResponseBody(Response response) throws Exception {
		String responseBody = null;
		String contentType = response.getContentType();
		if (contentType.contains("json"))
			responseBody = convertJSONStringtoPrettyFormat(response.getBody().asString());
		else
			responseBody = response.getBody().asString();

		if (responseBody.length() > maxJsonFileLength || contentType.contains("html")) {
			return getResponseInHyperLink(response);
		}
		return responseBody;
	}

	/**
	 * To log the API requests and responses as a table
	 * 
	 * @param callType
	 * @param requestUrl
	 * @param endPoint
	 * @param headers
	 * @param queryParams
	 * @param response
	 * @param requestBody
	 */
	public static void apiLogMessageFormatter(String callType, String requestUrl, String endPoint,
			Map<String, String> headers, Map<String, String> queryParams, Response response, String requestBody, boolean...logAsEvent) {
		try {
			StringBuilder htmlTable = new StringBuilder();
			if (headers != null && headers.getOrDefault("Authorization", "none").contains("Bearer"))
				htmlTable.append("<table border='1' margin='10,5,5,50' width='90%' style='table-layout: fixed;'>");
			else
				htmlTable.append("<table border='1' margin='10,5,5,50'>");

			htmlTable.append("<tr><th width='10%'>Request Type:</th><td>" + callType + "</td></tr>");
			htmlTable.append("<tr><th width='10%'>Request URL:</th><td>" + requestUrl + endPoint + "</td></tr>");
			htmlTable.append("<tr><th width='10%'>Request headers: </th><td style='word-wrap: break-word;'><pre style='white-space: pre-wrap;'>"
					+ convertJSONStringtoPrettyFormat(getJsonFromMap(headers).toString()) + "</pre></td></tr>");
			if (!callType.equalsIgnoreCase("post") || (queryParams != null && !queryParams.isEmpty())) { // As per RFC 2616, section 9.5 POST method doesn't need query parameters
				htmlTable.append("<tr><th>Query Params: </th><td style='word-wrap: break-word;'><pre style='white-space: pre-wrap;'>"
						+ convertJSONStringtoPrettyFormat(getJsonFromMap(queryParams).toString()) + "</pre></td></tr>");
			}
			if (!callType.equalsIgnoreCase("get") && !callType.equalsIgnoreCase("delete")) { // As per RFC 2616, section 9.3 GET and DELETE method doesn't need request body
				htmlTable.append("<tr><th>Request body: </th><td style='word-wrap: break-word;'><pre style='white-space: pre-wrap;'>"
						+ convertJSONStringtoPrettyFormat(requestBody) + "</pre></td></tr>");
			}
			htmlTable.append("<tr><th>Response Status Code: </th><td>" + response.getStatusCode() + "</td></tr>");
			htmlTable.append("<tr><th>Response Body: </th><td style='word-wrap: break-word;'><pre style='white-space: pre-wrap;'>"
					+ getResponseBody(response) + "</pre></td></tr>");
			htmlTable.append("</table>");

			if(logAsEvent.length > 0) {
				if (logAsEvent[0])
					Log.event(htmlTable.toString());
				else
					Log.message(htmlTable.toString());
			}
			else {
				Log.message(htmlTable.toString());
			}
		} catch (Exception e) {
			Log.event("Error while formatting given response " + e.getMessage());
		}
	}
	
	 /**
     * Click on an option in a select tag
     * @param dropdown - which select dropdown to expand
     * @param optionToSelect - text of the option to select
     */
    public static void selectOption(WebElement dropdown, String optionToSelect) {
        Select selectList = new Select(dropdown);
        selectList.selectByVisibleText(optionToSelect);
    }

    /**
     * To scroll into particular element
     * 
     * @param driver -
     * @param element - the element to scroll to
     */
    public static void scrollIntoView(final WebDriver driver, WebElement element) {
        try {
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                                            + "var elementTop = arguments[0].getBoundingClientRect().top;"
                                            + "window.scrollBy(0, elementTop-(viewPortHeight/2));";
            ((JavascriptExecutor) driver).executeScript(scrollElementIntoMiddle, element);
            (new WebDriverWait(driver, Duration.ofSeconds(20)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class, StaleElementReferenceException.class).withMessage("Realize spinners/page not loading")).until(RealizeUtils.realizeLoad);
        } catch (Exception ex) {
            Log.event("Moved to element..");
        }
    }
    
    /**
     * To get matching text element from List of web elements
     * 
     * @param elements - 
     * @param contenttext - text to match
     * @return elementToBeReturned as WebElement
     * @throws Exception -
     */
    public static WebElement getMachingTextElementFromList(List<WebElement> elements, String contenttext) throws Exception {
        WebElement elementToBeReturned = null;
        boolean found = false;
        if (elements.size() > 0) {
            for (WebElement element : elements) {
                if (element.getText().trim().replaceAll("\\s+", " ").equalsIgnoreCase(contenttext)) {
                    elementToBeReturned = element;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new Exception("Didn't find the correct text(" + contenttext + ")..! on the page");
            }
        } else {
            throw new Exception("Expected element list is not available");
        }
        return elementToBeReturned;
    } 
	
	 /**
     * Wait for element to be clickable
     * @param element
     * @param driver
     */
    public static void waitForElementToBeClickable(WebElement element, WebDriver driver, int maxWait) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }
   
    
	 /**
     * To wait for the specific element on the page to become clickable
     * 
     * @param driver -
     * @param element - webelement to wait for to appear
     * @param maxWait - how long to wait for in seconds
     * @return boolean - return true if element is present else return false
     */
    public static boolean waitForElementToBeClickable(WebDriver driver, WebElement element, int maxWait) {
        boolean statusOfElementToBeReturned = false;
        long startTime = StopWatch.startTime();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
        try {
            WebElement waitElement = wait.until(ExpectedConditions.elementToBeClickable(element));
            if (waitElement.isDisplayed() && waitElement.isEnabled()) {
                statusOfElementToBeReturned = true;
                Log.event("Element is displayed and clickable:: " + element.toString());
            }
        } catch (Exception ex) {
            statusOfElementToBeReturned = false;
            Log.event("Unable to find a element clickable after " + StopWatch.elapsedTime(startTime) + " sec ==> " + element.toString());
        }
        return statusOfElementToBeReturned;
    }


	/**
     * To wait for Realize page load with global load wait time It will check
     * all page elements getting load ex: spinners, ajax load, DOM element,
     * frames load, dialog box and side bar
     * 
     * @param driver -
     * @param maxWait - how long to wait for Realize home page to load
     * @return boolean - true if page get refreshed or else return false
     */
    public static boolean waitForRealizePageLoad(final WebDriver driver, int maxWait) {
    	boolean isRefreshed = false;
        long startTime = StopWatch.startTime();
        FluentWait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait)).pollingEvery(Duration.ofMillis(500)).ignoring(StaleElementReferenceException.class).withMessage("Page Load Timed Out");
        try {
            //Utils.waitForPageLoad(driver, maxWait); // This wait takes long time. Do we need it ?
            wait.until(realizeLoad);
        } catch (TimeoutException ex) {
        	
            //Check for modal dialog presence
            boolean anyModalDialogPresent = false;
        	try {
        		Thread.sleep(500);
        		if (waitForElement(driver, driver.findElement(By.cssSelector(".modal-header")), 20)) {
        			anyModalDialogPresent = true;
        		}
        	} catch (Exception nee) {
        		anyModalDialogPresent = false;
        	}
        	if (!anyModalDialogPresent) {
	        	Log.message("Page did not load - Refreshing the page...", driver);
	        	driver.navigate().refresh();
	        	isRefreshed = true;
	            wait.until(realizeLoad);
        	} else {
        		Log.message("Modal dialog present! did not refresh the page..");
        	}
        }
        Log.event("Realize Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));
        return isRefreshed;
    }
    
	 /**
     * To wait for Realize page load with global load wait time It will check
     * all page elements getting load ex: spinners, ajax load, DOM element,
     * frames load, dialog box and side bar
     * 
     * @param driver -
     */
    public static void waitForRealizePageLoad(final WebDriver driver) {
        waitForRealizePageLoad(driver, WebDriverFactory.maxPageLoadWait);
    }
    
	/**
     * To check element visibility using offset height and width
     * 
     * @param driver 
     * @param element 
     * @return boolean 
     */
    public static boolean checkElementVisibilityUsingOffsetWidthHeight(WebDriver driver, WebElement element){
    	JavascriptExecutor jse = (JavascriptExecutor) driver;
    	int elementOffsetWidth = Integer.parseInt(jse.executeScript("return arguments[0].offsetWidth;", element).toString());
    	int elementOffsetHeight = Integer.parseInt(jse.executeScript("return arguments[0].offsetHeight;", element).toString());
    	return elementOffsetWidth > 0 && elementOffsetHeight > 0;
    }
    
	 /**
     * To wait for the specific element on the page
     * 
     * @param driver -
     * @param element - webelement to wait for to appear
     * @param maxWait - how long to wait for
     * @return boolean - return true if element is present else return false
     */
    public static boolean waitForElement(WebDriver driver, WebElement element, int maxWait) {
        boolean statusOfElementToBeReturned = false;
        long startTime = StopWatch.startTime();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
        try {
            WebElement waitElement = wait.until(ExpectedConditions.visibilityOf(element));
            if (waitElement.isDisplayed() && waitElement.isEnabled()) {
                statusOfElementToBeReturned = true;
                //Log.event("Element is displayed:: " + element.toString());
            }
        } catch (Exception ex) {
        	//statusOfElementToBeReturned = false;
            //Log.event("Unable to find a element after " + StopWatch.elapsedTime(startTime) + " sec ==> " + element.toString());
        	
        	//#### Workaround for safari driver issue. Failed to check visibility/isDisplayed ####//
        	if (((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase().matches(".*safari.*")) {
        		try{
	        		if (element.isEnabled() && checkElementVisibilityUsingOffsetWidthHeight(driver, element)) {
	                    statusOfElementToBeReturned = true;
	                    Log.event("Element is displayed:: " + element.toString());
	                }
        		}catch (Exception e) {
        			statusOfElementToBeReturned = false;
    	            Log.event("Unable to find a element after " + StopWatch.elapsedTime(startTime) + " sec ==> " + element.toString());
        		}
        	}else{
	            statusOfElementToBeReturned = false;
	            Log.event("Unable to find a element after " + StopWatch.elapsedTime(startTime) + " sec ==> " + element.toString());
        	}
        	//#### Will be removed once the issue is fixed in Safari driver ####//
        }
        return statusOfElementToBeReturned;
    }
    
	
	/**
	 * To wait for the specific element on the page
	 * 
	 * @param driver -
	 * @param element - webelement to wait for to appear
	 * @return boolean - return true if element is present else return false
	 */
	public static boolean waitForElement(WebDriver driver, WebElement element) {
		return waitForElement(driver, element, realizeMaxElementWait);
	}
	
	 /**
     * To wait for page load with global load wait time It will check. 
     * Includes wait for genaral web pages and Realize pages
     * all page elements getting load ex: spinners, ajax load, DOM element,
     * frames load, dialog box and side bar
     * 
     * @param driver -
     * @param maxWait - how long to wait for Realize home page to load
     * @return boolean - true if page get refreshed or else return false
     */
    public static boolean waitForPageLoad(final WebDriver driver, int maxWait) {
    	boolean isRefreshed = false;
        long startTime = StopWatch.startTime();
        FluentWait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait)).pollingEvery(Duration.ofMillis(500)).ignoring(StaleElementReferenceException.class).withMessage("Page Load Timed Out");
        try {
            Utils.waitForPageLoad(driver, maxWait); // This wait takes long time. Do we need it ?
            wait.until(realizeLoad);
        } catch (TimeoutException ex) {
        	
            //Check for modal dialog presence
            boolean anyModalDialogPresent = false;
        	try {
        		Thread.sleep(500);
        		if (waitForElement(driver, driver.findElement(By.cssSelector(".modal-header")), 20)) {
        			anyModalDialogPresent = true;
        		}
        	} catch (Exception nee) {
        		anyModalDialogPresent = false;
        	}
        	if (!anyModalDialogPresent) {
	        	Log.message("Page did not load - Refreshing the page...", driver);
	        	driver.navigate().refresh();
	        	isRefreshed = true;
	            wait.until(realizeLoad);
        	} else {
        		Log.message("Modal dialog present! did not refresh the page..");
        	}
        }
        Log.event("Realize Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));
        return isRefreshed;
    }
	
	 /**
     * To wait for Realize page load with global load wait time It will check
     * Includes wait for genaral web pages and Realize pages
     * all page elements getting load ex: spinners, ajax load, DOM element,
     * frames load, dialog box and side bar
     * 
     * @param driver -
     */
    public static void waitForPageLoad(final WebDriver driver) {
        waitForPageLoad(driver, WebDriverFactory.maxPageLoadWait);
    }
    
    /**
   	 * To scroll the given element into the middle of the document
   	 * @param driver
   	 * @param element
   	 */
   	public static void scrollElementIntoMiddle(WebDriver driver, WebElement element) {
   		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({ behavior: 'auto', block: 'center' });", element);
   	}
   	
   	/**
	 * To Scroll into Top/Bottom of the page
	 * 
	 * @param driver
     * @param viewPort - Top/Bottom to scroll
	 */
	public static void scrollInToPage(final WebDriver driver, String viewPort){
		if(viewPort.equalsIgnoreCase("Top")){
			((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0)");
		}else if(viewPort.equalsIgnoreCase("Bottom")){
			((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		}else{
			Log.message("Scroll bar is not exists");
		}
	}

	/**
	 * To click given webElement using JavaScript click
	 * @param driver
	 * @param element
	 */
	public static void clickJS(WebDriver driver, WebElement element) {
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		} catch (WebDriverException e) {
			Log.message("Unable to click given element using JS. Error: " + e.getMessage());
		}
	}
	
	/**
	 * To wait for Item Selection page load with Browse Program or My Program
	 * 
	 * @param driver  -
	 * @param maxWait - time to wait
	 */
	public static void waitForItemSelectionPageLoad(final WebDriver driver, int maxWait) {
		long startTime = StopWatch.startTime();
		FluentWait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait)).pollingEvery(Duration.ofMillis(500))
				.ignoring(StaleElementReferenceException.class).withMessage("Item Selection webapp is still loading");
		try {
			wait.until(ExpectedConditions.urlContains("/program"));
			Utils.waitForPageLoad(driver);
			wait.until(discoverPageLoad);
		} catch (TimeoutException ex) {
			wait.until(discoverPageLoad);
		}
		Log.event("Item Selection Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));
	}
	
	/**
     * Wait until element disappears in the page
     * 
     * @param driver - driver instance
     * @param element - webelement to wait to have disaapear
     * @return true if element is not appearing in the page
     */
    public static boolean waitUntilElementDisappear(WebDriver driver, final WebElement element, int maxWait) {
        boolean isNotDisplayed = false;

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
        try {
			isNotDisplayed = wait.until(new ExpectedCondition<Boolean>() {
			    @Override
			    public Boolean apply(WebDriver webDriver) {
			        boolean isPresent = false;
			        try {
			            if (element.isDisplayed()) {
			                isPresent = false;
			                Log.event("Element " + element.toString() + ", is still visible in page");
			            }
			        } catch (Exception ex) {
			            isPresent = true;
			            Log.event("Element " + element.toString() + ", is not displayed in page ");
			            return isPresent;
			        }
			        return isPresent;
			    }
			});
		} catch (TimeoutException e) {
			isNotDisplayed = false;
		}
        return isNotDisplayed;
    }

    /**
     * To wait for the specific locator on the page
     * 
     * @param driver -
     * @param element - webElement to wait for to present in the current page
     * @param maxWait - how long to wait for
     * @return boolean - return true if element is present else return false
     */
    public static boolean waitForLocatorToPresent(WebDriver driver, By locator, int maxWait) {
        boolean statusOfElementToBeReturned = false;
        long startTime = StopWatch.startTime();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            statusOfElementToBeReturned = true;
        } catch (Exception ex) {
            statusOfElementToBeReturned = false;
            Log.event("Unable to find a element after " + StopWatch.elapsedTime(startTime) + " sec ==> " + locator);
        }
        return statusOfElementToBeReturned;
    }

    /**
     * To compare two array list values,then print unique list value and print
     * missed list value
     * 
     * @param expectedElements - expected element list
     * @param actualElements - actual element list
     * @return statusToBeReturned - returns true if both the lists are equal,
     *         else returns false
     */
    public static boolean compareTwoList(List<String> expectedElements, List<String> actualElements) {
        boolean statusToBeReturned = false;
        List<String> uniqueList = new ArrayList<String>();
        List<String> missedList = new ArrayList<String>();
        for (String item : expectedElements) {
            if (actualElements.contains(item)) {
                uniqueList.add(item);
            } else {
                missedList.add(item);
            }
        }
        Collections.sort(expectedElements);
        Collections.sort(actualElements);
        if (expectedElements.equals(actualElements)) {
            Log.event("All elements checked on this page:: " + uniqueList);
            statusToBeReturned = true;
		} else {
			statusToBeReturned = false;
			if (missedList.size() > 0) {
				Log.event("Missing element on this page:: " + missedList);
			} else {
				List<String> extraElements = new ArrayList<String>(actualElements);
				extraElements.removeAll(expectedElements);
				Log.event("Extra elements on this list:: " + extraElements);
			}
		}
        return statusToBeReturned;
    }

    /**
	 * To convert a keyString value into MD% Hash code
	 * 
	 * @param keyString
	 * @return MD5 Hash value
	 * @throws NoSuchAlgorithmException
	 */
	public static String getMD5Hash(String keyString) throws NoSuchAlgorithmException {
		Log.event("Converting '"+keyString+"' into MD5 Hash code");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest = md.digest(keyString.getBytes(StandardCharsets.UTF_8));
		String md5String = DatatypeConverter.printHexBinary(digest);
	
		return md5String;
	}
	
	 /**
     * Verifies if all the values in the boolean array are true or not
     * 
     * @param array of boolean values
     * @return overall status of the booleans in the array
     */
    public static boolean isAllTrue(ArrayList<Boolean> array) {
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }
        return array != null && array.size() > 0;
    }
    
    /**
	 * To get JSONObject from the given URL
	 * @param fileurl 
	 * @return - JSONObject
	 * @throws Exception
	 */
	public static JSONObject getJsonFromURL(String fileurl) throws Exception {
		JSONObject jsonData = null;
		try {
			final URL url = new URL(fileurl);
			JSONParser parser = new JSONParser();
			final Reader reader = new InputStreamReader(new BOMInputStream(url.openStream()), "UTF-8");
			Object obj = parser.parse(reader);
			org.json.simple.JSONObject jsonRequest = (org.json.simple.JSONObject) obj;
			jsonData = new JSONObject(jsonRequest.toJSONString());
		} catch (Exception e) {
			throw new Exception("Error while reading json from given URL " + e.getMessage());
		}
	    return jsonData;
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
	 * To parse the given date as per the realize assignment date
	 * 
	 * @param givenDate - Given realize assignment dateTime
	 * @param timeZone  - IANA TimeZone
	 * @return Instant - a point in time
	 */
	public static Instant parseAssignmentDate(String givenDate, String timeZone) {
		Instant parsedTime = Instant.now();
		try {
			DateTimeFormatter TEACHER_DATETIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy h:mm a")
					.withZone(ZoneId.of(timeZone));
			parsedTime = ZonedDateTime.parse(givenDate.trim().toUpperCase(), TEACHER_DATETIME_FORMAT).toInstant();
		} catch (DateTimeParseException e) {
			DateTimeFormatter STUDENT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy h:mma")
					.withZone(ZoneId.of(timeZone));
			parsedTime = ZonedDateTime.parse(givenDate.trim().toUpperCase(), STUDENT_DATETIME_FORMAT).toInstant();
		}
		return parsedTime;
	}
	
	 /**
     * Switching between tabs or windows in a browser
     * 
     * @param driver -
     */
    public static void switchToNewWindow(WebDriver driver) {
        String winHandle = driver.getWindowHandle();  
		if (configProperty.hasProperty("runNWJS") && configProperty.getProperty("runNWJS").equalsIgnoreCase("true")) {
			for (int count = 0; count <= 10; count++) {
				if (driver.getWindowHandles().size() == 1) {
					nap(1); // Used thread to fix the sync issue which happens in nwjs during multiple window handling.
				} else {
					break;
				}
			}
		}
        for (String index : driver.getWindowHandles()) {
            if (!index.equals(winHandle)) {
            	if (((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase().matches(".*edge.*")) 
            		driver.switchTo().window(index.toString());
            	else
            		driver.switchTo().window(index);
                break;
            }
      }
        if (!((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase().matches(".*safari.*")) {
            ((JavascriptExecutor) driver).executeScript("if(window.screen)"
                    + "{window.moveTo(0, 0);"
                    + " window.resizeTo(window.screen.availWidth, window.screen.availHeight);"
                    + "};");
        }
    }   
    
	/**
	 * To compare two JSON array when parameter as JSON
	 * 
	 * @param expectedJson
	 * @param actualJson
	 * @return true - if two JSON matched
	 */
	public static boolean compareJson(JsonElement expectedJson, JsonElement actualJson) {
		boolean isEqual = true;
		
		// Check whether both jsonElement are not null
		if (expectedJson != null && actualJson != null) {

			// Check whether both jsonElement are objects
			if (expectedJson.isJsonObject() && actualJson.isJsonObject()) {
				Set<Entry<String, JsonElement>> ens1 = ((JsonObject) expectedJson).entrySet();
				Set<Entry<String, JsonElement>> ens2 = ((JsonObject) actualJson).entrySet();
				JsonObject json2obj = (JsonObject) actualJson;
				if (ens1 != null && ens2 != null && (ens2.size() == ens1.size())) {
					// Iterate JSON Elements with Key values
					for (Entry<String, JsonElement> en : ens1) {
						isEqual = isEqual && compareJson(en.getValue(), json2obj.get(en.getKey()));
					}
				} else {
					isEqual = false;
				}
			}

			// Check whether both jsonElement are arrays
			else if (expectedJson.isJsonArray() && actualJson.isJsonArray()) {
				JsonArray jarr1 = expectedJson.getAsJsonArray();
				JsonArray jarr2 = actualJson.getAsJsonArray();
				if (jarr1.size() != jarr2.size()) {
					isEqual = false;
				} else {
					int i = 0;
					// Iterate JSON Array to JSON Elements
					for (JsonElement je : jarr1) {
						isEqual = isEqual && compareJson(je, jarr2.get(i));
						i++;
					}
				}
			}

			// Check whether both jsonElement are null
			else if (expectedJson.isJsonNull() && actualJson.isJsonNull()) {
				isEqual = true;
			}

			// Check whether both jsonElement are primitives
			else if (expectedJson.isJsonPrimitive() && actualJson.isJsonPrimitive()) {
				if (expectedJson.equals(actualJson)) {
					isEqual = true;
				} else {
					isEqual = false;
				}
			} else {
				isEqual = false;
			}
		} else if (expectedJson == null && actualJson == null) {
			isEqual = true;
		} else {
			isEqual = false;
		}
		return isEqual;
	}
	
    
    /**
	 * To compare two JSON array when parameter as string 
	 * 
	 * @param expectedJson
	 * @param actualJson
	 * @return true - if two JSON matched
	 */
	public static boolean compareJson(String expectedJson, String actualJson) {
		boolean isEqual = false;
		try {
			JsonParser parser = new JsonParser();
			JsonElement json1 = parser.parse(expectedJson);
			JsonElement json2 = parser.parse(actualJson);
			isEqual = compareJson(json1, json2);
		} catch (Exception e) {
			Log.event("Error in parsing JSON. " + e.getMessage());
		}
        return isEqual;
	}
	
	/**
     * To get the text of a WebElement
     * @param element - the input field you need the value/text of
     * @param driver -
     * @return text of the input's value
     */
    public static String getTextOfWebElement(WebElement element, WebDriver driver) {
        String sDataToBeReturned = null;
        if (waitForElement(driver, element)) {
            sDataToBeReturned = element.getText().trim().replaceAll("\\s+", " ");
        }
        Log.event("Text fetched from UI: " + sDataToBeReturned);
        return sDataToBeReturned;
    }
    
    /**
   	 * Skip the test case if the configuration is not a stable release of Windows 10 Google Chrome
   	 * @param browserName - as returned by getBrowerName()
   	 * @throws Exception
   	 */
   	public static void skipIfNotWin10ChromeStable(String browserName) throws Exception {
   		if (!browserName.equalsIgnoreCase("win10_chrome")||browserName.equalsIgnoreCase("Windows_10_Chrome"))
   			Log.exception(new SkipException("****** STATUS:SKIPPED: " + browserName
   					+ " This scenario is LTI-A functionality and will be executed only in a stable release of 'Windows 10 Google Chrome' ********"));
   	}
   	
   	/**
     * To get the OS details from javascript command
     * 
     * @param driver -
     * @return defined os tag as string
     */
    public static String getOsDetails(final WebDriver driver) {
    	
        String osDetails = null;
        String appVersion = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent", "");
        Log.event("userAgentString:: " + appVersion);
        appVersion = appVersion.split("\\)")[0].split("\\(")[1];
        if (appVersion.contains("Windows NT 6.1") || appVersion.contains("Windows 7")) {
            osDetails = "win7";
        } else if (appVersion.contains("Windows NT 6.2") || appVersion.contains("Windows 8")) {
            osDetails = "win8";
        } /*else if (appVersion.contains("Windows NT 6.3") || appVersion.contains("Windows 8.1")) {
            osDetails = "win8_1";
        }*/ else if (appVersion.contains("Windows NT 10") || appVersion.contains("Windows 10")) {
            osDetails = "win10";
        } else if (appVersion.contains("Mac OS X 10_9")) {
            osDetails = "mac10_9";
        } else if (appVersion.contains("Mac OS X 10_12") || appVersion.contains("Mac OS X 10.12")) {
            osDetails = "mac10_12";
        } else if (appVersion.contains("Mac OS X 10_11") || appVersion.contains("Mac OS X 10.11")) {
            osDetails = "mac10_11";
        }else if (appVersion.contains("Mac OS X 10_13") || appVersion.contains("Mac OS X 10.13")) {
            osDetails = "mac10_13";
        }else if (appVersion.contains("Mac OS X 10_15") || appVersion.contains("Mac OS X 10.15")) {
            osDetails = "mac10_15";
        } else {
            // general combo handling
            if (appVersion.contains("Windows")) {
                osDetails = "win7";
            } else if (appVersion.contains("Mac")) {
                osDetails = "mac";
            }
        }
        return osDetails;
    }
   	
    /**
	 * To get the Chromebook details from the webDriver
	 * @param driver
	 * @return - Chromebook name and version
	 */
	public static String getChromeBookDetails(final WebDriver driver) {
    	String osDetails = null;
    	String browser_name = "", browser_version = "";
    	String userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent", "");
        Log.event("userAgentString:: " + userAgent);
        if (userAgent.matches(".*[cC]hrome/(\\S+).*")) {	//For Google Chrome browser
			Pattern googlePattern = Pattern.compile("Chrome/(\\S+)");
			Matcher googleMatcher = googlePattern.matcher(userAgent);
			if (googleMatcher.find()) {
				browser_version = googleMatcher.group(1).split("\\.")[0];
				browser_name = "Chromebook";
			}
		}
        osDetails = browser_name + "_" + browser_version;
        return osDetails;
    }
    
	
   	/**
	 * To get the browser Name from webDriver
	 * @param driver
	 * @return - browser name
	 */
	public static String getBrowerName(WebDriver driver) {
    	String browserNameWithOSInfo = "";
    	if(configProperty.getProperty("runNWJS") != null && configProperty.getProperty("runNWJS").equalsIgnoreCase("true")) {
    		browserNameWithOSInfo = getChromeBookDetails(driver);	//Browser Name for NWJS and chrome.
    	} else {
    		String browserName = ((RemoteWebDriver)driver).getCapabilities().getBrowserName();
    		if (browserName.equalsIgnoreCase("chrome")) {
    			String browser_version = "";
    			String userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent", "");
    	        Log.event("userAgentString:: " + userAgent);
    	        if (userAgent.matches(".*[cC]hrome/(\\S+).*")) {	//For Google Chrome browser
    				Pattern googlePattern = Pattern.compile("Chrome/(\\S+)");
    				Matcher googleMatcher = googlePattern.matcher(userAgent);
    				if (googleMatcher.find()) {
    					browser_version = googleMatcher.group(1).split("\\.")[0];
    				}
    			}
    	        
    	        if (browser_version.equals(configProperty.getProperty("chromeBetaVersion"))) {
    	        	browserName = "chromebeta";
    	        }
    		}
    		browserNameWithOSInfo = getOsDetails(driver) + "_" + browserName;
    	}
    	return browserNameWithOSInfo;
    }
	
	/**
     * To scroll into particular element
     * 
     * @param driver -
     * @param element - the element to scroll to
     */
    public static void scrollIntoViewIfNeeded(WebDriver driver, WebElement element, int maxWait) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoViewIfNeeded(true);", element);
            (new WebDriverWait(driver, Duration.ofSeconds(maxWait)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class, StaleElementReferenceException.class).withMessage("Realize spinners/page not loading")).until(RealizeUtils.realizeLoad);
        } catch (Exception ex) {
            Log.event("Moved to element..");
        }
    }
    
    /**
	 * To wait for School Selection page to load
	 * 
	 * @param driver  - WebDriver
	 * @param maxWait - time to wait
	 */
	public static void waitForSchoolSelectionPageLoad(final WebDriver driver, int maxWait) {
		long startTime = StopWatch.startTime();
		FluentWait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait)).pollingEvery(Duration.ofMillis(500))
				.ignoring(StaleElementReferenceException.class).withMessage("School Selection page is still loading");
		try {
			wait.until(accountLinkPageLoad);
		} catch (TimeoutException ex) {
			wait.until(accountLinkPageLoad);
		}
		Log.event("School Selection Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));
	}
	
	/**
	 * To clear the text fields
	 * @param element
	 * @param driver
	 * @param screenshot
	 */
	public static void clearFields(WebElement element, WebDriver driver,boolean screenshot) {
		Log.event("Clearing the text fields");
		if (((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase().contains("edge"))
		{
			element.sendKeys(Keys.chord(Keys.CONTROL,"a",Keys.DELETE));
		}
		else if (((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase().contains("safari")) {
			String title = element.getAttribute("value");
			for(int i=0;i<title.length();i++)
				element.sendKeys(Keys.BACK_SPACE);
		}
		else {
			element.clear();
		}
		if (screenshot)
			Log.message("Cleared the text fields", driver, screenshot);
		else
			Log.event("Cleared the text fields");
	}
	
	/**
     * To convert color of an element from rgba to hex
     * 
     * @param color -
     * @return String of hex value
     */
    public static String convertColorFromRgbaToHex(String color) {
        String[] hexValue = color.replaceAll("[^,0-9]", "").split(",");

        int hexValue1 = Integer.parseInt(hexValue[0]);
        hexValue[1] = hexValue[1].trim();
        int hexValue2 = Integer.parseInt(hexValue[1]);
        hexValue[2] = hexValue[2].trim();
        int hexValue3 = Integer.parseInt(hexValue[2]);

        String actualColor = String.format("#%02x%02x%02x", hexValue1, hexValue2, hexValue3);

        return actualColor;
    }
    
    /**
     * To wait for the specific list elements on the page
     * 
     * @param driver -
     * @param elements - List elements to wait for to appear
     * @param maxWait - how long to wait for
     * @return boolean - return true if element is present else return false
     */
    
    public static boolean waitForListElement(WebDriver driver, List<WebElement> elements, int maxWait) {
        boolean statusOfElementToBeReturned = false;
//        long startTime = StopWatch.startTime();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
        try {
			 wait.until(ExpectedConditions.visibilityOfAllElements(elements));
                statusOfElementToBeReturned = true;
                //Log.event("List Element is displayed:: " + elements.toString()); //Commented to check the impact in execution time
        } catch (Exception ex) {
            statusOfElementToBeReturned = false;
            //Log.event("Unable to find list element after " + StopWatch.elapsedTime(startTime) + " sec ==> " + elements.toString()); //Commented to check the impact in execution time
        }
        return statusOfElementToBeReturned;
    }
    
    /**
     * To get the visible text of a WebElement
     * @param element - the input field you need the value/text of
     * @param driver -
     * @return text of the input's value
     */
    
	public static String getVisibleTextFromElement(WebElement element, WebDriver driver) {
		String sDataToBeReturned = null;
		if (waitForElement(driver, element)) {
			try {
			sDataToBeReturned = (String) ((JavascriptExecutor) driver).executeScript(
					"var clone = $(arguments[0]).clone();" + "clone.appendTo('body').find(':hidden').remove();"
							+ "var text = clone.text();" + "clone.remove(); return text;",
					element);
			sDataToBeReturned = sDataToBeReturned.replaceAll("\\s+", " ").trim();
			}catch(Exception e) {
				sDataToBeReturned = element.getText().toString().trim();
				sDataToBeReturned = sDataToBeReturned.replaceAll("\\s+", " ").trim();
			}
		}
		Log.event("Text fetched from UI: " + sDataToBeReturned);
		return sDataToBeReturned;
	}

	/**
     * Verify contents of a WebElement equals a passed in string variable
     * @param textToVerify - expected text
     * @param elementToVerify - element to verify the text of
     * @return true if text on screen matches passed variable contents
     */
    public static boolean verifyWebElementTextEquals(WebElement elementToVerify, String textToVerify) {
        boolean status = false; 
        String actualText =  elementToVerify.getText().trim().replaceAll("\\s+", " ");
        Log.event("Actual Text : " + actualText);
        Log.event("Expected Text : "+ textToVerify);

        if (actualText.equals(textToVerify)) {
            status = true;
        }
        return status;
    }
    
    
    /**
     * Waits for element to visible
     * 
     * @param driver -
     * @param element - element to wait for visibility of
     * @param timeSeconds - how long to wait
     * @throws Exception -
     */
    public static void waitForElementVisible(final WebDriver driver, WebElement element, int maxWait) throws Exception {
        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxWait));
            wait.until(ExpectedConditions.visibilityOf(element));

        } catch (TimeoutException te) {
            Log.exception(te);
        }
    }
    
    /**
     * To get the value of an input field.
     * @param element - the input field you need the value/text of
     * @param driver -
     * @return text of the input's value
     */
    public static String getValueOfInputField(WebElement element, WebDriver driver) {
        String sDataToBeReturned = null;
        if (waitForElement(driver, element)) {
            sDataToBeReturned = element.getAttribute("value");
        }
        return sDataToBeReturned;
    }
    
    /**
     * To check whether scrollbar is displayed for an element
     * 
     * @param driver
     * @param element
     * @return boolean
     * 		   true if scrollbar is displayed
     */
    public static boolean verifyScrollExistForElement(final WebDriver driver, WebElement element){
    	JavascriptExecutor js = (JavascriptExecutor)driver;
    	double clientHeight = Double.valueOf(js.executeScript("return arguments[0].clientHeight;", element).toString());
   	 	double scrollHeight = Double.valueOf(js.executeScript("return arguments[0].scrollHeight;", element).toString());
   	 	return clientHeight < scrollHeight;
    }
    
    /**
     * To perform mouse hover on an element using javascript
     * @param driver
     * @param element
     */
    public static void moveToElementJS(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(MOUSE_HOVER_JS, element);
    }
    
    
	 /**
     * To check color of given element
     * 
     * @param elementToCheck - WebElement that we are checking
     * @param desiredColor - hex value of a color
     * @return true if the desired color matches actual color
     * @throws Exception -
     */
    public static boolean checkColor(WebElement elementToCheck, String desiredColor) throws Exception {
        boolean flag = false;
        try {
            String color = elementToCheck.getCssValue("color");
            String actualColor = convertColorFromRgbaToHex(color);
            Log.event("Actual text color : "+ actualColor);
            flag = actualColor.equalsIgnoreCase(desiredColor);  
        } catch (NoSuchElementException ex) {
            Log.exception(ex);
        }
        return flag;
    }
    
    /**
     * To perform mouse hover on an element using selenium api
     * @param driver
     * @param element
     */
    public static void moveToElementSelenium(WebDriver driver, WebElement element) {
        Actions action = new Actions(driver);
        action.moveToElement(element).build().perform();
    }
    
    /**
     * To highlight an element
     * 
     * @param driver
     * @param element
     */
    public static void highlightElement(final WebDriver driver, WebElement element){
    	((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid red'", element);
    }
    
    /**
     * To verify matching text element present in the given List of web elements
     * 
     * @param elements - 
     * @param contenttext - text to match
     * @return true if matching element present in the given list
     * @throws none
     */
    public static boolean isMatchingElementFoundInList(List<WebElement> elements, String contenttext) throws Exception {
    	for (WebElement element : elements) {
    		if (element.getText().trim().replaceAll("\\s+", " ").equalsIgnoreCase(contenttext)) {
    			return true;
    		}
    	}
    	return false;
    }  
    
    /**
     * Verify contents of a WebElement contains a passed in string variable
     * @param textToVerify - expected text
     * @param elementToVerify - element to verify the text of
     * @return true if text on screen matches passed variable contents
     */
    public static boolean verifyWebElementTextContains(WebElement elementToVerify, String textToVerify) {
        boolean status = false;
        String UITextToVerify = elementToVerify.getText();
        Log.event("Text fetched from UI: " + UITextToVerify);
        Log.event("Text to compare: " + textToVerify);
        if (UITextToVerify.trim().replaceAll("\\s+", " ").contains(textToVerify.trim())) {
            status = true;
        }
        return status;
    }
    
    /**
	 * To maximize current window to full screen
	 * 
	 * @param driver
	 */
	public static void maximizeWindow(WebDriver driver) {
		try {
			if (configProperty.hasProperty("runNWJS")
					&& configProperty.getProperty("runNWJS").equalsIgnoreCase("true")) {
				((JavascriptExecutor) driver).executeScript("window.resizeTo(screen.width, screen.height);");
			} else {
				driver.manage().window().maximize();
			}
		} catch (Exception e) {
			Log.message("Unable to maximize window. Error: " + e.getMessage());
		}
	}

	
    /**
	 * Switching between most recently opened window in a browser
	 * 
	 * @param driver
	 */
	public static void switchToMostRecentWindow(Set<String> allwins, WebDriver driver) {
		for (String win : driver.getWindowHandles()) {
			if (!allwins.contains(win)) {
				driver.switchTo().window(win.toString());
				maximizeWindow(driver);
			}
		}
	}
    
}
