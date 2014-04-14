package fr.jobby.chooseyourmemory;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuActivity extends Activity {

	//    private static final int TOP_SIZE = 10;
	private static final int EASY = R.string.easy;
	private static final int MEDIUM = R.string.medium;
	private static final int DIFFICULT = R.string.difficult;
	private static final int EXPERT = R.string.expert;

	private static final int OPTION_BUTTON_COLOR = Color.rgb(100, 130, 160);
	private static final Level[] levels = {
		new Level(EASY, 4, 3, Color.rgb(30, 250, 30)), 
		new Level(MEDIUM, 6, 4, Color.rgb(247, 254, 46)), 
		new Level(DIFFICULT, 8, 5, Color.rgb(200, 64, 0)), 
		new Level(EXPERT, 10, 6, Color.rgb(255, 0, 0))
	};

	private static final float[] LEFT_BUTTON_RADII = {10, 10, 2, 2, 2, 2, 4, 10};
	private static final float[] RIGHT_BUTTON_RADII = {2, 2, 10, 10, 4, 10, 2, 2};
//	private static final float OPTION_TEXT_SIZE = 32;
//	private static final float MENU_TEXT_SIZE = 24;

	private AdHandler adHandlerSingleton;
	private InterstitialAd interstitial;
	private int actionNumber = -1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);        
		initUI();
	}

	private void initUI() {

		LinearLayout generalLayout = new LinearLayout(this);
		generalLayout.setBackgroundResource(R.drawable.menu_bg);
		//        generalLayout.setBackgroundColor(Color.rgb(123, 12, 34));
		SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
		SharedPreferences settings = getSharedPreferences(optionSingleton.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
		int deviceHeight = settings.getInt("device-height", -1);
		int deviceWidth = settings.getInt("device-width", -1);
		boolean firstTime = settings.getBoolean("first-time", true);

		Typeface font = Typeface.createFromAsset(getAssets(), settings.getString("default-font", "sans-serif"));

		int cellWidth = deviceWidth / 2;
		int cellHeight = deviceHeight / levels.length;
		int paddingWidth = (int)(cellWidth * 0.25);
		int paddingHeight = (int)(cellHeight * 0.25);
		int buttonWidth = (int)(cellWidth - paddingWidth * 1.5);
		int buttonHeight = (cellHeight - paddingHeight - (paddingHeight / levels.length));
		int textSize;
		float buttonRadiiMultFactor = buttonWidth / 40f;
		float[] leftButtonRadii = new float[8];
		float[] rightButtonRadii = new float[8];
		for (int i = 0 ; i < 8 ; i++) {
			leftButtonRadii[i] = LEFT_BUTTON_RADII[i] * buttonRadiiMultFactor;
			rightButtonRadii[i] = RIGHT_BUTTON_RADII[i] * buttonRadiiMultFactor;
		}

		int maxTextLength = 0;
		for (int i = 0 ; i < levels.length ; i++) {
			Level level = levels[i];
			maxTextLength = Math.max(maxTextLength, this.getString(level.getLevel()).length());
		}
		
		textSize = (buttonWidth / maxTextLength) + 1;
		
		/** Level buttons (left side) */
		LinearLayout levelsLayout = new LinearLayout(this);
		levelsLayout.setOrientation(LinearLayout.VERTICAL);

		for (int i = 0 ; i < levels.length ; i++) {
			LinearLayout levelLayout = new LinearLayout(this);
			levelLayout.setPadding(paddingWidth, paddingHeight, 0, 0);

			Level level = levels[i];
			Button button = new Button(this);
			button.setText(level.getLevel());
			//            this.setButtonBackground(button, level.getColor());
			//            button.setTextSize(textSize);
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			button.setShadowLayer(2, 4, 4, Color.rgb(50, 50, 50));            
			button.setHeight(buttonHeight);
			button.setWidth(buttonWidth);
			button.setTypeface(font);
			this.setButtonBackground(button, leftButtonRadii, level.getColor());

			//            button.getBackground().setColorFilter(level.getColor(), PorterDuff.Mode.MULTIPLY);            
			button.setOnClickListener(new LevelButtonListener(this, level));    
			levelLayout.addView(button);
			levelsLayout.addView(levelLayout);
		}

		generalLayout.addView(levelsLayout);

		/** Option buttons (right side) */
		int optionButtonNumber = 2;
		int optionsLayoutPaddingHeight = (deviceHeight - (cellHeight * optionButtonNumber)) / 2;
		LinearLayout optionsLayout = new LinearLayout(this);
		optionsLayout.setOrientation(LinearLayout.VERTICAL);

		maxTextLength = Math.max(this.getString(R.string.select_pictures).length(),
				this.getString(R.string.options).length());
		textSize = Math.min(textSize, 1+ (buttonWidth / maxTextLength));

		// Select pictures option
		LinearLayout selectLayout = new LinearLayout(this);
		selectLayout.setPadding(paddingWidth, optionsLayoutPaddingHeight, 0, paddingHeight);
		Button selectButton = new Button(this);
		selectButton.setText(R.string.select_pictures);
		//        selectButton.setTextSize(textSize);
		//        selectButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, OPTION_TEXT_SIZE);
		selectButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		selectButton.setShadowLayer(2, 2, 2, Color.rgb(50, 50, 50));
		selectButton.setHeight(buttonHeight);
		selectButton.setWidth(buttonWidth);
		selectButton.setTypeface(font);
		this.setButtonBackground(selectButton, rightButtonRadii, OPTION_BUTTON_COLOR);

		//        selectButton.getBackground().setColorFilter(optionButtonsColor, PorterDuff.Mode.MULTIPLY);
		selectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InterstitialAd ad = getInterstitialAdOrNot();
				if (ad != null) {
					ad.setAdListener(new AdListener() {
						@Override
						public void onAdClosed() {
							loadInterstitialAd();
							Intent intent = new Intent(MenuActivity.this, ImageListActivity.class);
							startActivity(intent);
						}
					});
					ad.show();
				} else {
					Intent intent = new Intent(MenuActivity.this, ImageListActivity.class);
					startActivity(intent);					
				}
			}
		});
		selectLayout.addView(selectButton);
		optionsLayout.addView(selectLayout);

		// Other options
		LinearLayout optionLayout = new LinearLayout(this);
		optionLayout.setPadding(paddingWidth, paddingHeight, paddingWidth, optionsLayoutPaddingHeight);
		Button optionButton = new Button(this);

		optionButton.setText(R.string.options);
		optionButton.setShadowLayer(2, 2, 2, Color.rgb(50, 50, 50));
		//        optionButton.setTextSize(textSize);
		optionButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		optionButton.setHeight(buttonHeight);
		optionButton.setWidth(buttonWidth);
		optionButton.setTypeface(font);
		this.setButtonBackground(optionButton, rightButtonRadii, OPTION_BUTTON_COLOR);
		//        optionButton.getBackground().setColorFilter(optionButtonsColor, PorterDuff.Mode.MULTIPLY);
		optionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, OptionActivity.class);
				startActivity(intent);
			}
		});

		optionLayout.addView(optionButton);
		optionsLayout.addView(optionLayout);
		generalLayout.addView(optionsLayout);

		this.setContentView(generalLayout);
		if (!firstTime) {
			this.loadInterstitialAd();
		} else {
			this.unsetFirstTime(settings);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void unsetFirstTime(SharedPreferences settings) {
        Editor editor = settings.edit();
        editor.putBoolean("first-time", false);
        // apply is only available from API version 9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        	editor.apply(); 
        } else {
        	editor.commit();
        }

	}

	private InterstitialAd getInterstitialAdOrNot() {
		InterstitialAd result = null;
		if (this.adHandlerSingleton == null) {
			this.adHandlerSingleton = AdHandler.getInstance();
		}
		if (this.actionNumber < 0) {
			this.actionNumber = (int)(Math.random() * this.adHandlerSingleton.getAdFrequency());
		}
		// Show
		if (this.actionNumber % this.adHandlerSingleton.getAdFrequency() == 0) {
			if (this.interstitial != null && this.interstitial.isLoaded()) {
				result = this.interstitial;
			}
		}
		this.actionNumber++;
		return result;
	}

	/** Load interstitial Ad */
	private void loadInterstitialAd() {
		if (this.adHandlerSingleton == null) {
			this.adHandlerSingleton = AdHandler.getInstance();
		}
		if (this.adHandlerSingleton.isAdEnabled()) {
			this.interstitial = new InterstitialAd(this);
			this.interstitial.setAdUnitId(this.adHandlerSingleton.getInterAdId());
			// Check the logcat output for your hashed device ID to get test ads on a physical device.
			AdRequest adRequest = new AdRequest.Builder()
//			.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//			.addTestDevice("1EFAE598BF601813A346F8AB33516DB8") // emulator genymotion (Samsung Galaxy S3)
//			.addTestDevice("868CB92008BDED12A64DB999622964A1") // emulator genymotion (Tablet 10")
//			.addTestDevice("9DC989DFCADA27DF6E920F128E27BE81")
//			.addTestDevice("83337F7F047CCA82366FDA5B25AD0BE9")
//			.addTestDevice("228D3AD99CCA811B11B9B292E318259D")
			.build();
			this.interstitial.loadAd(adRequest);
		} else {
			this.interstitial = null;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setButtonBackground(Button button, float[] radii, int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {
					Color.DKGRAY, 
					color, color, color, color, color, 
					Color.DKGRAY
			});
			gradientDrawable.setGradientType(GradientDrawable.RECTANGLE);
			gradientDrawable.setCornerRadii(radii);
			button.setBackground(gradientDrawable);
		} else {
			button.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);	
		}
	}

	public class LevelButtonListener implements View.OnClickListener {

		private MenuActivity mainMenu;
		private Level level;

		public LevelButtonListener(MenuActivity menu, Level level) {
			this.mainMenu = menu;
			this.level = level;
		}

		@Override
		public void onClick(View v) {
			InterstitialAd ad = getInterstitialAdOrNot();

			if (ad != null) {
				ad.setAdListener(new AdListener() {
					@Override
					public void onAdClosed() {
						Intent intent = new Intent(LevelButtonListener.this.mainMenu, BoardActivity.class);
						Bundle b = new Bundle();
						b.putInt("x", level.getxNumber()); 
						b.putInt("y", level.getyNumber());
						intent.putExtras(b); 
						startActivity(intent);
						loadInterstitialAd();
					}
				});
				ad.show();
			} else {
				Intent intent = new Intent(LevelButtonListener.this.mainMenu, BoardActivity.class);
				Bundle b = new Bundle();
				b.putInt("x", level.getxNumber()); 
				b.putInt("y", level.getyNumber());
				intent.putExtras(b); 
				startActivity(intent);
			}
		}
	}
}
