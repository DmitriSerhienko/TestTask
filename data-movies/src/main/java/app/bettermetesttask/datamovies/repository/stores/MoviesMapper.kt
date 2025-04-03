package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.domainmovies.entries.Movie
import javax.inject.Inject

class MoviesMapper @Inject constructor() {

    private val mapToLocal: (Movie) -> MovieEntity = {
        with(it) {
            MovieEntity(id, title, description, posterPath)
        }
    }

    private val mapFromLocal: (MovieEntity) -> Movie = {
        with(it) {
            Movie(id, title, description, posterPath)
        }
    }

    fun mapListToLocal(movies: List<Movie>): List<MovieEntity> {
        return movies.map(mapToLocal)
    }

    fun mapListFromLocal(movieEntities: List<MovieEntity>): List<Movie> {
        return movieEntities.map(mapFromLocal)
    }

}
