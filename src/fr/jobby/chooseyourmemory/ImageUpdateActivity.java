package fr.jobby.chooseyourmemory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.channels.FileChannel;

import com.google.android.gms.ads.AdView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ImageUpdateActivity extends Activity {

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private static final int CROP_FROM_FILE = 4;

	private static final int STATUS_UNCHANGED = 0;
	private static final int STATUS_BACK_TO_DEFAULT = 1;
	private static final int STATUS_NEW_PICTURE = 2;
	
	private static final double DELAY_BEFORE_SHOW_DIALOG = 0.25;
	
//	private static final int MIN_MAX_IMAGE_SIZE_DP = 150;
	private static final int MAX_IMAGE_SIZE_DP = 400;
	
//	private static final int INTERVAL_BETWEEN_ADS = 20;

	private static final int BUTTON_COLOR = Color.GRAY;

	private Uri mImageCaptureUri;
	private int imageSize;
	private SavedPictureHandler pictureHandler;
	private Typeface font;
	private SharedPreferences pictureSettings;
	private ImageView imageView;
//	private Bitmap[] bitmaps;
//	private int[] addedPictureFileNames;
	private AdView[] adViews;
//	private HorizontalScrollView scrollView;

	private ImageButtonsLayout imageButtonsLayout;
	
	private int pictureIndex;
	private int status;
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user's current game state
	    savedInstanceState.putInt("pictureIndex", this.pictureIndex);
	    
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.pictureHandler = SavedPictureHandler.getInstance();
		
		if (savedInstanceState == null) {
			Bundle b = getIntent().getExtras();
			this.pictureIndex = b.getInt("pictureIndex");
		} else {
			this.pictureIndex = savedInstanceState.getInt("pictureIndex");
		}

		initUI(savedInstanceState);
	}

	private void initUI(Bundle savedInstanceState) {
		SharedPreferences settings = getSharedPreferences(pictureHandler.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
		this.pictureSettings = getSharedPreferences(this.pictureHandler.getPicturePreferenceFileName(), Context.MODE_PRIVATE); 
		int deviceHeight = settings.getInt("device-height", -1);
		int deviceWidth = settings.getInt("device-width", -1);
		// Max image size in real pixel
		Resources r = getResources();
		float maxImageSizePX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_IMAGE_SIZE_DP, r.getDisplayMetrics());

		LinearLayout generalLayout = new LinearLayout(this);
		generalLayout.setOrientation(LinearLayout.VERTICAL);
		generalLayout.setBackgroundResource(R.drawable.pictures_bg);
//		generalLayout.setGravity(Gravity.LEFT);
		
		// Get ad information
//		AdHandler adHandler = AdHandler.getInstance();
		
		this.imageSize = Math.min((int)maxImageSizePX, 
									(int)((deviceHeight - 20) * 4 / 5));  
		int pictureLayoutHeight = this.imageSize * 5 / 4;
		int paddingTop = (deviceHeight - pictureLayoutHeight) / 2;
		int paddindLeft = (deviceWidth - this.imageSize) / 2;
		
		generalLayout.setPadding(paddindLeft, paddingTop, paddindLeft, 0);
		
//		int interval = (deviceHeight - pictureLayoutHeight - adHandler.getStandardBannerHeight()) / 3;

		this.font = Typeface.createFromAsset(getAssets(), settings.getString("default-font", "sans-serif"));
//		this.scrollView = new HorizontalScrollView(this);

//		LinearLayout picturesLayout = new LinearLayout(this);
//		picturesLayout.setGravity(Gravity.CENTER_VERTICAL);
//		picturesLayout.setPadding(0, interval, 0, interval);

		//////////////
		final String [] items = new String [] {this.getString(R.string.select_image_from_camera), 
				this.getString(R.string.select_image_from_gallery), 
				this.getString(R.string.select_image_from_default),
				this.getString(R.string.cancel)};    
		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(this.getString(R.string.select_image));
		builder.setAdapter(adapter, new ChangePictureClickAdapter());

		final AlertDialog dialog = builder.create();

		LinearLayout imageLayout = new LinearLayout(this);
		imageLayout.setOrientation(LinearLayout.VERTICAL);
		this.imageView = new ImageView(this);
		Bitmap bitmap;
		switch (this.status) {
		case STATUS_NEW_PICTURE:
			bitmap = this.pictureHandler.getModifiedBitmap(this, pictureIndex, this.imageSize - 4);
			break;
		case STATUS_BACK_TO_DEFAULT:
			bitmap = this.pictureHandler.getDefaultBitmap(this, pictureIndex, this.imageSize - 4);
			break;
		default:
			bitmap = this.pictureHandler.getSavedBitmap(this, pictureIndex, this.imageSize - 4, this.pictureSettings);
			break;
		}
		this.imageView.setImageBitmap(bitmap);
		this.imageView.setOnClickListener(new ClickImageClickListener(dialog));    
		imageLayout.addView(this.imageView);
		
		ImageButtonsLayout buttonsLayout = new ImageButtonsLayout(this, pictureIndex, this.imageSize, this.status,
					this.pictureSettings);

		imageLayout.addView(buttonsLayout);
		this.imageButtonsLayout = buttonsLayout;
		//			picturesLayout.addView(imageLayout);			
		generalLayout.addView(imageLayout);

//		LinearLayout adsLayout = null;

//		if (adHandler.isAdEnabled()) {
//			adsLayout = new LinearLayout(this);
//			adsLayout.setMinimumHeight(deviceHeight - pictureLayoutHeight - 2 * interval);
//			adsLayout.setGravity(Gravity.CENTER_VERTICAL);
//
//			adsLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));			
//			generalLayout.addView(adsLayout);
//		}
		
		this.setContentView(generalLayout);

//		// Ad
//		if (adHandler.isAdEnabled()) {
//			// Create ads
//			int deviceWidth = settings.getInt("device-width", -1);
//			// Ad size in real pixels
//			float adSizePX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adHandler.getStandardBannerWidth(), r.getDisplayMetrics());
//
//			int adNumber = deviceWidth / ((int)adSizePX + INTERVAL_BETWEEN_ADS); 
//			int paddingLeft = (deviceWidth - (adNumber * ((int)adSizePX + INTERVAL_BETWEEN_ADS))) / 2;
//			this.adViews = new AdView[adNumber];
//
//			for (int i = 0 ; i < adNumber ; i++) {
//				LinearLayout adLayout = new LinearLayout(this);
//				if (i == 0) {
//					adLayout.setPadding(paddingLeft, 0, 0, 0);
//				} else {
//					adLayout.setPadding(INTERVAL_BETWEEN_ADS, 0, 0, 0);
//				}
//				AdView adView = new AdView(this);
//				adView.setAdSize(AdSize.BANNER);
//				adView.setAdUnitId(adHandler.getPictureOptionsBannerAdId());
//
//				//		    this.adView.setMinimumHeight(adHandler.getStandardBannerHeight());
//
//				adLayout.addView(adView);
//
//				// Create an ad request. Check logcat output for the hashed device ID to
//				// get test ads on a physical device.
//				AdRequest adRequest = new AdRequest.Builder()
//				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//				.addTestDevice("1EFAE598BF601813A346F8AB33516DB8") // emulator genymotion (Samsung Galaxy S3)
//				.addTestDevice("868CB92008BDED12A64DB999622964A1") // emulator genymotion (Tablet 10")
//				.addTestDevice("9DC989DFCADA27DF6E920F128E27BE81") 
//				.addTestDevice("83337F7F047CCA82366FDA5B25AD0BE9") 
//				.addTestDevice("228D3AD99CCA811B11B9B292E318259D") 
//
//				//        		.addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB") // emulator ID
//				//        		.addTestDevice("839c72c997cd54f17129bd02b8faeda8") // tablet ID (3215DC55BFD47CD1)
//				.build();
//
//				// Start loading the ad in the background.
//				adView.loadAd(adRequest);
//				this.adViews[i] = adView;
//				adsLayout.addView(adLayout);
//			}
//		}
		if (savedInstanceState == null) {
			Handler handler = new Handler(); 
			handler.postDelayed(new Runnable() { 
				public void run() {
					dialog.show();
				} 
			}, (int)(DELAY_BEFORE_SHOW_DIALOG * 1000)); 
		}
	}

