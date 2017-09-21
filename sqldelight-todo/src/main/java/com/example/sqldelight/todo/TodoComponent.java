package com.example.sqldelight.todo;

import com.example.sqldelight.todo.ui.ItemsFragment;
import com.example.sqldelight.todo.ui.ListsFragment;
import com.example.sqldelight.todo.ui.NewItemFragment;
import com.example.sqldelight.todo.ui.NewListFragment;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = TodoModule.class)
public interface TodoComponent {

  void inject(ListsFragment fragment);

  void inject(ItemsFragment fragment);

  void inject(NewItemFragment fragment);

  void inject(NewListFragment fragment);
}
