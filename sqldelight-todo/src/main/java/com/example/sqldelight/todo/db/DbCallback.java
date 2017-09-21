package com.example.sqldelight.todo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class DbCallback extends SQLiteOpenHelper {
  static final int VERSION = 1;

  public DbCallback(Context context) {
    super(context, "sample.db", null, VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(TodoList.CREATE_TABLE);
    db.execSQL(TodoItem.CREATE_TABLE);
    db.execSQL(TodoItem.CREATELISTIDINDEX);

    db.execSQL(TodoList.SEEDDATA);
    db.execSQL(TodoItem.SEEDDATA);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }
}
