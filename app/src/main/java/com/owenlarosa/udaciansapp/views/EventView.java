package com.owenlarosa.udaciansapp.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.owenlarosa.udaciansapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/29/16.
 */

public class EventView extends LinearLayout {

    @BindView(R.id.event_about_text_view)
    public TextView aboutTextView;
    @BindView(R.id.attendees_recycler_view)
    public RecyclerView recyclerView;
    @BindView(R.id.event_write_post_view)
    public WritePostView writePostView;

    Unbinder unbinder;

    public EventView(Context context) {
        super(context);
        setupViews(context);
    }

    public EventView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupViews(context);
    }

    public EventView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        // initialize views from the layout with Butterknife
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.event_info_header, this);

        unbinder = ButterKnife.bind(this, rootView);
    }


}
