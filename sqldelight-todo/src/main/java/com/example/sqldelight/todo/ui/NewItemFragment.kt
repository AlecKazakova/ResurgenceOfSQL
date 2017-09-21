package com.example.sqldelight.todo.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.EditText
import com.example.sqlbrite.todo.db.TodoItemModel
import com.example.sqlbrite.todo.db.TodoItemModel.InsertItem
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

class NewItemFragment : DialogFragment() {

  @Inject internal lateinit var db: BriteDatabase

  private val createClicked = PublishSubject.create<String>()
  private val insertItem: TodoItemModel.InsertItem by lazy {
    InsertItem(db.writableDatabase)
  }

  private val listId: Long
    get() = arguments.getLong(KEY_LIST_ID)

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    TodoApp.getComponent(activity).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = activity
    val view = LayoutInflater.from(context).inflate(R.layout.new_item, null)

    val name = view.findViewById<EditText>(android.R.id.input)
    Observable.combineLatest(createClicked, RxTextView.textChanges(name),
        BiFunction<String, CharSequence, String> { _, text -> text.toString() }) //
        .observeOn(Schedulers.io())
        .subscribe { description ->
          db.bindAndExecute(insertItem) { bind(listId, description) }
        }

    return AlertDialog.Builder(context) //
        .setTitle(R.string.new_item)
        .setView(view)
        .setPositiveButton(R.string.create) { _, _ -> createClicked.onNext("clicked") }
        .setNegativeButton(R.string.cancel) { _, _ -> }
        .create()
  }

  companion object {
    private val KEY_LIST_ID = "list_id"

    fun newInstance(listId: Long): NewItemFragment {
      val arguments = Bundle()
      arguments.putLong(KEY_LIST_ID, listId)

      val fragment = NewItemFragment()
      fragment.arguments = arguments
      return fragment
    }
  }
}
