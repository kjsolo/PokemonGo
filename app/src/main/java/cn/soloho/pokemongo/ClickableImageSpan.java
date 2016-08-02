package cn.soloho.pokemongo;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.view.View;

/**
 * Created by solo on 16/8/2.
 */
public class ClickableImageSpan extends ImageSpan {

    private final View.OnClickListener onClickListener;

    public ClickableImageSpan(Drawable d, String source, View.OnClickListener onClickListener) {
        super(d, source);
        this.onClickListener = onClickListener;
    }

    public void onClick(View view) {
        onClickListener.onClick(view);
    }
}
