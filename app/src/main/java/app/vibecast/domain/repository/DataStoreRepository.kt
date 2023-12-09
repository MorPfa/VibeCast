package app.vibecast.domain.repository

import app.vibecast.data.data_repository.repository.Unit

interface DataStoreRepository {
    suspend fun putUnit(unit: Unit)
    suspend fun getUnit(): Unit?
    suspend fun clearUnit()
}