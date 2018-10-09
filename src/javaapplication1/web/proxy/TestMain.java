/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web.proxy;

import java.net.URL;

/**
 *
 * @author alibaba0507
 */
public class TestMain {

    public static void main(String[] args) {
        String u = "tor://10001/http://how2seduce.com/ssdddd?s=weewew&r=assda";
        try {
            URL.setURLStreamHandlerFactory(new TorURLStreamHandlerFactory());
            URL url = new URL(u);
            System.out.println(" Protocol[" + url.getProtocol() + "]");
            System.out.println(" Host[" + url.getHost()+ "]");
            System.out.println(" Flle[" + url.getFile().substring(1) + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
