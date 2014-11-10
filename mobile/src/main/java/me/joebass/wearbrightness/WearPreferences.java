package me.joebass.wearbrightness;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Map;


public class WearPreferences extends PreferenceFragment implements SensorEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "WearBrightness";

    private GoogleApiClient mGoogleApiClient;

    private SensorManager mSensorManager;

    private Sensor mSignificantMotionSensor;

    private TriggerEventListener mTriggerEventListener;

    private SharedPreferences preferences;

    private String prefModeKey;

    private String prefUsePhoneSensor;

    private String prefAutoDetect;

    private String prefManual;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my);
        addPreferencesFromResource(R.xml.preferences);
        mGoogleApiClient = ((WearActivity) getActivity()).getGoogleApiClient();
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSignificantMotionSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        initSummary(getPreferenceScreen());
        mTriggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                Log.v(TAG, event.toString());
            }
        };
        mSensorManager.requestTriggerSensor(mTriggerEventListener, mSignificantMotionSensor);


        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Map<String, ?> all = preferences.getAll();
        for (Map.Entry<String, ?> stringEntry : all.entrySet()) {
            Log.v(TAG, stringEntry.getKey() + "->" + stringEntry.getValue());
        }
        prefModeKey = "pref_mode";
        prefUsePhoneSensor = getString(R.string.pref_useSensor);
        prefAutoDetect = getString(R.string.pref_autoDetect);
        prefManual = getString(R.string.pref_manual);

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int newBrightnessAuto = 255;
        float value = event.values[0];
        if (value > 5) {
            newBrightnessAuto = 255;
        } else {
            newBrightnessAuto = 1;
        }
        String detectionMode = preferences.getString(prefModeKey, prefUsePhoneSensor);
        if (detectionMode.equals(prefUsePhoneSensor)) {
            updateDataItem(newBrightnessAuto);
        }
    }

    private void updateDataItem(int newBrightness) {
        PutDataMapRequest putRequest = PutDataMapRequest.create("/brightness");
        DataMap map = putRequest.getDataMap();
        map.putInt("BRIGHTNESS", newBrightness);
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest()).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
        if (preferences.getString(prefModeKey, prefManual).equals(prefManual) && (key.equals("pref_manual") || key.equals(prefModeKey))) {
            updateDataItem(sharedPreferences.getInt("pref_manual", 0));
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().toLowerCase().contains("password")) {
                p.setSummary("******");
            } else {
                p.setSummary(editTextPref.getText());
            }
        }
        if (p instanceof MultiSelectListPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    public interface OnGotNodesListener {
        public void onGotNodes(ArrayList<String> nodes);
    }


}
