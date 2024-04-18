import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class WeatherApp extends Application 
{

    public static void main(String[] args) 
    {
        launch(args);        
    }
    
    public static String parse(String responseBody) 
    {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
            JSONObject current = (JSONObject) jsonResponse.get("current");
            double temp_f = ((double) current.get("temp_f"));
            int humidity = ((Long) current.get("humidity")).intValue();
            double wind_mph = ((double) current.get("wind_mph"));
            String condition = ((JSONObject) current.get("condition")).get("text").toString();
            
            
            JSONObject location = (JSONObject) jsonResponse.get("location");
            String name = (String) location.get("name");
            System.out.println("Location entered: " + name);
            
            return "Location: " + name + "\nHumidity: " + humidity + "%\nTempearture: " + temp_f + "°F\nCondition: " + condition +  "\nWind Speed: " + wind_mph;
        } catch (ParseException e) 
        
        {
            e.printStackTrace();   
        }
        return "Failed to parse weather information";
    }

    
    @Override
    public void start(Stage primaryStage) 
    {
        TextField locationInput = new TextField();
        Button getWeatherButton = new Button("Get Weather");
        Label weatherInfoLabel = new Label();

        getWeatherButton.setOnAction(event -> 
        {
            
            String zipCode = locationInput.getText(); //gets zipcode entered by user
            HttpClient client = HttpClient.newHttpClient();
            
            //System.out.println("Request URL: " + "https://api.weatherapi.com/v1/current.json?key=65ec47db0e5246189fb163942241604&q=" + zipCode); 
            
            // Print the constructed URL
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.weatherapi.com/v1/current.json?key=65ec47db0e5246189fb163942241604&q=" + zipCode))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    //.thenApply(WeatherApp::parse)
                    .thenAccept(System.out::println)
                    .join();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        
                        // Parse the JSON response and update the weatherInfoLabel
                        System.out.println("Response Body: " + responseBody);
                        String weatherInfo = parse(responseBody);
                        Platform.runLater(() -> weatherInfoLabel.setText(weatherInfo));
                    })
                    .exceptionally(ex -> 
                    {
                        // Handle exceptions
                        ex.printStackTrace();
                        Platform.runLater(() -> 
                        {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Failed to get weather information");
                            alert.setContentText("An error occurred while fetching weather information. Please try again.");
                            alert.showAndWait();
                        });
                        return null;
                    });
        });
        
        VBox root = new VBox(10);
        root.getChildren().addAll(locationInput, getWeatherButton, weatherInfoLabel);
        
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Weather App");
        primaryStage.show();
    }
}
