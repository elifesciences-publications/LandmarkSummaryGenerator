package landmarksummarygenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;

/**
 *
 * @author Dr. Felix Seifert, cropSeq bioinformatics
 */
public class LandmarkTimingSummary {
    public LandmarkTimingSummary(String landmarkDefinitionFileName) throws IOException {
        landmarkData = new LandmarkData();
        
        this.loadLandmarkDefinition(landmarkDefinitionFileName);
    }
    
    public LandmarkData getLandmarkData() {
        return landmarkData;
    }
    
    private HashMap<String,HashMap<String,Double>> getLandmarkStart() {
        HashMap<String,HashMap<String,Double>> sampleLandmarkStartHashMap = new HashMap();
        
        for(String sampleTitle : landmarkData.getSampleSet()) {
            sampleLandmarkStartHashMap.put(sampleTitle, new HashMap());
            
            for(Double time : landmarkData.getSampleTimepointSet(sampleTitle)) {
                String landmark = landmarkData.getSampleLandmark(sampleTitle, time);
                
                if(landmark == null) {
                    continue;
                }
                
                if(landmark.toLowerCase().contains("s")) {
                    String currentLandmark = landmark.toLowerCase().replace("s", "");
                
                    sampleLandmarkStartHashMap.get(sampleTitle).put(currentLandmark, time);
                }
            }
        }
        
        return sampleLandmarkStartHashMap;
    }
    
    public HashMap<String,HashMap<String,Double>> getLandmarkDuration() {
        HashMap<String,HashMap<String,Double>> landmarkStartHashMap = this.getLandmarkStart();
        HashMap<String,HashMap<String,Double>> sampleLandmarkDurationHashMap = new HashMap();
        
        for(String sampleTitle : landmarkStartHashMap.keySet()) {
            sampleLandmarkDurationHashMap.put(sampleTitle, new HashMap());
        
            String currentLandmark = null;
            Double currentTime = null;
            
            for(String landmark : landmarkData.getLandmarks()) {
                Double time = landmarkStartHashMap.get(sampleTitle).get(landmark);
                
                if(time == null) {
                    if(landmarkData.isOptional(landmark)) {
                        boolean landmarkPresent = false;
                        
                        for(Double timeCompare : landmarkData.getSampleTimepointSet(sampleTitle)) {
                            String landmarkCompare = landmarkData.getSampleLandmark(sampleTitle, timeCompare);
                            
                            if(landmarkCompare.equals(landmark)) {
                                landmarkPresent = true;
                                
                                break;
                            }
                        }
                        
                        if(!landmarkPresent) {
                            continue;
                        }
                    }
                    
                    currentLandmark = null;
                    currentTime = null;
                }
                else {
                    if(currentTime != null) {
                        sampleLandmarkDurationHashMap.get(sampleTitle).put(currentLandmark, (time - currentTime));
                    }
                
                    currentLandmark = landmark;
                    currentTime = time;
                }
            }
        }
        
        return sampleLandmarkDurationHashMap;
    }
    
    public LinkedList<String> getLandmarks() {
        return landmarkData.getLandmarks();
    }
    
    private void loadLandmarkDefinition(String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        
        try {
            String fileLine;
            while((fileLine = bufferedReader.readLine()) != null) {
                String[] fileLineParts  = fileLine.split(SEPARATOR);
            
                try {
                    Double.valueOf(fileLineParts[0].replace("(", "").replace(")", ""));
                }
                catch(NumberFormatException e) {
                    landmarkData = null;
                    
                    break;
                }
            
                landmarkData.addLandmark(fileLineParts[0].replace("(", "").replace(")", ""), (fileLineParts[0].contains("(") && fileLineParts[0].contains(")")));
            }
        }
        finally {
            bufferedReader.close();
        }
    }
        
    public static String getDatasetTitle(String fileName) throws IOException {
        BufferedReader landmarkTimingBufferedReader = new BufferedReader(new FileReader(fileName));
        String datasetTitle = landmarkTimingBufferedReader.readLine().split(SEPARATOR)[0];
        landmarkTimingBufferedReader.close();
        
        return datasetTitle;
    }
    
    public static int getLandmarkDefinitionSummary(String fileName) throws IOException {
        int landmarkDefinitionCount = 0;
        
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        
        try {
            String fileLine;
            while((fileLine = bufferedReader.readLine()) != null) {
                String[] fileLineParts  = fileLine.split(SEPARATOR);
            
                try {
                    Double.valueOf(fileLineParts[0].replace("(", "").replace(")", ""));
                }
                catch(NumberFormatException e) {
                    landmarkDefinitionCount = 0;
                    
                    break;
                }
            
                landmarkDefinitionCount++;
            }
        }
        finally {
            bufferedReader.close();
        }
        
        return landmarkDefinitionCount;
    }
    
