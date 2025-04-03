package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val localStore: MoviesLocalStore,
    private val mapper: MoviesMapper,
    private val moviesStore: MoviesRestStore,
) : MoviesRepository {

    override suspend fun getMovies(): Result<List<Movie>> {
        val localMovies = localStore.getMovies()

        if (localMovies.isNotEmpty()) {
            return Result.Success(mapper.mapListFromLocal(localMovies))
        }

        return when (val remoteResult = moviesStore.getMovies()) {
            is Result.Success -> {
                val remoteMovies = remoteResult.data

                if (remoteMovies.isNotEmpty()) {
                    localStore.insertMovies(mapper.mapListToLocal(remoteMovies))
                }

                Result.Success(remoteMovies)
            }
            is Result.Error -> {
                Result.Error(remoteResult.error)
            }
        }
    }

    override fun observeLikedMovieIds(): Flow<List<Int>> {
        return localStore.observeLikedMoviesIds()
    }

    override suspend fun addMovieToFavorites(movieId: Int) {
        localStore.likeMovie(movieId)
    }

    override suspend fun removeMovieFromFavorites(movieId: Int) {
        localStore.dislikeMovie(movieId)
    }
}