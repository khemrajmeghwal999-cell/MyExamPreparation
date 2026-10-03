package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NoteStorage {

    private const val PREF_NAME = "notes_storage"
    private const val NOTES_KEY = "notes_list"

    fun saveNotes(context: Context, notes: List<Note>) {
        val existing = loadCustomNotes(context)
        val merged = (existing + notes)
            .associateBy { it.id }
            .values
            .toList()

        val jsonArray = JSONArray()
        merged.forEach { note ->
            val json = JSONObject()
            json.put("id", note.id)
            json.put("subject", note.subject)
            json.put("topic", note.topic)
            json.put("title", note.title)
            json.put("content", note.content)

            val summaryArray = JSONArray()
            note.summaryPoints.forEach { summaryArray.put(it) }
            json.put("summaryPoints", summaryArray)

            json.put("isBookmarked", note.isBookmarked)
            jsonArray.put(json)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(NOTES_KEY, jsonArray.toString())
            .apply()
    }

    private fun loadCustomNotes(context: Context): List<Note> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString(NOTES_KEY, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(savedData)
            val list = mutableListOf<Note>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)

                val summaryArray = json.optJSONArray("summaryPoints")
                val summaryList = mutableListOf<String>()
                if (summaryArray != null) {
                    for (j in 0 until summaryArray.length()) {
                        summaryList.add(summaryArray.getString(j))
                    }
                }

                list.add(
                    Note(
                        id = json.getInt("id"),
                        subject = json.optString("subject"),
                        topic = json.optString("topic"),
                        title = json.getString("title"),
                        content = json.getString("content"),
                        summaryPoints = summaryList,
                        isBookmarked = json.optBoolean("isBookmarked", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllNotes(context: Context): List<Note> {
        val customNotes = loadCustomNotes(context)
        val mergedMap = (sampleNotes + customNotes).associateBy { it.id }
        return mergedMap.values.toList()
    }

    fun updateBookmark(context: Context, noteId: Int, isBookmarked: Boolean) {
        val all = getAllNotes(context)
        val target = all.find { it.id == noteId } ?: return
        val updated = target.copy(isBookmarked = isBookmarked)
        saveNotes(context, listOf(updated))
    }

    fun getBookmarkedNotes(context: Context): List<Note> {
        return getAllNotes(context).filter { it.isBookmarked }
    }
}
