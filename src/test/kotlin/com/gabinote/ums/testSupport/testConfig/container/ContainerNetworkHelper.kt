package com.gabinote.ums.testSupport.testConfig.container

import org.testcontainers.containers.Network
import java.net.ServerSocket

object ContainerNetworkHelper {
    @JvmStatic
    val testNetwork: Network = Network.newNetwork()

    fun getAvailablePort(): Int {
        return ServerSocket(0).use { socket ->
            socket.localPort
        }
    }
}