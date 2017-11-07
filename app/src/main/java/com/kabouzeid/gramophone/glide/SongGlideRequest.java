package com.kabouzeid.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.glide.audiocover.AudioFileCover;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteTranscoder;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import io.compactd.compactd.CompactdManager;
import io.compactd.compactd.models.CompactdAlbum;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongGlideRequest {

    public static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.NONE;
    public static final int DEFAULT_ERROR_IMAGE = R.drawable.default_album_art;
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;

    public static class Builder {
        final RequestManager requestManager;
        final Album album;
        private final Context context;
        boolean ignoreMediaStore;

        public static Builder from(@NonNull Context context, @NonNull RequestManager requestManager, Album album) {
            return new Builder(context, requestManager, album);
        }

        private Builder(Context context, @NonNull RequestManager requestManager, Album album) {
            this.requestManager = requestManager;
            this.context = context;
            this.album = album;
        }

        public PaletteBuilder generatePalette(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(context, this);
        }

        public Builder checkIgnoreMediaStore(Context context) {
            return ignoreMediaStore(PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork());
        }

        public Builder ignoreMediaStore(boolean ignoreMediaStore) {
            this.ignoreMediaStore = ignoreMediaStore;
            return this;
        }

        public DrawableRequestBuilder<GlideDrawable> build() {
            //noinspection unchecked
            return createBaseRequest(context, requestManager, album, ignoreMediaStore)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .listener(new RequestListener() {
                        @Override
                        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                            e.printStackTrace();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .animate(DEFAULT_ANIMATION)
                    .signature(createSignature(album));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;
        private final Context context;

        public BitmapBuilder(Context context, Builder builder) {
            this.builder = builder;
            this.context = context;
        }

        public BitmapRequestBuilder<?, Bitmap> build() {
            //noinspection unchecked
            return createBaseRequest(context, builder.requestManager, builder.album, builder.ignoreMediaStore)
                    .asBitmap()
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .listener(new RequestListener() {
                        @Override
                        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                            e.printStackTrace();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .animate(DEFAULT_ANIMATION)
                    .signature(createSignature(builder.album));
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public BitmapRequestBuilder<?, BitmapPaletteWrapper> build() {
            //noinspection unchecked
            return createBaseRequest(context, builder.requestManager, builder.album, builder.ignoreMediaStore)
                    .asBitmap()
                    .transcode(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .listener(new RequestListener() {
                        @Override
                        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                            e.printStackTrace();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .animate(DEFAULT_ANIMATION)
                    .signature(createSignature(builder.album));
        }
    }

    public static DrawableTypeRequest createBaseRequest(Context context, RequestManager requestManager, Album album, boolean ignoreMediaStore) {

        return requestManager.load(new AudioFileCover(CompactdAlbum.findById(
                CompactdManager.getInstance(context),
                album.getId(),
                true
        )));
    }

    public static Key createSignature(Album album) {
        return new MediaStoreSignature("", album.getDateModified(), 0);
    }
}
