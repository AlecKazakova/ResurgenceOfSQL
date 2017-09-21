package com.example.room.todo.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface TodoListDao {
  @Insert
  fun insert(list: TodoList)

  @Query("" +
      "SELECT list._id, list.name, count(item._id) AS item_count\n" +
      "FROM todo_list AS list\n" +
      "LEFT OUTER JOIN todo_item AS item ON (list._id = item.todo_list_id)\n" +
      "GROUP BY list._id")
  fun selectListItems(): Flowable<List<ListsItem>>
}
