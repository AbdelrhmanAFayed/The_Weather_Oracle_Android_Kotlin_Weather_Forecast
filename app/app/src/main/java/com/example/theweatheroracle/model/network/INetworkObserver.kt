package com.example.theweatheroracle.model.network

import kotlinx.coroutines.flow.Flow

interface INetworkObserver {

    fun observe() : Flow<Status>

    fun isOnline(): Boolean


    enum class Status{
        Available , Unavailable , Losing , Lost
    }
}