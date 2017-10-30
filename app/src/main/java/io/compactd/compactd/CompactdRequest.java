package io.compactd.compactd;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdRequest {
    private static final String TAG = "CompactdRequest";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private URL mBaseURL;
    private String mEndpoint;
    private String mSessionToken;

    public CompactdRequest(URL baseURL, String endpoint) {
        this.mBaseURL = baseURL;
        this.mEndpoint = endpoint;
    }
    public CompactdRequest(URL baseURL, String endpoint, String token) {
        this.mBaseURL = baseURL;
        this.mEndpoint = endpoint;
        this.mSessionToken = token;
    }
    public String getSessionToken() {
        return mSessionToken;
    }

    public CompactdRequest setSessionToken(String sessionToken) {
        this.mSessionToken = sessionToken;
        return this;
    }
    public JSONObject post (JSONObject data) throws IOException, JSONException, CompactdException {
        OkHttpClient client = new OkHttpClient();

        String remote = mBaseURL.toString();

        if (remote.endsWith("/") && this.mEndpoint.startsWith("/")) {
            remote = remote.substring(0, remote.length() - 1);
        }

        remote = remote + this.mEndpoint;

        Log.d(TAG, "POST " + remote);
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            if (key.equals("password")) {
                int length = data.get(key).toString().length();

                Log.d(TAG, "POST   " + key + " = " +
                        new String(new char[length]).replace('\0', '*'));
            } else {

                Log.d(TAG, "POST   " + key + " = " + data.get(key));
            }
        }

        RequestBody body = RequestBody.create(JSON, data.toString());
        Request request = new Request.Builder()
                .url(new URL(remote))
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        String raw = response.body().string();

        Log.d(TAG, "Response: " + raw);

        JSONObject res = new JSONObject(raw);

        if (res.has("error")) {
            String error = res.getString("error");

            if (error != null && !error.isEmpty()) {
                if (error.equals("Invalid credentials")) {
                    throw new CompactdException(CompactdErrorCode.INVALID_CREDENTIALS);
                }
                throw new CompactdException(CompactdErrorCode.SERVER_ERROR);
            }
        }
        return res;

    }
}
