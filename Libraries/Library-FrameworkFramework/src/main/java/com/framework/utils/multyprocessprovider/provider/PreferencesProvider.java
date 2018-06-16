package com.framework.utils.multyprocessprovider.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.framework.BuildConfig;
import com.framework.utils.multyprocessprovider.provider.base.BaseContentProvider;
import com.framework.utils.multyprocessprovider.provider.preferences.PreferencesColumns;

import java.util.Arrays;

public class PreferencesProvider extends BaseContentProvider {
    private static final String TAG = PreferencesProvider.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TYPE_CURSOR_ITEM = "vnd.android.cursor.item/";
    private static final String TYPE_CURSOR_DIR = "vnd.android.cursor.dir/";
    private static final String LIBRARY_DEFAULT_AUTHORITY = "com.demo.demo";
    private static final int URI_TYPE_PREFERENCES = 0;
    private static final int URI_TYPE_PREFERENCES_ID = 1;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    public static String CONTENT_URI_BASE;

    public static String getHostProviderAuthorities(Context appContext) throws IllegalArgumentException {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (applicationInfo == null) {
                throw new IllegalArgumentException(" get application info = null, has no meta data! ");
            }
            return applicationInfo.metaData.getString("CONTENTPROVIDER_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException(" get application info error! ", e);
        }
    }

    private static void setAuthority(String authority) {
        URI_MATCHER.addURI(authority, PreferencesColumns.TABLE_NAME, URI_TYPE_PREFERENCES);
        URI_MATCHER.addURI(authority, PreferencesColumns.TABLE_NAME + "/#", URI_TYPE_PREFERENCES_ID);
        CONTENT_URI_BASE = "content://" + authority;
    }

    @Override
    public boolean onCreate() {
        super.onCreate();
//        String authority = getContext().getString(R.string.preferences_provider_authority);
        String authority = getHostProviderAuthorities(getContext());
        Log.e("yy", "authority:" + authority);
        if (LIBRARY_DEFAULT_AUTHORITY.equals(authority)) {
            throw new IllegalStateException("Please don't use the library's default authority for your app. \n " +
                    "Multiple apps with the same authority will fail to install on the same device.\n " +
                    "Please add the line: \n " +
                    "==================================================================================================\n " +
                    " resValue \"string\", \"preferences_provider_authority\", \"${applicationId}" +
                    ".preferencesprovider\" \n " +
                    "==================================================================================================\n " +
                    "in your build.gradle file");
        }
        setAuthority(authority);
        return true;
    }

    @Override
    protected SQLiteOpenHelper createSqLiteOpenHelper() {
        return PreferencesSQLiteOpenHelper.getInstance(getContext());
    }

    @Override
    protected boolean hasDebug() {
        return DEBUG;
    }

    @Override
    public String getType(Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_TYPE_PREFERENCES:
                return TYPE_CURSOR_DIR + PreferencesColumns.TABLE_NAME;
            case URI_TYPE_PREFERENCES_ID:
                return TYPE_CURSOR_ITEM + PreferencesColumns.TABLE_NAME;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.d(TAG, "insert uri=" + uri + " values=" + values);
        return super.insert(uri, values);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (DEBUG) Log.d(TAG, "bulkInsert uri=" + uri + " values.length=" + values.length);
        return super.bulkInsert(uri, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (DEBUG)
            Log.d(TAG, "update uri=" + uri + " values=" + values + " selection=" + selection + " selectionArgs=" +
                    Arrays.toString(selectionArgs));
        return super.update(uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (DEBUG)
            Log.d(TAG, "delete uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString
                    (selectionArgs));
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG)
            Log.d(TAG, "query uri=" + uri + " selection=" + selection + " selectionArgs=" + Arrays.toString
                    (selectionArgs) + " sortOrder=" + sortOrder
                    + " groupBy=" + uri.getQueryParameter(QUERY_GROUP_BY) + " having=" + uri.getQueryParameter
                    (QUERY_HAVING) + " limit=" + uri.getQueryParameter(QUERY_LIMIT));
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected QueryParams getQueryParams(Uri uri, String selection, String[] projection) {
        QueryParams res = new QueryParams();
        String id = null;
        int matchedId = URI_MATCHER.match(uri);
        switch (matchedId) {
            case URI_TYPE_PREFERENCES:
            case URI_TYPE_PREFERENCES_ID:
                res.table = PreferencesColumns.TABLE_NAME;
                res.idColumn = PreferencesColumns._ID;
                res.tablesWithJoins = PreferencesColumns.TABLE_NAME;
                res.orderBy = PreferencesColumns.DEFAULT_ORDER;
                break;

            default:
                throw new IllegalArgumentException("The uri '" + uri + "' is not supported by this ContentProvider");
        }

        switch (matchedId) {
            case URI_TYPE_PREFERENCES_ID:
                id = uri.getLastPathSegment();
        }
        if (id != null) {
            if (selection != null) {
                res.selection = res.table + "." + res.idColumn + "=" + id + " and (" + selection + ")";
            } else {
                res.selection = res.table + "." + res.idColumn + "=" + id;
            }
        } else {
            res.selection = selection;
        }
        return res;
    }
}
