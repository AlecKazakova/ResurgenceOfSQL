package com.example.room.todo;

import com.example.room.todo.ui.ItemsFragment;
import com.example.room.todo.ui.ListsFragment;
import com.example.room.todo.ui.NewItemFragment;
import com.example.room.todo.ui.NewListFragment;
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
