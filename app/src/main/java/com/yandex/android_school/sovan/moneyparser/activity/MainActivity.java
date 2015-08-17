package com.yandex.android_school.sovan.moneyparser.activity;

import android.app.Activity;
import android.app.LoaderManager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yandex.android_school.sovan.moneyparser.R;
import com.yandex.android_school.sovan.moneyparser.adapter.CategoryAdapter;
import com.yandex.android_school.sovan.moneyparser.api.MoneyService;
import com.yandex.android_school.sovan.moneyparser.categories.CategoryItem;
import com.yandex.android_school.sovan.moneyparser.loader.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import retrofit.RestAdapter;
import retrofit.RetrofitError;


public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private ListView mListView;
    private CategoryAdapter mCategoryAdapter;
    private Stack<Long> mParentIdStack;
    private Stack<Integer> mPositionStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView);
        mCategoryAdapter = new CategoryAdapter(this, null);
        mListView.setAdapter(mCategoryAdapter);
        mParentIdStack = new Stack<Long>();
        mPositionStack = new Stack<Integer>();
        getLoaderManager().initLoader(R.id.category_manager, Bundle.EMPTY, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == R.id.category_manager) {
            return new AsyncTaskLoader(getApplicationContext());
        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == R.id.category_manager) {
            ContentResolver db = getContentResolver();
            Cursor dataFiltered = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
                    new String[]{"-1"}, null);
            mCategoryAdapter.swapCursor(dataFiltered);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == R.id.category_manager) {
            mCategoryAdapter.swapCursor(null);
        }
    }

    @Override
    protected void onResume() {
        mListView.setOnItemClickListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mListView.setOnItemClickListener(null);
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mCategoryAdapter.getItem(position);
        long parentId = cursor.getLong(cursor.getColumnIndex(CategoryItem.Columns.HASH_CODE));
        long previousId = cursor.getLong(cursor.getColumnIndex(CategoryItem.Columns.PARENT_ID));
        ContentResolver db = getContentResolver();
        Cursor data = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
                new String[]{Long.toString(parentId)}, null);
        if (data.getCount() > 0) {
            mParentIdStack.add(previousId);
            mPositionStack.add(position);
            mCategoryAdapter.swapCursor(data);
        }
    }

    public void onAddClick(View view) {
        ContentResolver db = getContentResolver();
        CategoryItem item1 = new CategoryItem(1, "Test1", null);
        db.insert(CategoryItem.URI, item1.toValues());
    }

    public void onClearClick(View view) {
        ContentResolver db = getContentResolver();
        db.delete(CategoryItem.URI, null, null);

    }

    public void onListClick(View view) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    RestAdapter restAdapter = new RestAdapter.Builder()
                            .setEndpoint("https://money.yandex.ru")
                            .build();
                    MoneyService service = restAdapter.create(MoneyService.class);
                    List<CategoryItem> categories = service.getCategories();
                    List<ContentValues> values = new ArrayList<ContentValues>();
                    for (CategoryItem cat : categories) {
                        values.addAll(cat.toValuesList(-1));
                    }
                    final ContentValues[] bulkCategories = values.toArray(new ContentValues[values.size()]);
//                    final ContentValues[] bulkCategories = new ContentValues[categories.size()];
//
//                    for (int i = 0; i < categories.size(); ++i) {
//                        bulkCategories[i] = categories.get(i).toValues();
//                    }
                    ContentResolver db = getContentResolver();
                    // Clearing base before refreshing
                    db.delete(CategoryItem.URI, null, null);
                    db.bulkInsert(CategoryItem.URI, bulkCategories);

                } catch (RetrofitError e) {
                    Log.e("Retrofit", e.getMessage(), e);
                }
            }

        });
        t.start();
    }

    public void onFilterClick(View view) {
        ContentResolver db = getContentResolver();
        Cursor data = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
                new String[]{"-1"}, null);
        mCategoryAdapter.swapCursor(data);
    }

    public void onBackClick(View view) {
        if (!mParentIdStack.empty()) {
            ContentResolver db = getContentResolver();
            Cursor data = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
                    new String[]{mParentIdStack.pop().toString()}, null);
            mCategoryAdapter.swapCursor(data);
            mListView.smoothScrollToPosition(mPositionStack.pop());


        }
    }
}
