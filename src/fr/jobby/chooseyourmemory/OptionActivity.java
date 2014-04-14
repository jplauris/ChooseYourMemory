package fr.jobby.chooseyourmemory;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class OptionActivity extends PreferenceActivity {
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        // Set header resource MUST BE CALLED BEFORE super.onCreate
//        setHeaderRes(R.xml.preferences);
//        // Set desired preference file and mode (optional)
//        setSharedPreferencesName("unified_preference_demo");
//        setSharedPreferencesMode(Context.MODE_PRIVATE);
        
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        SavedPictureHandler optionSingleton = SavedPictureHandler.getInstance();
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(optionSingleton.getMiscPreferenceFileName());
        addPreferencesFromResource(R.xml.preferences);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
//
//    public static class GeneralPreferenceFragment extends UnifiedPreferenceFragment {}
//
//    public static class NotificationPreferenceFragment extends UnifiedPreferenceFragment {}
//
//    public static class DataSyncPreferenceFragment extends UnifiedPreferenceFragment {}
//

//    /**
//     * Populate the activity with the top-level headers.
//     */
//    @Override
//    public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.preferences, target);
//    }
//
//    /**
//     * This fragment shows the preferences for the first header.
//     */
//    public static class Prefs1Fragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Make sure default values are applied.  In a real app, you would
//            // want this in a shared function that is used to retrieve the
//            // SharedPreferences wherever they are needed.
//            PreferenceManager.setDefaultValues(getActivity(),
//                    R.xml.advanced_preferences, false);
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.fragmented_preferences);
//        }
//    }
//
//    /**
//     * This fragment contains a second-level set of preference that you
//     * can get to by tapping an item in the first preferences fragment.
//     */
//    public static class Prefs1FragmentInner extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Can retrieve arguments from preference XML.
//            Log.i("args", "Arguments: " + getArguments());
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.fragmented_preferences_inner);
//        }
//    }
//
//    /**
//     * This fragment shows the preferences for the second header.
//     */
//    public static class Prefs2Fragment extends PreferenceFragment {
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            // Can retrieve arguments from headers XML.
//            Log.i("args", "Arguments: " + getArguments());
//
//            // Load the preferences from an XML resource
//            addPreferencesFromResource(R.xml.preference_dependencies);
//        }
//    }
}