package fr.jobby.chooseyourmemory;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ImageListActivity extends Activity {
	private static final int IMAGE_X_NUMBER = 8;
	private static final int IMAGE_Y_NUMBER = 4;
	
//	private static final int MAX_IMAGE_SIZE_DP = 300;
	private static final int INTERVAL_BETWEEN_IMAGES = 5;
	private static final int INTERVAL_BETWEEN_ADS = 20;

	private static final int BUTTON_COLOR = Color.GRAY;
	
	private AdView[] adViews;
	private ImageView[] imageViews;
	private int imageSize;
	private SavedPictureHandler pictureHandler;
	private SharedPreferences pictureSettings;
	private int clickedPicture;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		initUI();
	}
	
	private void initUI() {
		this.clickedPicture = -1;
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.pictureHandler = SavedPictureHandler.getInstance();
		this.pictureSettings = getSharedPreferences(this.pictureHandler.getPicturePreferenceFileName(), Context.MODE_PRIVATE); 

		SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
		SharedPreferences settings = getSharedPreferences(optionSingleton.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
		int deviceHeight = settings.getInt("device-height", -1);
		int deviceWidth = settings.getInt("device-width", -1);
		
		// Get ad information
		AdHandler adHandler = AdHandler.getInstance();
		int maxTableHeight = deviceHeight - adHandler.getStandardBannerHeight() - INTERVAL_BETWEEN_IMAGES * 2;
		
		this.imageSize = Math.min((maxTableHeight / IMAGE_Y_NUMBER),
				(deviceWidth - INTERVAL_BETWEEN_IMAGES) / IMAGE_X_NUMBER) - INTERVAL_BETWEEN_IMAGES;
				
		this.imageViews = new ImageView[optionSingleton.getDefaultPictureNumber()];

//		int totalHorizPadding = deviceWidth - (imageSize * IMAGE_X_NUMBER);
//		int totalVertPadding = deviceHeight - (imageSize * IMAGE_Y_NUMBER);
//		int horizPaddingSize = totalHorizPadding / (IMAGE_X_NUMBER + 1);
//		int verticalPaddingSize = totalVertPadding / (IMAGE_Y_NUMBER + 1);

		//            this.cards = new ArrayList<CardLayout>();
		//            this.cardIds = new ArrayList<Integer>();
		//            this.cardIds = new int[this.imageNumber];
		// Init basic settings
//		SavedPictureHandler pictureHandler = SavedPictureHandler.getInstance();
	
		LinearLayout generalLayout = new LinearLayout(this);
		generalLayout.setOrientation(LinearLayout.VERTICAL);
		generalLayout.setBackgroundResource(R.drawable.pictures_bg);        

		int i = 0;
//		TableLayout tableLayout = new TableLayout(this);
//		tableLayout.setBackgroundResource(R.drawable.pictures_bg);        

		for (int y = 0 ; y < IMAGE_Y_NUMBER ; y++) {
//			TableRow tableRow = new TableRow(this);
			LinearLayout rowLayout = new LinearLayout(this);
			for (int x = 0 ; x < IMAGE_X_NUMBER ; x++) {
				FrameLayout imageLayout = new FrameLayout(this);
				imageLayout.setPadding(INTERVAL_BETWEEN_IMAGES, INTERVAL_BETWEEN_IMAGES, 0, 0);
				ImageView image = new ImageView(this);
				Bitmap bitmap = this.pictureHandler.getSavedBitmap(this, i, this.imageSize, this.pictureSettings);
				image.setImageBitmap(bitmap);
				image.setOnClickListener(new ImageClickListener(i, this));    
				imageLayout.addView(image);
				
				TextView numberView = new TextView(this);
				numberView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				numberView.setBackgroundColor(Color.TRANSPARENT);
				numberView.setText(" " + (i + 1));
				imageLayout.addView(numberView);
				
				rowLayout.addView(imageLayout);
				this.imageViews[i] = image;
				i++;
				// Add back button
				if (i == optionSingleton.getDefaultPictureNumber()) {
					// Back button
					LinearLayout buttonLayout = new LinearLayout(this);
					buttonLayout.setPadding(this.imageSize / 2, this.imageSize / 4, 0, 0);
//					buttonLayout.setGravity(Gravity.CENTER_VERTICAL);
					Typeface font = Typeface.createFromAsset(getAssets(), settings.getString("default-font", "sans-serif"));
					String backText = this.getString(R.string.back);
					int backButtonFontSize = (int)(this.imageSize / backText.length()) - 2; 
					Button backButton = new Button(this);
					backButton.setHeight(this.imageSize / 2);
					backButton.setWidth(this.imageSize);
					backButton.setText(backText);
					backButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, backButtonFontSize);
					backButton.setTypeface(font, Typeface.ITALIC);
					backButton.setShadowLayer(2, 2, 2, Color.rgb(50, 50, 50));
					backButton.setOnClickListener(new BackButtonClickListener());
					backButton.getBackground().setColorFilter(BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);	
					buttonLayout.addView(backButton);
					rowLayout.addView(buttonLayout);
					break;
				}
			}
			generalLayout.addView(rowLayout);
		}
//		generalLayout.addView(tableLayout);
		
		LinearLayout adsLayout = null;

		if (adHandler.isAdEnabled()) {
			adsLayout = new LinearLayout(this);
//			adsLayout.setMinimumHeight(deviceHeight - pictureLayoutHeight - 2 * interval);
			adsLayout.setGravity(Gravity.CENTER_VERTICAL);
//			adLayout.setGravity(Gravity.CENTER_HORIZONTAL);

			adsLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));			
