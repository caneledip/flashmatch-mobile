package com.flashmatch.mobile

import android.content.SharedPreferences

class FakeSharedPreferences(
    private val map: MutableMap<String, Any> = mutableMapOf()
) : SharedPreferences {

    override fun getBoolean(key: String, defValue: Boolean) = map[key] as? Boolean ?: defValue
    override fun edit(): SharedPreferences.Editor = FakeEditor(map)
    override fun getAll(): Map<String, Any> = map.toMap()
    override fun getString(key: String?, defValue: String?) = defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues
    override fun getInt(key: String?, defValue: Int) = defValue
    override fun getLong(key: String?, defValue: Long) = defValue
    override fun getFloat(key: String?, defValue: Float) = defValue
    override fun contains(key: String?) = map.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
}

class FakeEditor(private val map: MutableMap<String, Any>) : SharedPreferences.Editor {
    override fun putBoolean(key: String, value: Boolean) = apply { map[key] = value }
    override fun putString(key: String?, value: String?) = this
    override fun putStringSet(key: String?, values: MutableSet<String>?) = this
    override fun putInt(key: String?, value: Int) = this
    override fun putLong(key: String?, value: Long) = this
    override fun putFloat(key: String?, value: Float) = this
    override fun remove(key: String?) = this
    override fun clear() = this
    override fun commit() = true
    override fun apply() {}
}
