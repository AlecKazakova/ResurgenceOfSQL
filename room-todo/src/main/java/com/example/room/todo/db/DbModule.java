package com.example.room.todo.db;

import android.app.Application;
import android.arch.persistence.room.Room;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public final class DbModule {
  @Provides @Singleton AppDatabase provideDatabase(Application application) {
    return Room.databaseBuilder(application, AppDatabase.class, "mydb.db")
        .addCallback(Seeder.INSTANCE)
        .build();
  }

  @Provides @Singleton TodoItemDao provideTodoItemDao(AppDatabase database) {
    return database.todoItemDao();
  }

  @Provides @Singleton TodoListDao provideTodoListDao(AppDatabase database) {
    return database.todoListDao();
  }
}
