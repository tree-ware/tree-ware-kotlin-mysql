package org.treeWare.mySql.test

import java.net.ServerSocket

fun getAvailableServerPort(): Int {
    val socket = ServerSocket(0)
    val port = socket.localPort
    socket.close()
    return port
}