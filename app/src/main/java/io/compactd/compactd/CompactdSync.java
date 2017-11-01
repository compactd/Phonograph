package io.compactd.compactd;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.ReplicationState;
import com.couchbase.lite.support.HttpClientFactory;
import com.readystatesoftware.chuck.ChuckInterceptor;


import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.compactd.compactd.models.CompactdAlbum;
import io.compactd.compactd.models.CompactdArtist;
import io.compactd.compactd.models.CompactdTrack;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
///**
// * Created by Vincent on 30/10/2017.
// */
//class CompactdAuthorizer extends BaseAuthorizer
//        implements CustomHeadersAuthorizer, CredentialAuthorizer {
//
//    public static final String TAG = Log.TAG_SYNC;
//    private String mToken;
//
//    public CompactdAuthorizer(String token) {
//        this.mToken = token;
//    }
//
//    @Override
//    public boolean authorizeURLRequest(Request.Builder builder) {
//        if (authUserInfo() == null)
//            return false;
//        builder.addHeader("Authorization", "Bearer " + mToken);
//        return true;
//    }
//
//    @Override
//    public String authUserInfo() {
//        if (this.mToken != null && !this.mToken.isEmpty()) {
//            try {
//                return new Authenticator().getUserFromToken(mToken);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                return null;
//            } catch (JSONException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return null;
//    }
    ////////////////////////////////////////////////////////////
    // Implementation of Authorizer
    ////////////////////////////////////////////////////////////

//    @Override
//    public boolean removeStoredCredentials() {
//        this.mToken = null;
//        return true;
//    }
//
//    public String getToken() {
//        // @optional
//        return mToken;
//    }
//}



public class CompactdSync {

    private static final String TAG = "CompactdSync";
    private static CompactdSync sInstance;
    private final String[] DATABASES = {
            CompactdArtist.DATABASE_NAME,
            CompactdAlbum.DATABASE_NAME,
            CompactdTrack.DATABASE_NAME
    };
    private final Manager mManager;
    private final Context mContext;
    private String mToken;
    private String mURL;
    private List<SyncEventListener> mListeners = new ArrayList<SyncEventListener>();

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public void setURL(String mURL) {
        this.mURL = mURL;
    }

    public interface SyncEventListener {
        public void finished ();
        public void databaseChanged (String database);
        public void databaseSyncStarted (String database);
        public void databaseSyncFinished (String database);
        public void onCouchException (CouchbaseLiteException exc);
        public void onURLException (MalformedURLException exc);
    }

    public interface DatabaseChangedListener {
        public void onDatabaseChanged ();
    }

    private CompactdSync(Context context) throws IOException {
        this.mContext = context;
        this.mManager = new Manager(new AndroidContext(context.getApplicationContext()), Manager.DEFAULT_OPTIONS);
    }

    public void start () {
        try {
            sync(0);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            for (SyncEventListener listener : mListeners) {
                listener.onCouchException(e);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            for (SyncEventListener listener : mListeners) {
                listener.onURLException(e);
            }

        }
    }
    public void subscribe (final DatabaseChangedListener listener) {
        this.addEventListener(new SyncEventListener() {
            boolean changed;

            @Override
            public void finished() {
                if (changed) {
                    listener.onDatabaseChanged();
                }
            }

            @Override
            public void databaseChanged(String database) {
                changed = true;
            }

            @Override
            public void databaseSyncStarted(String database) {

            }

            @Override
            public void databaseSyncFinished(String database) {

            }

            @Override
            public void onCouchException(CouchbaseLiteException exc) {

            }

            @Override
            public void onURLException(MalformedURLException exc) {

            }
        });
    }
    public void addEventListener (SyncEventListener l) {
        mListeners.add(l);
    }
    private void sync (final int index) throws CouchbaseLiteException, MalformedURLException {
        final String database = DATABASES[index];
        android.util.Log.d(TAG, "sync: " + database);
        
        HttpClientFactory factory = new HttpClientFactory() {
            @Override
            public OkHttpClient getOkHttpClient() {
                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                        .build();
                return new OkHttpClient.Builder()
                        .addInterceptor(new ChuckInterceptor(mContext))
                        .connectionSpecs(Collections.singletonList(spec))
                        .build();
            }

            @Override
            public void addCookies(List<Cookie> cookies) {
                android.util.Log.e(TAG, "addCookies: " + cookies);
            }

            @Override
            public void deleteCookie(String name) {
                android.util.Log.e(TAG, "deleteCookie: " + name);
            }

            @Override
            public void deleteCookie(URL url) {
                android.util.Log.e(TAG, "deleteCookie: " + url );
            }

            @Override
            public void resetCookieStore() {
                android.util.Log.e(TAG, "resetCookieStore:");
            }

            @Override
            public CookieJar getCookieStore() {
                android.util.Log.e(TAG, "getCookieStore: ");
                return null;
            }
        };
        
        mManager.setDefaultHttpClientFactory(factory);


        Database db = mManager.getDatabase(database);

        Replication rep = db.createPullReplication(new URL(mURL + "/database/" + database));

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Authorization", "Bearer " + this.mToken);
        rep.setHeaders(headers);

        rep.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
            Log.d(TAG, event.toString());
            if (event.getChangeCount() > 0) {
                for (SyncEventListener listener : mListeners) {
                    listener.databaseChanged(database);
                }
            }
            if (event.getTransition() != null && event.getTransition().getDestination().equals(ReplicationState.STOPPED)) {

               if (index < DATABASES.length - 1) {
                   try {
                       for (SyncEventListener listener : mListeners) {
                           listener.databaseSyncFinished(database);
                           listener.databaseSyncStarted(database);
                       }

                       sync(index + 1);
                   } catch (CouchbaseLiteException e) {
                       e.printStackTrace();
                       for (SyncEventListener listener : mListeners) {
                           listener.onCouchException(e);
                       }

                   } catch (MalformedURLException e) {
                       e.printStackTrace();
                       for (SyncEventListener listener : mListeners) {
                           listener.onURLException(e);
                       }

                   }
               } else {
                   for (SyncEventListener listener : mListeners) {
                       listener.finished();
                   }
               }
            }
            }
        });
        rep.setContinuous(false);

        rep.start();

    }
    public static CompactdSync getInstance(Context context) {
        if (sInstance == null) {
            try {
                sInstance = new CompactdSync(context.getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }
}


