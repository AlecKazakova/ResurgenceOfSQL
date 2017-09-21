package com.example.sqldelight.todo.db;

import android.os.Parcelable;
import com.example.sqlbrite.todo.db.TodoListModel;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TodoList implements Parcelable, TodoListModel {
  public static final Factory<TodoList> FACTORY =
      new TodoListModel.Factory<>(AutoValue_TodoList::new);
}
