package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class HelpFragment extends Fragment {

    @BindView(R.id.report_bug_edit_text)
    EditText reportBugEditText;

    private Unbinder mUnbinder;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference bugsReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        bugsReference = mFirebaseDatabase.getReference().child("bugs");

        return rootView;
    }

    @OnClick(R.id.report_bug_button)
    public void reportBug() {
        String report = reportBugEditText.getText().toString();
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        HashMap<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("report", report);
        data.put("time", ServerValue.TIMESTAMP);
        bugsReference.push().setValue(data);
        reportBugEditText.setText("");
    }

}
