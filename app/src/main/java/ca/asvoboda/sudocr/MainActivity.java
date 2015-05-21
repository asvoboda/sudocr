package ca.asvoboda.sudocr;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.asvoboda.sudocr.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity  implements View.OnClickListener, CvCameraViewListener2 {

    private static final String TAG = "Main";
    private static final Size BLUR_SIZE = new Size(5, 5);

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Failed to load OpenCV module");
        }
    }

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean isAttemptSolve = false;
    private boolean isFinished = false;
    private Mat frame = null;
    private SudokuSolver solver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "called onCreate");

        solver = new SudokuSolver();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_preview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.disableFpsMeter();

        bindClickHandlers();
    }

    private void bindClickHandlers() {
        Button hint = (Button)findViewById(R.id.button_hint);
        hint.setOnClickListener(this);

        Button reset = (Button)findViewById(R.id.button_reset);
        reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_hint:
                Log.d(TAG, "solve for hint");
                isAttemptSolve = true;
                break;

            case R.id.button_reset:
                Log.d(TAG, "reset");
                isAttemptSolve = false;
                if (mOpenCvCameraView != null) {
                    mOpenCvCameraView.enableView();
                }
                break;

            default:
                break;
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //manually load opencv libraries instead of through the manager app
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //empty
    }

    @Override
    public void onCameraViewStopped() {
        //empty
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        if (isAttemptSolve && isFinished) {
            mOpenCvCameraView.disableView();
        } else if (isAttemptSolve && !isFinished) {
            Imgproc.GaussianBlur(frame, frame, BLUR_SIZE, 0);
            Imgproc.adaptiveThreshold(frame, frame, 255, Imgproc.THRESH_BINARY, 1, 19, 2);
            isFinished = true;
        } else if (!isAttemptSolve){
            frame = inputFrame.gray();
            isFinished = false;
        }

        return frame;
    }
}
