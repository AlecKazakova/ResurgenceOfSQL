package com.example.sqldelight.todo.db;

import android.os.Parcelable;
import com.example.sqlbrite.todo.db.TodoItemModel;
import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.RowMapper;

@AutoValue
public abstract class TodoItem implements TodoItemModel, Parcelable {
  public static final Factory<TodoItem> FACTORY =
      new Factory<>(AutoValue_TodoItem::new);
}
