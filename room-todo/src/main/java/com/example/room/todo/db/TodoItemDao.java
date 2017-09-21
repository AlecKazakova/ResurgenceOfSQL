package com.example.room.todo.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import io.reactivex.Flowable;
import java.util.List;

@Dao
public interface TodoItemDao {
  @Insert
  void insertItem(TodoItem item);

  @Query("UPDATE todo_item SET complete = :complete WHERE _id = :id")
  void setComplete(boolean complete, long id);

  @Query("SELECT * FROM todo_item WHERE todo_list_id = :todoListId")
  Flowable<List<TodoItem>> selectForList(long todoListId);

  @Query("" +
      "SELECT name, count(*) AS count\n" +
      "FROM todo_list\n" +
      "LEFT JOIN todo_item ON (todo_list._id = todo_list_id)\n" +
      "WHERE todo_list._id = :todoListId AND complete = 0\n" +
      "GROUP BY todo_list._id"
  )
  Flowable<TitleAndCount> titleAndCount(long todoListId);
}