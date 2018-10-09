/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web.browser;


import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javaapplication1.web.proxy.TorURLStreamHandlerFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author alibaba0507
 */
public class TorBrowser extends Region{

    final WebView browser;
    final WebEngine webEngine;
    private int torPort = 10001;
    private NewTabInitListener tabListener;
    public TorBrowser(int torPort,NewTabInitListener tabListener) {
        super();
        if (torPort > 0) {
            this.torPort = torPort;
        }
        this.tabListener = tabListener;
        // Cookies
        CookieStore store = new MyCookieStore();
        CookiePolicy policy = new MyCookiePolicy();
        CookieManager handler = new CookieManager(store, policy);
        CookieHandler.setDefault(handler);
        URL.setURLStreamHandlerFactory(new TorURLStreamHandlerFactory());
        browser = new WebView();
        webEngine = browser.getEngine();

        // ennable javascript
        webEngine.setJavaScriptEnabled(true);
        
        
        // redirect
        webEngine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
               
                if (newValue != null && (newValue.startsWith("http")))
                {
                    if (TorBrowser.this.tabListener != null)
                        TorBrowser.this.tabListener.createNewTab(newValue, TorBrowser.this.torPort);
                }else
                {
                    load(newValue);
                    //urlField.setText(newValue);
                }System.out.println("OLD[" + oldValue + "] => [" + newValue + "]");
            }
        });
        getChildren().add(browser);
    }

    public void load(String url) {
        // create new protocol for tor connection
        url = "tor://" + torPort + "/" + url;
        webEngine.load(url);
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER.CENTER, VPos.CENTER);
    }

    protected double computePrefWidth(double height) {
        return 750;
    }

    protected double computePrefHeight(double width) {
        return 500;
    }

}

class MyCookiePolicy implements CookiePolicy {

    public boolean shouldAccept(URI uri, HttpCookie cookie) {
//    String host = uri.getHost();
        //  return host.equals("localhost");
       // Iterator it = cookie.
        System.out.println("Cookie [" +uri.toString() + "][" + cookie.getValue() + "]");
        return true;
    }
}

class MyCookieStore implements CookieStore {

    private Map<URI, List<HttpCookie>> map = new HashMap<URI, List<HttpCookie>>();

    public void add(URI uri, HttpCookie cookie) {
        List<HttpCookie> cookies = map.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<HttpCookie>();
            map.put(uri, cookies);
        }
        cookies.add(cookie);
    }

    public List<HttpCookie> get(URI uri) {
        List<HttpCookie> cookies = map.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<HttpCookie>();
            map.put(uri, cookies);
        }
        return cookies;
    }

    public List<HttpCookie> getCookies() {
        Collection<List<HttpCookie>> values = map.values();
        List<HttpCookie> result = new ArrayList<HttpCookie>();
        for (List<HttpCookie> value : values) {
            result.addAll(value);
        }
        return result;
    }

    public List<URI> getURIs() {
        Set<URI> keys = map.keySet();
        return new ArrayList<URI>(keys);

    }

    public boolean remove(URI uri, HttpCookie cookie) {
        List<HttpCookie> cookies = map.get(uri);
        if (cookies == null) {
            return false;
        }
        return cookies.remove(cookie);
    }

    public boolean removeAll() {
        map.clear();
        return true;
    }
}
