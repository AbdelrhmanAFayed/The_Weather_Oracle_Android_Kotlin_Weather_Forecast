package com.example.theweatheroracle.model.network

import kotlinx.coroutines.flow.Flow

interface INetworkObserver {

    fun observe() : Flow<Status>

    enum class Status{
        Available , Unavailable , Losing , Lost
    }
}