//			adLayout.setBackgroundColor(Color.GRAY);
//			scrollView.setBackgroundColor(Color.CYAN);
			generalLayout.addView(adsLayout);
		}
		
		this.setContentView(generalLayout);
		
		if (adHandler.isAdEnabled()) {
			// Create ads
			// Ad size in real pixels
			Resources r = getResources();
			float adSizePX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adHandler.getStandardBannerWidth(), r.getDisplayMetrics());

			int adNumber = deviceWidth / ((int)adSizePX + INTERVAL_BETWEEN_ADS); 
			int paddingLeft = (deviceWidth - (adNumber * ((int)adSizePX + INTERVAL_BETWEEN_ADS))) / 2;
			this.adViews = new AdView[adNumber];

			for (i = 0 ; i < adNumber ; i++) {
				LinearLayout adLayout = new LinearLayout(this);
				if (i == 0) {
					adLayout.setPadding(paddingLeft, 0, 0, 0);
				} else {
					adLayout.setPadding(INTERVAL_BETWEEN_ADS, 0, 0, 0);
				}
				AdView adView = new AdView(this);
				adView.setAdSize(AdSize.BANNER);
				adView.setAdUnitId(adHandler.getPictureOptionsBannerAdId());

				//		    this.adView.setMinimumHeight(adHandler.getStandardBannerHeight());

				adLayout.addView(adView);

				// Create an ad request. Check logcat output for the hashed device ID to
				// get test ads on a physical device.
				AdRequest adRequest = new AdRequest.Builder()
//				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//				.addTestDevice("1EFAE598BF601813A346F8AB33516DB8") // emulator genymotion (Samsung Galaxy S3)
//				.addTestDevice("868CB92008BDED12A64DB999622964A1") // emulator genymotion (Tablet 10")
//				.addTestDevice("9DC989DFCADA27DF6E920F128E27BE81") 
//				.addTestDevice("83337F7F047CCA82366FDA5B25AD0BE9") 
//				.addTestDevice("228D3AD99CCA811B11B9B292E318259D") 
				.build();

				// Start loading the ad in the background.
				adView.loadAd(adRequest);
				this.adViews[i] = adView;
				adsLayout.addView(adLayout);
			}
		}
	}
	
	private void update(int clickedPicture) {
		Bitmap bitmap = this.pictureHandler.getSavedBitmap(this, clickedPicture, this.imageSize, this.pictureSettings);
		this.imageViews[clickedPicture].setImageBitmap(bitmap);
	}
	
	public class BackButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			ImageListActivity.this.finish();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.adViews != null) {
			for (AdView adView : this.adViews) {
				adView.resume();
			}
		}
		if (this.clickedPicture > -1) {
			this.update(this.clickedPicture);
		}
	}


	@Override
	public void onPause() {
		if (this.adViews != null) {
			for (AdView adView : this.adViews) {
				adView.pause();
			}
		}
		super.onPause();
	}

	/** Called before the activity is destroyed. */
	@Override
	public void onDestroy() {
		// Destroy the AdView.
		if (this.adViews != null) {
			for (AdView adView : this.adViews) {
				adView.destroy();
			}
		}
		super.onDestroy();
	}

	
	public class ImageClickListener implements View.OnClickListener {

		private int pictureIndex;
		private ImageListActivity parentActivity;

		public ImageClickListener(int pictureIndex, ImageListActivity parentActivity) {
			this.pictureIndex = pictureIndex;
			this.parentActivity = parentActivity;
		}

		@Override
		public void onClick(View v) {
			ImageListActivity.this.clickedPicture = this.pictureIndex;
			Intent intent = new Intent(this.parentActivity, ImageUpdateActivity.class);
			Bundle b = new Bundle();
			b.putInt("pictureIndex", this.pictureIndex); 
			intent.putExtras(b); 
			startActivity(intent);
		}
	}
}

