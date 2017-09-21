package com.example.sqldelight.todo.util

import com.squareup.sqlbrite2.BriteDatabase
import com.squareup.sqldelight.SqlDelightCompiledStatement
import com.squareup.sqldelight.SqlDelightStatement

fun BriteDatabase.createQuery(query: SqlDelightStatement) = createQuery(query.tables, query.statement, *query.args)

inline fun <T: SqlDelightCompiledStatement> BriteDatabase.bindAndExecute(compiledStatement: T, bind: T.() -> Unit): Long {
  synchronized(compiledStatement) {
    compiledStatement.bind()
    return when (compiledStatement) {
      is SqlDelightCompiledStatement.Insert -> {
        executeInsert(compiledStatement.table, compiledStatement.program)
      }
      is SqlDelightCompiledStatement.Update -> {
        executeUpdateDelete(compiledStatement.table, compiledStatement.program).toLong()
      }
      is SqlDelightCompiledStatement.Delete -> {
        executeUpdateDelete(compiledStatement.table, compiledStatement.program).toLong()
      }
      else -> throw IllegalStateException("Call execute() on non-mutating compiled statements.")
    }
  }
}