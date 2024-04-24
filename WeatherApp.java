import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.geometry.Pos;
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

        // Create an ImageView for the search image
        ImageView imageView = new ImageView(getClass().getResource("Images/search.png").toExternalForm());
        imageView.setFitHeight(18);
        imageView.setFitWidth(18);
        // Create an ImageView for the humidity image
        ImageView humidityImageView = new ImageView(getClass().getResource("Images/humidity.png").toExternalForm());
        humidityImageView.setFitHeight(60);
        humidityImageView.setFitWidth(60);
        // Create an ImageView for the humidity image
        ImageView windspeedImageView = new ImageView(getClass().getResource("Images/windspeed.png").toExternalForm());
        windspeedImageView.setFitHeight(60);
        windspeedImageView.setFitWidth(60);
        // Create an ImageView for the cloudy image
        ImageView cloudyImageView = new ImageView(getClass().getResource("Images/cloudy.png").toExternalForm());
        cloudyImageView.setFitHeight(120);
        cloudyImageView.setFitWidth(120);
        // Create an ImageView for the clear image
        ImageView clearImageView = new ImageView(getClass().getResource("Images/clear.png").toExternalForm());
        clearImageView.setFitHeight(120);
        clearImageView.setFitWidth(120);
        // Create an ImageView for the rain image
        ImageView rainImageView = new ImageView(getClass().getResource("Images/rain.png").toExternalForm());
        rainImageView.setFitHeight(120);
        rainImageView.setFitWidth(120);
        // Create an ImageView for the rain image
        ImageView snowImageView = new ImageView(getClass().getResource("Images/snow.png").toExternalForm());
        snowImageView.setFitHeight(120);
        snowImageView.setFitWidth(120);

        // Create a button and set the graphic to the ImageView instance
        Button getWeatherButton = new Button();
        getWeatherButton.setGraphic(imageView);
        
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

        // Set the preferred width and height of the TextField and Button
        double prefWidth = 200.0;
        locationInput.setPrefWidth(prefWidth);


        // Use HBox for horizontal layout
        HBox search = new HBox(10);
        search.getChildren().addAll(locationInput, getWeatherButton);
        search.setAlignment(Pos.CENTER);

        HBox graphics1 = new HBox(10);
        graphics1.getChildren().addAll(clearImageView, cloudyImageView);
        graphics1.setAlignment(Pos.CENTER);

        HBox graphics2 = new HBox(10);
        graphics2.getChildren().addAll(rainImageView, snowImageView);
        graphics2.setAlignment(Pos.CENTER);

        // Add the HBox and Label to the VBox
        VBox root = new VBox(10);
        root.getChildren().addAll(search, graphics1, graphics2, weatherInfoLabel);

        HBox graphics3 = new HBox(80);
        graphics3.getChildren().addAll(humidityImageView, windspeedImageView);
        graphics3.setAlignment(Pos.CENTER);

        root.getChildren().add(graphics3);
        
        Scene scene = new Scene(root, 300, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Weather App");
        primaryStage.show();
    }
}