//	private int getImageSize(int deviceHeight, int adHeight, int minMaxInPixels, int maxMaxInPixels) {
//		if (deviceHeight - adHeight - (minMaxInPixels * 5 / 4) < 0) {
//			return deviceHeight * 4 / 5;
//		} else {
//			return getImageSize2(deviceHeight, adHeight, maxMaxInPixels);
//		}
//	}
//	
//	private int getImageSize2(int deviceHeight, int adHeight, int maxMaxInPixels) {
//		if (deviceHeight - adHeight - (maxMaxInPixels * 5 / 4) > 0) {
//			return maxMaxInPixels;
//		} 
//		else {
//			return this.getImageSize2(deviceHeight, adHeight, maxMaxInPixels - 10);
//		}
//	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (this.adViews != null) {
			for (AdView adView : this.adViews) {
				adView.resume();
			}
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
//		initUI();
	}
	
//	@Override
//	public void onStop() {
//		super.onStop();
//	}

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


	public class ImageButtonsLayout extends LinearLayout {

		public ImageButtonsLayout(Context context, int pictureIndex, int width, int status,
				SharedPreferences pictureSettings) {
			super(context);
			if (status == STATUS_UNCHANGED) {
				this.resetWithChangePictureText(context, pictureIndex, width);
			} else {
				this.installConfirmButtons(context, pictureIndex, width, pictureSettings);
			}
		}

		public void resetWithChangePictureText(Context context, int pictureIndex, int width) {
			this.removeAllViews();
			Button backButton = new Button(context);
			String backText = context.getString(R.string.back);
			backButton.setWidth((int)((double)width / 2.0));
			backButton.setTypeface(ImageUpdateActivity.this.font);
			backButton.setHeight(width / 4);
			backButton.setText(backText);
			backButton.getBackground().setColorFilter(BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
			backButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ImageUpdateActivity.this.onBackPressed();
				}
			});
			int textSize = (int)(width / 2 / backText.length());
			backButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

			this.addView(backButton);
			this.setGravity(Gravity.CENTER_HORIZONTAL);
