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
    private VBox root; // Declaring root as a class-level variable
    private String condition = "";

    @Override
    public void start(Stage primaryStage) 
    {
        TextField locationInput = new TextField();
        locationInput.setStyle("-fx-font-size: 18px;");
        locationInput.setPrefWidth(200);
        
        // Create an ImageView for the search image
        ImageView imageView = new ImageView(getClass().getResource("Images/search.png").toExternalForm());
        imageView.setFitHeight(33);
        imageView.setFitWidth(33);
        
        // Create an ImageView for the humidity image
        ImageView humidityImageView = new ImageView(getClass().getResource("Images/humidity.png").toExternalForm());
        humidityImageView.setFitHeight(60);
        humidityImageView.setFitWidth(60);
        
        // Create an ImageView for the windspeed image
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
        
        // Create an ImageView for the snow image
        ImageView snowImageView = new ImageView(getClass().getResource("Images/snow.png").toExternalForm());
        snowImageView.setFitHeight(120);
        snowImageView.setFitWidth(120);

        // Create a button and set the graphic to the ImageView instance
        Button getWeatherButton = new Button();
        getWeatherButton.setGraphic(imageView);
        
        // Create Labels for weather information
        Label locationLabel = new Label("Location : ");
        locationLabel.setStyle("-fx-font-size: 18px;");

        Label humidityLabel = new Label("Humidity: ");
        humidityLabel.setStyle("-fx-font-size: 18px;");

        Label temperatureLabel = new Label("Temperature: ");
        temperatureLabel.setStyle("-fx-font-size: 18px;");

        Label conditionLabel = new Label("Condition: ");
        conditionLabel.setStyle("-fx-font-size: 18px;");

        Label windSpeedLabel = new Label("Wind Speed: ");
        windSpeedLabel.setStyle("-fx-font-size: 18px;");

        getWeatherButton.setOnAction(event -> 
        {
            //gets zipcode entered by user
            String zipCode = locationInput.getText(); 
            HttpClient client = HttpClient.newHttpClient();
            
            //System.out.println("Request URL: " + "https://api.weatherapi.com/v1/current.json?key=65ec47db0e5246189fb163942241604&q=" + zipCode); 
            
            // Print the constructed URL
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.weatherapi.com/v1/current.json?key=65ec47db0e5246189fb163942241604&q=" + zipCode))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> 
                    {             
                               
                        // Update Labels with parsed weather data
                        Platform.runLater(() -> 
                        {
                            locationLabel.setText("Location: " + parseLocation(responseBody));
                            humidityLabel.setText("Humidity: " + parseHumidity(responseBody) + "%");
                            temperatureLabel.setText("Temperature: " + parseTemperature(responseBody) + "°F");
                            conditionLabel.setText("Condition: " + parseCondition(responseBody));
                            windSpeedLabel.setText("Wind Speed: " + parseWindSpeed(responseBody) + " mph");

                            // set image based on the condition
                            String condition = parseCondition(responseBody);
                            if (condition.equals("rain")) {
                                root.getChildren().removeAll(clearImageView, cloudyImageView, snowImageView);
                                root.getChildren().add(rainImageView);
                            } else if (condition.equals("clear")) {
                                root.getChildren().removeAll(rainImageView, cloudyImageView, snowImageView);
                                root.getChildren().add(clearImageView);
                            } else if (condition.equals("cloudy")) {
                                root.getChildren().removeAll(rainImageView, clearImageView, snowImageView);
                                root.getChildren().add(cloudyImageView);
                            } else if (condition.equals("snow")) {
                                root.getChildren().removeAll(rainImageView, clearImageView, cloudyImageView);
                                root.getChildren().add(snowImageView);
                            } 
                        });
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

        //layout code

        // Use HBox for horizontal layout
        HBox search = new HBox(10);
        search.getChildren().addAll(locationInput, getWeatherButton);
        search.setAlignment(Pos.CENTER);

        //Add Hbox and location
        VBox inputVBox = new VBox(10);
        inputVBox.getChildren().addAll(search, locationLabel);
        inputVBox.setAlignment(Pos.CENTER);

        // Humidity image and data
        VBox humidityVBox = new VBox(10);
        humidityVBox.getChildren().addAll(humidityImageView, humidityLabel);
        humidityVBox.setAlignment(Pos.CENTER);

        //Windspeed image and data
        VBox winspeedVBox = new VBox(10);
        winspeedVBox.getChildren().addAll(windspeedImageView, windSpeedLabel);
        winspeedVBox.setAlignment(Pos.CENTER);

        // Assign the VBox root
        root = new VBox(20);
        root.getChildren().addAll(inputVBox, temperatureLabel, humidityVBox, winspeedVBox, conditionLabel);
        root.setAlignment(Pos.CENTER);

        if (condition.equals("rain")) {
            root.getChildren().removeAll(clearImageView, cloudyImageView, snowImageView);
            root.getChildren().addAll(rainImageView, conditionLabel); // Add rainImageView above conditionLabel
        } else if (condition.equals("clear")) {
            root.getChildren().removeAll(rainImageView, cloudyImageView, snowImageView);
            root.getChildren().addAll(clearImageView, conditionLabel); // Add clearImageView above conditionLabel
        } else if (condition.equals("cloudy")) {
            root.getChildren().removeAll(rainImageView, clearImageView, snowImageView);
            root.getChildren().addAll(cloudyImageView, conditionLabel); // Add cloudyImageView above conditionLabel
        } else if (condition.equals("snow")) {
            root.getChildren().removeAll(rainImageView, clearImageView, cloudyImageView);
            root.getChildren().addAll(snowImageView, conditionLabel); // Add snowImageView above conditionLabel
        }

        // background color
        root.setStyle("-fx-background-color: #b294e3;");
        
        Scene scene = new Scene(root, 300, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Weather App");
        primaryStage.show();

    }

    private String parseLocation(String responseBody) 
{
    JSONParser parser = new JSONParser();
    try 
    {
        JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
        JSONObject location = (JSONObject) jsonResponse.get("location");
        String name = (String) location.get("name");
        return name;
    } 
    catch (ParseException e) 
    {
        e.printStackTrace();   
    }
    return "Failed to parse location";
}

private int parseHumidity(String responseBody) 
{
    JSONParser parser = new JSONParser();
    try 
    {
        JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
        JSONObject current = (JSONObject) jsonResponse.get("current");
        int humidity = ((Long) current.get("humidity")).intValue();
        return humidity;
    } 
    catch (ParseException e) 
    {
        e.printStackTrace();   
    }
    return -1; // Return -1 as an error value
}

private double parseTemperature(String responseBody) 
{
    JSONParser parser = new JSONParser();
    try 
    {
        JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
        JSONObject current = (JSONObject) jsonResponse.get("current");
        double temp_f = ((double) current.get("temp_f"));
        return temp_f;
    } 
    catch (ParseException e) 
    {
        e.printStackTrace();   
    }
    return -1.0; // Return -1.0 as an error value
}

private String parseCondition(String responseBody) 
{
    JSONParser parser = new JSONParser();
    try 
    {
        JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
        JSONObject current = (JSONObject) jsonResponse.get("current");
        String condition = ((JSONObject) current.get("condition")).get("text").toString().toLowerCase();
        
            // condition for rain image
        if (condition.contains("rain") || condition.contains("shower") || condition.contains("thundery") || condition.contains("drizzle")) 
        {
            return "rain";
        }   
            // Condition for cloudy image
        else if (condition.contains("cloudy") || condition.contains("overcast")) 
        {
            return "cloudy";
        }   // Condition for clear image
        else if (condition.contains("sunny") || condition.contains("clear")) 
        {
            return "clear";
        }   // Condition for snow image
        else if (condition.contains("snow") || condition.contains("blizzard")) 
        {
            return "snow";
        } 
        else 
        {
            // Return other conditions
            return condition;
        }
    } 
    catch (ParseException e) 
    {
        e.printStackTrace();   
    }
    return "Failed to parse condition";
}

private double parseWindSpeed(String responseBody) 
{
    JSONParser parser = new JSONParser();
    try 
    {
        JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
        JSONObject current = (JSONObject) jsonResponse.get("current");
        double wind_mph = ((double) current.get("wind_mph"));
        return wind_mph;
    } 
    catch (ParseException e) 
    {
        e.printStackTrace();   
    }
    return -1.0; // Return -1.0 as an error value
}

public static void main(String[] args) 
{
    launch(args);        
}
    
}
