package com.example.sqldelight.todo.ui;

import com.example.sqlbrite.todo.db.TodoListModel.SelectListItemsCreator;
import com.example.sqlbrite.todo.db.TodoListModel.SelectListItemsModel;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ListsItem implements SelectListItemsModel {
  public static final SelectListItemsCreator<ListsItem> CREATOR =
      AutoValue_ListsItem::new;
}
