package ca.asvoboda.sudocr;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import org.asvoboda.sudocr.R;

public class MainActivity extends Activity  implements View.OnClickListener {

    private static final String TAG = "Main";

    private FrameLayout mLayout;
    private Camera mCamera;
    private CameraPreview mPreview;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        Button capt = (Button) findViewById(R.id.button_capture);
        capt.setOnClickListener(this);
        Button hint = (Button) findViewById(R.id.button_hint);
        hint.setOnClickListener(this);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        if (mCamera == null) {
            Log.d(TAG, "Error getting camera" );
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        mLayout = (FrameLayout) findViewById(R.id.camera_preview);
        mLayout.addView(mPreview);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            /*
            case R.id.button_capture:
                intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;

            case R.id.button_hint:
                intent = new Intent(this, CameraPreviewTestActivity.class);
                startActivity(intent);
                break;
            */
            default:
                break;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Camera is either in use or not available");
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void releaseCamera(){
        mLayout.removeView(mPreview); // This is necessary.
        mPreview = null;
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
