package com.yan.haha.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yan.haha.MainApplication;
import com.yan.haha.units.BrainRiddle;

public class RiddleDb {
    private static RiddleDb mInstance = null;
    private static SQLiteDatabase mDatabase = null;

    private static String DB_NAME = "riddles.db";
    private static String TABLE_NAME = "Riddles";

    public static RiddleDb getInstance() {
        Context context = MainApplication.getContext();
        if (context == null) {
            return null;
        }
        if (mInstance == null) {
            mInstance = new RiddleDb();
            // 创建或打开数据库
            String sql = "create table if not exists " + TABLE_NAME + " (id integer primary key," +
                    " question text, answer text)";
            mDatabase = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            mDatabase.execSQL(sql);
            mDatabase.close();
        }
        return mInstance;
    }

    public static void openDatabase() {
        Context context = MainApplication.getContext();
        if (mDatabase == null && context != null) {
            mDatabase = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        }
    }

    public static void closeDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    private static boolean isDatabaseOpened() {
        return (mDatabase != null && mDatabase.isOpen());
    }

    public static BrainRiddle getRiddle(String id) {
        if (!isDatabaseOpened()) {
            return null;
        }
        BrainRiddle riddle = null;
        String querySQL = "SELECT * FROM " + TABLE_NAME + " WHERE id=" + id;
        Cursor cursor = mDatabase.rawQuery(querySQL, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            String question = cursor.getString(cursor.getColumnIndex("question"));
            String answer = cursor.getString(cursor.getColumnIndex("answer"));
            riddle = new BrainRiddle("" + id, question, answer);
        }
        cursor.close();
        return riddle;
    }

    public static boolean isRiddleSaved(String id) {
        return (getRiddle(id) != null);
    }

    public static void saveRiddle(String id, String question, String answer) {
        if (!isDatabaseOpened()) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("question", question);
        cv.put("answer", answer);
        if (isRiddleSaved(id)) {
            mDatabase.update(TABLE_NAME, cv, "id=?", new String[]{id});
        } else {
            mDatabase.insert(TABLE_NAME, null, cv);
        }
    }

    public static void saveRiddle(BrainRiddle riddle) {
        saveRiddle(riddle.getId(), riddle.getQuestion(), riddle.getAnswer());
    }

    public static void deleteRiddle(String id) {
        if (!isDatabaseOpened()) {
            return;
        }
        mDatabase.delete(TABLE_NAME, "id=?", new String[]{id});
    }
}
