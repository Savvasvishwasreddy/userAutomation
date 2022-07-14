package com.learningservices.itemselection.mfe.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v100.fetch.Fetch;
import org.openqa.selenium.devtools.v100.fetch.model.RequestPattern;
import org.openqa.selenium.devtools.v100.fetch.model.RequestStage;
import org.openqa.selenium.devtools.v100.network.Network;
import org.openqa.selenium.remote.Augmenter;

public class RequestMockUtils {

	/**
	 * Sets the response body.
	 *
	 * @param driver the driver
	 * @param reqUrl the url for which the response body to be modified
	 * @param method the method
	 * @param responseBody the response body
	 */
	public static DevTools setResponse(WebDriver driver, String url, String response, String method) {
		driver = new Augmenter().augment(driver);
		DevTools tool = ((HasDevTools)driver).getDevTools();
		tool.createSession();
		RequestPattern pattern = new RequestPattern(Optional.empty(), Optional.empty(), Optional.of(RequestStage.RESPONSE));
		tool.send(Network.setBypassServiceWorker(true));
		tool.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		tool.send(Fetch.enable(Optional.of(Arrays.asList(pattern)), Optional.empty()));
		tool.addListener(Fetch.requestPaused(),
				req -> {
					System.out.println(req.getRequest().getUrl() + " : " + req.getRequest().getMethod().toLowerCase());
					if(req.getRequest().getUrl().equals(url) && req.getRequest().getMethod().toLowerCase().equals(method)) {
						System.out.println("request matched");
						tool.send(Fetch.fulfillRequest(req.getRequestId(), req.getResponseStatusCode().get(), req.getResponseHeaders(), Optional.empty(), 
								Optional.of(Base64.getEncoder().encodeToString(response.getBytes())), Optional.empty()));
						tool.send(Fetch.disable());
						tool.send(Network.setBypassServiceWorker(false));
						System.out.println("Response text : " + req.getResponseStatusText());
					}  else {
						tool.send(Fetch.continueRequest(req.getRequestId(), Optional.empty(), Optional.empty(), 
								Optional.empty(), Optional.empty(), Optional.empty()));
					}
				});
		return tool;
	}

	/**
	 * Sets the response code.
	 *
	 * @param driver the driver
	 * @param url the url for which the response status code to be modified
	 * @param statusCode the status code
	 * @return the dev tools
	 */
	public static DevTools setResponseCode(WebDriver driver, String url, int statusCode) {
		driver = new Augmenter().augment(driver);
		DevTools tool = ((HasDevTools)driver).getDevTools();
		tool.createSession();
		tool.send(Network.setBypassServiceWorker(true));
		tool.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		tool.send(Fetch.enable(Optional.empty(), Optional.empty()));		
		tool.addListener(Fetch.requestPaused(), req -> {
			if(req.getRequest().getUrl().contains(url)) {
				tool.send(Fetch.fulfillRequest(req.getRequestId(), statusCode, Optional.empty(), 
						Optional.empty(), Optional.empty(), Optional.empty()));	
			} else {
				tool.send(Fetch.continueRequest(req.getRequestId(), Optional.empty(), Optional.empty(), 
						Optional.empty(), Optional.empty(), Optional.empty()));
			}
		}); 
		return tool;
	}

	/**
	 * Sets the response body.
	 *
	 * @param driver the driver
	 * @param reqUrl the request url
	 * @param reqPayloadMatchingText key text to be compared with post body.
	 * @param method get / post / delete
	 * @param responseBody Body to be mocked
	 */
	public static DevTools setResponse(WebDriver driver, String reqUrl, String reqPayloadMatchingText, String method, String responseBody) {
		driver = new Augmenter().augment(driver);
		DevTools tool = ((HasDevTools)driver).getDevTools();
		tool.createSession();
		RequestPattern pattern = new RequestPattern(Optional.empty(), Optional.empty(), Optional.of(RequestStage.RESPONSE));
		tool.send(Network.setBypassServiceWorker(true));
		tool.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		tool.send(Fetch.enable(Optional.of(Arrays.asList(pattern)), Optional.empty()));
		tool.addListener(Fetch.requestPaused(),
				req -> {
					if(req.getRequest().getUrl().equals(reqUrl) && !req.getRequest().getHasPostData().isEmpty() && req.getRequest().getPostData().get().contains(reqPayloadMatchingText) && req.getRequest().getMethod().toLowerCase().equals(method)) {
						System.out.println("request matched");
						tool.send(Fetch.fulfillRequest(req.getRequestId(), req.getResponseStatusCode().get(), req.getResponseHeaders(), Optional.empty(), 
								Optional.of(Base64.getEncoder().encodeToString(responseBody.getBytes())), Optional.empty()));
						tool.send(Fetch.disable());
						tool.send(Network.setBypassServiceWorker(false));
					}  else {
						tool.send(Fetch.continueRequest(req.getRequestId(), Optional.empty(), Optional.empty(), 
								Optional.empty(), Optional.empty(), Optional.empty()));
					}
				});
		return tool;
	}

	/**
	 * Used clear and close the listener
	 * @param tool - Current dev tool
	 */
	public static void closeMock(DevTools tool) {
		tool.clearListeners();
		tool.close();
	}
	
	/**
	 * 
	 * @param driver
	 * @return
	 */
	public static void networkDisable(WebDriver driver) {
		driver = new Augmenter().augment(driver);
		DevTools tool = ((HasDevTools)driver).getDevTools();
		tool.createSession();
		tool.send(Network.enable(Optional.ofNullable(0), Optional.ofNullable(0), Optional.ofNullable(0)));
		tool.send(Network.emulateNetworkConditions(
				true,
				50,
				15000000,
				7000000,
				Optional.empty()
				));
	}	
}