package com.card.reader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Locale;

public class color extends Activity implements CvCameraViewListener2{
    private static final String  TAG              = "MainActivity";



    private MenuItem             menuTypeCamera = null;
    private MenuItem			 menuWhiteOrBlack = null;
    private MenuItem             menuModeRecognition = null;
    private boolean              typeCamera = false;
    private boolean				 ModeGreay = false;
    private boolean				 ModeRecognition = false; //Color recognition mode. Accurate (true) or (false). by default we stat in precise
    private int					 CameraWidth = 1280; //960; <--> Nexus
    TextToSpeech textToSpeech;

    //Camera
    private CameraBridgeViewBase camera;
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                camera.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public color() {

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);

        if (!OpenCVLoader.initDebug())
            Log.e("OpenCv", "Unable to load OpenCV");
        else
            Log.d("OpenCv", "OpenCV loaded");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //Screen ON Permanente

        //
        //Maximum brightness permanente
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;


        setContentView(R.layout.activity_color);

        //We change the current camera type	JAVA <-->NATIVE
        if (typeCamera){
            camera =(JavaCameraView) findViewById(R.id.camara_nativa);
        }else{
            camera = (JavaCameraView)findViewById(R.id.camara_java);
        }

        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCvCameraViewListener(this);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                } else {
                    Log.e("error", "Failed to Initialize");
                }
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (camera != null)
            camera.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    public void onDestroy() {
        super.onDestroy();
        if (camera != null)
            camera.disableView();
    }



    //We create the menu and options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");

        menuTypeCamera = menu.add("\n" + "Change Native Camera/Java");
        menuWhiteOrBlack = menu.add("\n" + "Black and white");
        menuModeRecognition = menu.add("Mode Processing /\n" + "Rank of Color");

        SubMenu subMenu = menu.addSubMenu(4, 4, 4, "\n" + "Select a resolution");
        subMenu.add(1, 10, 1, "\n" + "High Resolution (1280x720)");
        subMenu.add(1, 11, 2, "Medium Resolution (960x720)");
        subMenu.add(1, 12, 3, "\n" + "Low Resolution (800x480)");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String mensajeToast;
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        //Button changes camera type
        if (item == menuTypeCamera) {
            camera.setVisibility(SurfaceView.GONE);
            typeCamera = !typeCamera;

            if (typeCamera) {
                camera =  findViewById(R.id.camara_nativa);
                mensajeToast = "Cámera Native";
            }else{
                camera = findViewById(R.id.camara_java);
                mensajeToast = "Cámera Java";
            }


            camera.setVisibility(SurfaceView.VISIBLE);
            camera.setCvCameraViewListener(this);
            camera.enableView();
            Toast toast = Toast.makeText(this, mensajeToast, Toast.LENGTH_LONG);
            toast.show();
        }
        //End Type Camera


        //Button puts black and white - Gray
        if(item == menuWhiteOrBlack){
            if(ModeGreay){
                ModeGreay = false;
                Toast toast = Toast.makeText(this, "'Gray mode' disabled.\n'Mode Normal' \n" + "enabled." , Toast.LENGTH_LONG);
                toast.show();
            }else{
                ModeGreay = true;
                Toast toast = Toast.makeText(this, "'Mode Normal' disabled.\n'Mode Gray' \n" + "enabled." , Toast.LENGTH_LONG);
                toast.show();
            }
        }
        //End Gray Mode

        //
        //Precise Mode Button/Shades mode
        if(item == menuModeRecognition ){
            if(ModeRecognition){
                ModeRecognition = false;
                Toast toast = Toast.makeText(this, "'Mode Precise' disabled.\n'Mode enabled' enabled." , Toast.LENGTH_LONG);
                toast.show();
            }else{
                ModeRecognition = true;
                Toast toast = Toast.makeText(this, "'Mode Shades' disabled.\n'Mode Precise' enabled." , Toast.LENGTH_LONG);
                toast.show();
            }
        }


        //Submenu to change HUD size
        switch(item.getItemId()){
            case 10: // Menu id, to verify that it has been pressed
                CameraWidth = 1280;
                Toast toast = Toast.makeText(this, "\n" + "Maximum HUD resolution" , Toast.LENGTH_LONG);
                toast.show();
                break;
            case 11:
                CameraWidth = 960;
                toast = Toast.makeText(this, "Medium HUD resolution" , Toast.LENGTH_LONG);
                toast.show();
                break;
            case 12:
                CameraWidth = 800;
                toast = Toast.makeText(this, "Minimum HUD resolution" , Toast.LENGTH_LONG);
                toast.show();
                break;

        }

        return true;
    }





    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }



    public Mat onCameraFrame(CvCameraViewFrame frame) {

        if(ModeGreay){
            //Black and White Mode
            return frame.gray();
        }else{


            //Mat, to work later in the frame the pixels
            Mat mat = frame.rgba();

            // PIXEL CENTRAL
            int height = mat.height() / 2;	//camera.getHeight() / 2;
            int width = mat.width() / 2;	//camera.getWidth() / 2;

            //We recover the color of the central pixel
            double[] color = mat.get(height, width);

            //Console log to see if you get the colors
            //Log.i(TAG , "COLORES RGB -->"+ color[0] +";"+ color[1] +";"+ color[2] +"");

            //The reverse color, to paint the crosshair and always see it
            double[] colorInverso = { 255 - color[0], 255 - color[1], 255 - color[2], 255};



            //Lineas Horizontales
            //Imgproc.line(mat, new Point(0, cameraHeight), new Point(CameraWidth - 3, cameraHeight), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Left
            //Imgproc.line(mat, new Point(CameraWidth + 3, cameraHeight), new Point(CameraWidth + CameraWidth, cameraHeight), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Right

            //Lineas Verticales
            //Imgproc.line(mat, new Point(CameraWidth, 0), new Point(CameraWidth, cameraHeight - 3), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Top
            //Imgproc.line(mat, new Point(CameraWidth, cameraHeight + 3), new Point(CameraWidth, cameraHeight + cameraHeight), new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1, 1, 1); //Bottom

            //Inner circle
            Imgproc.circle(mat, new Point(width, height), 3, new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), -1);

            //Outer circle
            Imgproc.circle(mat, new Point(width, height), 50, new Scalar(colorInverso[0], colorInverso[1], colorInverso[2]), 1);



            //TEXT
            //Text generated in each frame with the color in BGR (float)
            //Yes, BGR, OpenCV handles colors like Blue Green Red, not like Red Green Blue
            String text = "RGB: " + color[0] + " " + color[1] + " " + color[2];
            //Core.putText(img, text, org, fontFace, fontScale, color);
            Imgproc.putText(mat, text, new Point(10, 50), 3, 1, new Scalar(255, 255, 255, 255), 2);

            //Text ColorName
            String nameColor = getColorName(color[0], color[1], color[2]);
            Imgproc.putText(mat, nameColor, new Point(width, 50), 3, 1, new Scalar(255, 255, 255, 255), 2);

            //Colored rectangle of current color
            //Core.rectangle(img, pt1, pt2, color, thickness);
            //If thickness <0, fill the rectangle (Fill it)
            Imgproc.rectangle(mat, new Point( 10 , 80), new Point(CameraWidth - 10, 100), new Scalar(color[0], color[1], color[2], 255), -1); //When painting, we use RGBA


            return mat;
        }

    }
    static String existinColor="";

    public String getColorName(double r, double g, double b){

        String ColorName = null;


        if(ModeRecognition){ //Mode Precise


            //White
            if(r > 140.0 && g > 140.0 && b > 140.0){
                if(r > 200.0 && g > 200.0 && b > 200.0){
                    ColorName = "" +
                            "Pure white";
                }else{
                    ColorName = "White";
                }
            }

            //Black
            if(r < 50.0 && g < 50.0 && b < 50.0){
                ColorName = "Black";
            }

            //Red
            if(r > 100.0 && g < 100.0 && b < 100.0){
                ColorName = "Red";
            }

            //Green
            if(r < 100.0 && g > 100.0 && b < 100.0){
                ColorName = "" + "Green";
            }

            //Blue
            if(r < 100.0 && g < 100.0 && b > 100.0){
                ColorName = "Blue";
            }

            //Yellow
            if(r > 180.0 && r < 230.0 && g > 200.0 && g < 230.0 && b < 30.0){
                ColorName = "" +
                        "Yellow";
            }

            //Cyan
            if(r < 10.0 && g > 200.0 && g < 230.0 && b > 230.0 && b < 240.0){
                ColorName = "Cyan";
            }

            //Magenta
            if(r > 200.0 && r < 220.0 && g > 30.0 && g < 50.0 && b > 220.0 && b < 240.0){
                ColorName = "Magenta";
            }


        }else{ //Color Ranges Mode

            // We calculate from the Hue, instead of the value ... This is how we take ranges
            // http://en.wikipedia.org/wiki/Hue

            //Red
            if(r >= g && g >= b){
                ColorName = "Red tone";
            }

            //Yellow
            if(g > r && r >= b){
                ColorName = "" +
                        "Yellow Tone";
            }

            //Green
            if(r<=10&&g>=200&&b<=10){
                ColorName = "Green Tone";
            }

            //Cyan
            if(b > g && g > r){
                ColorName = "Cyan Tone";
            }

            //Azul
            if(b > r && r >= g){
                ColorName = "" +
                        "Blue tone";
            }

            //Magenta
            if(r >= b && b > g){
                ColorName = "Magenta tone";
            }

            //Negro
            if(r < 10.0 && g < 10.0 && b < 10.0){
                ColorName = "Black tone";
            }


            if(r>=128&&g<=50&&b>=128){
                ColorName = "Purple";
            }

            if(r>=200&&g<=150&&g>100&&b<=100){
                ColorName = "Corel";
            }

            if(r>=200&&g<=50&&b<=100){
                ColorName = "Amarnath";
            }

            if(r>=200&&g>=150&&g<=200&&b<=50){
                ColorName = "Amber";
            }

            if(r<=200&&g>=100&&g<=150&&b>=200){
                ColorName = "Amethyst";
            }

            if(r>=200&&g>=200&&b<=200){
                ColorName = "Apricot";
            }

            if(r<=200&&g>=200&&b>=200){
                ColorName = "Aquamarine";
            }

            if(r<=10&&g>=100&&g<=150&&b>=200){
                ColorName = "Azure";
            }
            if(r<=150&&g>=200&&b>=200){
                ColorName = "Baby Blue";
            }

            if(r>=10&&g>=100&&g<=150&&b<=200){
                ColorName = "Blue Green";
            }

            if(r<=150&&g<=50&&b>=200){
                ColorName = "Blue Violet";
            }

            if(r<=200&&g<=100&&b<=10){
                ColorName = "Brown";
            }

            if(r<=150&&g<=10&&b<=50){
                ColorName = "Burgundry";
            }

            if(r<=150&&g>=50&&g<=100&&b<=10){
                ColorName = "Chocolate";
            }

            if(r<=150&&g>=50&&g<=100&&b<=100){
                ColorName = "Coffe";
            }

            if(r<=200&&g>=100&&g<=150&&b<=100){
                ColorName = "Copper";
            }

            if(r<=100&&g>=200&&b<=10){
                ColorName = "Harlequin";
            }
            if(r<=150&&g>=100&&g<=150&&b>=100){
                ColorName = "Gray";
            }

            if(r>=200&&g>=200&&b<=10){
                ColorName = "Gold";
            }

            if(r<=100&&g<=10&&b>=100){
                ColorName = "Indigo";
            }
            if(r<=150&&g<=100&&b<=100){
                ColorName = "Maroon";
            }

            if(r>=200&&g>=100&&g<=150&&b<=100){
                ColorName = "Orange";
            }

            if(r>=200&&g<=100&&b<=10){
                ColorName = "Orange Red";
            }

            if(r>=200&&g>=100&&g<=150&&b>=200){
                ColorName = "orchid";
            }


            if(r>=200&&g>=200&&b<=200){
                ColorName = "peach";
            }


            if(r>=200&&g>=200&&b<=50){
                ColorName = "pear";
            }


            if(r>=200&&g>=150&&g<=200&&b>=200){
                ColorName = "pink";
            }


            if(r>=200&&g>=150&&g<=200&&b<=200){
                ColorName = "Silver";
            }

            if(r>=200&&g<=50&&b<=50){
                ColorName = "Ruby";
            }

            if(r<=10&&g<=10&&b<=10){
                ColorName = "Black";
            }


            if(r<=150&&g<=10&&b>=200){
                ColorName = "Violet";
            }
            //Blanco
            if(r > 140.0 && g > 140.0 && b > 140.0){
                if(r > 255.0 && g > 255.0 && b > 255.0){
                    ColorName = "Pure white";
                }else{
                    ColorName = "white tone";
                }
            }

        }

        String text = ColorName;
        assert ColorName != null;
        if(!text.equals(existinColor)) {
            existinColor=text;
            textToSpeech.speak(text.trim(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
        return ColorName;

    }

}

