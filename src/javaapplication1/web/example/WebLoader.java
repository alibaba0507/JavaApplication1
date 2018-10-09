/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1.web.example;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
/**
 *
 * @author alibaba0507
 */
public class WebLoader extends Application {
    VBox vb = new VBox();
    private String url;
  
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    vb.setId("root");

    WebView  browser = new WebView();
    WebEngine engine = browser.getEngine();
     url = "http://how2seduce.blogspot.com";
    engine.load(url);
    engine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
                  //  if (newLocation != null && newLocation.endsWith("example.com/pathabc")) {
                   System.out.println("OLD[" + oldLocation + "] => [" + newLocation + "]");
                  engine.load(newLocation);
                   // }
                });
    vb.setPadding(new Insets(30, 50, 50, 50));
    vb.setSpacing(10);
    vb.setAlignment(Pos.CENTER);
    vb.getChildren().addAll(browser);

    Scene scene = new Scene(vb);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
