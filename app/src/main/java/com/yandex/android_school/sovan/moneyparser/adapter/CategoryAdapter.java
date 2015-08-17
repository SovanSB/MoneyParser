package com.yandex.android_school.sovan.moneyparser.adapter;

import android.content.Context;
import android.database.Cursor;
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
        // Add no id check
        ((TextView)view.findViewById(R.id.textViewId)).
                setText(Long.toString(cursor.getLong(cursor.getColumnIndex(CategoryItem.Columns.ID))));
        ((TextView)view.findViewById(R.id.textViewSubs)).
                setText(cursor.getString(cursor.getColumnIndex(CategoryItem.Columns.SUB_NAMES)));
    }
}
