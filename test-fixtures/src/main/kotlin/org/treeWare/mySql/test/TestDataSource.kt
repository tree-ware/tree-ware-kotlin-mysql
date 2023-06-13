package org.treeWare.mySql.test

import javax.sql.DataSource

val testDataSource: DataSource = MySqlTestContainer.getDataSource()