package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMoviesUseCase @Inject constructor(
    private val repository: MoviesRepository
) {

    suspend operator fun invoke(): Flow<Result<List<Movie>>> {
        return repository.getMovies()
            .let { result ->
                when (result) {
                    is Result.Success -> combineMoviesWithLiked(result.data)
                    is Result.Error -> flowOf(result)
                }
            }
    }

    private fun combineMoviesWithLiked(movies: List<Movie>): Flow<Result<List<Movie>>> {
        return repository.observeLikedMovieIds()
            .map { likedMovieIds ->
                val updatedMovies = movies.map {
                    it.copy(liked = likedMovieIds.contains(it.id))
                }
                Result.Success(updatedMovies)
            }
    }
}