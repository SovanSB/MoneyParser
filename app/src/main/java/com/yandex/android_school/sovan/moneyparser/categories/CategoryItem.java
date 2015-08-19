package com.yandex.android_school.sovan.moneyparser.categories;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;


import com.yandex.android_school.sovan.moneyparser.BuildConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ListIterator;



public class CategoryItem implements Serializable {

    public static final Uri URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/tasks");


    private long id;
    private String title;
    private List<CategoryItem> subs;
    private long mParentId;
 //   private int subNumber;  // number of subs.. isn't really used here, but was left not to lose information
    private String subIds;
    private String subNames;
 //   private long mHashCode;


    public CategoryItem(long id, String title, List<CategoryItem> subs) {
        this.id = id;
        this.title = title;
//        this.mHashCode = hashCode();

//        int tempSubNumber = 0; // counts number of subs
//        this.subs = mSubs;
        if (subs == null) {
            subs = Collections.<CategoryItem>emptyList();
        }
        this.subs = subs;

        String[] subFields = checkSubs(subs, this.hashFunction());
        this.subNames = subFields[0];
        this.subIds = subFields[1];

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CategoryItem> getSubs() {
        return subs;
    }

    public void setSubs(List<CategoryItem> subs) {
        if (subs == null) {
            subs = Collections.<CategoryItem>emptyList();
        }
        this.subs = subs;
//        int tempSubNumber = 0;
        String[] subFields = checkSubs(subs, this.hashFunction());
        this.subNames = subFields[0];
        this.subIds = subFields[1];
    }

    public long getParentId() {
        return mParentId;
    }

    public void setParentId(long parentId) {
        mParentId = parentId;
    }

 //   public int getSubNumber() {
//        return subNumber;
//    }

    public String getSubNames() {
        return subNames;
    }

    public String getSubIds() {
        return subIds;
    }

    public static interface Columns extends BaseColumns {
        String ID="id";
        String TITLE="title";
        String SUB_IDS="sub_ids";
  //      String SUB_NUMBER="sub_number";
        String SUB_NAMES="sub_names";
        String PARENT_ID="parent_id";
        String HASH_CODE="hash_code";
    }

    public long hashFunction() {
        return this.getTitle().hashCode() + 17 * this.getId();
    }

    public ContentValues toValues() {
        if (subs == null) {
            subs = Collections.<CategoryItem>emptyList();
        }


        String[] subFields = checkSubs(subs, this.hashFunction());
        this.subNames = subFields[0];
        this.subIds = subFields[1];

        final ContentValues values = new ContentValues();
        values.put(Columns.ID, id);
        values.put(Columns.TITLE, title);
        values.put(Columns.SUB_IDS, subIds);
 //       values.put(Columns.SUB_NUMBER, subNumber);
        values.put(Columns.SUB_NAMES, subNames);
        values.put(Columns.PARENT_ID, mParentId);
        values.put(Columns.HASH_CODE, hashFunction());
        return values;
    }

    public List<ContentValues> toValuesList(long parent) {
        this.mParentId = parent;
        List<ContentValues> valuesList = new ArrayList<ContentValues>();
        valuesList.add(this.toValues());
        for (CategoryItem sub : subs) {
            valuesList.addAll(sub.toValuesList(this.hashFunction()));
        }
        return valuesList;
    }

    public String[] checkSubs(List<CategoryItem> subs, long parent) {
        StringBuilder stringNumberBuilder = new StringBuilder();
        StringBuilder stringTitleBuilder = new StringBuilder();
        for (CategoryItem sub : subs) {
            if (stringNumberBuilder.length() > 0) {
                stringNumberBuilder.append("\n");
            }
            if (stringTitleBuilder.length() > 0) {
                stringTitleBuilder.append("\n");
            }
            stringNumberBuilder.append(sub.getId());
            stringTitleBuilder.append(sub.getTitle());
            sub.mParentId = parent;
        }
        return new String[]{stringTitleBuilder.toString(), stringNumberBuilder.toString()};
    }

}