//			TextView textView = new TextView(context);
//			String text = (pictureIndex + 1) + " - " + context.getString(R.string.click_to_change_picture);
//			textView.setText(text);
//			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(width / text.length()) + 1);
//			textView.setTypeface(ImageUpdateActivity.this.font);
//			textView.setWidth(width);
//			textView.setGravity(Gravity.CENTER);
//			this.addView(textView);
		}

		public void installConfirmButtons(final Context context, final int pictureIndex, final int width, 
				final SharedPreferences settings) {
			this.removeAllViews();
			Button cancelButton = new Button(context);
			String cancelText = context.getString(R.string.cancel);
			cancelButton.setWidth((int)((double)width / 2.0));
			cancelButton.setTypeface(ImageUpdateActivity.this.font);
			cancelButton.setHeight(width / 4);
			cancelButton.setText(cancelText);
			cancelButton.getBackground().setColorFilter(BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bitmap bitmap = ImageUpdateActivity.this.pictureHandler.getSavedBitmap(ImageUpdateActivity.this, 
							pictureIndex, width, settings);
					ImageUpdateActivity.this.imageView.setImageBitmap(bitmap);
//					ImageUpdateActivity.this.imageViews[pictureIndex].setImageBitmap(bitmap);
//					ImageUpdateActivity.this.bitmaps[pictureIndex] = bitmap;
					resetWithChangePictureText(context, pictureIndex, width);
					status = STATUS_UNCHANGED;
				}
			});

			Button okButton = new Button(context);
			String okText = context.getString(R.string.save);
			okButton.setWidth((int)((double)width / 2.0));
			okButton.setTypeface(ImageUpdateActivity.this.font);
			okButton.setHeight(width / 4);
			okButton.setText(okText);
			okButton.getBackground().setColorFilter(BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);	
			okButton.setOnClickListener(new OnClickListener() {
				@TargetApi(Build.VERSION_CODES.GINGERBREAD)
				@Override
				public void onClick(View v) {
//					boolean modified = (ImageListActivity.this.addedPictureFileNames[pictureIndex] != STATUS_UNCHANGED);
					Editor editor = settings.edit();
					switch (status) {
					// if current image is default picture
					case STATUS_BACK_TO_DEFAULT:
						editor.putInt("image" + ImageUpdateActivity.this.pictureIndex, ImageUpdateActivity.this.pictureHandler.getDefaultPictureId(ImageUpdateActivity.this.pictureIndex));
						break;
					case STATUS_NEW_PICTURE:
						try {
							savePicture(pictureIndex);
						} catch (IOException e) {
							Toast.makeText(ImageUpdateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						editor.putInt("image" + pictureIndex, -1);
						break;
					default:
//						Toast.makeText(ImageListActivity.this, "Impossible", Toast.LENGTH_SHORT).show();
						break;
					}
//					// if current image is default picture
//					if (!modified) {
//						editor.putInt("image" + clickedPicture, ImageListActivity.this.pictureHandler.getDefaultPictureId(clickedPicture));
//					}
//					// if custom picture
//					else {
////						savePicture(pictureIndex, (Bitmap)currentImage);
//						try {
//							savePicture(pictureIndex);
//						} catch (IOException e) {
//							Toast.makeText(ImageListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//						}
//						editor.putInt("image" + clickedPicture, -1);
//					}
					// apply is only available from API version 9
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
						editor.apply(); 
					} else {
						editor.commit();
					}
//					resetWithChangePictureText(context, pictureIndex, width);
					status = STATUS_UNCHANGED;
					ImageUpdateActivity.this.onBackPressed();
//					currentBitmaps.remove(pictureIndex);
				}
			});

			int textSize = (int)(((width / 2 / Math.max(cancelText.length(), okText.length()))));
			cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			okButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

			this.addView(cancelButton);
			this.addView(okButton);
		}
	}


	private void savePicture(int pictureIndex) throws IOException {
		if (this.pictureHandler == null) {
			this.pictureHandler = SavedPictureHandler.getInstance();
		}
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(this.pictureHandler.getCameraFile(this, pictureIndex)).getChannel();
			outputChannel = new FileOutputStream(this.pictureHandler.getCustomImageFile(this, pictureIndex)).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
//		Toast.makeText(ImageListActivity.this, "Picture " + pictureIndex + " saved to " + this.pictureHandler.getCustomImageFile(this, pictureIndex).getAbsolutePath(), Toast.LENGTH_SHORT).show();

//		File saveFile = this.pictureHandler.getCustomImageFile(this, pictureIndex);
//		try {
//			if(saveFile.exists())
//				saveFile.delete();
//			saveFile.createNewFile();
//			OutputStream fOut = new FileOutputStream(saveFile);
//			// 100 means no compression, the lower you go, the stronger the compression
//			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//			fOut.flush();
//			fOut.close();
//		}
//		catch (Exception e) {
//			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//			dialog.setTitle(e.getClass().getName());
//			dialog.setMessage(e.getMessage());
//			dialog.setNeutralButton("Cool", null);
//			dialog.create().show();
//		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		
		switch (requestCode) {
		case PICK_FROM_CAMERA:
			doCrop(CROP_FROM_CAMERA);
			break;

		case PICK_FROM_FILE:
			mImageCaptureUri = data.getData();
			doCrop(CROP_FROM_FILE);
			break;  

		case CROP_FROM_CAMERA:  
			if (this.mImageCaptureUri == null) {
				this.mImageCaptureUri = Uri.fromFile(ImageUpdateActivity.this.pictureHandler.getTempCameraFile(ImageUpdateActivity.this));
			}
			File f = new File(mImageCaptureUri.getPath());

			if (f.exists()) {
				saveNewImage(data, f);				
			} else {
				Toast.makeText(this, this.getString(R.string.crop_information_have_been_lost), Toast.LENGTH_SHORT).show();
			}
			break;
		case CROP_FROM_FILE:
			saveNewImage(data, null);
			break;
		}	
	}

	private void saveNewImage(Intent data, File f) {
		Bundle extras = data.getExtras();

		if (extras != null) {   
			Bitmap photo = extras.getParcelable("data");
			Bitmap resizedPhoto = Bitmap.createScaledBitmap(photo, this.imageSize, this.imageSize, true);
			this.imageView.setImageBitmap(resizedPhoto);
			// record this picture as modified
			this.status = STATUS_NEW_PICTURE;
//					this.currentBitmaps.put(clickedPicture, photo);
			this.imageButtonsLayout.installConfirmButtons(this, ImageUpdateActivity.this.pictureIndex, this.imageSize, this.pictureSettings);
			
			File saveFile = this.pictureHandler.getCameraFile(this, ImageUpdateActivity.this.pictureIndex);
			try {
				if (saveFile.exists()) {
					saveFile.delete();
				}
				saveFile.createNewFile();
				OutputStream fOut = new FileOutputStream(saveFile);
				// 100 means no compression, the lower you go, the stronger the compression
				resizedPhoto.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
				fOut.close();
//						if (this.imageViews == null) {
//							resizedPhoto.recycle();
//							resizedPhoto = null;
//						}
//						Toast.makeText(this, "File saved to " + saveFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			}
			catch (Exception e) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle(e.getClass().getName());
				dialog.setMessage(e.getMessage());
				dialog.setNeutralButton("Cool", null);
				dialog.create().show();
			}
			photo.recycle();
			photo = null;
		}

		if (f != null && f.exists()) f.delete();
	}
	
	private void doCrop(final int status) {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );

		int size = list.size();

		if (size == 0) {    
			Toast.makeText(this, this.getString(R.string.cannot_find_image_crop_app), Toast.LENGTH_SHORT).show();
			return;
		} else if (status == CROP_FROM_CAMERA && this.mImageCaptureUri == null) {
			this.mImageCaptureUri = Uri.fromFile(ImageUpdateActivity.this.pictureHandler.getTempCameraFile(ImageUpdateActivity.this));
			File f = new File(mImageCaptureUri.getPath());
			if (!f.exists()) {
				Toast.makeText(this, this.getString(R.string.crop_information_have_been_lost), Toast.LENGTH_SHORT).show();
				return;
			}
		}
		intent.setData(mImageCaptureUri);

		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);

		if (size == 1) {
			Intent i = new Intent(intent);
			ResolveInfo res = list.get(0);

			i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

			startActivityForResult(i, status);
		} else {
			for (ResolveInfo res : list) {
				final CropOption co = new CropOption();

				co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
				co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
				co.appIntent= new Intent(intent);

				co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				cropOptions.add(co);
			}

			CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.choose_crop_app);
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int item ) {
					startActivityForResult( cropOptions.get(item).appIntent, status);
				}
			});

			builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel( DialogInterface dialog ) {

					if (mImageCaptureUri != null ) {
						getContentResolver().delete(mImageCaptureUri, null, null);
						mImageCaptureUri = null;
					}
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	public class BackButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			ImageUpdateActivity.this.onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
		if (this.status != STATUS_UNCHANGED) {
			AlertDialog dialog = new AlertDialog.Builder(ImageUpdateActivity.this).create();
			dialog.setTitle(R.string.confirm);
			dialog.setMessage(ImageUpdateActivity.this.getString(R.string.alert_unsaved_changes));
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, ImageUpdateActivity.this.getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int buttonId) {
					ImageUpdateActivity.this.finish();      	        
				}
			});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, ImageUpdateActivity.this.getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int buttonId) {

				}
			});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.show();
		} else {
			ImageUpdateActivity.this.finish(); 
		}
	}

	public class ClickImageClickListener implements View.OnClickListener {

//		private int pictureIndex;
		private AlertDialog dialog;

		public ClickImageClickListener(AlertDialog dialog) {
//			this.pictureIndex = pictureIndex;
			this.dialog = dialog;
		}

		@Override
		public void onClick(View v) {
			this.dialog.show();
		}
	}


	public class ChangePictureClickAdapter implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int item) { 
			// pick from camera
			if (item == 0) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (ImageUpdateActivity.this.pictureHandler == null) {
					ImageUpdateActivity.this.pictureHandler = SavedPictureHandler.getInstance();
				}

				mImageCaptureUri = Uri.fromFile(ImageUpdateActivity.this.pictureHandler.getTempCameraFile(ImageUpdateActivity.this));

				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
				try {
					intent.putExtra("return-data", true);
					startActivityForResult(intent, PICK_FROM_CAMERA);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
			// pick from file
			else if (item == 1) { 
				Intent intent = new Intent();

				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);

				startActivityForResult(Intent.createChooser(intent, ImageUpdateActivity.this.getString(R.string.select_image_from_gallery_which_app)), 
						PICK_FROM_FILE);
			}
			// pick default
			else if (item == 2) {
				Bitmap bitmap = ImageUpdateActivity.this.pictureHandler.getDefaultBitmap(ImageUpdateActivity.this, ImageUpdateActivity.this.pictureIndex, ImageUpdateActivity.this.imageSize);
				ImageUpdateActivity.this.imageView.setImageBitmap(bitmap);
//				ImageUpdateActivity.this.imageViews[clickedPicture].setImageBitmap(bitmap);
//				ImageUpdateActivity.this.bitmaps[clickedPicture] = bitmap;
				// Picking default is a change only if the picture was NOT the default before...
				int imageId = ImageUpdateActivity.this.pictureSettings.getInt("image" + ImageUpdateActivity.this.pictureIndex, ImageUpdateActivity.this.pictureHandler.getDefaultPictureId(ImageUpdateActivity.this.pictureIndex));
				if (imageId == -1) {
					status = STATUS_BACK_TO_DEFAULT;
//					currentBitmaps.put(clickedPicture, Boolean.valueOf(true));
					ImageUpdateActivity.this.imageButtonsLayout.installConfirmButtons(ImageUpdateActivity.this, ImageUpdateActivity.this.pictureIndex, ImageUpdateActivity.this.imageSize, ImageUpdateActivity.this.pictureSettings);
				}
			}
		}
	}
}

