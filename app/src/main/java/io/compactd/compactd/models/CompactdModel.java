package io.compactd.compactd.models;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vinz243 on 30/10/2017.
 */

public abstract class CompactdModel implements Cloneable{

    public enum ModelState {
        Barebone, Prefetched, Fetched, Deleted
    }

    public enum FindMode {
        OnlyIds, Prefetch, Fetch
    }

    static final char LAST_CHARACTER = '\uffff';
    static final String START_KEY = "library/";

    final String mId;
    final Manager mManager;
    ModelState mState;

    CompactdModel(Manager manager, String id) {
        mManager = manager;
        mId = id;
        mState = ModelState.Barebone;
    }

    /**
     * Copy constructor
     * @param model
     */
    CompactdModel(CompactdModel model) {
        mId = model.mId;
        mManager = model.mManager;
    }

    public String getId() {
        return mId;
    }

    public void fromMap (Map<String, Object> map) {
        mState = ModelState.Prefetched;
    }
    public abstract void fetch () throws CouchbaseLiteException;

    public abstract Map<String, String> getURIProps();

    public ModelState getState() {
        return mState;
    }
}
