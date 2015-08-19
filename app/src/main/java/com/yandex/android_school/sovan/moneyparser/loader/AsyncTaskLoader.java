package com.yandex.android_school.sovan.moneyparser.loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.util.Log;

import com.yandex.android_school.sovan.moneyparser.R;
import com.yandex.android_school.sovan.moneyparser.api.MoneyService;
import com.yandex.android_school.sovan.moneyparser.categories.CategoryItem;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

/**
 * Created by Sovan on 12.08.2015.
 */
public class AsyncTaskLoader extends CursorLoader {
    public AsyncTaskLoader(Context context) {
        super(context, CategoryItem.URI, null, null, null, null);
    }

    @Override
    public Cursor loadInBackground() {
        try {
            MoneyService service = getService("https://money.yandex.ru");
            List<CategoryItem> categories = service.getCategories();
            List<ContentValues> values = new ArrayList<ContentValues>();
            for (CategoryItem cat : categories) {
                values.addAll(cat.toValuesList(R.id.ROOT_PARENT));
            }
            final ContentValues[] bulkCategories = values.toArray(new ContentValues[values.size()]);

            ContentResolver db = getContext().getContentResolver();
            // Clearing base before refreshing
            db.delete(CategoryItem.URI, null, null);
            db.bulkInsert(CategoryItem.URI, bulkCategories);

        } catch (RetrofitError e) {
            Log.e("Retrofit", e.getMessage(), e);
        }

        return super.loadInBackground();
    }

    MoneyService getService(String endpoint) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .build()
                .create(MoneyService.class);
    }
}