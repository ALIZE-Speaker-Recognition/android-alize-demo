package fr.univavignon.alize.AndroidALIZEDemo;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.SimpleSpkDetSystem;

/**
 * Singleton meant to get the instance of the Alize system without initializing it every time.
 *
 * @author Nicolas Duret
 */
class SharedAlize {
    private static SimpleSpkDetSystem alizeSystem;

    static SimpleSpkDetSystem getInstance(Context appContext) throws IOException, AlizeException {
        if (alizeSystem == null) {
            // We create a new spk det system with a config (extracted from the assets) and a directory where it can store files (models + temporary files)
            InputStream configAsset = appContext.getAssets().open("AlizeDefault.cfg");
            alizeSystem = new SimpleSpkDetSystem(configAsset, appContext.getFilesDir().getPath());
            configAsset.close();

            // We also load the background model from the application assets
            InputStream backgroundModelAsset = appContext.getAssets().open("gmm/world.gmm");
            alizeSystem.loadBackgroundModel(backgroundModelAsset);
            backgroundModelAsset.close();
        }
        return alizeSystem;
    }
}
