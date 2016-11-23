package com.owenlarosa.udacians.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Owen LaRosa on 11/23/16.
 */

public class LinkImageButton extends ImageButton {

    // url to be opened in the browser by this button
    private String url = "";

    public LinkImageButton(Context context) {
        super(context);
    }

    public LinkImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinkImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LinkImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getUrl() {
        return url;
    }
    
    public void setUrl(final String url) {
        this.url = url;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // convert to Uri for use with intent
                Uri linkUri = Uri.parse(url);
                // open the link in the browser
                Intent intent = new Intent(Intent.ACTION_VIEW, linkUri);
                getContext().startActivity(intent);
            }
        });
    }

}
