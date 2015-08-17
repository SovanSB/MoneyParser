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


/**
 * Created by Sovan on 10.08.2015.
 */

public class CategoryItem implements Serializable {

    public static final Uri URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/tasks");


    private long id;
    private String title;
    private List<CategoryItem> subs;
    private long mParentId;
    private int subNumber;
    private String subIds;
    private String subNames;
 //   private long mHashCode;


    public CategoryItem(long id, String title, List<CategoryItem> subs) {
        this.id = id;
        this.title = title;
//        this.mHashCode = hashCode();
        int tempSubNumber = 0;
//        this.subs = mSubs;
        if (subs == null) {
            subs = Collections.<CategoryItem>emptyList();
        }
        this.subs = subs;

        StringBuilder stringBuilder = new StringBuilder();
        for (CategoryItem sub : subs) {
            tempSubNumber++;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(sub.getId());
            // TODO: Test this!!!
            sub.mParentId = this.hashFunction();
        }
        this.subNumber = tempSubNumber;
        this.subIds = stringBuilder.toString();

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
        int tempSubNumber = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (CategoryItem sub : subs) {
            tempSubNumber++;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(sub.getId());
            // TODO: Test this!!!
            sub.mParentId = this.hashFunction();
        }
        this.subNumber = tempSubNumber;
        this.subIds = stringBuilder.toString();
    }

    public long getParentId() {
        return mParentId;
    }

    public void setParentId(long parentId) {
        mParentId = parentId;
    }

    public int getSubNumber() {
        return subNumber;
    }

    public String getSubNames() {
        return subNames;
    }

    //    public void setSubNumber(int subNumber) {
//        this.subNumber = subNumber;
//    }

    public String getSubIds() {
        return subIds;
    }

//    public void setSubIds(String subIds) {
//        this.subIds = subIds;
//    }

    public static interface Columns extends BaseColumns {
        String ID="id";
        String TITLE="title";
        String SUB_IDS="sub_ids";
        String SUB_NUMBER="sub_number";
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

        int tempSubNumber = 0;
        StringBuilder stringNumberBuilder = new StringBuilder();
        StringBuilder stringTitleBuilder = new StringBuilder();
        for (CategoryItem sub : subs) {
            tempSubNumber++;
            if (stringNumberBuilder.length() > 0) {
                stringNumberBuilder.append("\n");
            }
            if (stringTitleBuilder.length() > 0) {
                stringTitleBuilder.append("\n");
            }
            stringNumberBuilder.append(sub.getId());
            stringTitleBuilder.append(sub.getTitle());
            // TODO: Test this!!!
            sub.mParentId = this.hashFunction();
        }
        this.subNumber = tempSubNumber;
        this.subNames = stringTitleBuilder.toString();
        this.subIds = stringNumberBuilder.toString();

        final ContentValues values = new ContentValues();
        values.put(Columns.ID, id);
        values.put(Columns.TITLE, title);
        values.put(Columns.SUB_IDS, subIds);
        values.put(Columns.SUB_NUMBER, subNumber);
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

}
