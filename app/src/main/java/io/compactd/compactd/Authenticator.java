package io.compactd.compactd;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by Vincent on 30/10/2017.
 */

public class Authenticator {
    private static final String TAG = "CompactdAuth";

    public String login (URL url, String username, String password) throws IOException, JSONException, CompactdException {

        CompactdRequest req = new CompactdRequest(url, "/api/sessions");
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        return req.post(body).getString("token");

    }
    private static JSONObject decode (String token) throws UnsupportedEncodingException, JSONException {
        String[] split = token.split("\\.");
        return new JSONObject(getJson(split[1]));
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    public boolean isTokenValid (String token) {
        Log.d(TAG, "isTokenValid: " + token);
        try {
            JSONObject decoded = decode(token);
            Log.d(TAG, "isTokenValid: " + decoded.toString());
            Log.d(TAG, "isTokenValid: checking exp against "+ (System.currentTimeMillis() + 1000 * 60 * 60) / 1000L);
            return (System.currentTimeMillis() + 1000 * 60 * 60) / 1000L <=
                    decoded.getInt("exp");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserFromToken (String token) throws UnsupportedEncodingException, JSONException {

        JSONObject decoded = decode(token);
        return decoded.getString("user");
    }
}
