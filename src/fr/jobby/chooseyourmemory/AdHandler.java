package fr.jobby.chooseyourmemory;

public class AdHandler {
	private static final String INTER_AD_ID = "ca-app-pub-7668138040965434/7993815304";
	private static final String PICTURE_OPTIONS_BANNER_AD_ID = "ca-app-pub-7668138040965434/3581805300";
	private static final int AD_FREQUENCY = 4;
	private static final boolean AD_ENABLED = true;
	
	private static final int STANDARD_BANNER_WIDTH = 320;
	private static final int STANDARD_BANNER_HEIGHT = 50;

	private static AdHandler singleton = new AdHandler( );


	/* A private Constructor prevents any other 
	 * class from instantiating.
	 */
	private AdHandler(){ 
	}
	
	/* Static 'instance' method */
	public static AdHandler getInstance( ) {
		return singleton;
	}

	public String getInterAdId() {
		return INTER_AD_ID;
	}
	
	public String getPictureOptionsBannerAdId() {
		return PICTURE_OPTIONS_BANNER_AD_ID;
	}
	
	public boolean isAdEnabled() {
		return AD_ENABLED;
	}

	public int getAdFrequency() {
		return AD_FREQUENCY;
	}

	public int getStandardBannerWidth() {
		return STANDARD_BANNER_WIDTH;
	}
	
	public int getStandardBannerHeight() {
		return STANDARD_BANNER_HEIGHT;
	}
}
