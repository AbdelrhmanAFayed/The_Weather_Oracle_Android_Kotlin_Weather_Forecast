package com.example.theweatheroracle.nav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _refreshFavorites = MutableLiveData<Unit>()
    val refreshFavorites: LiveData<Unit> get() = _refreshFavorites

    fun triggerRefreshFavorites() {
        _refreshFavorites.postValue(Unit)
    }
}