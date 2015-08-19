package com.yandex.android_school.sovan.moneyparser.activity;

import android.app.Activity;
import android.app.LoaderManager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

    private Button mButton;

    private Stack<Long> mParentIdStack;
    private Stack<Integer> mPositionStack;

    private long mCurrentParent = R.id.ROOT_PARENT;
    private int mPosition = 0;

    // Shared Preferences file name
    public static final String APP_PREFERENCES = "mysettings";
    // Shared preferences parameter names
    public static final String APP_PREFERENCES_LOADED = "loaded";
    public static final String APP_PREFERENCES_PARENT = "parent";
    public static final String APP_PREFERENCES_POSITION = "postition";

    SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.btn_back);
        mButton.setVisibility(View.GONE);
        mListView = (ListView) findViewById(R.id.listView);
        mCategoryAdapter = new CategoryAdapter(this, null);
        mListView.setAdapter(mCategoryAdapter);
        mParentIdStack = new Stack<Long>();
        mPositionStack = new Stack<Integer>();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        mCurrentParent = mSettings.getLong(APP_PREFERENCES_PARENT, R.id.ROOT_PARENT);
        mPosition = mSettings.getInt(APP_PREFERENCES_POSITION, 0);
        if (!mSettings.getBoolean(APP_PREFERENCES_LOADED, false)) {
            getLoaderManager().initLoader(R.id.category_manager, Bundle.EMPTY, this);
        }
        jumpTo(mCurrentParent, mPosition);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_POSITION, mListView.getLastVisiblePosition());
        editor.putLong(APP_PREFERENCES_PARENT, mCurrentParent);
        editor.apply();
        super.onDestroy();
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
            jumpTo(mCurrentParent, mPosition);
//            ContentResolver db = getContentResolver();
//            Cursor dataFiltered = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
//                    new String[]{Long.toString(R.id.ROOT_PARENT)}, null);
//            mCategoryAdapter.swapCursor(dataFiltered);
//            mParentIdStack.clear();
//            mPositionStack.clear();
//            mButton.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Database updated", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_LOADED, true);
            editor.apply();
            Log.d("Loader", "finished");
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
        // If there are subs, we form stacks and go deeper
        if (data.getCount() > 0) {

            mParentIdStack.add(previousId);
            mPositionStack.add(parent.getLastVisiblePosition());
            mCurrentParent = parentId;
            mPosition = parent.getLastVisiblePosition();
            mButton.setVisibility(View.VISIBLE);
            mCategoryAdapter.swapCursor(data);
        }
    }

//    public void onAddClick(View view) {
//        ContentResolver db = getContentResolver();
//        CategoryItem item1 = new CategoryItem(1, "Test1", null);
//        db.insert(CategoryItem.URI, item1.toValues());
//    }

//    public void onClearClick(View view) {
//        ContentResolver db = getContentResolver();
//        db.delete(CategoryItem.URI, null, null);
//        SharedPreferences.Editor editor = mSettings.edit();
//        editor.putBoolean(APP_PREFERENCES_LOADED, false);
//        editor.apply();
//
//    }

    public void onListClick(View view) {

        getLoaderManager().initLoader(R.id.category_manager, Bundle.EMPTY, this);

//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    RestAdapter restAdapter = new RestAdapter.Builder()
//                            .setEndpoint("https://money.yandex.ru")
//                            .build();
//                    MoneyService service = restAdapter.create(MoneyService.class);
//                    List<CategoryItem> categories = service.getCategories();
//                    List<ContentValues> values = new ArrayList<ContentValues>();
//                    for (CategoryItem cat : categories) {
//                        values.addAll(cat.toValuesList(-1));
//                    }
//                    final ContentValues[] bulkCategories = values.toArray(new ContentValues[values.size()]);
////                    final ContentValues[] bulkCategories = new ContentValues[categories.size()];
////
////                    for (int i = 0; i < categories.size(); ++i) {
////                        bulkCategories[i] = categories.get(i).toValues();
////                    }
//                    ContentResolver db = getContentResolver();
//                    // Clearing base before refreshing
//                    db.delete(CategoryItem.URI, null, null);
//                    db.bulkInsert(CategoryItem.URI, bulkCategories);
//
//                } catch (RetrofitError e) {
//                    Log.e("Retrofit", e.getMessage(), e);
//                }
//            }
//
//        });
//        t.start();
    }

    public void onFilterClick(View view) {
        jumpTo(R.id.ROOT_PARENT, 0);
        mParentIdStack.clear();
        mPositionStack.clear();
        mButton.setVisibility(View.GONE);
    }

    private void jumpTo(long parentID, int position) {
        ContentResolver db = getContentResolver();
        Cursor data = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
                new String[]{Long.toString(parentID)}, null);
        mCategoryAdapter.swapCursor(data);
        mCurrentParent = parentID;
        mPosition = position;
        mListView.smoothScrollToPosition(position);
    }

    public void onBackClick(View view) {
        if (!mParentIdStack.empty()) {
            jumpTo(mParentIdStack.pop(),mPositionStack.pop());
//            ContentResolver db = getContentResolver();
//            Cursor data = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
//                    new String[]{mParentIdStack.pop().toString()}, null);
//            mCategoryAdapter.swapCursor(data);
//            mListView.smoothScrollToPosition(mPositionStack.pop());
        }
        if (mParentIdStack.empty()) {
            mButton.setVisibility(View.GONE);
        }
    }
}
