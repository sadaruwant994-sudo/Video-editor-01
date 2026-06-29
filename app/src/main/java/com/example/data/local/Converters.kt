package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ScenePlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val scenesAdapter = moshi.adapter<List<ScenePlan>>(
        Types.newParameterizedType(List::class.java, ScenePlan::class.java)
    )
    private val stringListAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun fromSceneList(scenes: List<ScenePlan>?): String? {
        return scenes?.let { scenesAdapter.toJson(it) }
    }

    @TypeConverter
    fun toSceneList(json: String?): List<ScenePlan>? {
        return json?.let { scenesAdapter.fromJson(it) } ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { stringListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        return json?.let { stringListAdapter.fromJson(it) } ?: emptyList()
    }
}
