package com.example.nesafood

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PesananStorage {

    private const val PREF_NAME = "StatusPesananPref"
    private const val KEY_PESANAN = "list_pesanan"

    fun save(context: Context, list: List<Pesanan>) {
        val jsonArray = JSONArray()
        for (p in list) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("nama", p.namaPembeli)
            obj.put("items", p.items)
            obj.put("total", p.total)
            obj.put("status", p.status)
            jsonArray.put(obj)
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PESANAN, jsonArray.toString())
            .apply()
    }

    fun load(context: Context): MutableList<Pesanan> {
        val list = mutableListOf<Pesanan>()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PESANAN, null) ?: return list

        val jsonArray = JSONArray(jsonStr)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Pesanan(
                    obj.getString("id"),
                    obj.getString("nama"),
                    obj.getString("items"),
                    obj.getInt("total"),
                    obj.getString("status")
                )
            )
        }
        return list
    }
}
