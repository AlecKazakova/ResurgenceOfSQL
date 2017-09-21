package com.alecstrong.resurgence.of.sql

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.alecstrong.resurgence.of.sql.data.Friendship
import com.alecstrong.resurgence.of.sql.data.MyDatabase
import com.alecstrong.resurgence.of.sql.data.User
import com.alecstrong.resurgence.of.sql.data.UserCheckin
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class QueryLogger {
  private val timesToRun = 10

  private val values = mapOf(
      (10L to 10L) to 4L,
      (10L to 100L) to 53L,
      (10L to 1000L) to 672L,
      (10L to 10000L) to 7746L,
      (100L to 10L) to 4L,
      (100L to 100L) to 64L,
      (100L to 1000L) to 814L,
      (100L to 10000L) to 9064L,
      (1000L to 10L) to 4L,
      (1000L to 100L) to 64L,
      (1000L to 1000L) to 814L,
      (1000L to 10000L) to 9064L,
      (10000L to 10L) to 4L,
      (10000L to 100L) to 64L,
      (10000L to 1000L) to 814L,
      (10000L to 10000L) to 9064L
  )

  @Test
  fun testQueries() {
    for (numUsers in listOf(10L, 100L, 1000L, 10000L)) {
      for (numCheckins in listOf(10L, 100L, 1000L, 10000L)) {
        val database = MyDatabase(InstrumentationRegistry.getContext(), null, numUsers, numCheckins)
        database.writableDatabase

        fun logTime(queryType: String, block: (MyDatabase) -> Long) {
          var time = 0L
          for (i in 0..timesToRun) {
            time += measureTimeMillis {
              assert(block(database) == values[numUsers to numCheckins])
            }
          }
          time /= timesToRun

          Log.i("QueryRunner",
              "users: $numUsers checkins: $numCheckins $queryType finished in $time")
        }
        logTime("inMemory", QueryRunner::inMemory)
        logTime("inMemoryFriends", QueryRunner::inMemoryFriends)
        logTime("doubleSubquery", QueryRunner::doubleSubquery)
        logTime("distinct", QueryRunner::distinct)
        logTime("joins", QueryRunner::joins)
        logTime("checkins", QueryRunner::checkin)
      }
    }
  }

  @Test
  fun testWithIndexes() {
    val numUsers = 10000L
    for (numCheckins in listOf(10L, 100L, 1000L, 10000L, 20000L, 30000L, 50000L)) {
      val database = MyDatabase(InstrumentationRegistry.getContext(), null, numUsers, numCheckins)
      database.writableDatabase.execSQL(FriendshipModel.FRIEND2INDEX)
      database.writableDatabase.execSQL(UserCheckinModel.CREATEINDEX)

      var time = 0L
      for (i in 0..timesToRun) {
        time += measureTimeMillis {
          QueryRunner.indexed(database)
        }
      }
      time /= timesToRun

      Log.i("QueryRunner",
          "users: $numUsers checkins: $numCheckins indexed finished in $time")
    }
  }

  @Test
  fun testInMemoryDontMeasureDb() {
    val numUsers = 10000L
    for (numCheckins in listOf(10L, 100L, 1000L, 10000L, 20000L, 30000L, 50000L)) {
      val database = MyDatabase(InstrumentationRegistry.getContext(), null, numUsers, numCheckins)

      var time = 0L
      for (i in 0..timesToRun) {
        time += inMemoryDontMeasureDb(database)
      }
      time /= timesToRun

      Log.i("QueryRunner",
          "users: $numUsers checkins: $numCheckins inMemoryDontMeasureDb finished in $time")
    }
  }

  fun inMemoryDontMeasureDb(database: MyDatabase): Long {
    var time = 0L
    val myFriendIds = ArrayList<Long>()

    database.readableDatabase.rawQuery(Friendship.FACTORY.selectAll().statement,
        emptyArray()).use {
      while (it.moveToNext()) {
        val friendship = Friendship.FACTORY.selectAllMapper().map(it)
        time += measureTimeMillis {
          if (friendship.friend1 == QueryRunner.MY_ID) myFriendIds.add(friendship.friend2)
          if (friendship.friend2 == QueryRunner.MY_ID) myFriendIds.add(friendship.friend1)
        }
      }
    }

    val checkinsWithFriends = LinkedHashSet<Long>()
    database.readableDatabase.rawQuery(UserCheckin.FACTORY.selectAll().statement,
        emptyArray()).use {
      while (it.moveToNext()) {
        time += measureTimeMillis {
          val userCheckin = UserCheckin.FACTORY.selectAllMapper().map(it)
          if (userCheckin.user_id in myFriendIds) checkinsWithFriends.add(userCheckin.checkin_id)
        }
      }
    }

    return time
  }
}
