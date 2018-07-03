package com.ihewro.android_expression_package.http;

import com.ihewro.android_expression_package.bean.web.WebExpressionFolder;
import com.ihewro.android_expression_package.bean.web.WebExpressionFolderList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface WebImageInterface {
    @GET("expFolderList.php")
    public Call<WebExpressionFolderList> getDirList();

    @GET("expFolderDetail.php")
    public Call<List<WebExpressionFolder>> getDirDetail(@Query("dir") int dir, @Query("page") int page, @Query("pageSize") int pageSize);

}
