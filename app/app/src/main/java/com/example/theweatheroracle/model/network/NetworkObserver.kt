package com.example.theweatheroracle.model.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class NetworkObserver(private val context: Context) : INetworkObserver {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<INetworkObserver.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    launch { send(INetworkObserver.Status.Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    launch { send(INetworkObserver.Status.Losing) }
                }

                override fun onLost(network: Network) {
                    launch { send(INetworkObserver.Status.Lost) }
                }

                override fun onUnavailable() {
                    launch { send(INetworkObserver.Status.Unavailable) }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

    override fun isOnline(): Boolean {
        return connectivityManager.activeNetwork != null
    }
}