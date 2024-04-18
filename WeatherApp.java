import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class WeatherApp extends Application {

    private static final String API_KEY = "e98c756e44024074930205054240904";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather App");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label zipLabel = new Label("Enter Zip Code:");
        GridPane.setConstraints(zipLabel, 0, 0);

        TextField zipField = new TextField();
        GridPane.setConstraints(zipField, 1, 0);

        Label countryLabel = new Label("Enter Country Code:");
        GridPane.setConstraints(countryLabel, 0, 1);

        TextField countryField = new TextField();
        GridPane.setConstraints(countryField, 1, 1);

        Button submitButton = new Button("Submit");
        GridPane.setConstraints(submitButton, 0, 2);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        GridPane.setConstraints(resultArea, 0, 3, 2, 1);

        submitButton.setOnAction(e -> {
            String zip = zipField.getText();
            String country = countryField.getText();
            String weatherData = getWeatherData(zip, country);
            resultArea.setText(weatherData);
        });

        grid.getChildren().addAll(zipLabel, zipField, countryLabel, countryField, submitButton, resultArea);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String getWeatherData(String zip, String country) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(
                    "http://api.openweathermap.org/geo/1.0/zip?zip=" + zip + "," + country + "&appid=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject locationData = new JSONObject(response.toString());
                double lat = locationData.getDouble("lat");
                double lon = locationData.getDouble("lon");
                String cityName = locationData.getString("name");

                URL weatherUrl = new URL("https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon
                        + "&exclude=minutely,daily&appid=" + API_KEY + "&units=imperial");
                HttpURLConnection weatherConn = (HttpURLConnection) weatherUrl.openConnection();
                weatherConn.setRequestMethod("GET");

                int weatherStatus = weatherConn.getResponseCode();
                if (weatherStatus == 200) {
                    BufferedReader weatherIn = new BufferedReader(new InputStreamReader(weatherConn.getInputStream()));
                    StringBuilder weatherResponse = new StringBuilder();
                    while ((inputLine = weatherIn.readLine()) != null) {
                        weatherResponse.append(inputLine);
                    }
                    weatherIn.close();

                    JSONObject weatherData = new JSONObject(weatherResponse.toString());
                    JSONArray hourly = weatherData.getJSONArray("hourly");

                    result.append(cityName).append("\n");
                    result.append("----------------------------\n");
                    for (int i = 0; i < 12; i++) {
                        JSONObject hour = hourly.getJSONObject(i);
                        long timestamp = hour.getLong("dt");
                        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC);
                        double temp = hour.getDouble("temp");
                        double feelsLike = hour.getDouble("feels_like");
                        int pop = hour.getInt("pop");
                        result.append(dateTime).append(" Temp: ").append(temp).append(" Feels Like: ").append(feelsLike)
                                .append(" Pop: ").append(pop).append("\n");
                    }
                } else {
                    result.append("Error fetching weather data.");
                }
                weatherConn.disconnect();
            } else {
                result.append("Error fetching location data.");
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            result.append("An error occurred.");
        }
        return result.toString();
    }
}
