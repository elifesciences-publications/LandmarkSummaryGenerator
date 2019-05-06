package landmarksummarygenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Dr. Felix Seifert, cropSeq bioinformatics
 */
public class LandmarkSummaryGenerator extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LandmarkSummary.fxml"));
        Parent parent = (Parent) fxmlLoader.load();
        Scene scene = new Scene(parent);
        
        LandmarkSummaryGeneratorController landmarkSummaryGeneratorController = fxmlLoader.getController();
        landmarkSummaryGeneratorController.setStage(stage);
        
        stage.setTitle("Landmark Summary Generator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
