package com.yandex.android_school.sovan.moneyparser.sqlite;

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

class SQLiteUriMatcher {

    static final int NO_MATCH = -1;

    static final int MATCH_ALL = 1;

    static int match(Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 1) {
            return MATCH_ALL;
        }
        return NO_MATCH;
    }

}
