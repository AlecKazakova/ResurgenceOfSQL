package com.alecstrong.resurgence.of.sql.data

import com.alecstrong.resurgence.of.sql.CheckinModel
import com.alecstrong.resurgence.of.sql.FriendshipModel
import com.alecstrong.resurgence.of.sql.UserCheckinModel
import com.alecstrong.resurgence.of.sql.UserModel
import com.squareup.sqldelight.ColumnAdapter
import java.util.Calendar

private object TIME_ADAPTER : ColumnAdapter<Calendar, Long> {
  override fun decode(databaseValue: Long): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = databaseValue
    return calendar
  }

  override fun encode(value: Calendar) = value.timeInMillis
}

data class Checkin(val _id: Long, val name: String, val time: Calendar): CheckinModel {
  override fun _id() = _id
  override fun name() = name
  override fun time() = time

  companion object {
    val FACTORY = CheckinModel.Factory<Checkin>(::Checkin, TIME_ADAPTER)
  }
}

data class Friendship(val friend1: Long, val friend2: Long, val became_friends: Long): FriendshipModel {
  override fun friend1() = friend1
  override fun friend2() = friend2
  override fun became_friends() = became_friends

  companion object {
    val FACTORY = FriendshipModel.Factory<Friendship>(::Friendship)
  }
}

data class User(val _id: Long, val name: String): UserModel {
  override fun _id() = _id
  override fun name() = name

  companion object {
    val FACTORY = UserModel.Factory<User>(::User)
  }
}

data class UserCheckin(val checkin_id: Long, val user_id: Long): UserCheckinModel {
  override fun checkin_id() = checkin_id
  override fun user_id() = user_id

  companion object {
    val FACTORY = UserCheckinModel.Factory<UserCheckin>(::UserCheckin)
  }
}
