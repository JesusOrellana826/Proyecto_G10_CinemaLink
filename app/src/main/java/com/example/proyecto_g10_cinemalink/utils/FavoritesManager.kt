package com.example.proyecto_g10_cinemalink.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.proyecto_g10_cinemalink.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "FavoritesPrefs"
        private const val KEY_FAVORITES = "favorites"
    }

    fun addFavorite(movie: Movie) {
        val favorites = getFavorites().toMutableList()

        if (!favorites.any { it.id == movie.id }) {
            favorites.add(movie)
            saveFavorites(favorites)
        }
    }

    fun removeFavorite(movieId: Int) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.id == movieId }
        saveFavorites(favorites)
    }

    fun isFavorite(movieId: Int): Boolean {
        return getFavorites().any { it.id == movieId }
    }

    fun getFavorites(): List<Movie> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavorites(favorites: List<Movie>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

}