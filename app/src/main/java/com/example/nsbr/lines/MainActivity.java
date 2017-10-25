package com.example.nsbr.lines;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends AppCompatActivity {

    private float uploadDurationSec;
    TextView tvTime;
    private static final String TAG = "nsbrTag";
    Mat img;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    process();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTime = (TextView)findViewById(R.id.tvTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "opencv succesfully loaded");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback);
        }
    }

    public void process(){
        img = new Mat();
        /*try {
            img = Utils.loadResource(this, R.drawable.img5, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.imgcc);
        Utils.bitmapToMat(bitmap, img);
        Imgproc.cvtColor(img, img, Imgcodecs.CV_LOAD_IMAGE_COLOR);

        Log.d(TAG, img.size().toString());



        Mat imgGray = new Mat(img.rows(), img.cols(), CvType.CV_8UC1);
        Mat dst = new Mat();
        Mat imgCanny = new Mat();
        Mat dst1 = new Mat();

        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(imgGray,dst,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 15);
        Mat element = getStructuringElement( MORPH_ELLIPSE,new  Size( 5, 5 ), new Point( 2, 2 ) );
        Imgproc.morphologyEx( dst, dst, MORPH_CLOSE, element );
        Imgproc.dilate(dst, dst, element);

        Imgproc.Canny(dst, imgCanny, 100, 200, 5, false);
        Imgproc.blur(imgCanny, dst1, new Size(3, 3));
        Imgproc.dilate(dst, dst, element);
        Mat mask = new Mat(img.size(), img.type(), new Scalar(0,0,0));

        Mat verticalLines=new Mat();
        Imgproc.HoughLinesP(dst1, verticalLines, 1, Math.PI , 90, 20, 1);
        for(int i = 0; i < verticalLines.rows(); i++ )
        {
            double[] l = verticalLines.get(i, 0);
            Imgproc.line(img, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255,0,0), 3);
        }

        Mat horizontalLines=new Mat();
        Imgproc.HoughLinesP(dst1, horizontalLines, 1, Math.PI/2 , 100, 100, 1);
        for(int i = 0; i < horizontalLines.rows(); i++ ) {
            double[] l = horizontalLines.get(i, 0);
            double angle = Math.atan2(l[3] - l[1], l[2] - l[0]) * 180.0 / Math.PI;

            if (true)
            Imgproc.line(img, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255,0,0), 3);
        }

       /* // Combine similar lines
        int size = 3;
        Mat element = getStructuringElement( MORPH_ELLIPSE,new  Size( 2*size + 1, 2*size+1 ), new Point( size, size ) );
        //Imgproc.morphologyEx( mask, mask, MORPH_CLOSE, element );
        Imgproc.dilate(mask, mask, element);


        //detect table cells
        Mat maskGray = new Mat();
        Mat canny = new Mat();
        ArrayList<MatOfPoint> contours1 = new ArrayList<MatOfPoint>();
        Mat hierarchy1 = new Mat();
        ArrayList<Mat> croplist = new ArrayList<Mat>();
        //Imgproc.cvtColor(mask, maskGray, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.adaptiveThreshold(maskGray,maskGray,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 15);
        Imgproc.Canny( mask, canny, 100, 200, 3, false );
        Imgproc.findContours( canny, contours1, hierarchy1, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
        Log.d(TAG, String.valueOf(contours1.size()));

        for( int i = 0; i< contours1.size(); i = i+2 )
        {
            Rect brect = Imgproc.boundingRect(contours1.get(i));
            if (brect.area() <1000 || brect.width < 10 || brect.height < 10)
                continue;
            if (brect.area() > img.width()*img.height()/2)
                continue;
            Imgproc.rectangle(img, brect.br(), brect.tl(), new Scalar(255,0,0), 3);
            Mat crop = new Mat(img, brect);
            croplist.add(crop);
        }
        Log.d(TAG, String.valueOf(croplist.size()));
*/
        //convert to bitmap:
        Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        //find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(R.id.imageView1);
        iv.setImageBitmap(bm);

    }

    public Double distanceToVerticalRight(Mat verticalLines, Point point){
        double valRight = img.size().width;
        for(int i = 0; i < verticalLines.rows(); i++ )
        {
            double[] l = verticalLines.get(i, 0);
            if (l[0]==l[2]){
                if (Math.abs(l[0]-point.x)<valRight && (l[0]-point.x>0) && ((point.y<l[3] && point.y>l[1]) || (point.y<l[1] && point.y>l[3])))
                    valRight = Math.abs(l[0]-point.x);
            }
        }
        if (valRight==img.size().width){
            tvTime.setText("page end");
            return null;
        }
        return valRight;
    }

    public Double distanceToVerticalLeft(Mat verticalLines, Point point){
        double valLeft = img.size().width;
        for(int i = 0; i < verticalLines.rows(); i++ )
        {
            double[] l = verticalLines.get(i, 0);
            if (l[0]==l[2]){
                if (Math.abs(l[0]-point.x) < valLeft && (l[0]-point.x<0) && ((point.y<l[3] && point.y>l[1]) || (point.y<l[1] && point.y>l[3])))
                    valLeft = Math.abs(l[0]-point.x);
            }
        }
        if (valLeft==img.size().width){
            tvTime.setText("page end");
            return null;
        }
        return valLeft;
    }

    public Double distanceToHorizontalUp(Mat horizontalLines, Point point){
        double valUp = img.size().height;
        for(int i = 0; i < horizontalLines.rows(); i++ )
        {
            double[] l = horizontalLines.get(i, 0);
            if (l[1]==l[3]){
                if (Math.abs(l[1]-point.y)<valUp && (l[1]-point.y>0) && ((point.x<l[0] && point.x>l[2]) || (point.x<l[0] && point.x>l[2])))
                    valUp = Math.abs(l[1]-point.y);
            }
        }
        if (valUp==img.size().height){
            tvTime.setText("page end");
            return null;
        }
        return valUp;
    }

    public Double distanceToHorizontalDown(Mat horizontalLines, Point point){
        double valDown = img.size().height;
        for(int i = 0; i < horizontalLines.rows(); i++ )
        {
            double[] l = horizontalLines.get(i, 0);
            if (l[1]==l[3]){
                if (Math.abs(l[1]-point.y)<valDown && (l[1]-point.y<0) && ((point.x<l[0] && point.x>l[2]) || (point.x<l[0] && point.x>l[2])))
                    valDown = Math.abs(l[1]-point.y);
            }
        }
        if (valDown==img.size().height){
            tvTime.setText("page end");
            return null;
        }
        return valDown;
    }

}
