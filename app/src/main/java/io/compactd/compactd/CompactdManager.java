package io.compactd.compactd;

import android.content.Context;

import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;

/**
 * Created by Vincent on 01/11/2017.
 */

public class CompactdManager {
    private static Manager sInstance;

    public static Manager getInstance(Context context) {
        if (sInstance == null) {
            try {
                sInstance = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private CompactdManager() {
    }
}
