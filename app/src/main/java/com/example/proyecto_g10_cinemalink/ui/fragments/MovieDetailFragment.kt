package com.example.proyecto_g10_cinemalink.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyecto_g10_cinemalink.R
import com.example.proyecto_g10_cinemalink.databinding.FragmentMovieDetailBinding
import com.example.proyecto_g10_cinemalink.model.Movie
import com.example.proyecto_g10_cinemalink.network.RetrofitClient
import com.example.proyecto_g10_cinemalink.utils.FavoritesManager
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesManager: FavoritesManager
    private var movieId: Int = 0
    private var currentMovie: Movie? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesManager = FavoritesManager(requireContext())

        movieId = arguments?.getInt("movieId") ?: 0

        if (movieId == 0) {
            Toast.makeText(context, "Error al cargar película", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupListeners()
        loadMovieDetails()
        loadTrailer()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddFavorite.setOnClickListener {
            currentMovie?.let { movie ->
                if (favoritesManager.isFavorite(movie.id)) {
                    favoritesManager.removeFavorite(movie.id)
                    Toast.makeText(context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    favoritesManager.addFavorite(movie)
                    Toast.makeText(context, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                }
                updateFavoriteButton()
            }
        }
    }

    private fun loadMovieDetails() {
        lifecycleScope.launch {
            try {
                val movie = RetrofitClient.instance.getMovieDetails(
                    movieId = movieId,
                    apiKey = RetrofitClient.API_KEY
                )

                currentMovie = movie
                binding.tvTitle.text = movie.title
                binding.tvReleaseDate.text = "Fecha de estreno: ${movie.release_date}"
                binding.tvRating.text = "${movie.vote_average} (${movie.vote_count} votos)"
                binding.tvOverview.text = movie.overview

                val imageUrl = "${RetrofitClient.IMAGE_BASE_URL}${movie.poster_path}"
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.ivPoster)

                updateFavoriteButton()

            } catch (e: Exception) {
                Log.e("MovieDetailFragment", "Error: ${e.message}")
                Toast.makeText(
                    context,
                    "Error al cargar detalles: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadTrailer() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMovieVideos(
                    movieId = movieId,
                    apiKey = RetrofitClient.API_KEY
                )

                val trailer = response.results.firstOrNull {
                    it.site == "YouTube" && it.type == "Trailer"
                }

                trailer?.let { video ->

                    val thumbnailUrl =
                        "https://img.youtube.com/vi/${video.key}/hqdefault.jpg"

                    Glide.with(requireContext())
                        .load(thumbnailUrl)
                        .into(binding.ivTrailerThumbnail)

                    binding.ivTrailerThumbnail.setOnClickListener {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=${video.key}")
                        )
                        startActivity(intent)
                    }
                }

            } catch (e: Exception) {
                Log.e("MovieDetailFragment", "Error cargando trailer: ${e.message}")
            }
        }
    }

    private fun updateFavoriteButton() {
        currentMovie?.let { movie ->
            if (favoritesManager.isFavorite(movie.id)) {
                binding.btnAddFavorite.text = "Quitar de Favoritos"
                binding.btnAddFavorite.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.heart_remove)
            } else {
                binding.btnAddFavorite.text = "Agregar a Favoritos"
                binding.btnAddFavorite.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.heart_add)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}