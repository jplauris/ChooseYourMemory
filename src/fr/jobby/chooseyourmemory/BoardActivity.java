package fr.jobby.chooseyourmemory;

import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;

public class BoardActivity extends Activity {

	private static final int DELAY_BETWEEN_SHUFFLE = 1;

	private static final int STATUS_ALL_HIDDEN = 0;
	private static final int STATUS_ONE_SHOWN = 1;
	private static final int STATUS_TWO_SHOWN = 2;
	private static final int STATUS_WON = 3;

	private static final int TABLE_LAYOUT_ID = Integer.MAX_VALUE;

	private static final String KEY_IMAGE_X_NUMBER = "image_x_number";
	private static final String KEY_IMAGE_Y_NUMBER = "image_y_number";
	private static final String KEY_IMAGE_SIZE = "image_size";
	private static final String KEY_STATUS = "status";
	private static final String KEY_REMAINING = "remaining";
	private static final String KEY_STROKE_NUMBER = "stroke_number";
	private static final String KEY_CARD1_ID = "card1Id";
	private static final String KEY_CARD1_INDEX = "card1Index";
	private static final String KEY_PICTURE_LIST = "pictureList";
	private static final String KEY_STATUS_LIST = "statusList";
	//    private static final String KEY_CARD_INDEXES = "cardIndexes";

	private static Random random = new Random();

	private Bitmap backBitmap;
	private Bitmap[] frontBitmaps;

	//    private ArrayList<CardLayout> cards;
	//    private ArrayList<Integer> cardIds;
	//    private int[] cardIds;

	private int remaining;
	private boolean locked;

	//    private TableLayout tableLayout;
	//    private CardLayout card1;
	private int card1Id;
	private int card1Index;
	private CardLayout card2;
	private int[] pictureList;
	private int[] statusList;

	private int status;
	private int strokeNumber;

	private int imageXNumber;
	private int imageYNumber;
	private int waitBeforeHideCards;
	private boolean resetAndPlayAgainWhenClicked;
	private int imageSize;
	private int imageNumber;
	private boolean soundEnabled;

	private MediaPlayer pairFoundSound;
	private MediaPlayer winSound;
	private MediaPlayer waitSound;

