package io.compactd.compactd.models;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vinz243 on 30/10/2017.
 */

public class CompactdArtist extends CompactdModel {
    public static final String DATABASE_NAME = "artists";
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
    public String getArtworkURI (URL base, int size) {
        String url = base.toString();

        url = url + "/api/aquarelle/" + getURIProps().get("name") + "?s=" + size;

        return url;
    }
    public static List<CompactdArtist> findAll (Manager manager) throws CouchbaseLiteException {
        return findAll(manager, "library/");
    }
    public static List<CompactdArtist> findAll (Manager manager, String key) throws CouchbaseLiteException {
        Database db = manager.getDatabase(DATABASE_NAME);
        Query query = db.createAllDocumentsQuery();
        query.setStartKeyDocId(key);
        query.setEndKeyDocId(key + "\u0000");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);

        List<CompactdArtist> artists = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdArtist artist = new CompactdArtist(manager, row.getDocumentId());
            artist.fetch();
            artists.add(artist);
        }
        return artists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompactdArtist that = (CompactdArtist) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
