package app.bettermetesttask.movies.sections

import app.bettermetesttask.domainmovies.entries.Movie

sealed class MoviesState {

    data object Loading : MoviesState()

    data class MoviesLoaded(
        val movies: List<Movie>,
        val event: MoviesEvent? = null,
        val selectedMovie: Movie? = null
    ) : MoviesState()

    data class MoviesLoadError(val errorMessage: String) : MoviesState()

}

sealed class MoviesEvent {
    data class LikeMovie(val movie: Movie) : MoviesEvent()
    data class OpenMovieDetails(val movie: Movie) : MoviesEvent()
}