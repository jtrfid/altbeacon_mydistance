package org.altbeacon.mydemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.logging.LogManager;
import org.altbeacon.beacon.logging.Loggers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * 
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity implements BeaconConsumer {
	protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		
		// 设置Altbeacon library log messages                                                                            
        LogManager.setLogger(Loggers.verboseLogger());

        // BeaconManager的获取唯一实例
        beaconManager = BeaconManager.getInstanceForApplication(this);
        
        // 识别Estimote beacons
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // 绑定服务org.altbeacon.beacon.service.BeaconService
        // Binds an Android Activity or Service to the BeaconService. 
        // The Activity or Service must implement the beaconConsuemr interface so that it can get a callback when the service is ready to use.
        beaconManager.bind(this);
        
        logToDisplay("开始监控");
	}
	
	public void onRangingClicked(View view) {
		Intent myIntent = new Intent(this, RangingActivity.class);
		this.startActivity(myIntent);
	}

    @Override 
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    
    /**
     *  当连接BeaconService时，回调此方法
     */
    @Override
    public void onBeaconServiceConnect() {
    	logToDisplay("onBeaconServiceConnect()!");  
    	// 设置监控通知回调，
    	// 当BeaconService看到和看不到某个区域的Beacons时执行的回调方法，即监控beacons
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
               Log.i(TAG, "I just saw an beacon for the first time!");  
               logToDisplay("didEnterRegion()");
            }
            @Override
            public void didExitRegion(Region region) {
               Log.i(TAG, "I no longer see an beacon");
               logToDisplay("didExitRegion()");
            }
            @Override
            public void didDetermineStateForRegion(int state, Region region) {
              Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);  
              logToDisplay("didDetermineStateForRegion()");
            }
        });
        
        try {
        	// 通知BeaconService,开始监控特定区域的Beacons
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    
        	Log.d(TAG,"RemoteException:"+e.toString());
        }
     }


    public void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    		Date date = new Date(System.currentTimeMillis());
	    	SimpleDateFormat sfd = new SimpleDateFormat("HH:mm:ss.SSS",Locale.CHINA);
	    	String dateStr = sfd.format(date);
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.append(dateStr+"=="+line+"\n");            	    	    		
    	    }
    	});
    }

}
