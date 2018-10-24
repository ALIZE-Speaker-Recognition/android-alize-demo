package fr.univavignon.alize.AndroidALIZEDemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.SimpleSpkDetSystem;

/**
 *  This class is the base activity.
 *  All activities will inherit from it.
 *
 * @author Nicolas Duret
 */
public class BaseActivity extends AppCompatActivity {

    protected Locale defaultLanguage;
    /**
     *  Allow to use Alize features.
     */
    protected SimpleSpkDetSystem alizeSystem;
    protected SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultLanguage = Locale.getDefault();
        SP = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        System.out.println(SP.getString("threshold", "30"));

        try {
            simpleSpkDetSystemInit();
        }
        catch (AlizeException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(SettingsActivity.class);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void startActivity(Class targetActivity) {
        startActivity(targetActivity, null);
    }

    /**
     * Start the specified activity with arguments in params.
     * @param targetActivity The targeted activity.
     * @param params Params passed to the activity.
     */
    protected void startActivity(Class targetActivity, Map<String, Object> params) {
        Intent intent = new Intent(BaseActivity.this, targetActivity);

        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue().toString());
            }
        }
        startActivity(intent);
    }

    /**
     * Display the text specified on the bottom of the screen.
     * @param text Text you want to display.
     */
    protected void makeToast(String text) {
        Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Innitialize the Alize system and call the Alize singleton instance.
     * @throws IOException Throws if the config or wold model files doesn't exists.
     * @throws AlizeException Thorws if there was a problem in the Alize system execution.
     */
    private void simpleSpkDetSystemInit() throws IOException, AlizeException {
        // Initialization:
        alizeSystem = SharedAlize.getInstance(getApplicationContext());

        // We also load the background model from the application assets
        InputStream backgroundModelAsset = getApplicationContext().getAssets().open("gmm/world.gmm");
        alizeSystem.loadBackgroundModel(backgroundModelAsset);
        backgroundModelAsset.close();
    }
}
