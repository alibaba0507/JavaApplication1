/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web;

import java.awt.GridBagConstraints;
import java.net.CookieHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.text.Document;
import javax.swing.text.Element;

public class TestBrowsers extends javax.swing.JFrame { 
private static final String ebaySignInUrl="http://how2seduce.blogspot.com";//"https://signin.ebay.co.uk/ws/eBayISAPI.dll?SignIn";      
private JFXPanel fxPanel;
private WebEngine webEngine;          
private String username;
private String pass;    
private boolean loginPage =false;  
public TestBrowsers(String username,String pass) {
    initComponents();
    System.out.println("Threads "+Thread.activeCount());
    System.out.println("Thread "+Thread.currentThread().toString());       
    this.username=username;
    this.pass=pass;      
    initComponents();       
    fxPanel = new JFXPanel();         
    Platform.runLater(new Runnable() {           
        @Override
        public void run() {
            initFxComponents();            
        }
   });             
   this.setSize(850, 650);
}    
  private void initFxComponents(){
      // This method is invoked on the JavaFX thread
      createScene();            
}

private void createScene(){        
    final StackPane root = new StackPane();
    CookieHandler.setDefault(MySessionManager.getInstance());
    final WebView browser = new WebView();   
    System.out.println("Thread count:: "+Thread.activeCount());        
    webEngine = browser.getEngine();    
    webEngine.load(ebaySignInUrl);
    final Scene scene = new Scene(root, 500, 400);
   // System.out.println("Browser loading..");
    //make visible
    setScene(scene);
    root.getChildren().add(browser);
    webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener() {

        @Override
        public void changed(ObservableValue ov, Object t, Object t1) {
            setTitle(webEngine.getLocation());
            Worker.State state = (Worker.State)t1;
            if(state==Worker.State.SUCCEEDED){                
                 if(!loginPage){
                     System.out.println("Login in.. "+webEngine.getLocation());                 
                         org.w3c.dom.Document doc = webEngine.getDocument();
                         org.w3c.dom.Element u = (org.w3c.dom.Element) doc.getElementById("userid");
                        if (u!= null)
                         u.setAttribute("value", username);
                         org.w3c.dom.Element p = (org.w3c.dom.Element)doc.getElementById("pass");
                         if (p !=null)
                         p.setAttribute("value", pass);

                  try{
                  /*
                   webEngine.executeScript("document.getElementById('sgnBt').click()");
                   loginPage=true;
                   *///show threads
                      System.out.println(Thread.currentThread().getName());
                      System.out.println(Thread.currentThread().getId());
                      }catch(Exception ex){
                          ex.printStackTrace();                          
                     }                   
                 }

            }
        } 
     });


}   

private void setScene(Scene scene){
    System.out.println("Displaying..");  
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill=GridBagConstraints.BOTH;
    gbc.weightx=1.0;
    gbc.weighty=1.0;      
    fxPanel.setScene(scene);       
    this.add(fxPanel,gbc);
    this.revalidate();
    this.setTitle(webEngine.getLocation());    
}   
 private void initComponents() {
    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    getContentPane().setLayout(new java.awt.GridBagLayout());        pack();
} 
public static void main(String args[]) {     
   java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {              
            new TestBrowsers("username","password").setVisible(true);             
        }
    });
}

}