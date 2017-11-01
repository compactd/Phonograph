package io.compactd.compactd.models;

import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Song;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdAlbum extends CompactdModel{
    public static final String DATABASE_NAME = "albums";
    private static final String TAG = "CompactdAlbum";
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
        query.setStartKey(key);
        query.setEndKey(key + "\uffff");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);

        List<CompactdAlbum> albums = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdAlbum album = new CompactdAlbum(manager, row.getDocumentId());
            if (fetch) {
                album.fetch();
            } else {
                album.fromMap(row.getDocumentProperties());
            }
            albums.add(album);
        }
        return albums;
    }

    public Album toAlbum() {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            for (CompactdTrack track : getTracks(true)) {
                songs.add(track.toSong());
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return new Album(songs);
    }

    @Nullable
    public static CompactdAlbum findById (Manager manager, int id, boolean fetch)  {
        List<CompactdAlbum> albums = null;
        try {
            albums = findAll(manager, fetch);
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
                return album;
            }
        }
        return null;
    }

    @Nullable
    public static CompactdAlbum findById (Manager manager, int id) {
        return findById(manager, id, true);
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
            return findById(manager, id.hashCode(), false);
        }
    }

    @Nullable
    public static CompactdAlbum findById (Manager manager, String id)  {
        return findById(manager, id, true);
    }
}
