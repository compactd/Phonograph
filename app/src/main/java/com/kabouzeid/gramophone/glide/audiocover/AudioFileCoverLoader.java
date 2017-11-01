package com.kabouzeid.gramophone.glide.audiocover;

import android.content.Context;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class AudioFileCoverLoader implements StreamModelLoader<AudioFileCover> {
    private Context context;
    private ModelLoader<GlideUrl, InputStream> urlLoader;
    private static final int TIMEOUT = 500;

    public AudioFileCoverLoader(Context context, ModelLoader<GlideUrl, InputStream> urlLoader) {
        this.context = context;
        this.urlLoader = urlLoader;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(AudioFileCover model, int width, int height) {
        return new AudioFileCoverFetcher(model, context, urlLoader, width, height);
    }


    public static class Factory implements ModelLoaderFactory<AudioFileCover, InputStream> {
        private OkHttpUrlLoader.Factory okHttpFactory;

        public Factory(Context context) {
            okHttpFactory = new OkHttpUrlLoader.Factory(new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                    .build());
        }
        @Override
        public ModelLoader<AudioFileCover, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new AudioFileCoverLoader(context, okHttpFactory.build(context, factories));
        }

        @Override
        public void teardown() {
        }
    }
}

