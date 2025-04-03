package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domaincore.utils.Result
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class MoviesRestStore @Inject constructor() {

    private val statusCodes = listOf(200, 201, 202, 304, 400)

    suspend fun getMovies(): Result<List<Movie>> {
        val statusCode = statusCodes.random()
        delay(Random.nextLong(500, 3000))

        return Result.of {
            when (statusCode) {
                in 200..202  -> MoviesFactory.createMoviesList()
                304 -> emptyList()
                400 -> throw IllegalArgumentException("Client error: Bad request")
                else -> throw IllegalStateException("Unhandled status code: $statusCode")
            }
        }
    }
}
