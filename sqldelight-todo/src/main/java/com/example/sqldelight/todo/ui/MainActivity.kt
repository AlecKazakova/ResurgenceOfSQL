package com.example.sqldelight.todo.ui

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.example.sqldelight.todo.R

class MainActivity : FragmentActivity(), ListsFragment.Listener, ItemsFragment.Listener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(android.R.id.content, ListsFragment.newInstance())
          .commit()
    }
  }

  override fun onListClicked(id: Long) {
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
            R.anim.slide_out_right)
        .replace(android.R.id.content, ItemsFragment.newInstance(id))
        .addToBackStack(null)
        .commit()
  }

  override fun onNewListClicked() {
    NewListFragment.newInstance().show(supportFragmentManager, "new-list")
  }

  override fun onNewItemClicked(listId: Long) {
    NewItemFragment.newInstance(listId).show(supportFragmentManager, "new-item")
  }
}
