package me.joebass.wearbrightness;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Set;


public class WearPreferences extends PreferenceFragment implements SensorEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private int newBrightness = 255;

    private static final String TAG = "WearBrightness";

    private GoogleApiClient mGoogleApiClient;

    private SensorManager mSensorManager;

    private Set<String> devices;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my);
        addPreferencesFromResource(R.xml.preferences);
        mGoogleApiClient = ((WearActivity) getActivity()).getGoogleApiClient();
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        initSummary(getPreferenceScreen());

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
        float value = event.values[0];
        if (value > 5) {
            newBrightness = 255;
        } else {
            newBrightness = 1;
        }
        updateDataItem();
    }

    private void updateDataItem() {
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
