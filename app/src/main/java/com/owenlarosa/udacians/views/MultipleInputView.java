package com.owenlarosa.udacians.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.owenlarosa.udacians.R;

/**
 * Created by Owen LaRosa on 12/11/16.
 */

public class MultipleInputView extends Dialog {

    private Context mContext;
    private Type mType;

    public enum Type {
        Topic, Article, Event;
    }

    public MultipleInputView(Context context, Type type) {
        super(context);
        mContext = context;
        mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_view);
        // dialog should be fullscreen, has translucent margins
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
    }

}
