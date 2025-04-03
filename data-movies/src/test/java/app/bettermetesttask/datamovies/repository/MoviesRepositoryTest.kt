package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class MoviesRepositoryImplTest {
    private lateinit var localStore: MoviesLocalStore
    private lateinit var mapper: MoviesMapper
    private lateinit var restStore: MoviesRestStore
    private lateinit var repository: MoviesRepositoryImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        localStore = mock(MoviesLocalStore::class.java)
        mapper = spy(MoviesMapper())
        restStore = mock(MoviesRestStore::class.java)
        repository = MoviesRepositoryImpl(localStore, mapper, restStore)
    }

    @Test
    fun `getMovies() return movies from local if not empty`() = runTest {
        val localMovies = listOf(
            MovieEntity(1, "Title 1", "Description 1", "PosterPath 1"),
            MovieEntity(2, "Title 2", "Description 2", "PosterPath 2")
        )
        val movies = listOf(
            Movie(1, "Title 1", "Description 1", "PosterPath 1"),
            Movie(2, "Title 2", "Description 2", "PosterPath 2")
        )

        `when`(localStore.getMovies()).thenReturn(localMovies)
        `when`(mapper.mapListFromLocal(localMovies)).thenReturn(movies)

        val result = repository.getMovies()

        assertEquals(Result.Success(movies), result)
        verify(localStore).getMovies()
        verify(mapper).mapListFromLocal(localMovies)
        verifyNoInteractions(restStore)
    }

    @Test
    fun `getMovies() return movies from remote when local is empty`() = runTest {
        val remoteMovies = listOf(
            Movie(1, "Title 1", "Description 1", "PosterPath 1"),
            Movie(2, "Title 2", "Description 2", "PosterPath 2")
        )
        val remoteResult = Result.Success(remoteMovies)
        val mappedMovies = listOf(
            MovieEntity(1, "Title 1", "Description 1", "PosterPath 1"),
            MovieEntity(2, "Title 2", "Description 2", "PosterPath 2")
        )
        `when`(localStore.getMovies()).thenReturn(emptyList())
        `when`(restStore.getMovies()).thenReturn(remoteResult)
        `when`(mapper.mapListToLocal(remoteMovies)).thenReturn(mappedMovies)

        val result = repository.getMovies()

        assertEquals(remoteResult, result)
        verify(localStore).getMovies()
        verify(restStore).getMovies()
        verify(localStore).insertMovies(mappedMovies)
    }

    @Test
    fun `getMovies() return error if remote fetch fails`() = runTest {

        val errorResult = Result.Error(Exception("Network Error"))
        `when`(localStore.getMovies()).thenReturn(emptyList())
        `when`(restStore.getMovies()).thenReturn(errorResult)


        val result = repository.getMovies()

        assertEquals(errorResult, result)
        verify(localStore).getMovies()
        verify(restStore).getMovies()
        verifyNoMoreInteractions(localStore)
    }


    @Test
    fun `addMovieToFavorites() saves movie to database`() = runTest {
        val movieId = 22

        repository.addMovieToFavorites(movieId)

        verify(localStore).likeMovie(movieId)
        verifyNoMoreInteractions(localStore)
    }


    @Test
    fun `removeMovieFromFavorites() removes movie from database`() = runTest {
        val movieId = 11

        repository.removeMovieFromFavorites(movieId)

        verify(localStore).dislikeMovie(movieId)
        verifyNoMoreInteractions(localStore)
    }
}