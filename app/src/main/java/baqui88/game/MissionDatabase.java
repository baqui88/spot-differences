package baqui88.game;

import android.content.Context;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class MissionDatabase {
    public static ArrayList<SingleMission> EasyMissions = new ArrayList<>();
    public static ArrayList<SingleMission> MediumMissions = new ArrayList<>();
    public static ArrayList<SingleMission> HardMissions = new ArrayList<>();

    // it will generate static ArrayLists of database from JSON Source
    public static void getMissionsFromFile(String filename, Context context){

        try {
            // Load data
            JSONParser parser = new JSONParser();
            String jsonString = AssetReader.loadJsonFromAsset(filename, context);
            JSONArray missions = (JSONArray) parser.parse(jsonString);

            // Get SingleMission objects from data
            for(Object o : missions){
                JSONObject jo = (JSONObject) o;
                SingleMission mission = new SingleMission();

                mission.path = (String) jo.get("path") ;
                mission.difficulty = ((Long) jo.get("difficulty")).intValue();
                mission.time = ((Long) jo.get("time")).intValue();
                mission.target = ((Long) jo.get("target")).intValue();

                if (mission.difficulty == 1)
                    EasyMissions.add(mission);
                else if (mission.difficulty == 2)
                    MediumMissions.add(mission);
                else
                    HardMissions.add(mission);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static SingleMission get(int index, int difficulty){
        if (difficulty == 1)
            return EasyMissions.get(index);
        else if (difficulty == 2)
            return MediumMissions.get(index);
        else
            return HardMissions.get(index);
    }

}

