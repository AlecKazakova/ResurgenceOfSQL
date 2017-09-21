package com.example.room.todo.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
import android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.example.room.todo.R
import com.example.room.todo.TodoApp
import com.example.room.todo.db.TodoItemDao
import com.example.room.todo.util.bindView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ItemsFragment : Fragment() {
  interface Listener {
    fun onNewItemClicked(listId: Long)
  }

  @Inject internal lateinit var itemDao: TodoItemDao

  private lateinit var disposables: CompositeDisposable

  private val listView: ListView by bindView(android.R.id.list)
  private val emptyView: View by bindView(android.R.id.empty)

  private var listener: Listener? = null
  private var adapter: ItemsAdapter? = null

  private val listId: Long
    get() = arguments.getLong(KEY_LIST_ID)

  override fun onAttach(activity: Context) {
    super.onAttach(context)
    if (activity !is Listener) {
      throw IllegalStateException("Activity must implement fragment Listener.")
    }

    super.onAttach(activity)
    TodoApp.getComponent(activity).inject(this)
    setHasOptionsMenu(true)

    listener = activity
    adapter = ItemsAdapter(activity)
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)

    val item = menu!!.add(R.string.new_item)
        .setOnMenuItemClickListener {
          listener!!.onNewItemClicked(listId)
          true
        }
    MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM or SHOW_AS_ACTION_WITH_TEXT)
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.items, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    listView.emptyView = emptyView
    listView.adapter = adapter

    RxAdapterView.itemClickEvents<ListAdapter>(listView) //
        .observeOn(Schedulers.io())
        .subscribe { event ->
          val newValue = !adapter!!.getItem(event.position()).complete
          itemDao.setComplete(newValue, event.id())
        }
  }

  override fun onResume() {
    super.onResume()
    disposables = CompositeDisposable()

    itemDao.titleAndCount(listId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { titleAndCount ->
          TODO()
        }

    disposables.add(itemDao.selectForList(listId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(adapter))
  }

  override fun onPause() {
    super.onPause()
    disposables.dispose()
  }

  companion object {
    private val KEY_LIST_ID = "list_id"

    fun newInstance(listId: Long): ItemsFragment {
      val arguments = Bundle()
      arguments.putLong(KEY_LIST_ID, listId)

      val fragment = ItemsFragment()
      fragment.arguments = arguments
      return fragment
    }
  }
}
