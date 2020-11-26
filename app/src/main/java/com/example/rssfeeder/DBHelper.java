package com.example.rssfeeder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "RSSFeeder";
    public static final String RSS_TABLE_NAME = "rss";
    public static final String RSS_COLUMN_ID = "id";
    public static final String RSS_COLUMN_NAME = "name";
    public static final String RSS_COLUMN_LINK = "link";
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table rss " +
                        "(id integer primary key, name text,link text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS rss");
        onCreate(db);
    }

    public boolean insertRss (String name, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("link", link);
        db.insert("rss", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from rss where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, RSS_TABLE_NAME);
        return numRows;
    }

    public boolean updateRss (Integer id, String name, String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("link", link);
        db.update("rss", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteRss (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("rss",
                "name = ? ",
                new String[] { name });
    }

    public ArrayList<String> getAllRss() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select name from rss", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(RSS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public String getLink (String name) {
        String link;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select link from rss where name = ?", new String[] { name });
        res.moveToFirst();
        link = res.getString(res.getColumnIndex(RSS_COLUMN_LINK));
        return link;
    }
}
