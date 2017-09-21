package com.example.sqldelight.todo.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.EditText
import com.example.sqlbrite.todo.db.TodoListModel.InsertList
import com.example.sqldelight.todo.R
import com.example.sqldelight.todo.TodoApp
import com.example.sqldelight.todo.util.bindAndExecute
import com.jakewharton.rxbinding2.widget.RxTextView
import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class NewListFragment : DialogFragment() {

  @Inject lateinit var db: BriteDatabase

  private val createClicked = PublishSubject.create<String>()
  private val insertList: InsertList by lazy {
    InsertList(db.writableDatabase)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    TodoApp.getComponent(activity).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = activity
    val view = LayoutInflater.from(context).inflate(R.layout.new_list, null)

    val name = view.findViewById<EditText>(android.R.id.input)
    Observable.combineLatest(createClicked, RxTextView.textChanges(name),
        BiFunction<String, CharSequence, String> { _, text -> text.toString() }) //
        .observeOn(Schedulers.io())
        .subscribe { name -> db.bindAndExecute(insertList) { bind(name) } }

    return AlertDialog.Builder(context) //
        .setTitle(R.string.new_list)
        .setView(view)
        .setPositiveButton(R.string.create) { dialog, which -> createClicked.onNext("clicked") }
        .setNegativeButton(R.string.cancel) { dialog, which -> }
        .create()
  }

  companion object {
    fun newInstance(): NewListFragment {
      return NewListFragment()
    }
  }
}
