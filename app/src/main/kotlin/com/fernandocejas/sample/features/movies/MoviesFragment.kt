/**
 * Copyright (C) 2018 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fernandocejas.sample.features.movies

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.fernandocejas.sample.core.platform.BaseFragment
import com.fernandocejas.sample.R
import com.fernandocejas.sample.features.movies.MovieFailure.ListNotAvailable
import com.fernandocejas.sample.core.exception.Failure
import com.fernandocejas.sample.core.exception.Failure.NetworkConnection
import com.fernandocejas.sample.core.exception.Failure.ServerError
import com.fernandocejas.sample.core.extension.failure
import com.fernandocejas.sample.core.extension.invisible
import com.fernandocejas.sample.core.extension.observe
import com.fernandocejas.sample.core.extension.viewModel
import com.fernandocejas.sample.core.extension.visible
import com.fernandocejas.sample.core.navigation.Navigator
import kotlinx.android.synthetic.main.fragment_movies.emptyView
import kotlinx.android.synthetic.main.fragment_movies.movieList
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject

class MoviesFragment : BaseFragment() {

    private val navigator: Navigator by inject()
    private val moviesAdapter: MoviesAdapter by inject()

    private val moviesViewModel: MoviesViewModel by viewModel()

    override fun layoutId() = R.layout.fragment_movies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(moviesViewModel){
            observe(movies, ::renderMoviesList)
            failure(failure, ::handleFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        loadMoviesList()
    }


    private fun initializeView() {
        movieList.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        movieList.adapter = moviesAdapter
        moviesAdapter.clickListener = { movie, navigationExtras ->
            navigator.showMovieDetails(activity!!, movie, navigationExtras)
        }
    }

    private fun loadMoviesList() {
        emptyView.invisible()
        movieList.visible()
        showProgress()
        moviesViewModel.loadMovies()
    }

    private fun renderMoviesList(movies: List<MovieView>?) {
        moviesAdapter.collection = movies.orEmpty()
        hideProgress()
    }

    private fun handleFailure(failure: Failure?) {
        when (failure) {
            is NetworkConnection -> renderFailure(R.string.failure_network_connection)
            is ServerError -> renderFailure(R.string.failure_server_error)
            is ListNotAvailable -> renderFailure(R.string.failure_movies_list_unavailable)
        }
    }

    private fun renderFailure(@StringRes message: Int) {
        movieList.invisible()
        emptyView.visible()
        hideProgress()
        notifyWithAction(message, R.string.action_refresh, ::loadMoviesList)
    }
}
