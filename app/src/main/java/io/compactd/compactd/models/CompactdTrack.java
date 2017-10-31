package io.compactd.compactd.models;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdTrack extends CompactdModel {
    public static final String DATABASE_NAME = "tracks";
    private String mName;
    private boolean mHidden;
    private CompactdArtist mArtist;
    private CompactdAlbum mAlbum;
    private double mDuration;
    private int mNumber;

    public CompactdTrack(Manager manager, String id) {
        super(manager, id);
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        mName = (String) map.get("name");
        mHidden = (boolean) map.get("hidden");
        mArtist = (CompactdArtist) map.get("artist");
        mAlbum = (CompactdAlbum) map.get("album");
        mDuration = (double) map.get("duration");
        mNumber = (int) map.get("number");
    }

    @Override
    public void fetch() throws CouchbaseLiteException {
        Database db = this.mManager.getDatabase(DATABASE_NAME);
        Document doc = db.getDocument(mId);

        Map<String, Object> props = new HashMap<>();
        props.putAll(doc.getProperties());

        CompactdArtist artist = new CompactdArtist(mManager, (String) props.get("artist"));
        artist.fetch();

        props.put("artist", artist);

        CompactdAlbum album = new CompactdAlbum(mManager, (String) props.get("album"));
        album.fetch();

        props.put("album", album);

        fromMap(props);
    }

    @Override
    public Map<String, String> getURIProps() {

        Map<String, String> props = new HashMap<>();
        String[] splat = getId().split("/");
        props.put("artist", splat[1]);
        props.put("album", splat[2]);
        props.put("number", splat[3]);
        props.put("name", splat[4]);
        return props;
    }

    public String getName() {
        return mName;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public void setHidden(boolean mHidden) {
        this.mHidden = mHidden;
    }

    public CompactdArtist getArtist() {
        return mArtist;
    }

    public CompactdAlbum getAlbum() {
        return mAlbum;
    }

    public double getDuration() {
        return mDuration;
    }

    public int getNumber() {
        return mNumber;
    }

    public static List<CompactdTrack> findAll (Manager manager, boolean fetch) throws CouchbaseLiteException {
        return findAll(manager, "library/", fetch);
    }
    public static List<CompactdTrack> findAll (Manager manager) throws CouchbaseLiteException {
        return findAll(manager, true);
    }
    public static List<CompactdTrack> findAll (Manager manager, String key, boolean fetch) throws CouchbaseLiteException {
        Database db = manager.getDatabase(DATABASE_NAME);
        Query query = db.createAllDocumentsQuery();
        query.setStartKeyDocId(key);
        query.setEndKeyDocId(key + "\u0000");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);

        List<CompactdTrack> tracks = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdTrack album = new CompactdTrack(manager, row.getDocumentId());
            if (fetch) {
                album.fetch();
            }
            tracks.add(album);
        }
        return tracks;
    }
}
