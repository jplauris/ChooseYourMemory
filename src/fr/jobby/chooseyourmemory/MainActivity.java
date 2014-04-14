package fr.jobby.chooseyourmemory;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private static final int TOP_SIZE = 10;

//    protected static int DEVICE_WIDTH;
//    protected static int DEVICE_HEIGHT;

    private final static String FONT_NAME = "Chantelli_Antiqua.ttf";
    
    private static final double SPLASH_DURATION = 1;
    
    private int deviceWidth;
    private int deviceHeight;
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point size = new Point();
            try {
                this.getWindowManager().getDefaultDisplay().getRealSize(size);
                this.deviceWidth = size.x;
                this.deviceHeight = size.y - TOP_SIZE;
            } catch (NoSuchMethodError e) {
            }
        } 
        if (this.deviceWidth == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            this.deviceWidth = metrics.widthPixels;
            this.deviceHeight = metrics.heightPixels - TOP_SIZE;
        }
        
        SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
        SharedPreferences settings = getSharedPreferences(optionSingleton.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        editor.putInt("device-height", this.deviceHeight);
        editor.putInt("device-width", this.deviceWidth);
        editor.putString("default-font", FONT_NAME);
        // apply is only available from API version 9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        	editor.apply(); 
        } else {
        	editor.commit();
        }
        initSplash(optionSingleton);        
    }

    private void initSplash(SavedPictureHandler optionSingleton) {
        LinearLayout layout = new LinearLayout(this);
        
        int imageSize = Math.min(this.deviceHeight, 400) - 20;
        int heightPadding = (this.deviceHeight - imageSize) / 2;
        int widthPadding = (this.deviceWidth - imageSize) / 2;
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setPadding(widthPadding, heightPadding, widthPadding, heightPadding);

        ImageView image = new ImageView(this);                
        Bitmap frontBMP = BitmapFactory.decodeResource(getResources(), R.drawable.backimage);
        Bitmap resizedFrontBMP = Bitmap.createScaledBitmap(frontBMP, imageSize, imageSize, true);
        image.setImageBitmap(resizedFrontBMP);
        layout.addView(image);

        this.setContentView(layout);
        
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() { 
            public void run() {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            } 
        }, (int)(SPLASH_DURATION * 1000));
        
        // clean external app cache
        File appDir = this.getExternalCacheDir();
        if (appDir != null) {
        	for (File file : appDir.listFiles()) {
        		if (file.canWrite()) {
        			file.delete();
        		}
        	}
        }
    }   
    
}
