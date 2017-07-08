package com.example.android.movieapp2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.movieapp2.MainActivity;
import com.example.android.movieapp2.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by charlotte on 7/7/17.
 */

public class FavoriteUtils {

    private static final String SHARED_PREFS = "prefs";

    public FavoriteUtils() {
        super();
    }

    public static void addFavorite(Context context, String mTitle) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> setList = prefs.getStringSet(context.getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        if (setList == null) setList = new HashSet<>();
        setList.add(mTitle);
        editor.putStringSet(context.getString(R.string.pref_fav_key), setList);
        editor.apply();
        MainActivity.setmFavoriteChanged(true);
    }

    public static void removeFavorite(Context context, String mTitle) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> setList = prefs.getStringSet(context.getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        if (setList == null) throw new Exception("SP Favorites are empty");
        setList.remove(mTitle);
        editor.putStringSet(context.getString(R.string.pref_fav_key), setList);
        editor.apply();
        MainActivity.setmFavoriteChanged(true);
    }

    public static boolean checkFavorite(Context context, String title) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        Set<String> setList = prefs.getStringSet(context.getString(R.string.pref_fav_key), null); // get Favorites Set<> from SP
        if (setList == null) {
            return false;
        } else {
            for (String s : setList) {
                if (s.equals(title)){
                    return true;}
            }
        }
        return false;
    }
}