    public static boolean isValidSampleFileFormat(String fileName) {
        boolean sampleHeaderFlag = true;
        boolean experimentHeaderFlag = true;
       
        String experimentTitle = null;
        String[] sampleTitles = null;
        
        try {
            File file = new File(fileName);
            
            String[] fileNameParts = fileName.split("\\.");
            if(fileNameParts.length == 1) {
                Text errorText = new Text("Error: The chosen file does not have the ending csv, please make sure it is a valid csv-file including this file-ending");
                errorText.setWrappingWidth(400);

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("File format");
                alert.setWidth(400);
                alert.getDialogPane().setContent(errorText);
                alert.showAndWait();
                
                return false;
            }
            else if(!fileNameParts[fileNameParts.length - 1].equals("csv")) {
                Text errorText = new Text("Error: The chosen file does not have the ending csv, please make sure it is a valid csv-file including this file-ending");
                errorText.setWrappingWidth(400);

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("File format");
                alert.setWidth(400);
                alert.getDialogPane().setContent(errorText);
                alert.showAndWait();
                
                return false;
            }

            BufferedReader landmarkTimingBufferedReader = new BufferedReader(new FileReader(fileName));
            String fileLine;
            int lineCount = 1;

            while((fileLine = landmarkTimingBufferedReader.readLine()) != null) {
                String[] fileLineParts = fileLine.split(SEPARATOR);

                if(!sampleHeaderFlag && ((sampleTitles != null) && ((fileLineParts.length - 1) != sampleTitles.length))) { /* skip header lines and inc*/
                    Text errorText = new Text("Error: timepoint " + fileLineParts[0] + " differs in number of samples.\nFile: " + file.getAbsolutePath());
                    errorText.setWrappingWidth(400);

                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Incomplete dataset");
                    alert.setWidth(400);
                    alert.getDialogPane().setContent(errorText);
                    alert.showAndWait();

                    landmarkTimingBufferedReader.close();

                    return false;            
                }

                if(fileLineParts[0].isEmpty()) {
                    Text errorText = new Text("Data line " + lineCount + " does not contain a timepoint. The file cannot be properly processed.\nFile: " + file.getAbsolutePath());
                    errorText.setWrappingWidth(400);

                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Incomplete dataset");
                    alert.setWidth(400);
                    alert.getDialogPane().setContent(errorText);
                    alert.showAndWait();

                    landmarkTimingBufferedReader.close();

                    return false;
                }

                if(experimentHeaderFlag || sampleHeaderFlag) {
                    if(experimentHeaderFlag) {
                        experimentTitle = fileLineParts[0];

                        experimentHeaderFlag = false;
                    }
                    else if(sampleHeaderFlag) {
                        sampleTitles = new String[fileLineParts.length - 1];

                        for(int index = 1; index < fileLineParts.length; index++) {
                            sampleTitles[index - 1] = experimentTitle + "-" + fileLineParts[index];
                        }

                        sampleHeaderFlag = false;
                    }
                }
                else {
                    try {
                        Double.valueOf(fileLineParts[0]);
                    }
                    catch(NumberFormatException e) {
                        Text errorText = new Text("The timepoint at line: " + lineCount + " is not numeric and will be omitted from analysis.\nFile: " + file.getAbsolutePath());
                        errorText.setWrappingWidth(400);

                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Faulty dataset");
                        alert.setWidth(400);
                        alert.getDialogPane().setContent(errorText);
                        alert.showAndWait();

                        landmarkTimingBufferedReader.close();

                        return false;
                    }
                }

                lineCount++;
            }
            
            landmarkTimingBufferedReader.close();
        }
        catch(IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An I/O exception occured");
            alert.setContentText(e.toString());
            alert.showAndWait();
        
            return false;
        }
        
        return true;
    }
    
    
    public void loadSampleLandmarkTiming(String fileName) throws IOException {
        boolean sampleHeaderFlag = true;
        boolean experimentHeaderFlag = true;
       
        String experimentTitle = null;
        String[] sampleTitles = null;
        
        BufferedReader landmarkTimingBufferedReader = new BufferedReader(new FileReader(fileName));
        String fileLine;
        
        while((fileLine = landmarkTimingBufferedReader.readLine()) != null) {
            String[] fileLineParts = fileLine.split(SEPARATOR);
            
            if(experimentHeaderFlag || sampleHeaderFlag) {
                if(experimentHeaderFlag) {
                    experimentTitle = fileLineParts[0];
                
                    experimentHeaderFlag = false;
                }
                else if(sampleHeaderFlag) {
                    sampleTitles = new String[fileLineParts.length - 1];
                
                    for(int index = 1; index < fileLineParts.length; index++) {
                        sampleTitles[index - 1] = experimentTitle + "-" + fileLineParts[index];
                    }
                
                    sampleHeaderFlag = false;
                }
            }
            else {
                try {
                    Double time = Double.valueOf(fileLineParts[0]);

                    for(int index = 1; index < fileLineParts.length; index++) {
                        landmarkData.addSampleLandmarkTiming(sampleTitles[index - 1], time, fileLineParts[index]);
                    }
                }
                catch(NumberFormatException e) {}
            }
        }
        
        landmarkTimingBufferedReader.close();
    }
       
    private static final String SEPARATOR = ",";
    
    private LandmarkData landmarkData;
}
