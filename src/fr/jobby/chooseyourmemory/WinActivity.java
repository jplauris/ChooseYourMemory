package fr.jobby.chooseyourmemory;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WinActivity extends Activity {
    
    protected static BoardActivity parentActivity = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Bundle b = getIntent().getExtras();
        int strokeNumber = b.getInt("strokeNumber");

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;

        FrameLayout winLayout = new FrameLayout(this);

        TextView winTextView = new TextView(this);
        String winText = this.getString(R.string.win_left) + " " + strokeNumber + " " + this.getString(R.string.win_right);
        
        SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
        SharedPreferences settings = this.getSharedPreferences(optionSingleton.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
        Typeface font = Typeface.createFromAsset(this.getAssets(), settings.getString("default-font", "sans-serif"));

        int textSize = 1+ (int)(displayWidth) / (winText.length());
        winTextView.setText(winText);
        winTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        winTextView.setShadowLayer(2, 4, 4, Color.rgb(50, 50, 50));
        winTextView.setGravity(Gravity.CENTER);
        winTextView.setWidth(displayWidth / 2);
        winTextView.setHeight(displayWidth / 6);
        winTextView.setTypeface(font, Typeface.ITALIC);
        winLayout.addView(winTextView);
        this.setContentView(winLayout);
       
        winTextView.setOnClickListener(new View.OnClickListener() {  
            @Override
            public void onClick(View v) {
                finish();
                parentActivity.finish();
            }
        });        
    }
    
	@Override
	public void onStop() {
		super.onStop();
		this.finish();   	
	}  
}
