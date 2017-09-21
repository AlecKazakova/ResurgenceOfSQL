package com.example.room.todo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.room.todo.db.ListsItem
import io.reactivex.functions.Consumer

internal class ListsAdapter(context: Context) : BaseAdapter(), Consumer<List<ListsItem>> {
  private val inflater: LayoutInflater = LayoutInflater.from(context)

  private var items = emptyList<ListsItem>()

  override fun accept(items: List<ListsItem>) {
    this.items = items
    notifyDataSetChanged()
  }

  override fun getCount(): Int {
    return items.size
  }

  override fun getItem(position: Int): ListsItem {
    return items[position]
  }

  override fun getItemId(position: Int): Long {
    return getItem(position)._id
  }

  override fun hasStableIds(): Boolean {
    return true
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val convertView = convertView ?: inflater.inflate(
        android.R.layout.simple_list_item_1, parent, false)

    val item = getItem(position)
    (convertView as TextView).text = item.name + " (" + item.item_count + ")"

    return convertView
  }
}
