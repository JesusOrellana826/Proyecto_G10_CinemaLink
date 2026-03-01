package com.example.proyecto_g10_cinemalink.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.proyecto_g10_cinemalink.R
import com.example.proyecto_g10_cinemalink.adapter.MovieAdapter
import com.example.proyecto_g10_cinemalink.databinding.FragmentHomeBinding
import com.example.proyecto_g10_cinemalink.model.Movie
import com.example.proyecto_g10_cinemalink.ui.viewmodels.HomeViewModel
import com.example.proyecto_g10_cinemalink.utils.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupChips()
        setupSearchView()
        setupListeners()
        observeViewModel()

        viewModel.loadAllMovies()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { navigateToDetail(it) }
        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)  // ← 2 columnas
            adapter = movieAdapter
        }
    }

    private fun setupChips() {
        if (viewModel.selectedChipId != -1) {
            val chipToCheck = when (viewModel.selectedChipId) {
                HomeViewModel.CHIP_TOP_RATED -> R.id.chipTopRated
                HomeViewModel.CHIP_UPCOMING  -> R.id.chipUpcoming
                else                         -> R.id.chipPopular
            }
            binding.chipGroup.check(chipToCheck)
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()

            val category = when (checkedIds[0]) {
                R.id.chipTopRated -> HomeViewModel.CHIP_TOP_RATED
                R.id.chipUpcoming -> HomeViewModel.CHIP_UPCOMING
                else              -> HomeViewModel.CHIP_POPULAR
            }
            viewModel.onCategorySelected(category)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.trim()?.let { viewModel.submitSearch(it) }
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQuery(newText?.trim() ?: "")
                return true
            }
        })
    }

    private fun setupListeners() {
        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.movies.observe(viewLifecycleOwner) { movies ->
            movieAdapter.submitList(movies)
            binding.rvMovies.scrollToPosition(0)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }

        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            binding.chipScrollView.visibility = if (isSearching) View.GONE else View.VISIBLE
        }

        viewModel.noResults.observe(viewLifecycleOwner) { noResults ->
            binding.tvNoResults.visibility = if (noResults) View.VISIBLE else View.GONE
            binding.rvMovies.visibility    = if (noResults) View.GONE   else View.VISIBLE
        }
    }

    private fun navigateToDetail(movie: Movie) {
        val bundle = Bundle().apply {
            putInt("movieId", movie.id)
            putString("movieTitle", movie.title)
        }
        findNavController().navigate(R.id.action_homeFragment_to_movieDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}