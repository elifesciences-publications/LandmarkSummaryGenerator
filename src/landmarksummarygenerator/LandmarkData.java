package landmarksummarygenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author Dr. Felix Seifert, cropSeq bioinformatics
 */
public class LandmarkData {
    public LandmarkData() {
        landmarkLinkedList = new LinkedList();
        landmarkOptionalHashMap = new HashMap();
        landmarkTimingHashMap = new HashMap();
    }
    
    public void addLandmark(String landmark, boolean optional) {
        this.landmarkLinkedList.add(landmark);
        
        if(optional) {
            landmarkOptionalHashMap.put(landmark, Boolean.TRUE);
        }
    }
    
    public void addSampleLandmarkTiming(String sample, Double time, String landmark) {
        if(landmarkTimingHashMap.get(sample) == null) {
            landmarkTimingHashMap.put(sample, new HashMap());
        }
        
        landmarkTimingHashMap.get(sample).put(time, landmark);
    }

    public LinkedList<String> getLandmarks() {
        return landmarkLinkedList;
    }

    public String getSampleLandmark(String sampleName, Double time) {
        if(landmarkTimingHashMap.get(sampleName) == null) {
            return null;
        }
        
        return landmarkTimingHashMap.get(sampleName).get(time);
    }
    
    public Set<String> getSampleSet() {
        return landmarkTimingHashMap.keySet();
    }
    
    public Set<Double> getSampleTimepointSet(String sample) {
        if(landmarkTimingHashMap.get(sample) == null) {
            return null;
        }
        
        return landmarkTimingHashMap.get(sample).keySet();
    }
    
    public boolean isOptional(String landmark) {
        return (landmarkOptionalHashMap.get(landmark) != null);
    }
    
    private final LinkedList<String> landmarkLinkedList;
    private final HashMap<String,Boolean> landmarkOptionalHashMap;
    private final HashMap<String,HashMap<Double,String>> landmarkTimingHashMap;
}
