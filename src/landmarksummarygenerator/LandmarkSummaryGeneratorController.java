package landmarksummarygenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Dr. Felix Seifert, cropSeq bioinformatics
 */
public class LandmarkSummaryGeneratorController implements Initializable {
    private Stage stage;
    
    @FXML 
    private Button summarizeDatasetsButton;
    
    @FXML
    private Label landmarkDefinitionLabel;
    
    @FXML
    private ListView datasetListView;

    private TreeMap<String,File> datasetFileTreeMap;
    private ObservableList observableList;

    private String landmarkDefinitionFileName;
    
    /* ButtonAction for "Add dataset" button */    
    @FXML
    private void addLandmarkDataset(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Landmark Dataset File");
        List<File> selectedFileList = fileChooser.showOpenMultipleDialog(stage);
    
        if(selectedFileList != null) {
            for(File selectedFile : selectedFileList) {
                if(!LandmarkTimingSummary.isValidSampleFileFormat(selectedFile.getAbsolutePath())) {
                    return;
                }

                String datasetTitle = null;
                try {
                    datasetTitle = LandmarkTimingSummary.getDatasetTitle(selectedFile.getAbsolutePath());
                }
                catch(IOException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("I/O exception");
                    alert.setHeaderText(null);
                    alert.setContentText(e.toString());
                    alert.showAndWait();

                    return;
                }
                
                if((datasetTitle != null) && (datasetFileTreeMap.get(datasetTitle) == null)) {
                    datasetFileTreeMap.put(datasetTitle, selectedFile);
                }
            }
            
            if(!datasetFileTreeMap.isEmpty() && (landmarkDefinitionFileName != null)) {
                summarizeDatasetsButton.setDisable(false);
            }

            observableList.setAll(datasetFileTreeMap.keySet());
        }
    }
    
    /* ButtonAction for "About" button */    
    @FXML
    private void infoButtonAction(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Landmark Summary Generator");
        alert.setContentText("Version: 1.4.1 (29.03.2019)\n\nAuthor: Dr. Felix Seifert, cropSeq bioinformatics\n\nE-mail: felix.seifert@cropseq.com\nWebsite: http://www.cropseq.com");
        alert.showAndWait();
    }
    
    /* ButtonAction for "Load landmark definition" button */    
    @FXML
    private void loadLandmarkDefinition(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Landmark Definition File");
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if(selectedFile != null) {
            landmarkDefinitionFileName = selectedFile.getAbsolutePath();
            
            int landmarkDefinitionCount = 0;
            
            try {
                landmarkDefinitionCount = LandmarkTimingSummary.getLandmarkDefinitionSummary(landmarkDefinitionFileName);
            }
            catch(IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("I/O exception");
                alert.setHeaderText(null);
                alert.setContentText(e.toString());
                alert.showAndWait();
                    
                return;
            }
            
            /* Output information about landmark definition in GUI */
            if(landmarkDefinitionCount < 1) {
                landmarkDefinitionLabel.setText("undefined");
                summarizeDatasetsButton.setDisable(true);
            }
            else {
                landmarkDefinitionLabel.setText(landmarkDefinitionCount + " landmarks");
                
                if(!datasetFileTreeMap.isEmpty()) {
                    summarizeDatasetsButton.setDisable(false);
                }
            }
        }
    }
    
    /* ButtonAction for "Remove selected dataset" button */    
    @FXML
    private void removeLandmarkDataset(ActionEvent event) {
        Iterator<String> selectedItemIterator = datasetListView.getSelectionModel().getSelectedItems().iterator();
        
        /* Remove all selected dataset files */
        while(selectedItemIterator.hasNext()) {
            datasetFileTreeMap.remove(selectedItemIterator.next());
        }

        /* No dataset file available, set summary button to disable */
        if(datasetFileTreeMap.isEmpty()) {
            summarizeDatasetsButton.setDisable(true);
        }
        
        /* Refresh dataset file output */
        observableList.setAll(datasetFileTreeMap.keySet());
    }

    /* ButtonAction for "Summarize dataset and write to file" button */    
    @FXML
    private void summarizeLandmarkDatasets(ActionEvent event) {
        try {
            LandmarkTimingSummary landmarkTimingSummary = new LandmarkTimingSummary(landmarkDefinitionFileName);
                
            /* Load landmark sample data from files */
            for(String landmarkDataset : datasetFileTreeMap.keySet()) {
                landmarkTimingSummary.loadSampleLandmarkTiming(datasetFileTreeMap.get(landmarkDataset).getAbsolutePath());
            }

            /* Retrieve landmark sample timings */
            HashMap<String,HashMap<String,Double>> landmarkDurationSummaryHashMap = landmarkTimingSummary.getLandmarkDuration();            

            /* Store sample titles in TreeMap for alphabetic order access */
            TreeMap<String,Boolean> sampleTitleTreeMap = new TreeMap();
            for(String sampleTitle : landmarkDurationSummaryHashMap.keySet()) {
                sampleTitleTreeMap.put(sampleTitle, Boolean.TRUE);
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save landmark summary");
            File outputFile = fileChooser.showSaveDialog(stage);

            if(outputFile != null) {
                String outputFileName = outputFile.getAbsolutePath();

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName));

                /* Write output header to result file */
                bufferedWriter.append("sample");
                for(String landmark : landmarkTimingSummary.getLandmarks()) {
                    bufferedWriter.append("," + landmark);
                }
                bufferedWriter.newLine();

                /* Write sample landmark duration to result file */
                for(String sampleTitle : sampleTitleTreeMap.keySet()) {
                    bufferedWriter.append(sampleTitle);

                    for(String landmark : landmarkTimingSummary.getLandmarks()) {
                        bufferedWriter.append(",");

                        if(landmarkDurationSummaryHashMap.get(sampleTitle).get(landmark) != null) {
                            bufferedWriter.append(landmarkDurationSummaryHashMap.get(sampleTitle).get(landmark).toString());
                        }
                    }

                    bufferedWriter.newLine();
                }

                bufferedWriter.flush();
                bufferedWriter.close();
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Completed");
            alert.setHeaderText(null);
            alert.setContentText("The landmark duration summary has successfully been generated.");
            alert.showAndWait();
        }
        catch(IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("I/O exception");
            alert.setHeaderText(null);
            alert.setContentText(e.toString());
            alert.showAndWait();

            return;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        datasetFileTreeMap = new TreeMap();
        observableList = FXCollections.observableList(new ArrayList());
        
        datasetListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        datasetListView.setItems(observableList);        
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
