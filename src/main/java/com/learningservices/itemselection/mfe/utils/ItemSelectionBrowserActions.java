package com.learningservices.itemselection.mfe.utils;

import java.time.Duration;

import java.util.ArrayList;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.EnvironmentPropertiesReader;
import com.learningservices.utils.Log;
import com.learningservices.utils.StopWatch;
import com.learningservices.utils.Utils;
import com.learningservices.utils.WebDriverFactory;

public class ItemSelectionBrowserActions {

	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();

	/**
	 * Used to click on the webelement
	 * @param driver
	 * @param btn - Element to be clicked
	 * @param elementDescription - Description of the element
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void click(WebDriver driver, WebElement btn, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		if (!Utils.waitForElement(driver, btn, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			btn.click();
			Log.message("Clicked on "+elementDescription, driver);
		} catch (NoSuchElementException e) {
			Log.fail("Failed to click on "+elementDescription+" "+e, driver);		
		}catch (ElementClickInterceptedException e2) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

		}
	}
	
	public static void clear(WebDriver driver, WebElement txtField, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		if (!Utils.waitForElement(driver, txtField, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			txtField.clear();
			Log.message("cleared on "+elementDescription, driver);
		} catch (NoSuchElementException e) {
			Log.fail("Failed to clear on "+elementDescription+" "+e, driver);		
		}
	}

	/**
	 * Used to time on the web element
	 * @param driver
	 * @param txt - text field in which text to be entered
	 * @param textToType - text to be entered
	 * @param elementDescription - Description of the element
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void type(WebDriver driver, WebElement txt, String textToType, String elementDescription, int... timeOutArray) {

		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		if (!Utils.waitForElement(driver, txt, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			txt.sendKeys(textToType);
			Log.message("Entered value "+textToType+" in"+elementDescription, driver);
		} catch (NoSuchElementException e) {
			Log.fail("Failed to enter value "+textToType+" in "+elementDescription+" "+e, driver);		
		}
	}

	/**
	 * Wait for the given text is present in the specified element.
	 * @param driver
	 * @param webElement - Element in which given text to be present
	 * @param expectedText - Expected Text
	 * @param elementDescription - Description
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void waitForExpectedTextToBePresent(WebDriver driver, WebElement webElement, String expectedText, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		if (!Utils.waitForElement(driver, webElement, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.textToBePresentInElement(webElement, expectedText));
			Log.message("Expected text "+expectedText+" displayed in "+elementDescription, driver);
		}catch(TimeoutException e) {
			Log.fail("Expected text "+expectedText+"not displayed in"+elementDescription+e, driver);	
		}
	}

	/**
	 * Wait for particular element to be present
	 * @param driver
	 * @param webElement - expected element
	 * @param elementDescription - Description
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void waitForElementToBePresent(WebDriver driver, WebElement webElement, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			Log.message(elementDescription+" displayed", driver);
		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" not displayed" +e, driver);	
		}
	}

	/**
	 * Wait till the expected element becomes clickable
	 * @param driver
	 * @param webElement - Expected element needs to clickable
	 * @param elementDescription - Description
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void waitForElementToBeClickable(WebDriver driver, WebElement webElement, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");

			WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.elementToBeClickable(webElement));
			Log.message(elementDescription+" is clickable", driver);
		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not clickable" +e, driver);	
		}
	}

	/**
	 * Used to verify given element is disabled or not
	 * @param driver
	 * @param webElement - Element to be checked
	 * @param elementDescription - Description
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void verifyElementIsDisabled(WebDriver driver, WebElement webElement, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");

			Log.assertThat(!webElement.isEnabled(), elementDescription+" is disabled", elementDescription+" is enabled", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not disabled" +e, driver);	
		}
	}

	/**
	 * Wait for the given text is present in the specified element for particular time period.
	 * @param driver
	 * @param webElement - Element in which given text to be present
	 * @param expectedText - Expected text to be present in the given element
	 * @param elementDescription - Description
	 * @param timeout - time period in secs
	 */
	public static void waitForExpectedTextToBeVisibleForFewSecs(WebDriver driver, WebElement webElement, String expectedText, String elementDescription, int timeout) {
		waitForExpectedTextToBePresent(driver, webElement, expectedText, elementDescription);
		try {
			WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(webElement,expectedText)));
			Log.message("Expected text "+expectedText+" is not visible after the expected"+timeout+" secs", driver);
		}catch(TimeoutException e) {
			Log.fail("Expected text "+expectedText+"is visible even after the "+timeout+" secs"+e, driver);	
		}
	}

	public static void verifyElementTextIsDisplayed(WebDriver driver, WebElement webElement,String expectedText, String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");

			Log.assertThat(webElement.getText().equals(expectedText), elementDescription+" is displayed", elementDescription+" is displayed", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not displayed" +e, driver);	
		}
		
	}

	public static void verifyRGBColourOfElement(WebDriver driver, WebElement webElement,String expectedHex, String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			String cssValue = webElement.getCssValue("color");
			String asHex = Color.fromString(cssValue).asHex();
			Log.assertThat(asHex.equals(expectedHex), elementDescription+" is ", elementDescription+" is matching", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not matching" +e, driver);	
		}
		
		
	}

	public static void verifyTwoListOfStringsEqual(WebDriver driver, WebElement webElement,ArrayList<String> expected,ArrayList<String> actual, String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			Log.assertThat(actual.equals(expected), elementDescription+" is matching", elementDescription+" is not matching", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not matching" +e, driver);	
		}
		
	}

	public static void verifyElementDisplayed(WebDriver driver, WebElement webElement,WebElement targetElement,String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			Log.assertThat(targetElement.isDisplayed(), elementDescription+" is displayed", elementDescription+" is not displayed", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not displayed" +e, driver);	
		}
		
	}
	
	public static void verifyElementNotDisplayed(WebDriver driver, WebElement webElement,WebElement targetElement,String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			Log.assertThat(!targetElement.isDisplayed(), elementDescription+" is displayed", elementDescription+" is not displayed", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not displayed" +e, driver);	
		}
		
	}

	public static void verifyElementIsDisabledUsingAttribute(WebDriver driver, WebElement webElement,WebElement targetElement,String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			Log.assertThat(targetElement.isDisplayed(), elementDescription+" is disabled", elementDescription+" is not disabled", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not disabled" +e, driver);	
		}
		
	}
	
    /**
     * Used to scroll down to particular element
     * @param element - Element to be scrolled To
     * @param driver
     */
    public static void scrollToViewElement(WebElement element, WebDriver driver) {
    	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
        Utils.waitForElement(driver, element, 10);
    }

	/**
	 * Used to scroll down to particular element and click
	 * @param driver
	 * @param btn - Webelement to be clicked
	 * @param elementDescription - Element description
	 * @param timeOutArray - Custom timeout
	 */
	public static void scrollToElementAndclick(WebDriver driver, WebElement btn, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		scrollToViewElement(btn, driver);
		if (!Utils.waitForElement(driver, btn, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			btn.click();
			Log.message("Clicked on "+elementDescription, driver);
		} catch (NoSuchElementException e) {
			Log.fail("Failed to click on "+elementDescription+" "+e, driver);		
		}
	}
	
	/**
	 * Used to click on the webelement
	 * @param driver
	 * @param btn - Element to be clicked
	 * @param elementDescription - Description of the element
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void clickJS(WebDriver driver, WebElement btn, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		if (!Utils.waitForElement(driver, btn, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
			Log.message("Clicked on "+elementDescription, driver);
		} catch (NoSuchElementException e) {
			Log.fail("Failed to click on "+elementDescription+" "+e, driver);		
		}
	}
	
	public static void verifyTwoListOfStringsContains(WebDriver driver, WebElement webElement,ArrayList<String> expected,ArrayList<String> actual, String elementDescription, int... timeOutArray) {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			if (!Utils.waitForElement(driver, webElement, timeout))
				throw new NoSuchElementException(elementDescription + " not found in page!!");
			Log.assertThat(actual.containsAll(expected), elementDescription+" is matching", elementDescription+" is not matching", driver);

		}catch(NoSuchElementException e) {
			Log.fail(elementDescription+" is not matching" +e, driver);	
		}
		
	}

	/**
	 * Used to click on the webelement
	 * @param driver
	 * @param btn - Element to be clicked
	 * @param elementDescription - Description of the element
	 * @param timeOutArray - custom timeout in secs
	 */
	public static void clickWithOutCapture(WebDriver driver, WebElement btn, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		if (!Utils.waitForElement(driver, btn, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			btn.click();
		} catch (NoSuchElementException e) {
			Log.fail("Failed to click on "+elementDescription+" "+e, driver);		
		}catch (ElementClickInterceptedException e2) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

		}
	}

	/**
	 * Used to scroll down to particular element and click
	 * @param driver
	 * @param btn - Webelement to be clicked
	 * @param elementDescription - Element description
	 * @param timeOutArray - Custom timeout
	 */
	public static void scrollToElementAndclickWithoutCapture(WebDriver driver, WebElement btn, String elementDescription, int... timeOutArray)   {
		int timeout = timeOutArray.length > 0?timeOutArray[0]:Integer.parseInt(configProperty.getProperty("maxElementWait"));

		scrollToViewElement(btn, driver);
		if (!Utils.waitForElement(driver, btn, timeout))
			throw new NoSuchElementException(elementDescription + " not found in page!!");
		try {
			btn.click();
		} catch (NoSuchElementException e) {
			Log.fail("Failed to click on "+elementDescription+" "+e, driver);		
		}
	}
	
	public static void waitForElementToBeInvisible(WebDriver driver, WebElement webElement, String elementDescription,
			int... timeOutArray) {
		int timeout = timeOutArray.length > 0 ? timeOutArray[0]
				: Integer.parseInt(configProperty.getProperty("maxElementWait"));
		try {
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
			wait.until(ExpectedConditions.invisibilityOf(webElement));
			Log.message(elementDescription + " element is not displayed");
		} catch (TimeoutException e) {
			Log.fail(elementDescription + " element is still displaying" + e, driver);
		}
	}
}
