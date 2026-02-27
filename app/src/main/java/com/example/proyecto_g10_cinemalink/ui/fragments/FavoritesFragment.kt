package com.example.proyecto_g10_cinemalink.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_g10_cinemalink.R
import com.example.proyecto_g10_cinemalink.adapter.MovieAdapter
import com.example.proyecto_g10_cinemalink.databinding.FragmentFavoritesBinding
import com.example.proyecto_g10_cinemalink.utils.FavoritesManager

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var favoritesManager: FavoritesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesManager = FavoritesManager(requireContext())

        setupRecyclerView()
        setupListeners()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { movie ->
            val bundle = Bundle().apply {
                putInt("movieId", movie.id)
                putString("movieTitle", movie.title)
            }
            findNavController().navigate(
                R.id.movieDetailFragment,
                bundle
            )
        }

        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = movieAdapter
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadFavorites() {
        val favorites = favoritesManager.getFavorites()

        if (favorites.isEmpty()) {
            binding.emptyLayout.visibility = View.VISIBLE
            binding.recyclerViewFavorites.visibility = View.GONE
        } else {
            binding.emptyLayout.visibility = View.GONE
            binding.recyclerViewFavorites.visibility = View.VISIBLE
            movieAdapter.submitList(favorites)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}