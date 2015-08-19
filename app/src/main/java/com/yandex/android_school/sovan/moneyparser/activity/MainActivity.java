package com.yandex.android_school.sovan.moneyparser.activity;

import android.app.Activity;
import android.app.LoaderManager;

import android.content.ContentResolver;
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
import com.yandex.android_school.sovan.moneyparser.categories.CategoryItem;
import com.yandex.android_school.sovan.moneyparser.loader.AsyncTaskLoader;

import java.util.Stack;


public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private ListView mListView;
    private CategoryAdapter mCategoryAdapter;

    // UI control for "backwards button", is used to show/hide it.
    private Button mButtonBack;

    // Stacks of previous parents' hash codes and corresponding positions of ListView,
    // so it is possible to return back to higher levels
    private Stack<Long> mParentIdStack;
    private Stack<Integer> mPositionStack;

    private long mCurrentParent = R.id.ROOT_PARENT;
    private int mPosition = 0;

    // Shared Preferences file name
    public static final String APP_PREFERENCES = "mysettings";
    // Shared preferences parameters names
    public static final String APP_PREFERENCES_LOADED = "loaded";
    public static final String APP_PREFERENCES_PARENT = "parent";
    public static final String APP_PREFERENCES_POSITION = "postition";

    SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonBack = (Button) findViewById(R.id.btn_back);
        mButtonBack.setVisibility(View.GONE);

        mListView = (ListView) findViewById(R.id.listView);
        mCategoryAdapter = new CategoryAdapter(this, null);
        mListView.setAdapter(mCategoryAdapter);

        // Initialising stacks
        mParentIdStack = new Stack<Long>();
        mPositionStack = new Stack<Integer>();

        // Recover previous parameters, if they exist.
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        mCurrentParent = mSettings.getLong(APP_PREFERENCES_PARENT, R.id.ROOT_PARENT);
        mPosition = mSettings.getInt(APP_PREFERENCES_POSITION, 0);

        // Checking flag not to download the base if it already exists without a command
        if (!mSettings.getBoolean(APP_PREFERENCES_LOADED, false)) {
            getLoaderManager().initLoader(R.id.category_manager, Bundle.EMPTY, this);
        }

        // Restoring previous ListView state
        jumpTo(mCurrentParent, mPosition);
    }

    @Override
    protected void onDestroy() {
        // Saving previous state for restoring after exiting or screen rotation
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
            // Restoring previous state of ListView
            jumpTo(mCurrentParent, mPosition);

            Toast.makeText(getApplicationContext(), "Database updated", Toast.LENGTH_SHORT).show();

            // Saving flag not to download base if it exists without a command
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
            mButtonBack.setVisibility(View.VISIBLE);
            mCategoryAdapter.swapCursor(data);
        }
    }

    // Refreshing the base
    public void onListClick(View view) {
        getLoaderManager().initLoader(R.id.category_manager, Bundle.EMPTY, this);
    }

    // Returning home (to the highest level)
    public void onFilterClick(View view) {
        jumpTo(R.id.ROOT_PARENT, 0);
        mParentIdStack.clear();
        mPositionStack.clear();
        mButtonBack.setVisibility(View.GONE);
    }

    // Function that leads us to selected level and position
    private void jumpTo(long parentID, int position) {
        ContentResolver db = getContentResolver();
        Cursor data = db.query(CategoryItem.URI, null, CategoryItem.Columns.PARENT_ID + "=?",
                new String[]{Long.toString(parentID)}, null);
        mCategoryAdapter.swapCursor(data);
        mCurrentParent = parentID;
        mPosition = position;
        mListView.smoothScrollToPosition(position);
    }

    // "Backwards" function: restores previous state and position
    public void onBackClick(View view) {
        if (!mParentIdStack.empty()) {
            jumpTo(mParentIdStack.pop(),mPositionStack.pop());
        }
        if (mParentIdStack.empty()) {
            mButtonBack.setVisibility(View.GONE);
        }
    }
}
