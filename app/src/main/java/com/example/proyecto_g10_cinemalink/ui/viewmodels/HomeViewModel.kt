package com.example.proyecto_g10_cinemalink.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_g10_cinemalink.model.Movie
import com.example.proyecto_g10_cinemalink.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private var popularMovies: List<Movie> = emptyList()
    private var topRatedMovies: List<Movie> = emptyList()
    private var upcomingMovies: List<Movie> = emptyList()
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val _isSearching = MutableLiveData<Boolean>(false)
    val isSearching: LiveData<Boolean> = _isSearching
    private val _noResults = MutableLiveData<Boolean>(false)
    val noResults: LiveData<Boolean> = _noResults
    var selectedChipId: Int = -1
    private var searchJob: Job? = null
    fun loadAllMovies() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                popularMovies  = RetrofitClient.instance.getPopularMovies(RetrofitClient.API_KEY).results
                topRatedMovies = RetrofitClient.instance.getTopRatedMovies(RetrofitClient.API_KEY).results
                upcomingMovies = RetrofitClient.instance.getUpcomingMovies(RetrofitClient.API_KEY).results

                _isLoading.value = false

                if (_movies.value.isNullOrEmpty()) {
                    _movies.value = popularMovies
                }

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al cargar películas: ${e.message}"
            }
        }
    }

    fun onCategorySelected(chipId: Int) {
        selectedChipId = chipId
        _isSearching.value = false
        _noResults.value = false

        _movies.value = when (chipId) {
            CHIP_POPULAR   -> popularMovies
            CHIP_TOP_RATED -> topRatedMovies
            CHIP_UPCOMING  -> upcomingMovies
            else           -> popularMovies
        }
    }

    fun onSearchQuery(query: String) {
        if (query.isEmpty()) {
            clearSearch()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            searchMovies(query)
        }
    }

    fun submitSearch(query: String) {
        if (query.isNotEmpty()) {
            searchJob?.cancel()
            viewModelScope.launch { searchMovies(query) }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _isSearching.value = false
        _noResults.value = false

        _movies.value = when (selectedChipId) {
            CHIP_TOP_RATED -> topRatedMovies
            CHIP_UPCOMING  -> upcomingMovies
            else           -> popularMovies
        }
    }

    private suspend fun searchMovies(query: String) {
        try {
            _isLoading.value = true
            _isSearching.value = true

            val results = RetrofitClient.instance.searchMovies(
                apiKey = RetrofitClient.API_KEY,
                query = query
            ).results

            _isLoading.value = false
            _noResults.value = results.isEmpty()
            _movies.value = results

        } catch (e: kotlinx.coroutines.CancellationException) {
            _isLoading.value = false
            throw e
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Error al buscar: ${e.message}"
        }
    }

    companion object {
        const val CHIP_POPULAR   = 0
        const val CHIP_TOP_RATED = 1
        const val CHIP_UPCOMING  = 2
    }
}