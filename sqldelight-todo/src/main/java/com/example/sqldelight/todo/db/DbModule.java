package com.example.sqldelight.todo.db;

import android.app.Application;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Singleton;
import timber.log.Timber;

@Module
public final class DbModule {
  @Provides @Singleton SqlBrite provideSqlBrite() {
    return new SqlBrite.Builder()
        .logger(message -> Timber.tag("Database").v(message))
        .build();
  }

  @Provides @Singleton BriteDatabase provideDatabase(SqlBrite sqlBrite, Application application) {
    BriteDatabase db = sqlBrite.wrapDatabaseHelper(new DbCallback(application), Schedulers.io());
    db.setLoggingEnabled(true);
    return db;
  }
}
