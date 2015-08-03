package org.altbeacon.mydemo;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.EditText;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranging);

        beaconManager.bind(this);
    }
    @Override 
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override 
    protected void onPause() {
    	super.onPause();
    	if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }
    @Override 
    protected void onResume() {
    	super.onResume();
    	if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

	/**
	 *  当连接BeaconService时，回调此方法
	 */
    @Override
    public void onBeaconServiceConnect() {
    	/**
         * 每个扫描周期结束，根据20秒内各beacon的RSSI平均值计算它的距离，该回调获取这些beacon的距离值
         * Called once per second (实际上是每扫描周期) to give an estimate of the mDistance to visible beacons
         */
        beaconManager.setRangeNotifier(new RangeNotifier() {
	        @Override 
	        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
	            if (beacons.size() > 0) {
	            	//EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
	                Beacon firstBeacon = beacons.iterator().next();
	                //logToDisplay("The first beacon "+firstBeacon.toString()+" is about "+firstBeacon.getDistance()+" meters away.");           
	                logToDisplay("distance="+firstBeacon.getDistance());           
	             }
	        }
        });

        /** 
		 *  启动测距修正
		 *  Tells the BeaconService to start looking for beacons that match the passed Region object, 
    	 *  and providing updates on the estimated mDistance every seconds(实际上是每个扫描周期) while beacons in the Region are visible. 
    	 *  Note that the Region's unique identifier must be retained to later call the stopRangingBeaconsInRegion method. 
    	 *  this will provide an update once per second with the estimated distance to the beacon in the didRAngeBeaconsInRegion method.
    	 */
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }
    private void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    		Date date = new Date(System.currentTimeMillis());
	    	SimpleDateFormat sfd = new SimpleDateFormat("HH:mm:ss.SSS",Locale.CHINA);
	    	String dateStr = sfd.format(date);
    	    public void run() {    	
    	    	EditText editText = (EditText)RangingActivity.this
    					.findViewById(R.id.rangingText);
    	    	editText.append(dateStr+"=="+line+"\n");            	
    	    }
    	});
    }
}
