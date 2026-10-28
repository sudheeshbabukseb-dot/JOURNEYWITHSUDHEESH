package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    @Query("SELECT * FROM field_verifications ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<VerificationRecord>>

    @Query("SELECT * FROM field_verifications WHERE id = :id")
    suspend fun getRecordById(id: Long): VerificationRecord?

    @Query("SELECT * FROM field_verifications WHERE applicantName LIKE '%' || :query || '%' OR applicationNo LIKE '%' || :query || '%' OR nearestConsumerNo LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchRecords(query: String): Flow<List<VerificationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VerificationRecord): Long

    @Update
    suspend fun updateRecord(record: VerificationRecord)

    @Delete
    suspend fun deleteRecord(record: VerificationRecord)

    @Query("SELECT COUNT(*) FROM field_verifications")
    suspend fun getRecordCount(): Int
}
