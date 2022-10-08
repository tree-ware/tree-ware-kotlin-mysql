package org.treeWare.mySql

import org.treeWare.mySql.test.MySqlTestContainer
import javax.sql.DataSource

internal val testDataSource: DataSource = MySqlTestContainer.getDataSource()