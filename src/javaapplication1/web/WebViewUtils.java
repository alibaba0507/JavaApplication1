/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web;

import com.sun.javafx.webkit.WebConsoleListener;
import com.sun.webkit.network.CookieManager;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.naming.ldap.ManageReferralControl;

/**
 *
 * @author alibaba0507
 */
public abstract class WebViewUtils {
     private final WebView webView = new WebView();
     private final WebEngine webEngine = webView.getEngine();
     
     
   //  com.sun.webkit.network.CookieManager manager = new CookieManager();
    
    public WebView createWebView() {
       // webView = new WebView();
        //webEngine = webView.getEngine();
        
        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            if (newLocation != null && newLocation.endsWith("example.com/pathabc")) {
                // webEngine.load("http://example.com/pathxyz");
                
                handleRedirect(oldLocation, newLocation, webView);
            }
        });
        //TODO: Setup Cookie handler
        WebConsoleListener.setDefaultListener(new WebConsoleListener() {
            @Override
            public void messageAdded(WebView webView, String message, int lineNumber, String sourceId) {
                System.out.println("Console: [" + sourceId + ":" + lineNumber + "] " + message);
            }
        });

        webEngine.getLoadWorker().exceptionProperty();
        // webEngine.getLoadWorker().messageProperty();
        //   webEngine.getLoadWorker().progressProperty();
        //  webEngine.getLoadWorker().runningProperty();
        webEngine.getLoadWorker().stateProperty();
        webEngine.getLoadWorker().titleProperty();
        webEngine.getLoadWorker().totalWorkProperty();
        webEngine.getLoadWorker().valueProperty();
        // process page loading
        webEngine.getLoadWorker().stateProperty().addListener(
                ((ov, t, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        handleEndOfLoading(webView);
                    }
                }));
        return webView;
    }
    public void loadHtmlDoc(String s)
    {
        webEngine.loadContent(s, "*/*");
    }
    /**
     *
     * @param oldLocation
     * @param newLocation
     * @param view - get Engine like view.getEngine() and load(newLocation) or
     * create new WebView and newView.getEngine() and newView.load(newLocation)
     */
    public abstract void handleRedirect(String oldLocation, String newLocation, WebView view);
    
    /**
     * To Use this listener first collect all redirects from handleRedirect
     * then when loading is finish execute redirects with new browser
     * @param view 
     */
    public abstract void handleEndOfLoading(WebView view);
}
