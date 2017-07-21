package com.example.android.movieapp2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.android.movieapp2.MainActivity;
import com.example.android.movieapp2.R;
import com.example.android.movieapp2.data.MovieContract;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by charlotte on 7/7/17.
 */

public class FavoriteUtils {

    private static final String SHARED_PREFS = "prefs";
    private static SharedPreferences sPrefs;
    private static SharedPreferences.Editor sEditor;
    private static Set<String> mSetList;

    public static void addFavorite(Context context, String title, String localID, ContentValues cv) {
        sPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        sEditor = sPrefs.edit();
        mSetList = sPrefs.getStringSet(context.getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        if (mSetList == null) mSetList = new HashSet<>();
        mSetList.add(title);
        sEditor.putStringSet(context.getString(R.string.pref_fav_key), mSetList);
        sEditor.apply();
        MainActivity.setmFavoriteChanged(true);
        Uri updateUri = Uri.parse(MovieContract.MovieEntry.MOVIE_TABLE_URI + "/" + localID);
        context.getContentResolver().update(updateUri, cv, null, null);
        printFravorites();
    }

    public static void removeFavorite(Context context, String mTitle, String localID, ContentValues cv) throws Exception {
        sPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        sEditor = sPrefs.edit();
        mSetList = sPrefs.getStringSet(context.getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        if (mSetList == null) throw new Exception("SP Favorites are empty");
        mSetList.remove(mTitle);
        sEditor.putStringSet(context.getString(R.string.pref_fav_key), mSetList);
        sEditor.apply();
        MainActivity.setmFavoriteChanged(true);
        Uri updateUri = Uri.parse(MovieContract.MovieEntry.MOVIE_TABLE_URI + "/" + localID);
        context.getContentResolver().update(updateUri, cv, null, null);
        Log.i("FavUtils", "Favorite Movie Removed: " + mTitle);

        printFravorites();
    }

    public static boolean checkFavorite(Context context, String title) {
        sPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        mSetList = sPrefs.getStringSet(context.getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        if (mSetList == null) {
            return false;
        } else {
            for (String s : mSetList) {
                if (s.equals(title)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void printFravorites() {
        Log.i("FavUtils", "************* Start of Favorites List ******************");
        for (String s : mSetList) {
            Log.i("FavUtils", "Favorite Movie: " + s);
        }
        Log.i("FavUtils", "************* End of Favorites List ******************");
    }
}
