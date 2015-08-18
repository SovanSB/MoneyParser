package com.yandex.android_school.sovan.moneyparser.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.yandex.android_school.sovan.moneyparser.R;
import com.yandex.android_school.sovan.moneyparser.categories.CategoryItem;

import java.util.Objects;

/**
 * Created by Sovan on 09.08.2015.
 */
public class CategoryAdapter extends ResourceCursorAdapter {
    public CategoryAdapter(Context context, Cursor c) {
        super(context,  R.layout.li_item, c,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view.findViewById(R.id.textViewTitle)).
                setText(cursor.getString(cursor.getColumnIndex(CategoryItem.Columns.TITLE)));
        long itemId = cursor.getLong(cursor.getColumnIndex(CategoryItem.Columns.ID));
        if (itemId != 0) {
            ((TextView) view.findViewById(R.id.textViewId)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.textViewId)).setText(Long.toString(itemId));
        }
        else {
            ((TextView) view.findViewById(R.id.textViewId)).setVisibility(View.GONE);;
        }
        final String subs = cursor.getString(cursor.getColumnIndex(CategoryItem.Columns.SUB_NAMES));
        if (TextUtils.isEmpty(subs)) {
            ((TextView) view.findViewById(R.id.textViewSubs)).setVisibility(View.GONE);
        }
        else {
            ((TextView) view.findViewById(R.id.textViewSubs)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.textViewSubs)).
                    setText(cursor.getString(cursor.getColumnIndex(CategoryItem.Columns.SUB_NAMES)));
        }
    }
}
