package com.example.room.todo.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(
    tableName = "todo_item",
    foreignKeys = arrayOf(ForeignKey(
        entity = TodoItem::class,
        parentColumns = arrayOf("_id"),
        childColumns = arrayOf("todo_list_id")
    ))
)
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long,
    @ColumnInfo(name = "todo_list_id", index = true)
    val todoListId: Long,
    val description: String,
    val complete: Boolean = false
)
