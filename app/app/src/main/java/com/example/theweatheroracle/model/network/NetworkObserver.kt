package com.example.theweatheroracle.model.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkObserver(private val context: Context)
    : INetworkObserver
{
    private val connectiveManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    override fun observe(): Flow<INetworkObserver.Status> {

        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback()
            {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        send(INetworkObserver.Status.Available)
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        send(INetworkObserver.Status.Losing)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        send(INetworkObserver.Status.Lost)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        send(INetworkObserver.Status.Unavailable)
                    }
                }
            }
            connectiveManager.registerDefaultNetworkCallback(callback)
            awaitClose{
                connectiveManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}