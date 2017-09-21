package com.example.room.todo.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL



@Database(entities = arrayOf(TodoItem::class, TodoList::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun todoListDao(): TodoListDao
  abstract fun todoItemDao(): TodoItemDao
}

object Seeder : RoomDatabase.Callback() {
  override fun onCreate(db: SupportSQLiteDatabase) {
    val groceryListId = db.insertList("Grocery List")
    db.insertItem(groceryListId, "Beer")
    db.insertItem(groceryListId, "Point Break on DVD")
    db.insertItem(groceryListId, "Bad Boys 2 on DVD")

    val holidayPresentsListId = db.insertList("Holiday Presents")
    db.insertItem(holidayPresentsListId, "Pogo Stick for Jake W.")
    db.insertItem(holidayPresentsListId, "Jack-in-the-box for Alec S.")
    db.insertItem(holidayPresentsListId, "Pogs for Matt P.")
    db.insertItem(holidayPresentsListId, "Cola for Jesse W.")

    val workListId = db.insertList("Work Items")
    db.insertItem(workListId, "Finish SqlBrite library")
    db.insertItem(workListId, "Finish SqlBrite sample app")
    db.insertItem(workListId, "Publish SqlBrite to GitHub")

    val birthdayPresentsListId = db.insertList("Birthday Presents")
    db.insertItem(birthdayPresentsListId, "New car")
  }

  private fun SupportSQLiteDatabase.insertList(name: String): Long {
    return insert("todo_list", CONFLICT_FAIL, ContentValues().apply {
      put("name", name)
      put("archived", 0)
    })
  }

  private fun SupportSQLiteDatabase.insertItem(listId: Long, description: String) {
    execSQL("INSERT INTO todo_item (todo_list_id, description, complete) VALUES ($listId, ?, 0)",
        arrayOf(description))
  }
}