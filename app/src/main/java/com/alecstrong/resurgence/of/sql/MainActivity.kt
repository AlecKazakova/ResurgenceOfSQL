package com.alecstrong.resurgence.of.sql

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.alecstrong.resurgence.of.sql.data.MyDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
  private val MY_ID = 2L

  private lateinit var valueText: TextView
  private lateinit var informationText: TextView
  private lateinit var computeButton: Button
  private lateinit var loading: View
  private lateinit var usersEditText: EditText
  private lateinit var checkinsEditText: EditText

  private var calculator: (MyDatabase) -> Long = QueryRunner::joins

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    valueText = findViewById(R.id.value)
    valueText.text = "_"

    informationText = findViewById(R.id.information)
    informationText.text = "press compute to calculate number of checkins with friends"

    computeButton = findViewById(R.id.compute)
    computeButton.setOnClickListener {
      computeValue()
    }

    loading = findViewById(R.id.loading)
    usersEditText = findViewById(R.id.num_users)
    checkinsEditText = findViewById(R.id.num_checkins)

    findViewById<View>(R.id.in_memory).setOnClickListener {
      calculator = QueryRunner::inMemory
    }
    findViewById<View>(R.id.in_memory_with_friends).setOnClickListener {
      calculator = QueryRunner::inMemoryFriends
    }
    findViewById<View>(R.id.double_sunquery).setOnClickListener {
      calculator = QueryRunner::doubleSubquery
    }
    findViewById<View>(R.id.distinct).setOnClickListener {
      calculator = QueryRunner::distinct
    }
    findViewById<View>(R.id.joins).setOnClickListener {
      calculator = QueryRunner::joins
    }
    findViewById<View>(R.id.checkins).setOnClickListener {
      calculator = QueryRunner::checkin
    }
  }

  private fun computeValue() {
    Observable
        .fromCallable {
          val database = MyDatabase(this, "mydb.db", usersEditText.text.toString().toLong(),
              checkinsEditText.text.toString().toLong())

          var value = 0L
          val timeToEvaluate = measureTimeMillis {
            value = calculator(database)
          }

          database.close()

          return@fromCallable value to timeToEvaluate
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe {
          loading.visibility = View.VISIBLE
          valueText.visibility = View.INVISIBLE
        }
        .subscribe { (value, timeToEvaluate) ->
          loading.visibility = View.INVISIBLE
          valueText.visibility = View.VISIBLE
          valueText.text = "$value"
          informationText.text = "Computed value in $timeToEvaluate milliseconds"
        }
  }

}
