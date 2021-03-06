package io.compactd.compactd.models;

import android.util.Log;
import android.util.SparseArray;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdAlbum extends CompactdModel {
    public static final String DATABASE_NAME = "albums";
    private static final String TAG = "CompactdAlbum";
    private String mName;
    private CompactdArtist mArtist;

    private static SparseArray<CompactdAlbum> cache = new SparseArray<>();

    public CompactdAlbum(Manager manager, String id) {
        super(manager, id);
    }

    public CompactdAlbum(CompactdAlbum other) {
        super(other);
        mArtist = new CompactdArtist(other.getArtist());
        mName   = other.getName();
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        mName   = (String) map.get("name");

        Object artist = map.get("artist");

        if (artist instanceof CompactdArtist) {
            mArtist = (CompactdArtist) artist;
        } else {
            mArtist = new CompactdArtist(mManager, (String) artist);
        }

        mState  = ModelState.Prefetched;
    }

    @Override
    public void fetch() throws CouchbaseLiteException {
        if (getState() == ModelState.Fetched) return;

        Database db = this.mManager.getDatabase(DATABASE_NAME);
        Document doc = db.getDocument(mId);

        Map<String, Object> props = new HashMap<>();
        props.putAll(doc.getProperties());

        CompactdArtist artist = new CompactdArtist(mManager, (String) props.get("artist"));
        artist.fetch();

        props.put("artist", artist);

        fromMap(props);

        mState = ModelState.Fetched;
    }

    @Override
    public Map<String, String> getURIProps() {
        Map<String, String> props = new HashMap<>();
        String[] splat = getId().split("/");
        props.put("name", splat[2]);
        props.put("artist", splat[1]);
        return props;
    }

    public String getName() {
        return mName;
    }

    public CompactdArtist getArtist() {
        return mArtist;
    }

    public List<CompactdTrack> getTracks (FindMode mode) throws CouchbaseLiteException {
        return CompactdTrack.findAll(mManager, getId(), mode);
    }

    public int getTrackCount () {
        try {
            return getTracks(FindMode.OnlyIds).size();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<CompactdAlbum> findAll (Manager manager, FindMode mode) throws CouchbaseLiteException {
        return findAll(manager, START_KEY, mode);
    }

    @Override
    public String toString() {
        return "CompactdAlbum{" +
                "mId='" + mId + '\'' +
                ", mManager=" + mManager +
                ", mState=" + mState +
                ", mName='" + mName + '\'' +
                ", mArtist=" + mArtist +
                '}';
    }

    public static List<CompactdAlbum> findAll (Manager manager, String key, FindMode mode) throws CouchbaseLiteException {
        Log.d(TAG, "findAll: key=" + key + ", mode="+ mode.name());
        Database db = manager.getDatabase(DATABASE_NAME);
        Query query = db.createAllDocumentsQuery();
        query.setStartKey(key);
        query.setEndKey(key + CompactdModel.LAST_CHARACTER);
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        query.setPrefetch(mode == FindMode.Prefetch);

        List<CompactdAlbum> albums = new ArrayList<>();
        QueryEnumerator result = query.run();

        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdAlbum album = new CompactdAlbum(manager, row.getDocumentId());
            Log.d(TAG, "findAll: " + row);

            if (mode == FindMode.Fetch) {
                album.fetch();
            } else if (mode == FindMode.Prefetch) {
                album.fromMap(row.getDocumentProperties());
            }

            albums.add(album);
        }
        return albums;
    }

    public Album toAlbum() {
        return new Album(this);
    }

    @Nullable
    public static CompactdAlbum findById (Manager manager, int id, boolean fetch)  {
        CompactdAlbum cached = cache.get(id);

        if (cached != null) {
            if (fetch && cached.getState() != ModelState.Fetched) {
                try {
                    cached.fetch();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
            return new CompactdAlbum(cached);
        }

        List<CompactdAlbum> albums = null;

        try {
            albums = findAll(manager, FindMode.OnlyIds);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
        for (CompactdAlbum album : albums) {
            if (album.getId().hashCode() == id) {
                if (fetch) {
                    try {
                        album.fetch();
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                cache.put(id, new CompactdAlbum(album));

                return album;
            }
        }
        return null;
    }

    @Nullable
    public static CompactdAlbum findById (Manager manager, String id, boolean fetch) {
        if (fetch) {
            CompactdAlbum album = new CompactdAlbum(manager, id);
            try {
                album.fetch();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return null;
            }
            return album;
        } else {
            return new CompactdAlbum(manager, id);
        }
    }

    public String getImagePath(int size) {
        if (size <= 0) {
            size = 300;
        }
        String uriName = getURIProps().get("name");
        String uriArtist = getURIProps().get("artist");
        return "/api/aquarelle/" + uriArtist + "/" + uriName + "?s=" + size;
    }
}
