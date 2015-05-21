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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity  implements View.OnClickListener, CvCameraViewListener2 {

    private static final String TAG = "Main";
    private static final Size BLUR_SIZE = new Size(5, 5);

    private static final int THRESHOLD_HOUGH = 50;
    private static final int MIN_LINE_SIZE = 20;
    private static final int LINE_GAP = 20;
    private static final Scalar RED = new Scalar(255, 0, 0);

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
        Mat dest = inputFrame.gray();
        if (isAttemptSolve) {

            Mat lines = new Mat();
            Mat kern = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            List<MatOfPoint2f> contours2f = new ArrayList<>();
            Mat hier = new Mat();
            Imgproc.Canny(dest, dest, 100, 100);
            Imgproc.GaussianBlur(dest, dest, BLUR_SIZE, 0);
            Imgproc.adaptiveThreshold(dest, dest, 255, Imgproc.THRESH_BINARY, 1, 19, 2);
            Imgproc.erode(dest, dest, kern);
            Imgproc.dilate(dest, dest, kern);
            //Imgproc.findContours(dest, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            /*

            MatOfPoint2f largest = new MatOfPoint2f();
            double maxArea = 0;

            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > 100.0) {
                    double peripheral = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
                    MatOfPoint2f approx = new MatOfPoint2f();
                    Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx , 0.02*peripheral, true);
                    if (area > maxArea && approx.toList().size() == 4) {
                        largest = approx;
                        maxArea = area;
                    }
                }
            }

            if (largest.toList().size() != 4) {
                Point[] points = largest.toArray();
                Core.line(dest, points[0], points[1], RED);
                Core.line(dest, points[1], points[2], RED);
                Core.line(dest, points[2], points[3], RED);
                Core.line(dest, points[3], points[4], RED);
            } else {
                Log.d(TAG, "didnt find square");
            }

            Imgproc.HoughLines(dest, lines, 1, Math.PI/180, THRESHOLD_HOUGH, MIN_LINE_SIZE, LINE_GAP);
            for (int x = 0; x < lines.cols(); x++) {
                double[] vec = lines.get(0, x);
                double x1 = vec[0],
                        y1 = vec[1],
                        x2 = vec[2],
                        y2 = vec[3];
                Point start = new Point(x1, y1);
                Point end = new Point(x2, y2);

                Core.line(dest, start, end, RED, 10);
            }
            */
        } else if (!isAttemptSolve){
            //empty
        }

        return dest;
    }
}
