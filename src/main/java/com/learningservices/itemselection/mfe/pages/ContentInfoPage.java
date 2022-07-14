package com.learningservices.itemselection.mfe.pages;

import java.util.ArrayList;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.testng.Assert;

import com.learningservices.itemselection.mfe.utils.ItemSelectionBrowserActions;
import com.learningservices.itemselection.mfe.utils.WaitUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.ShadowDOMUtils;
import com.learningservices.utils.Utils;

import LSTFAI.customfactories.AjaxElementLocatorFactory;
import LSTFAI.customfactories.IFindBy;
import LSTFAI.customfactories.PageFactory;

public class ContentInfoPage extends LoadableComponent<ContentInfoPage> {

	WebDriver driver;
	private boolean isPageLoaded;
	public ElementLayer elementLayer;
	public static List<Object> pageFactoryKey = new ArrayList<Object>();
	public static List<String> pageFactoryValue = new ArrayList<String>();

	@IFindBy(how = How.CSS, using = "div.header-title__wrapper", AI = false)
	public WebElement lblHeaderForInfoPage;

	@IFindBy(how=How.XPATH, using ="//cel-button[contains(@class,'header-back__btn')]", AI = false)
	public WebElement btnExit;
	/**********************************************************************************************************
	 ************************************ShadowDOM********************************************************
	 ***********************************************************************************************************/
	//public String btnExit[]= {"cel-button","document.querySelector('cel-button').shadowRoot.querySelector('span')"};

	public ContentInfoPage() {}

	public ContentInfoPage(WebDriver driver){
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
		elementLayer = new ElementLayer(driver);
	}

	@Override
	protected void load() {
		isPageLoaded = true;
		WaitUtils.waitForPageLoad(driver);
	}// load

	@Override
	protected void isLoaded() {
		if (!isPageLoaded) {
			Assert.fail();
		}

		//WebElement exitButton =ShadowDOMUtils.getWebElement(driver, this.btnExit[0], this.btnExit[1]);
		if (isPageLoaded && !(Utils.waitForElement(driver, btnExit))) {
			Log.fail("Page did not open up. Site might be down.", driver);
		}
		elementLayer = new ElementLayer(driver);
	}// isLoaded

	/**
	 * To click on exit button for info content
	 * @param driver
	 * @throws InterruptedException 
	 */
	public  ContentInfoPage clickExitContent(WebDriver driver, String contentName) throws InterruptedException {
		//Utils.waitForElement(driver, driver.findElement(By.xpath("//*[normalize-space(text())='" + contentName + "']")), 30);
		String contentTxt = "//*[normalize-space(text())='" + contentName + "']";
		int waitTime = 30;
		boolean elementDisplayed = false;
		for(int i=0; i<waitTime; i++) {
			if(driver.findElements(By.xpath(contentTxt)).size() > 0) {
				Log.event("Exit content displayed");
				elementDisplayed = true;
				break;
			}
			Thread.sleep(500);
		}
		Log.assertThat(elementDisplayed, "Exit content displayed", "Exit content not displayed", driver);
		try {
			//WebElement exitButton =ShadowDOMUtils.getWebElement(driver, this.btnExit[0], this.btnExit[1]);
			ItemSelectionBrowserActions.click(driver, btnExit, "exit button");
		} catch (Exception e) {
			ItemSelectionBrowserActions.click(driver, driver.findElement(By.cssSelector("div.backNavArrow span")), "exit button");
		}
		return this;
	}
}
