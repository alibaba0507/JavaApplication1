/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web.browser;

/**
 *
 * @author alibaba0507
 */
public interface NewTabInitListener {
    public TorBrowser createNewTab(String url,int proxyPort);
}
