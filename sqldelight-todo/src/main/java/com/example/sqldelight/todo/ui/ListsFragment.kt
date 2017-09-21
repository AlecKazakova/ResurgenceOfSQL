/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.sqldelight.todo.ui

import android.app.Activity
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
import android.widget.AdapterView
import android.widget.ListView
import com.example.sqldelight.todo.R
import com.example.sqldelight.todo.TodoApp
import com.example.sqldelight.todo.db.TodoList
import com.example.sqldelight.todo.util.createQuery
import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ListsFragment : Fragment() {
  internal interface Listener {
    fun onListClicked(id: Long)
    fun onNewListClicked()
  }

  @Inject lateinit var db: BriteDatabase

  internal lateinit var listView: ListView
  internal lateinit var emptyView: View

  private var listener: Listener? = null
  private var adapter: ListsAdapter? = null
  private var disposable: Disposable? = null

  override fun onAttach(activity: Activity?) {
    if (activity !is Listener) {
      throw IllegalStateException("Activity must implement fragment Listener.")
    }

    super.onAttach(activity)
    TodoApp.getComponent(activity).inject(this)
    setHasOptionsMenu(true)

    listener = activity
    adapter = ListsAdapter(activity)
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)

    val item = menu!!.add(R.string.new_list)
        .setOnMenuItemClickListener {
          listener!!.onNewListClicked()
          true
        }
    MenuItemCompat.setShowAsAction(item, SHOW_AS_ACTION_IF_ROOM or SHOW_AS_ACTION_WITH_TEXT)
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.lists, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    listView = view!!.findViewById(android.R.id.list)
    emptyView = view.findViewById(android.R.id.empty)
    listView.emptyView = emptyView
    listView.adapter = adapter
    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
      listener!!.onListClicked(id)
    }
  }

  override fun onResume() {
    super.onResume()

    activity.title = "To-Do"

    disposable = db.createQuery(TodoList.FACTORY.selectListItems())
        .mapToList<ListsItem>(TodoList.FACTORY.selectListItemsMapper(ListsItem.CREATOR)::map)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(adapter!!)
  }

  override fun onPause() {
    super.onPause()
    disposable!!.dispose()
  }

  companion object {

    internal fun newInstance(): ListsFragment {
      return ListsFragment()
    }
  }
}