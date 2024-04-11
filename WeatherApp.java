import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WeatherApp extends Application 
{
    public static void main(String[] args) 
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) 
    {
        primaryStage.setTitle("Zip Code Input");

        Label label = new Label("Enter your Zip Code:");
        TextField textField = new TextField();

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String zipCode = textField.getText();
            System.out.println("Zip Code entered: " + zipCode);
        });

        VBox vbox = new VBox(10, label, textField, submitButton);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
