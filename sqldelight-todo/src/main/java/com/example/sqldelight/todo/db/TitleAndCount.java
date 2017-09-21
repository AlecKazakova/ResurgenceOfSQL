package com.example.sqldelight.todo.db;

import com.example.sqlbrite.todo.db.TodoItemModel.TitleAndCountCreator;
import com.example.sqlbrite.todo.db.TodoItemModel.TitleAndCountModel;
import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.RowMapper;

@AutoValue
public abstract class TitleAndCount implements TitleAndCountModel {
  public static final TitleAndCountCreator CREATOR
      = AutoValue_TitleAndCount::new;
  public static final RowMapper<TodoItem> MAPPER =
      TodoItem.FACTORY.titleAndCountMapper(CREATOR);
}
