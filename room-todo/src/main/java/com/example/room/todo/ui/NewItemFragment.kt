package com.example.room.todo.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.EditText
import com.example.room.todo.R
import com.example.room.todo.TodoApp
import com.example.room.todo.db.TodoItem
import com.example.room.todo.db.TodoItemDao
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class NewItemFragment : DialogFragment() {

  @Inject internal lateinit var itemDao: TodoItemDao

  private val createClicked = PublishSubject.create<String>()

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
          itemDao.insertItem(TodoItem(0, listId, description))
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
