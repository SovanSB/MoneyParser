package com.yandex.android_school.sovan.moneyparser.api;

/**
 * Created by Sovan on 13.08.2015.
 */

import com.yandex.android_school.sovan.moneyparser.categories.CategoryItem;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface MoneyService {
    @GET("/api/categories-list")
    List<CategoryItem> getCategories();
}
