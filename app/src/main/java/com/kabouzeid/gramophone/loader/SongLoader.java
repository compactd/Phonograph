package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.BlacklistStore;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import io.compactd.compactd.CompactdManager;
import io.compactd.compactd.models.CompactdTrack;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    protected static final String BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''";

    @NonNull
    public static ArrayList<Song> getAllSongs(@NonNull Context context) {
        Cursor cursor = makeSongCursor(context, null, null);
        return getSongs(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final String query) {
        Cursor cursor = makeSongCursor(context, AudioColumns.TITLE + " LIKE ?", new String[]{"%" + query + "%"});
        return getSongs(cursor);
    }

    @NonNull
    public static Song getSong(@NonNull final Context context, final int queryId) {
        Cursor cursor = makeSongCursor(context, AudioColumns._ID + "=?", new String[]{String.valueOf(queryId)});
        return getSong(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@Nullable final Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return songs;
    }

    @NonNull
    public static Song getSong(@Nullable Cursor cursor) {
        Song song;
        if (cursor != null && cursor.moveToFirst()) {
            song = getSongFromCursorImpl(cursor);
        } else {
            song = Song.EMPTY_SONG;
        }
        if (cursor != null) {
            cursor.close();
        }
        return song;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        final int id = cursor.getInt(0);
        final String title = cursor.getString(1);
        final int trackNumber = cursor.getInt(2);
        final int year = cursor.getInt(3);
        final long duration = cursor.getLong(4);
        final String data = cursor.getString(5);
        final long dateModified = cursor.getLong(6);
        final int albumId = cursor.getInt(7);
        final String albumName = cursor.getString(8);
        final int artistId = cursor.getInt(9);
        final String artistName = cursor.getString(10);

        return new Song(id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName);
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable final String selection, final String[] selectionValues) {
        return makeSongCursor(context, selection, selectionValues, PreferenceUtil.getInstance(context).getSongSortOrder());
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable String selection, String[] selectionValues, final String sortOrder) {
        Log.d("SongLoader", "makeSongCursor: selection=" + selection + "; selectionValues="
                + (selectionValues != null ? TextUtils.join(", ", selectionValues) : "null") + "; sortOrder=" + sortOrder);
        if (selection != null && !selection.trim().equals("")) {
            selection = BASE_SELECTION + " AND " + selection;
        } else {
            selection = BASE_SELECTION;
        }

        // Blacklist
        ArrayList<String> paths = BlacklistStore.getInstance(context).getPaths();
        if (!paths.isEmpty()) {
            selection = generateBlacklistSelection(selection, paths.size());
            selectionValues = addBlacklistSelectionValues(selectionValues, paths);
        }

        try {
            return CompactdTrack.makeCursor(CompactdManager.getInstance(context));
        } catch (SecurityException e) {
            return null;
        }
    }

    private static String generateBlacklistSelection(String selection, int pathCount) {
        String newSelection = selection != null && !selection.trim().equals("") ? selection + " AND " : "";
        newSelection += AudioColumns.DATA + " NOT LIKE ?";
        for (int i = 0; i < pathCount - 1; i++) {
            newSelection += " AND " + AudioColumns.DATA + " NOT LIKE ?";
        }
        return newSelection;
    }

    private static String[] addBlacklistSelectionValues(String[] selectionValues, ArrayList<String> paths) {
        if (selectionValues == null) selectionValues = new String[0];
        String[] newSelectionValues = new String[selectionValues.length + paths.size()];
        System.arraycopy(selectionValues, 0, newSelectionValues, 0, selectionValues.length);
        for (int i = selectionValues.length; i < newSelectionValues.length; i++) {
            newSelectionValues[i] = paths.get(i - selectionValues.length) + "%";
        }
        return newSelectionValues;
    }
}
