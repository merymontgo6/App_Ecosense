package com.example.app_ecosense.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlantasViewModel : ViewModel() {
    private val _dataUpdateEvent = MutableLiveData<UpdateType>()
    val dataUpdateEvent: LiveData<UpdateType> = _dataUpdateEvent

    enum class UpdateType {
        PLANTA_ACTUALIZADA,
        ZONA_ACTUALIZADA,
        TODOS_LOS_DATOS
    }

    fun notifyDataUpdated(type: UpdateType = UpdateType.TODOS_LOS_DATOS) {
        _dataUpdateEvent.value = type
    }
}