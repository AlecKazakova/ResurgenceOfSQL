package com.alecstrong.resurgence.of.sql

import com.alecstrong.resurgence.of.sql.data.Checkin
import com.alecstrong.resurgence.of.sql.data.Friendship
import com.alecstrong.resurgence.of.sql.data.MyDatabase
import com.alecstrong.resurgence.of.sql.data.UserCheckin

object QueryRunner {
  val MY_ID = 2L

  fun inMemory(database: MyDatabase): Long {
    val myFriendIds = ArrayList<Long>()

    database.readableDatabase.rawQuery(Friendship.FACTORY.selectAll().statement,
        emptyArray()).use {
      while (it.moveToNext()) {
        val friendship = Friendship.FACTORY.selectAllMapper().map(it)
        if (friendship.friend1 == MY_ID) myFriendIds.add(friendship.friend2)
        if (friendship.friend2 == MY_ID) myFriendIds.add(friendship.friend1)
      }
    }

    val checkinsWithFriends = LinkedHashSet<Long>()
    database.readableDatabase.rawQuery(UserCheckin.FACTORY.selectAll().statement,
        emptyArray()).use {
      while (it.moveToNext()) {
        val userCheckin = UserCheckin.FACTORY.selectAllMapper().map(it)
        if (userCheckin.user_id in myFriendIds) checkinsWithFriends.add(userCheckin.checkin_id)
      }
    }

    return checkinsWithFriends.size.toLong()
  }

  fun inMemoryFriends(database: MyDatabase): Long {
    val myFriendIds = ArrayList<Long>()
    val friendsQuery = Friendship.FACTORY.selectMyFriends(MY_ID)

    database.readableDatabase.rawQuery(friendsQuery.statement, friendsQuery.args).use {
      while (it.moveToNext()) {
        myFriendIds.add(it.getLong(0))
      }
    }

    val checkinsWithFriends = LinkedHashSet<Long>()
    database.readableDatabase.rawQuery(UserCheckin.FACTORY.selectAll().statement,
        emptyArray()).use {
      while (it.moveToNext()) {
        val userCheckin = UserCheckin.FACTORY.selectAllMapper().map(it)
        if (userCheckin.user_id in myFriendIds) checkinsWithFriends.add(userCheckin.checkin_id)
      }
    }

    return checkinsWithFriends.size.toLong()
  }

  fun doubleSubquery(database: MyDatabase): Long {
    val query = Checkin.FACTORY.checkinsWithFriendsDoubleSubquery(MY_ID)

    database.readableDatabase.rawQuery(query.statement, query.args).use {
      it.moveToFirst()
      return it.getLong(0)
    }
  }

  fun distinct(database: MyDatabase): Long {
    val query = Checkin.FACTORY.checkinsWithFriendsDistinctSubquery(MY_ID)

    database.readableDatabase.rawQuery(query.statement, query.args).use {
      it.moveToFirst()
      return it.getLong(0)
    }
  }

  fun joins(database: MyDatabase): Long {
    val query = Checkin.FACTORY.checkinsWithFriendsJoin(MY_ID)

    database.readableDatabase.rawQuery(query.statement, query.args).use {
      it.moveToFirst()
      return it.getLong(0)
    }
  }

  fun checkin(database: MyDatabase): Long {
    val query = Checkin.FACTORY.checkinsWithFriends(MY_ID)

    database.readableDatabase.rawQuery(query.statement, query.args).use {
      it.moveToFirst()
      return it.getLong(0)
    }
  }

  fun indexed(database: MyDatabase): Long {
    val query = Checkin.FACTORY.indexedQuery(MY_ID)

    database.readableDatabase.rawQuery(query.statement, query.args).use {
      it.moveToFirst()
      return it.getLong(0)
    }
  }
}
