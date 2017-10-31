package io.compactd.compactd.models;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vinz243 on 30/10/2017.
 */

public abstract class CompactdModel {
    protected final String mId;
    protected final Manager mManager;

    public CompactdModel(Manager manager, String id) {
        mManager = manager;
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public abstract void fromMap (Map<String, Object> map);
    public abstract void fetch () throws CouchbaseLiteException;

    public abstract Map<String, String> getURIProps();
}
