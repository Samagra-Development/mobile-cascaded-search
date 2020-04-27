package com.samagra.odktest;

import android.os.Environment;

import java.io.File;
import java.util.HashMap;

/**
 * These are constants required by the app module. All values declared should be public static and the constants should
 * preferably be final constants.
 *
 * @author Pranav Sharma
 */
public class AppConstants {
    public static final String PREF_FILE_NAME = "SAMAGRA_PREFS";
    public static final String FILE_PATH =  Environment.getExternalStorageDirectory()
            + File.separator + "app" + "/data2.json";

    public static final HashMap<String, String> FORM_LIST = new HashMap<String, String>() {{
        put("CHT_V1", "Class 1st to 5th CHT School Visit Form");
        put("BRCC_V1", "Class 1st to 8th School Visit Form");
        put("BPO_V1", "Class 9th to 12th School Visit Form");
    }};
}
