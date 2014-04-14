package fr.jobby.chooseyourmemory;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class CardLayout extends FrameLayout {

    private static final int BORDER_SIZE = 3;
    private static final int NORMAL_BORDER_COLOR = Color.rgb(100, 100, 100);
    private static final int DONE_BORDER_COLOR = Color.rgb(255, 0, 0);
    private static final int WIN_BACKGROUND_COLOR = Color.rgb(187, 34, 34);
    
    private ImageView imageView;
    private Bitmap currentBitmap;
   
    private int cardFrontImageId;
    private int imageSize;
    private FrameLayout imageLayout;

    private boolean hidden;
    private BoardActivity main;
    private LinearLayout winLayout;
    private Typeface font;
    
    public CardLayout(BoardActivity context, int imageSize, int horizPaddingSize, int verticalPaddingSize) {
        super(context);
        this.main = context;
        this.imageSize = imageSize;
        
        this.imageLayout = new FrameLayout(context);
        this.imageLayout.setPadding(horizPaddingSize - BORDER_SIZE * 2, verticalPaddingSize - BORDER_SIZE * 2, 0, 0);

        this.imageView = new ImageView(context);                
        this.imageView.setPadding(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
        this.imageView.setBackgroundColor(NORMAL_BORDER_COLOR);

        this.currentBitmap = context.getBackBitmap();
        this.imageView.setImageBitmap(this.currentBitmap);

        this.imageLayout.addView(this.imageView);
        
        this.addView(this.imageLayout);
        this.hidden = true;
//        this.won = false;
        this.winLayout = null;
//        this.frontImageLayout = null;
        this.imageView.setOnClickListener(new CardClickListener(this));
        
        SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
        SharedPreferences settings = main.getSharedPreferences(optionSingleton.getMiscPreferenceFileName(), Context.MODE_PRIVATE);
        this.font = Typeface.createFromAsset(this.main.getAssets(), settings.getString("default-font", "sans-serif"));

    }
    
    /**
     * @return the cardId
     */
    public int getCardId() {
        return this.cardFrontImageId;
    }

    public void setCardId(int cardId) {
        this.cardFrontImageId = cardId;
    }
    
    
    public class CardClickListener implements View.OnClickListener {

        private CardLayout card;
//        private boolean won;
        
        public CardClickListener(CardLayout card) {
            this.card = card;
//            this.won = won;
        }
        
        @Override
        public void onClick(View v) {
            main.cardClicked(this.card);
        }
    }
        
    protected boolean show() {
        this.showNoAnimation();
        if (this.hidden) {
            this.hidden = false;
            return true;
        } else {
            return false;
        }
    }
    
    private void showNoAnimation() {
    	this.currentBitmap = null;
    	this.currentBitmap = main.getFrontBitmap(this.cardFrontImageId);
        this.imageView.setImageBitmap(this.currentBitmap);
    }
    
    public void hide() {
    	this.currentBitmap = null;
    	this.currentBitmap = this.main.getBackBitmap();
        this.imageView.setImageBitmap(this.currentBitmap);
        this.hidden = true;
    }

    public void done() {
        this.imageView.setBackgroundColor(DONE_BORDER_COLOR);
    }
    
    public void win() {
//        if (!won) {
            if (this.winLayout != null) {
                this.imageLayout.removeView(this.winLayout);
            }
            Random random = new Random();
            this.winLayout = new LinearLayout(this.main);

            int textHeight = this.imageSize / 4;
            int textWidth = this.imageSize / 2;

            int top = BORDER_SIZE + random.nextInt(this.imageSize - textHeight - BORDER_SIZE * 2);
            int left = BORDER_SIZE + random.nextInt(this.imageSize - textWidth - BORDER_SIZE * 2);

            winLayout.setPadding(left, top, 0, 0);
            TextView winTextView = new TextView(this.main);
            String winText = this.main.getString(R.string.win);

            int textSize = (textWidth) / winText.length();

            winTextView.setText(winText);
            winTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            winTextView.setGravity(Gravity.CENTER);
            winTextView.setWidth(textWidth);
            winTextView.setHeight(textHeight);
            winTextView.setBackgroundColor(WIN_BACKGROUND_COLOR);
            
            winTextView.setTypeface(this.font, Typeface.ITALIC);

            winTextView.setOnClickListener(new View.OnClickListener() {  
                @Override
                public void onClick(View v) {
                    main.done();
                }
            });
            winLayout.addView(winTextView);
            this.imageLayout.addView(winLayout);

//            this.won = true;
//        }
    }
    

}
