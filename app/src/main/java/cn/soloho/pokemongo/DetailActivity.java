package cn.soloho.pokemongo;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import cn.soloho.pokemongo.databinding.DetailBinding;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by solo on 16/8/2.
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    public static final String KEY_URL = "URL";
    public static final String KEY_TITLE = "TITLE";

    private List<Target> targets = new ArrayList<>();

    private DetailBinding binding;
    private Subscription subscription;
    private PhotoViewAttacher attacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getIntent().getStringExtra(KEY_TITLE));

        binding = DataBindingUtil.setContentView(this, R.layout.detail);
        binding.body.setMovementMethod(FixTouchLinkMovementMethod.getInstance());
        binding.body.setClickable(false);
        binding.body.setFocusable(false);
        binding.body.setLongClickable(false);

        attacher = new PhotoViewAttacher(binding.photo);
        attacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                binding.setShowPhoto(false);
                binding.photo.setImageDrawable(null);
            }
        });
        binding.setShowPhoto(false);

        String url = getIntent().getStringExtra(KEY_URL);
        subscription = DataManager.getInstance().getPokemonDetail(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySubscriber());

        Picasso.with(getActivity()).setLoggingEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        for (Target target : targets) {
            Picasso.with(this).cancelRequest(target);
        }
    }

    public Activity getActivity() {
        return this;
    }

    private class MySubscriber extends Subscriber<String> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }

        @Override
        public void onNext(String body) {
            targets.clear();
            Spanned html = Html.fromHtml(body, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(final String source) {
                    Log.v(TAG, "Get Drawable: " + source);

                    final URLDrawable urlDrawable = new URLDrawable();

                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Log.i(TAG, "Load bitmap: " + source);

                            int[] scaleSize = getScaleSize(bitmap);
                            int left = getLeft(scaleSize[0]);
                            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                            drawable.setBounds(left, 0, left + scaleSize[0], scaleSize[1]);

                            urlDrawable.setBounds(left, 0, left + drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                            urlDrawable.drawable = drawable;
                            binding.body.invalidate();
                            binding.body.setText(binding.body.getText());
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            Log.w(TAG, "Load bitmap failed: " + source);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    };

                    targets.add(target);
                    Picasso.with(getActivity()).load(source).into(target);

                    return urlDrawable;
                }
            }, null);

            SpannableStringBuilder builder = new SpannableStringBuilder(html);

            ImageSpan[] imageSpans = html.getSpans(0, body.length(), ImageSpan.class);
            for (ImageSpan span : imageSpans) {
                int start = html.getSpanStart(span);
                int end = html.getSpanEnd(span);
                int flags = html.getSpanFlags(span);
                builder.setSpan(new ClickableImageSpan(span.getDrawable(), span.getSource(),
                        getClickEvent(span.getSource())), start, end, flags);
            }

            binding.setBody(builder);
        }

        private int getLeft(int width) {
            return (getBodyViewWith() - width) / 2;
        }

        private int[] getScaleSize(Bitmap bitmap) {
            int width = getBodyViewWith();

            int iw = bitmap.getWidth();
            int ih = bitmap.getHeight();

            int heightC = width * ih / iw;
            if (heightC > ih) {
                // dont let hegiht be greater then set max
                heightC = ih;
                width = heightC * iw / ih;
            }
            return new int[]{width, heightC};
        }
    }

    private int getBodyViewWith() {
        return binding.body.getWidth()
                - binding.body.getPaddingLeft()
                - binding.body.getPaddingRight();
    }

    private View.OnClickListener getClickEvent(final String url) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.setShowPhoto(true);
                Picasso.with(getActivity()).load(url).into(binding.photo, new Callback() {
                    @Override
                    public void onSuccess() {
                        attacher.update();
                    }

                    @Override
                    public void onError() {
                        attacher.update();
                    }
                });
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (binding.getShowPhoto()) {
            binding.setShowPhoto(false);
            return;
        }
        super.onBackPressed();
    }
}
