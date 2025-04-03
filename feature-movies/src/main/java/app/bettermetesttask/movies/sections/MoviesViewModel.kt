package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
) : ViewModel() {

    private val moviesMutableFlow: MutableStateFlow<MoviesState> = MutableStateFlow(MoviesState.Loading)

    val moviesStateFlow: StateFlow<MoviesState>
        get() = moviesMutableFlow.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {

            moviesMutableFlow.update { MoviesState.Loading }

            observeMoviesUseCase()
                .map { result -> result.toMoviesState() }
                .catch { e ->
                    moviesMutableFlow.emit(MoviesState.MoviesLoadError(e.message ?: "Error while loading movies"))
                }
                .collect { state ->
                    moviesMutableFlow.emit(state)
                }
        }
    }

    private fun Result<List<Movie>>.toMoviesState(): MoviesState {
        return when (this) {
            is Result.Success -> MoviesState.MoviesLoaded(data)
            is Result.Error -> MoviesState.MoviesLoadError(error.message ?: "Unknown error")
        }
    }

    fun onMovieEvent(event: MoviesEvent) {
        when (event) {
            is MoviesEvent.LikeMovie -> likeMovie(event.movie)
            is MoviesEvent.OpenMovieDetails -> showMovieDetails(event.movie)
        }
    }

    private fun likeMovie(movie: Movie) {
        viewModelScope.launch {
            if (!movie.liked) {
                likeMovieUseCase(movie.id)
            } else {
                dislikeMovieUseCase(movie.id)
            }
        }
    }

    private fun showMovieDetails(movie: Movie) {
        moviesMutableFlow.update { currentState ->
            if (currentState is MoviesState.MoviesLoaded) {
                currentState.copy(
                    selectedMovie = if (currentState.selectedMovie == movie) null else movie
                )
            } else {
                currentState
            }
        }
    }

}