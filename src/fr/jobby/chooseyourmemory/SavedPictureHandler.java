package fr.jobby.chooseyourmemory;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SavedPictureHandler {
	private final static int[] DEFAULT_PICTURES = {R.drawable.default1, R.drawable.default2, R.drawable.default3, R.drawable.default4, R.drawable.default5,
		R.drawable.default6, R.drawable.default7, R.drawable.default8, R.drawable.default9, R.drawable.default10,
		R.drawable.default11, R.drawable.default12, R.drawable.default13, R.drawable.default14, R.drawable.default15,
		R.drawable.default16, R.drawable.default17, R.drawable.default18, R.drawable.default19, R.drawable.default20,
		R.drawable.default21, R.drawable.default22, R.drawable.default23, R.drawable.default24, R.drawable.default25,
		R.drawable.default26, R.drawable.default27, R.drawable.default28, R.drawable.default29, R.drawable.default30
	};
	private final static String PICTURE_PREFERENCE_FILE_NAME = "pictures";
	private final static String MISC_PREFERENCE_FILE_NAME = "misc";
	private final static String CUSTOM_IMAGE_DIRNAME = "customPictures";
	private final static String CUSTOM_PICTURE_PREFIX = "pict";

	private static SavedPictureHandler singleton = new SavedPictureHandler( );


	/* A private Constructor prevents any other 
	 * class from instantiating.
	 */
	private SavedPictureHandler(){ }

	/* Static 'instance' method */
	public static SavedPictureHandler getInstance( ) {
		return singleton;
	}

	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			
			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	private static Bitmap decodeSampledBitmapFromFilePath(String path, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public Bitmap getBackBitmap(Context context, int imageSize) {
		// Init images
//		return decodeSampledBitmapFromResource(context.getResources(), R.drawable.backimage, imageSize, imageSize);

//		Bitmap backBMP = BitmapFactory.decodeResource(context.getResources(), R.drawable.backimage);
		Bitmap backBMP = decodeSampledBitmapFromResource(context.getResources(), R.drawable.backimage, imageSize, imageSize);
		Bitmap backBMPRescaled = Bitmap.createScaledBitmap(backBMP, imageSize, imageSize, true);
//		backBMP.recycle();
//		backBMP = null;
		return backBMPRescaled;
	}

	public Bitmap getDefaultBitmap(Context context, int pictureIndex, int imageSize) {
//		return decodeSampledBitmapFromResource(context.getResources(), DEFAULT_PICTURES[pictureIndex], imageSize, imageSize);
//		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), DEFAULT_PICTURES[pictureIndex]);
		Bitmap bitmap = decodeSampledBitmapFromResource(context.getResources(), DEFAULT_PICTURES[pictureIndex], imageSize, imageSize);
		Bitmap bitmapRescaled = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);
//		bitmap.recycle();
//		bitmap = null;
		return bitmapRescaled;
	}

	public Bitmap getModifiedBitmap(Context context, int pictureIndex, int imageSize) {
		File modifiedPictureFile = this.getCameraFile(context, pictureIndex);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//		Bitmap bitmap = BitmapFactory.decodeFile(modifiedPictureFile.getAbsolutePath(), options);
		Bitmap bitmap = decodeSampledBitmapFromFilePath(modifiedPictureFile.getAbsolutePath(), imageSize, imageSize);
		Bitmap bitmapRescaled = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);
//		bitmap.recycle();
//		bitmap = null;
//		return decodeSampledBitmapFromFilePath(modifiedPictureFile.getAbsolutePath(), imageSize, imageSize);
		return bitmapRescaled;
	}

	public Bitmap getSavedBitmap(Context context, int pictureIndex, int imageSize, SharedPreferences settings) {
		int imageId = settings.getInt("image" + pictureIndex, DEFAULT_PICTURES[pictureIndex]);
		// user-defined picture
		if (imageId == -1) {
//			return decodeSampledBitmapFromFilePath(getCustomImageFile(context, pictureIndex).getAbsolutePath(), imageSize, imageSize);	
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//			Bitmap bitmap = BitmapFactory.decodeFile(getCustomImageFile(context, pictureIndex).getAbsolutePath(), options);
			Bitmap bitmap = decodeSampledBitmapFromFilePath(getCustomImageFile(context, pictureIndex).getAbsolutePath(), imageSize, imageSize);	
			Bitmap bitmapRescaled = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);
//			bitmap.recycle();
//			bitmap = null;
			return bitmapRescaled;
		}
		// default picture
		else {
			return getDefaultBitmap(context, pictureIndex, imageSize);
		}
	}

	public int getDefaultPictureId(int index) {
		return DEFAULT_PICTURES[index];
	}

	public int getDefaultPictureNumber() {
		return DEFAULT_PICTURES.length;
	}

	public String getPicturePreferenceFileName() {
		return PICTURE_PREFERENCE_FILE_NAME;
	}
	public String getMiscPreferenceFileName() {
		return MISC_PREFERENCE_FILE_NAME;
	}

	protected File getTempCameraFile(Context context) {
		return new File(context.getExternalCacheDir(), "tmp_picture_camera.png");
	}

	protected File getCameraFile(Context context, int clickedPosition) {
		return new File(context.getExternalCacheDir(), "tmp_picture_" + clickedPosition + ".png");
	}

	protected File getCustomImageFile(Context context, int pictureIndex) {
		File dir = context.getFilesDir();
		File imageDir = new File(dir, CUSTOM_IMAGE_DIRNAME);
		if (!imageDir.exists()) {
			imageDir.mkdirs();
		}
		return new File(imageDir, CUSTOM_PICTURE_PREFIX + pictureIndex + ".jpg");
	}
}
