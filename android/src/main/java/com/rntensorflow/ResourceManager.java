package com.rntensorflow;

import android.content.res.Resources;
import android.webkit.URLUtil;
import com.facebook.react.bridge.ReactContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class ResourceManager {

    private ReactContext reactContext;

    public ResourceManager(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    public String loadResourceAsString(String resource) {
        return new String(loadResource(resource));
    }

    public byte[] loadResource(String resource) {
        if(resource.startsWith("file://")) {
            return loadFromLocal(resource.substring(7));
        } else if(URLUtil.isValidUrl(resource)) {
            return loadFromUrl(resource);
        } else {
            return loadFromLocal(resource);
        }
    }

    private byte[] loadFromLocal(String resource) {
        try {
            // .pb and label .txt go into "raw", .jpg will go into "drawable", so try both
            int identifier = reactContext.getResources().getIdentifier(resource, "raw", reactContext.getPackageName());
            if (identifier == 0) {
                identifier = reactContext.getResources().getIdentifier(resource, "drawable", reactContext.getPackageName());
            }
            InputStream inputStream = reactContext.getResources().openRawResource(identifier);
            Log.i("ReactNative", "Successfully returning resource identifier: " + identifier + " for file " + resource);
            return inputStreamToByteArray(inputStream);
        } catch (IOException | Resources.NotFoundException e) {
            try {
                InputStream inputStream = reactContext.getAssets().open(resource);
                return inputStreamToByteArray(inputStream);
            } catch (IOException e1) {
                try {

                    InputStream inputStream = new FileInputStream(resource);
                    return inputStreamToByteArray(inputStream);
                } catch (IOException e2) {
                    throw new IllegalArgumentException("Could not load resource", e2);
                }
            }
        }
    }

    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        byte[] b = new byte[inputStream.available()];
        inputStream.read(b);
        return b;
    }

    private byte[] loadFromUrl(String url) {
        OkHttpClient client = new OkHttpClient();

        try {
            Request request = new Request.Builder().url(url).get().build();
            Response response = client.newCall(request).execute();
            return response.body().bytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not fetch data from url " + url);
        }
    }
}
