package io.compactd.compactd.models;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.opengl.Matrix;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

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
        mName     = (String) map.get("name");
        mHidden   = map.containsKey("hidden") && (boolean) map.get("hidden");
        mArtist   = (CompactdArtist) map.get("artist");
        mAlbum    = (CompactdAlbum) map.get("album");
        mDuration = getMillisDurationFromSeconds(map.get("duration"));
        mNumber   = (Integer) map.get("number");
    }

    private int getMillisDurationFromSeconds (Object ms) {
        if (ms instanceof  Double) {
            return (int) Math.floor((Double) ms * 1000);
        }
        if (ms instanceof  Integer) {
            return (int) Math.floor((Integer) ms * 1000);
        }
        return 0;
    }

    public Song toSong () {
        return new Song(
                    getId().hashCode(),
                    getName(), getNumber(),
                    0,
                    (long) Math.floor(getDuration() * 1000),
                    "",
                    0,
                    getAlbum().getId().hashCode(),
                    getAlbum().getName(),
                    getArtist().getId().hashCode(),
                    getArtist().getName()
                );
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
        query.setStartKey(key);
        query.setEndKey(key + "\uffff");
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);

        List<CompactdTrack> tracks = new ArrayList<>();
        QueryEnumerator result = query.run();
        while (result.hasNext()) {
            QueryRow row = result.next();
            CompactdTrack album = new CompactdTrack(manager, row.getDocumentId());
            if (fetch) {
                album.fetch();
            } else {
                album.fromMap(row.getDocumentProperties());
            }
            tracks.add(album);
        }
        return tracks;
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, int id, boolean fetch) {
        List<CompactdTrack> tracks = null;
        try {
            tracks = findAll(manager, fetch);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
        for (CompactdTrack track : tracks) {
            if (track.getId().hashCode() == id) {
                if (fetch) {
                    try {
                        track.fetch();
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return track;
            }
        }
        return null;
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, int id) {
        return findById(manager, id, true);
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, String id, boolean fetch) {
        if (fetch) {
            CompactdTrack track = new CompactdTrack(manager, id);
            try {
                track.fetch();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return null;
            }
            return track;
        } else {
            return findById(manager, id.hashCode(), false);
        }
    }

    @Nullable
    public static CompactdTrack findById (Manager manager, String id) {
        return findById(manager, id, true);
    }

    public static Cursor makeCursor (Manager manager) {
        String[] columns = new String[]{
                BaseColumns._ID,// 0
                MediaStore.Audio.AudioColumns.TITLE,// 1
                MediaStore.Audio.AudioColumns.TRACK,// 2
                MediaStore.Audio.AudioColumns.YEAR,// 3
                MediaStore.Audio.AudioColumns.DURATION,// 4
                MediaStore.Audio.AudioColumns.DATA,// 5
                MediaStore.Audio.AudioColumns.DATE_MODIFIED,// 6
                MediaStore.Audio.AudioColumns.ALBUM_ID,// 7
                MediaStore.Audio.AudioColumns.ALBUM,// 8
                MediaStore.Audio.AudioColumns.ARTIST_ID,// 9
                MediaStore.Audio.AudioColumns.ARTIST,// 10
        };
        MatrixCursor cursor = new MatrixCursor(columns);

        try {
            for (CompactdTrack track : findAll(manager, true)) {
                cursor.addRow(new Object[] {
                    track.getId().hashCode(),
                    track.getName(),
                    track.getNumber(),
                    0, (long) track.getDuration(),
                    "", 0,
                    track.getAlbum().getId().hashCode(),
                    track.getAlbum().getName(),
                    track.getArtist().getId().hashCode(),
                    track.getArtist().getName()
                });
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return cursor;
    }

}
