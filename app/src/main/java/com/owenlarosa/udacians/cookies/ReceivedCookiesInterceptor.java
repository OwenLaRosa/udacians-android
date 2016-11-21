package com.owenlarosa.udacians.cookies;

import android.content.Context;
import android.preference.PreferenceManager;

import com.owenlarosa.udacians.R;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * This Interceptor add all received Cookies to the app DefaultPreferences.
 * Your implementation on how to save the Cookies on the Preferences MAY VARY.
 * <p>
 * Created by tsuharesu on 4/1/15.
 */
public class ReceivedCookiesInterceptor implements Interceptor {

    private Context mContext;

    public ReceivedCookiesInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }

            PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                    .putStringSet(mContext.getString(R.string.pref_cookies), cookies)
                    .apply();
        }

        return originalResponse;
    }
}
