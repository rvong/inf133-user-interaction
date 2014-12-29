package com.example.inf133;

import java.io.IOException;
import java.text.DecimalFormat;

import android.support.v7.app.ActionBarActivity;
import android.content.res.AssetFileDescriptor;
import android.hardware.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class MainActivity extends ActionBarActivity {
	// Resources
	// http://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])
	// http://stackoverflow.com/questions/10291322/what-is-the-alternative-to-android-orientation-sensor
	// http://www.techrepublic.com/article/pro-tip-create-your-own-magnetic-compass-using-androids-internal-sensors/
	
	// Orientation
	private SensorManager mSensorManager;
	private Sensor mLightSensor, mAccelerometer, mMagnetometer;
	private SensorEventListener mEventListener;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    // Orientation Values
	private float mAzimuth = 0f;
	private float mPitch = 0f;
	private float mRoll = 0f;
	
	private double mAzimuthDeg = 0f;
	private double mPitchDeg = 0f;
	private double mRollDeg = 0f;
	
	private int lastOrientation = 0;
	
    // Audio
	private MediaPlayer mp;
	private AssetFileDescriptor afdA1, afdB1, afdC1, afdD1, afdE1, afdF1;
	
	// Text
	private TextView mTextViewOrientation;
	private EditText mEditTextAzimuth;
	private EditText mEditTextPitch;
	private EditText mEditTextRoll;

	// Format
	private DecimalFormat df;
	private long lastUpdateTime;
	
	private void updateUI() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (lastUpdateTime + 400 > System.currentTimeMillis()) return;
				lastUpdateTime = System.currentTimeMillis();
				mEditTextAzimuth.setText(df.format(mAzimuthDeg));
				mEditTextPitch.setText(df.format(mPitchDeg));
				mEditTextRoll.setText(df.format(mRollDeg));

				if (degBetween(mPitchDeg, -30, 30)) {
					if (degBetween(mRollDeg, -30, 30)) {
						mTextViewOrientation.setText("5) Flat, Face Up");
						playAudio(afdE1);
					}
					else if (mRollDeg < -145 || mRollDeg > 145) {
						mTextViewOrientation.setText("6) Flat, Face Down");
						playAudio(afdF1);
					}
					else if (degBetween(mRollDeg, -120, 60)) {
						mTextViewOrientation.setText("3) Landscape, Counter-Clockwise");
						playAudio(afdC1);
					}
					else if (degBetween(mRollDeg, 45, 145)) {
						mTextViewOrientation.setText("4)Landscape, Clockwise");
						playAudio(afdD1);
					}
				} else if (degBetween(mPitchDeg, -90, -45)) {
					mTextViewOrientation.setText("1) Upright");
					playAudio(afdA1);
				} else if (degBetween(mPitchDeg, 45, 90)) {
					mTextViewOrientation.setText("2) Upside Down");
					playAudio(afdB1);
				}
			}
		});
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        mEditTextAzimuth = (EditText) findViewById(R.id.editTextAzimuth);
        mEditTextPitch = (EditText) findViewById(R.id.editTextPitch);
        mEditTextRoll = (EditText) findViewById(R.id.editTextRoll);
        mTextViewOrientation = (TextView) findViewById(R.id.textViewOrientation);
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    
        loadAudioResources();
        df = new DecimalFormat("000.0");
        
        mEventListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) { }

			@Override
			public void onSensorChanged(SensorEvent event) {
		        if (event.sensor == mAccelerometer) {
		            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
		            mLastAccelerometerSet = true;
		        } else if (event.sensor == mMagnetometer) {
		            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
		            mLastMagnetometerSet = true;
		        }
		        if (mLastAccelerometerSet && mLastMagnetometerSet) {
		            // Azimuth: Rotation around Z-axis
		            //   Pitch: Rotation around X-axis
		            //    Roll: Rotation around Y-axis
		            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
		            SensorManager.getOrientation(mR, mOrientation);
		            
		            mAzimuth = mOrientation[0];
		            mPitch = mOrientation[1];
		            mRoll = mOrientation[2];
		            
		            mAzimuthDeg = Math.toDegrees(mAzimuth);
		            mPitchDeg = Math.toDegrees(mPitch);
		            mRollDeg = Math.toDegrees(mRoll);
		            
		            updateUI();
		        }
			}
        };
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mSensorManager.registerListener(mEventListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    public void onStop() {
    	mSensorManager.unregisterListener(mEventListener);
    	super.onStop();
    }
    
    private void loadAudioResources() {
        mp = new MediaPlayer();
        afdA1 = getApplicationContext().getResources().openRawResourceFd(R.raw.a1);
        afdB1 = getApplicationContext().getResources().openRawResourceFd(R.raw.b1);
        afdC1 = getApplicationContext().getResources().openRawResourceFd(R.raw.c1);
        afdD1 = getApplicationContext().getResources().openRawResourceFd(R.raw.d1);
        afdE1 = getApplicationContext().getResources().openRawResourceFd(R.raw.e1);
        afdF1 = getApplicationContext().getResources().openRawResourceFd(R.raw.f1);   
    }
    
	private synchronized void playAudio(AssetFileDescriptor afd) {
		if (mp.isPlaying()) return;
		try {
			mp.reset();
			mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			mp.prepare();
			mp.start();
		} catch (IllegalArgumentException e) {
			Log.d("playAudio: ", e + "\n afd: " + afd.toString());
		} catch (IllegalStateException e) {
			Log.d("playAudio: ", e + "\n afd: " + afd.toString());
		} catch (IOException e) {
			Log.d("playAudio: ", e + "\n afd: " + afd.toString());
		}
	}
    
    private boolean degBetween(double n, double a, double b) {
    	return (n >= a && n <= b);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
   
}
