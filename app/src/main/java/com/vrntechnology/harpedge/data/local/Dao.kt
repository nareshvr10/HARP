package com.vrntechnology.harpedge.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HarpDao {

    // Nodes
    @Query("SELECT * FROM cached_nodes ORDER BY nodeId ASC")
    fun getAllCachedNodes(): Flow<List<CachedNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<CachedNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: CachedNodeEntity)

    // Sensor Readings
    @Query("SELECT * FROM cached_readings WHERE nodeId = :nodeId ORDER BY timestamp DESC LIMIT 20")
    fun getReadingsForNode(nodeId: String): Flow<List<CachedReadingEntity>>

    @Query("SELECT * FROM cached_readings ORDER BY timestamp DESC LIMIT 50")
    fun getAllLatestReadings(): Flow<List<CachedReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<CachedReadingEntity>)

    // Events
    @Query("SELECT * FROM cached_events ORDER BY lastUpdated DESC")
    fun getAllCachedEvents(): Flow<List<CachedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CachedEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CachedEventEntity)

    // Alerts
    @Query("SELECT * FROM cached_alerts ORDER BY createdAt DESC")
    fun getAllCachedAlerts(): Flow<List<CachedAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<CachedAlertEntity>)

    // Field Inspections (Offline sync queue)
    @Query("SELECT * FROM pending_inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<CachedInspectionEntity>>

    @Query("SELECT * FROM pending_inspections WHERE isSynced = 0")
    suspend fun getUnsyncedInspections(): List<CachedInspectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: CachedInspectionEntity)

    @Update
    suspend fun updateInspection(inspection: CachedInspectionEntity)

    @Query("DELETE FROM pending_inspections WHERE inspectionId = :id")
    suspend fun deleteInspection(id: String)
}
