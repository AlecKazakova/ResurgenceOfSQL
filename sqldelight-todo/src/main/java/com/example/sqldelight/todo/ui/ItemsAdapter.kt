package com.example.sqldelight.todo.ui

import android.content.Context
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView
import com.example.sqldelight.todo.db.TodoItem
import io.reactivex.functions.Consumer

internal class ItemsAdapter(context: Context) : BaseAdapter(), Consumer<List<TodoItem>> {
  private val inflater: LayoutInflater = LayoutInflater.from(context)

  private var items = emptyList<TodoItem>()

  override fun accept(items: List<TodoItem>) {
    this.items = items
    notifyDataSetChanged()
  }

  override fun getCount(): Int {
    return items.size
  }

  override fun getItem(position: Int): TodoItem {
    return items[position]
  }

  override fun getItemId(position: Int): Long {
    return getItem(position)._id()
  }

  override fun hasStableIds(): Boolean {
    return true
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val convertView = convertView ?: inflater.inflate(
        android.R.layout.simple_list_item_multiple_choice, parent, false)

    val item = getItem(position)
    val textView = convertView as CheckedTextView?
    textView!!.isChecked = item.complete()

    var description: CharSequence = item.description()
    if (item.complete()) {
      val spannable = SpannableString(description)
      spannable.setSpan(StrikethroughSpan(), 0, description.length, 0)
      description = spannable
    }

    textView.text = description

    return convertView
  }
}
