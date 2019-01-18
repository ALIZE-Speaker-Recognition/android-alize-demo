package fr.univavignon.alize.AndroidALIZEDemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import AlizeSpkRec.AlizeException;

import static java.lang.reflect.Array.getInt;

public class ThresholdPreference extends Preference {
    private Context context;
    private String thresholdKey = "thresholdValue";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ThresholdPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public ThresholdPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public ThresholdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ThresholdPreference(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.edit_text_preference_view, parent, false);
        EditText thresholdValueEditText = view.findViewById(R.id.thresholdValue);
        thresholdValueEditText.addTextChangedListener(thresholdListener);

        SharedPreferences preferences = getSharedPreferences();
        double thresholdValue = getDouble(preferences, thresholdKey, 0.0);
        thresholdValueEditText.setText(String.valueOf(thresholdValue));

        return view;
    }

    private TextWatcher thresholdListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try {
                double value;
                try {
                    value = Double.valueOf(charSequence.toString());
                }
                catch (Exception e) {
                    value = 0.0;
                }

                DemoSpkRecSystem.getSharedInstance(context).setDecisionThreshold(value);
                SharedPreferences.Editor editor = getEditor();
                putDouble(editor, thresholdKey, value);
                editor.apply();
            } catch (AlizeException | IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    private double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }
}
