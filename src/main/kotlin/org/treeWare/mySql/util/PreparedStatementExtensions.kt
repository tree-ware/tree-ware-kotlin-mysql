package org.treeWare.mySql

import java.sql.PreparedStatement

fun PreparedStatement.getBoundSql(): String = this.toString().split(": ").drop(1).joinToString(": ")