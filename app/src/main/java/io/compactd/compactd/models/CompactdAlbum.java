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

public class CompactdAlbum extends CompactdModel{
    public static final String DATABASE_NAME = "albums";
    private String mName;
    private CompactdArtist mArtist;

    public CompactdAlbum(Manager manager, String id) {
        super(manager, id);
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        mName   = (String) map.get("name");
        mArtist = (CompactdArtist) map.get("artist");
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

        fromMap(props);
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

    public List<CompactdTrack> getTracks (boolean fetch) throws CouchbaseLiteException {
        return CompactdTrack.findAll(mManager, getId(), fetch);
    }

    public List<CompactdTrack> getTracks () throws CouchbaseLiteException {
        return getTracks(true);
    }

    public int getTrackCount () {
        try {
            return getTracks(false).size();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<CompactdAlbum> findAll (Manager manager, boolean fetch) throws CouchbaseLiteException {
        return findAll(manager, "library/", fetch);
    }
    public static List<CompactdAlbum> findAll (Manager manager) throws CouchbaseLiteException {
        return findAll(manager, true);
    }
    public static List<CompactdAlbum> findAll (Manager manager, String key, boolean fetch) throws CouchbaseLiteException {
        Database db = manager.getDatabase(DATABASE_NAME);
        Query query = db.createAllDocumentsQuery();
        query.setStartKeyDocId(key);
        query.setEndKeyDocId(key + "\u0000");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);

        List<CompactdAlbum> albums = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdAlbum album = new CompactdAlbum(manager, row.getDocumentId());
            if (fetch) {
                album.fetch();
            }
            albums.add(album);
        }
        return albums;
    }
}
