package me.joebass.wearbrightness;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Iterator;

/**
 * Created by josephbass on 9/27/14.
 */
public class DeviceDataListenerService extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Iterator<DataEvent> iterator = dataEvents.iterator();
        while (iterator.hasNext()) {
            DataEvent next = iterator.next();
            DataMap dataMap = DataMap.fromByteArray(next.getDataItem().getData());
            int brightness = dataMap.get("BRIGHTNESS");
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    brightness);
            Log.v("TESTING", "New brightness is " + brightness);
        }
    }
}
