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
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by vinz243 on 30/10/2017.
 */

public class CompactdArtist extends CompactdModel {
    public static final String DATABASE_NAME = "artists";
    private static final String TAG = "CompactdArtist";
    private String name;

    public CompactdArtist(Manager manager, String id) {
        super(manager, id);
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        this.setName((String) map.get("name"));
    }

    @Override
    public void fetch() throws CouchbaseLiteException {
        Database db = this.mManager.getDatabase(DATABASE_NAME);
        Document doc = db.getDocument(mId);
        fromMap(doc.getProperties());
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public List<CompactdAlbum> getAlbums () throws CouchbaseLiteException {
        return getAlbums(true);
    }
    public List<CompactdAlbum> getAlbums (boolean fetch) throws CouchbaseLiteException {
        return CompactdAlbum.findAll(mManager, getId(), fetch);
    }
    public int getAlbumCount () {
        try {
            return getAlbums(false).size();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int getTrackCount () {
        try {
            return CompactdAlbum.findAll(mManager, getId(), false).size();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public List<CompactdAlbum> getTracks () throws CouchbaseLiteException {
        return CompactdAlbum.findAll(mManager, getId(), true);
    }
    @Override
    public Map<String, String> getURIProps () {
        Map<String, String> props = new HashMap<>();
        String[] splat = getId().split("/");
        props.put("name", splat[1]);
        return props;
    }

    public Artist toArtist () {
        ArrayList<Album> albums = new ArrayList<>();
        try {
            for (CompactdAlbum album : getAlbums()) {
                albums.add(album.toAlbum());
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return new Artist(albums);
    }

    public String getArtworkURI (URL base, int size) {
        String url = base.toString();

        url = url + "/api/aquarelle/" + getURIProps().get("name") + "?s=" + size;

        return url;
    }

    public static List<CompactdArtist> findAll (Manager manager) throws CouchbaseLiteException {
        return findAll(manager, "library/", true);
    }
    public static List<CompactdArtist> findAll (Manager manager, boolean fetch) throws CouchbaseLiteException {
        return findAll(manager, "library/", fetch);
    }

    public static List<CompactdArtist> findAll (Manager manager, String key, boolean fetch) throws CouchbaseLiteException {
        Database db = manager.getDatabase(DATABASE_NAME);
        Query query = db.createAllDocumentsQuery();
        query.setStartKey(key);
        query.setEndKey(key + "\uffff");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        List<CompactdArtist> artists = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdArtist artist = new CompactdArtist(manager, row.getDocumentId());

            if (fetch) {
                artist.fetch();
            } else {
                artist.fromMap(row.getDocumentProperties());
            }
            artists.add(artist);
        }
        return artists;
    }

    @Nullable
    public static CompactdArtist findById (Manager manager, int id, boolean fetch)  {
        List<CompactdArtist> artists = null;
        try {
            artists = findAll(manager, fetch);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
        for (CompactdArtist artist : artists) {
            if (artist.getId().hashCode() == id) {
                if (fetch) {
                    try {
                        artist.fetch();
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return artist;
            }
        }
        return null;
    }

    @Nullable
    public static CompactdArtist findById (Manager manager, int id) {
        return findById(manager, id, true);
    }

    @Nullable
    public static CompactdArtist findById (Manager manager, String id, boolean fetch) {
        if (fetch) {
            CompactdArtist artist = new CompactdArtist(manager, id);
            try {
                artist.fetch();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return null;
            }
            return artist;
        } else {
            return findById(manager, id.hashCode(), false);
        }
    }

    @Nullable
    public static CompactdArtist findById (Manager manager, String id)  {
        return findById(manager, id, true);
    }
}
