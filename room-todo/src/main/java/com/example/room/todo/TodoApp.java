package com.example.room.todo;

import android.app.Application;
import android.content.Context;
import timber.log.Timber;

public final class TodoApp extends Application {
  private TodoComponent mainComponent;

  @Override public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    mainComponent = DaggerTodoComponent.builder().todoModule(new TodoModule(this)).build();
  }

  public static TodoComponent getComponent(Context context) {
    return ((TodoApp) context.getApplicationContext()).mainComponent;
  }
}
