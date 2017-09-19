package com.alecstrong.resurgence.of.sql.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.alecstrong.resurgence.of.sql.CheckinModel
import com.alecstrong.resurgence.of.sql.FriendshipModel
import com.alecstrong.resurgence.of.sql.UserCheckinModel
import com.alecstrong.resurgence.of.sql.UserModel
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyDatabase(
    context: Context,
    name: String?,
    private val num_users: Long,
    private val num_checkins: Long
): SQLiteOpenHelper(context, name, null, 1) {
  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(CheckinModel.CREATE_TABLE)
    db.execSQL(FriendshipModel.CREATE_TABLE)
    db.execSQL(UserModel.CREATE_TABLE)
    db.execSQL(UserCheckinModel.CREATE_TABLE)

    val insertUser = UserModel.InsertUser(db)

    for (i in 1..num_users) {
      insertUser.bind("user$i")
      insertUser.program.executeInsert()
    }

    val insertFriendship = FriendshipModel.InsertFriendship(db)

    // A user is friends with all its factors.
    for (i in 1L..num_users) {
      for (friend in (2..i/2).filter { i % it == 0L }) {
        insertFriendship.bind(i, friend)
        insertFriendship.program.executeInsert()
      }
    }

    val date = Calendar.getInstance()
    val insertCheckin = CheckinModel.InsertCheckin(db, Checkin.FACTORY)

    for (i in 1..num_checkins) {
      insertCheckin.bind("checkin$i", date)
      insertCheckin.program.executeInsert()
      date.timeInMillis -= TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
    }

    val insertUserCheckin = UserCheckinModel.InsertUserCheckin(db)

    // Each user whose number is a subsequence of checkin number checked in for that checkin.
    for (checkin in 1..num_checkins) {
      val textValue = checkin.toString()
      for (user in (1..num_users).filter { it.toString() in textValue }) {
        insertUserCheckin.bind(checkin, user)
        insertUserCheckin.program.executeInsert()
      }
    }
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