	private Handler callBackHandler;
	private Runnable wait;
	private SharedPreferences settings;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		savedInstanceState.putInt(KEY_IMAGE_X_NUMBER, this.imageXNumber);
		savedInstanceState.putInt(KEY_IMAGE_Y_NUMBER, this.imageYNumber);
		savedInstanceState.putInt(KEY_IMAGE_SIZE, this.imageSize);
		savedInstanceState.putInt(KEY_STATUS, this.status);
		savedInstanceState.putInt(KEY_REMAINING, this.remaining);
		savedInstanceState.putInt(KEY_STROKE_NUMBER, this.strokeNumber);
		savedInstanceState.putInt(KEY_CARD1_ID, this.card1Id);
		savedInstanceState.putInt(KEY_CARD1_INDEX, this.card1Index);
		//    	savedInstanceState.putIntArray(KEY_CARD_INDEXES, this.cardIds);
		savedInstanceState.putIntArray(KEY_PICTURE_LIST, this.pictureList);
		savedInstanceState.putIntArray(KEY_STATUS_LIST, this.statusList);
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();  // Always call the superclass method first
		if (this.status == STATUS_TWO_SHOWN) {
			this.reset();
		}
		if (this.soundEnabled) {
			if (this.waitSound.isPlaying()) {
				this.waitSound.stop();
				this.waitSound.release();
			}
			//		if (this.winSound.isPlaying()) {
			//			this.winSound.stop();
			//			this.winSound.release();
			//		}
			if (this.pairFoundSound.isPlaying()) {
				this.pairFoundSound.stop();
				this.pairFoundSound.release();
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (this.status == STATUS_WON) {
			this.finish();
		}    	
	}  

	@Override 
	public void onResume() {
		super.onResume();
		
		SavedPictureHandler pictureHandler = SavedPictureHandler.getInstance();
		SharedPreferences prefs = this.getSharedPreferences(pictureHandler.getMiscPreferenceFileName(), Context.MODE_PRIVATE);

		// Init sound players
		this.soundEnabled = prefs.getBoolean("soundEnabled", true);
		if (this.soundEnabled) {
			this.pairFoundSound = MediaPlayer.create(this, R.raw.found_pair);
			this.pairFoundSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.pairFoundSound.setOnErrorListener(new SoundErrorListener());
			this.winSound = MediaPlayer.create(this, R.raw.victory);
			this.winSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.winSound.setOnErrorListener(new SoundErrorListener());
			this.waitSound = MediaPlayer.create(this, R.raw.ding);
			this.waitSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.waitSound.setLooping(true);
			this.waitSound.setOnErrorListener(new SoundErrorListener());
		}
		
		this.resetAndPlayAgainWhenClicked = prefs.getBoolean("resetAndPlayAgainWhenClicked", true);
	}
	
	private static class SoundErrorListener implements OnErrorListener {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			mp.reset();
			try {
				mp.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
		SharedPreferences settings = getSharedPreferences(optionSingleton.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
		int deviceHeight = settings.getInt("device-height", -1);
		int deviceWidth = settings.getInt("device-width", -1);

		if (savedInstanceState == null) {
			Bundle b = getIntent().getExtras();
			this.imageXNumber = b.getInt("x");
			this.imageYNumber = b.getInt("y");
			this.imageNumber = imageXNumber * imageYNumber;
			int cellSize = Math.min(deviceWidth / imageXNumber, deviceHeight / imageYNumber);
			int paddingSize = (int)(cellSize * 0.10);
			this.imageSize = (cellSize - paddingSize);
			this.pictureList = null;
			this.statusList = new int[this.imageNumber];
			for (int i = 0 ; i < this.statusList.length ; i++) {
				this.statusList[i] = STATUS_ALL_HIDDEN;
			}
			this.status = STATUS_ALL_HIDDEN;
			this.remaining = this.imageNumber;
			this.card1Index = -1;    
			this.card1Id = -1;
		} else {
			this.imageXNumber = savedInstanceState.getInt(KEY_IMAGE_X_NUMBER);
			this.imageYNumber = savedInstanceState.getInt(KEY_IMAGE_Y_NUMBER);
			this.imageNumber = imageXNumber * imageYNumber;
			this.imageSize = savedInstanceState.getInt(KEY_IMAGE_SIZE);
			this.status = savedInstanceState.getInt(KEY_STATUS);
			this.remaining = savedInstanceState.getInt(KEY_REMAINING);
			this.strokeNumber = savedInstanceState.getInt(KEY_STROKE_NUMBER);
			this.card1Id = savedInstanceState.getInt(KEY_CARD1_ID);
			this.card1Index = savedInstanceState.getInt(KEY_CARD1_INDEX);
			this.pictureList = savedInstanceState.getIntArray(KEY_PICTURE_LIST);
			this.statusList = savedInstanceState.getIntArray(KEY_STATUS_LIST);
		}
		this.backBitmap = optionSingleton.getBackBitmap(this, this.imageSize);

		int totalHorizPadding = deviceWidth - (imageSize * imageXNumber);
		int totalVertPadding = deviceHeight - (imageSize * imageYNumber);
		int horizPaddingSize = totalHorizPadding / (imageXNumber + 1);
		int verticalPaddingSize = totalVertPadding / (imageYNumber + 1);

		//            this.cards = new ArrayList<CardLayout>();
		//            this.cardIds = new ArrayList<Integer>();
		//            this.cardIds = new int[this.imageNumber];
		// Init basic settings
		SavedPictureHandler pictureHandler = SavedPictureHandler.getInstance();
		this.settings = getSharedPreferences(pictureHandler.getPicturePreferenceFileName(), Context.MODE_PRIVATE);
		SharedPreferences prefs = this.getSharedPreferences(pictureHandler.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
		this.waitBeforeHideCards = Integer.parseInt(prefs.getString("waitToHideCardsPref", this.getString(R.string.waitToHideCardsPref_defaultValue)));

		this.frontBitmaps = new Bitmap[imageNumber / 2];
		
		int i = 0;
		TableLayout tableLayout = new TableLayout(this);
		tableLayout.setBackgroundResource(R.drawable.pictures_bg);        

		for (int y = 0 ; y < imageYNumber ; y++) {
			TableRow tableRow = new TableRow(this);
			for (int x = 0 ; x < imageXNumber ; x++) {
				CardLayout view = new CardLayout(this, imageSize, horizPaddingSize, verticalPaddingSize);
				// Ids must be positive
				view.setId(i + 1);
				tableRow.addView(view);
				i++;
			}
			tableLayout.addView(tableRow);
		}
		tableLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BoardActivity.this.finish();
			}
		});    
		this.locked = false;
		tableLayout.setId(TABLE_LAYOUT_ID);
		this.setContentView(tableLayout);
		
		if (this.status == STATUS_WON) {
			this.win(this.strokeNumber, true);
		} else {
			this.pictureList = this.installCards(this.pictureList, tableLayout, imageNumber, false);

			//        this.card1 = null;
			this.card2 = null;
			this.callBackHandler = new Handler();
		}		
		// When coming back from a detroyed activity
		// and the user won just before it was detroyed
	}

	private int[] installCards(int[] pictureList, final TableLayout tableLayout, final int imageNumber, boolean won) {
		int pictureIndex;

		CardLayout card;
		if (pictureList == null) {
			int[] selectedPictures = new int[imageNumber / 2];
			for (int i = 0 ; i < selectedPictures.length ; i++) {
				selectedPictures[i] = 0;
			}
			pictureList = new int[imageNumber];
			for (int i = 0 ; i < imageNumber ; i++) {
				do {
					pictureIndex = random.nextInt(selectedPictures.length);
				} while (selectedPictures[pictureIndex] == 2);
				selectedPictures[pictureIndex] = selectedPictures[pictureIndex] + 1;
				card = (CardLayout)this.findViewById(i + 1);            
				card.setCardId(pictureIndex); 
				pictureList[i] = pictureIndex;
				if (won) {
					card.show();
					if (imageNumber < 20) {
						card.win();
					}
				}
			}
		} else {
			for (int i = 0 ; i < imageNumber ; i++) {
				card = (CardLayout)this.findViewById(i + 1);            
				card.setCardId(pictureList[i]);
				if (this.statusList[i] == STATUS_ONE_SHOWN) {
					card.show();
				}
				else if (this.statusList[i] == STATUS_WON) {
					card.show();
					card.done();
				} else {
					card.hide();
				}
			}
		}

		if (won) {
			tableLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BoardActivity.this.finish();
				}
			});
			Handler handler = new Handler(); 
			handler.postDelayed(new Runnable() { 
				public void run() {
					// Recall random cards while the user
					// doesn't touch the screen
					installCards(null, tableLayout, imageNumber, true);
				} 
			}, DELAY_BETWEEN_SHUFFLE * 1000); 
		} else {
			this.strokeNumber = 0;
			tableLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (areTwoCardsShown()) {
						reset();
					} 
				}
			});
		}
		return pictureList;
	}

	protected void reset() {
		if (this.soundEnabled) {
			if (waitSound.isPlaying()) {
				waitSound.pause();
			}
		}
		//        if (card1 != null)
			//            card1.hide();
		if (card1Id != -1) {
			//            this.cards.get(card1Index).hide();
			//            CardLayout card = (CardLayout)this.findViewById(this.cardIds.get(card1Index));
			//            CardLayout card = (CardLayout)this.findViewById(this.cardIds[card1Index]);
			CardLayout card = (CardLayout)this.findViewById(card1Id);
			card.hide();
			this.statusList[card1Id - 1] = STATUS_ALL_HIDDEN;
		}
		if (card2 != null) {
			card2.hide();
			this.statusList[card2.getId() - 1] = STATUS_ALL_HIDDEN;
		}
		//        card1 = null;
		card1Index = -1;
		card2 = null;
		status = STATUS_ALL_HIDDEN;
	}

	public void cardClicked(CardLayout card) {
		if (this.locked) {
			return;
		}
		this.locked = true;
		switch (this.getStatus()) {
		case BoardActivity.STATUS_WON:            
			this.finish();
			break;
		case BoardActivity.STATUS_ALL_HIDDEN:
			if (card.show()) {
				//                this.card1 = card;
				this.card1Index = card.getCardId();
				this.card1Id = card.getId();
				this.statusList[this.card1Id - 1] = STATUS_ONE_SHOWN;
				this.status = STATUS_ONE_SHOWN;
			}
			break;
		case BoardActivity.STATUS_ONE_SHOWN:
			if (card.show()) {  
				this.strokeNumber++;
				this.card2 = card;
				this.statusList[card.getId() - 1] = STATUS_ONE_SHOWN;
				// Pair matches
				//                if (this.card1.getCardId() == this.card2.getCardId()) {
				if (this.card1Index == this.card2.getCardId()) {
					this.remaining -= 2;
					this.gotOne(this.remaining == 0, this.card1Id);
					//                    this.card1 = null;
					this.card1Index = -1;
					this.card1Id = -1;
					this.card2 = null;
					this.status = STATUS_ALL_HIDDEN;
					// If game is done
					if (this.remaining == 0) {
						Handler handler = new Handler(); 
						handler.postDelayed(new Runnable() { 
							public void run() {
								win(strokeNumber);
							} 
						}, 1000); 
					} 
				}
				// Pair doesn't match
				else {
					this.status = STATUS_TWO_SHOWN;
					if (this.waitBeforeHideCards > 0) {
						waitBeforeHide(this.waitBeforeHideCards); 
					} 
				}
			}
			break;
		case BoardActivity.STATUS_TWO_SHOWN:
			if (this.wait != null) {
				this.callBackHandler.removeCallbacks(this.wait);
			}
			this.reset();
			if (this.resetAndPlayAgainWhenClicked) {
				this.locked = false;
				this.cardClicked(card);
			}
			break;
		default:
			break;
		}
		this.locked = false;
	}

	private void gotOne(boolean lastOne, int viewId) {
		if (this.soundEnabled && !lastOne) {
			if (!this.pairFoundSound.isPlaying()) {
				this.pairFoundSound.start();
			}
		}
		//        this.card1.done();
		//        this.cards.get(this.card1Index).done();
		//        CardLayout card1 = (CardLayout)this.findViewById(this.cardIds.get(card1Index));
		CardLayout card1 = (CardLayout)this.findViewById(viewId);
		card1.done();
		this.card2.done();
		this.statusList[card1.getId() - 1] = STATUS_WON;
		this.statusList[card2.getId() - 1] = STATUS_WON;
	}
	
	private void win(int strokeNumber) {
		this.win(strokeNumber, false);
	}

	private void win(int strokeNumber, boolean fromRecreate) {
		if (this.soundEnabled && !fromRecreate) {
			this.winSound.start();
		}
		this.status = STATUS_WON;
		TableLayout tableLayout = (TableLayout)this.findViewById(TABLE_LAYOUT_ID);
		this.installCards(null, tableLayout, this.imageNumber, true);

		Intent intent = new Intent(this, WinActivity.class);
		Bundle b = new Bundle();
		b.putInt("strokeNumber", this.strokeNumber);
		intent.putExtras(b); 
		WinActivity.parentActivity = this; 
		startActivity(intent);
	}

	private void waitBeforeHide(final int seconds) {
		if (this.soundEnabled) {
			this.waitSound.start();
		}
		this.wait = new Runnable() { 
			public void run() {
				if (status == STATUS_TWO_SHOWN) {
					reset();
				}
			} 
		};
		this.callBackHandler.postDelayed(this.wait, seconds * 1000);             
	}





	protected void done() {
		this.finish();
	}

	public boolean areTwoCardsShown() {
		//        return (this.card1 != null && this.card2 != null);
		return (this.card1Index != -1 && this.card2 != null);
	}

	/**
	 * @return the locked
	 */
	public boolean isLocked() {
		return locked;
	}

	//    /**
	//     * @param locked the locked to set
	//     */
	//    public void setLocked(boolean locked) {
	//        this.locked = locked;
	//    }

	public int getStatus() {
		return this.status;
	}

	/**
	 * @return the backBitmap
	 */
	public Bitmap getBackBitmap() {
		return backBitmap;
	}

	public Bitmap getFrontBitmap(int pictureIndex) {
		if (this.frontBitmaps[pictureIndex] == null) {
			SavedPictureHandler pictureHandler = SavedPictureHandler.getInstance();        	
			Bitmap frontBMP = pictureHandler.getSavedBitmap(this, pictureIndex, imageSize, settings);
			Bitmap resizedBMP = Bitmap.createScaledBitmap(frontBMP, imageSize, imageSize, true);
			this.frontBitmaps[pictureIndex] = resizedBMP;
			return resizedBMP;
		} else {
			return this.frontBitmaps[pictureIndex];
		}
	}
}
