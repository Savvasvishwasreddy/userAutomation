package com.learningservices.itemselection.mfe.pages;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ByIdOrName;
import org.openqa.selenium.support.How;

import LSTFAI.customfactories.IFindBy;
import LSTFAI.customfactories.WebDriverEventListener;
import LSTFAI.customfactories.WriteProperties;

public class EventListener implements WebDriverEventListener {

	public String callerClassPackage, callerClassName, temp, desiredKey, desiredClass;
	public Field fieldKey, fieldValue, desiredField;
	public List<String> fieldKeyList = new ArrayList<String>();
	public List<String> fieldValueList = new ArrayList<String>();

	public void afterFindBy(By by, WebElement element, WebDriver driver) {

		try {
			callerClassPackage = this.getClass().getName().toString();
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();

			for (StackTraceElement className : trace) {
				if ((className.getClassName().toString()).contains(callerClassPackage.substring(0, callerClassPackage.lastIndexOf("."))) && !((className.getClassName().toString().contains("EventListener")))) {

					flushPageFactoryList();
					callerClassName = className.getClassName().toString();		

					@SuppressWarnings("deprecation")
					Object currentInstantiatedClass = Class.forName(className.getClassName().toString()).getDeclaredConstructor().newInstance();

					fieldKey = currentInstantiatedClass.getClass().getField("pageFactoryKey");
					temp = fieldKey.get(currentInstantiatedClass).toString();
					for(String key : temp.substring(1, temp.length()-1).split(","))
						fieldKeyList.add(key.trim());

					fieldValue = currentInstantiatedClass.getClass().getField("pageFactoryValue");
					temp = fieldValue.get(currentInstantiatedClass).toString();
					for(String value : temp.substring(1, temp.length()-1).split(", B"))
						fieldValueList.add(("B"+value.trim()).replaceFirst("BBy.", "By."));

					if ((fieldValueList.indexOf(by.toString()) != -1)) {
						desiredKey  = fieldKeyList.get(fieldValueList.indexOf(by.toString()));
						desiredClass = currentInstantiatedClass.getClass().getSimpleName();

						desiredField = currentInstantiatedClass.getClass().getDeclaredField(desiredKey);
						desiredField.setAccessible(true);
						//System.out.println("I am currently calling Key is: " + desiredKey+ " className is:" +desiredClass);

						Type desiredKeyType = desiredField.getGenericType();
						String sb = by.toString();
						IFindBy ifindBy = desiredField.getAnnotation(IFindBy.class);
						if(desiredKeyType.toString().contains("List")) {
							String desiredChild = ifindBy.child();

							if(by.toString().contains("By.cssSelector: "))
								sb = sb.replace("By.cssSelector: ", "");
							else
								sb = sb.replace(sb.substring(0, sb.indexOf(": ")),"");
							sb = sb.replace(ifindBy.child(),"");
							if(sb.contains(",")) {
								WriteProperties.getLocatorForWebelement(driver, driver.findElement(By.cssSelector(sb)),desiredKey,desiredClass, desiredChild);
							}	else {
								How how = ifindBy.how();

								switch (how) {
								case CLASS_NAME:
									by = By.className(sb);
									break;

								case CSS:
									by = By.cssSelector(sb);
									break;

								case ID:
								case UNSET:
									by = By.id(sb);
									break;

								case ID_OR_NAME:
									by = new ByIdOrName(sb);
									break;

								case LINK_TEXT:
									by = By.linkText(sb);
									break;

								case NAME:
									by =  By.name(sb);

								case PARTIAL_LINK_TEXT:
									by =  By.partialLinkText(sb);
									break;

								case TAG_NAME:
									by = By.tagName(sb);
									break;

								case XPATH:
									by = By.xpath(sb);
									break;

								}

								WriteProperties.getLocatorForWebelement(driver, driver.findElements(by).get(0),desiredKey,desiredClass, desiredChild);

							}
						}

						else
							WriteProperties.getLocatorForWebelement(driver, driver.findElement(by),desiredKey,desiredClass);

					}
					break;
				}
			}

		} catch (NoSuchFieldException | InstantiationException | IllegalAccessException | ClassNotFoundException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void flushPageFactoryList() {

		fieldKeyList.clear();
		fieldValueList.clear();

	}

	@Override
	public void beforeAlertAccept(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterAlertAccept(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterAlertDismiss(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeAlertDismiss(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeNavigateTo(String url, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterNavigateTo(String url, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeNavigateBack(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterNavigateBack(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeNavigateForward(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterNavigateForward(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeNavigateRefresh(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterNavigateRefresh(WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeFindBy(By by, WebElement element, WebDriver driver) {
	}

	@Override
	public void beforeClickOn(WebElement element, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterClickOn(WebElement element, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeScript(String script, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterScript(String script, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onException(Throwable throwable, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeSwitchToWindow(String windowName, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterSwitchToWindow(String windowName, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public <X> void beforeGetScreenshotAs(OutputType<X> target) {
		// TODO Auto-generated method stub

	}

	@Override
	public <X> void afterGetScreenshotAs(OutputType<X> target, X screenshot) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeGetText(WebElement element, WebDriver driver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterGetText(WebElement element, WebDriver driver, String text) {
		// TODO Auto-generated method stub

	}


}
