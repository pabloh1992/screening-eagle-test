package com.pablodev.weatherapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.pablodev.weatherapp.databinding.FragmentHomeBinding
import com.pablodev.weatherapp.network.ApiWeatherModule
import com.pablodev.weatherapp.network.NetworkModule
import com.pablodev.weatherapp.utils.Logger

class HomeFragment : Fragment() {

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val logger = Logger.getInstance(javaClass)

    private val viewModel : HomeViewModel by viewModels {
        HomeViewModel.HomeViewModelFactory(
            networkModule = NetworkModule(
                api = ApiWeatherModule.provideApiService()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getWeatherByCity(cityName = "London")
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.weatherResponse.observe(viewLifecycleOwner) {
            logger.debug("Weather of London is $it")
        }
    }
}