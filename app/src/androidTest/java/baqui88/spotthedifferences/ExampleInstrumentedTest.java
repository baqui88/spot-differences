package baqui88.spotthedifferences;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import baqui88.game.HomeScreen;
import baqui88.game.MissionDatabase;
import baqui88.game.SingleMission;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("baqui88.spotthedifferences", appContext.getPackageName());
    }
    @Test
    public void testImages() throws Exception {
        ArrayList<SingleMission> database = new ArrayList<>();
        database.addAll(MissionDatabase.EasyMissions);
        database.addAll(MissionDatabase.MediumMissions);
        database.addAll(MissionDatabase.HardMissions);

        for (SingleMission mission : database){
            String path = mission.getPathInAssets();
            String mirrorPath = mission.getMirrorPathInAssets();
            String layerPath = mission.getLayerPathInAssets();
            assertTrue(checkFileExistInAsset(path));
            assertTrue(checkFileExistInAsset(mirrorPath));
            assertTrue(checkFileExistInAsset(layerPath));
        }
    }

    public boolean checkFileExistInAsset(String pathInAssets) throws IOException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        AssetManager mg = appContext.getResources().getAssets();
        InputStream is = null;
        try {
            is = mg.open(pathInAssets);
            //File exists so do something with it
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}


