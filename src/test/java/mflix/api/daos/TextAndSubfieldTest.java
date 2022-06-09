package mflix.api.daos;

import com.mongodb.client.MongoClient;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class TextAndSubfieldTest extends TicketTest {

	private MovieDao dao;
	private String sortKey;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	@BeforeEach
	public void setup() {
		this.dao = new MovieDao(mongoClient, databaseName);
		this.sortKey = "tomatoes.viewer.numReviews";
	}

	@Test
	public void testTextSearch() {
		String keywords = "dress";
		int skip = 0;
		int limit = 20;

		Iterable<Document> cursor = dao.getMoviesByText(limit, skip, keywords);

		Document firstMovie = cursor.iterator().next();

		assertEquals("The Dress", firstMovie.getString("title"),
				"Movie title does not match expected. Check your sort");

		int actualMoviesCount = 0;
		for (Document doc : cursor) {
			actualMoviesCount++;
		}

		assertEquals(limit, actualMoviesCount, "The expect number of movies does not match. Check your query filter");
	}

	@Test
	public void testTextSearchCount() {
		long expected = 151;
		String keywords = "dress";

		assertEquals(expected, dao.getTextSearchCount(keywords),
				"Text search matched documents does not match. Check your query filter");
	}

	@Test
	public void testSearchByCast() {
		String cast = "Elon Musk";

		Iterable<Document> cursor = dao.getMoviesByCast(sortKey, 10, 0, cast);

		Document movie = cursor.iterator().next();
		assertEquals("Racing Extinction", movie.getString("title"),
				"Expected title does not match. Check your query filter");

		int expectedCount = 1;
		int actualCount = 0;
		for (Document doc : cursor) {
			actualCount++;
		}
		assertEquals(expectedCount, actualCount,
				"The expect number of documents does not match. Check your query filter");
	}

	@Test
	public void testSearchMultipleCast() {
		ArrayList<String> cast = new ArrayList<>();
		cast.add("Elon Musk");
		cast.add("Robert Redford");
		cast.add("Julia Roberts");
		int expectedCount = 62;
		Iterable<Document> cursor = dao.getMoviesByCast(sortKey, 33, 0, cast.toArray(new String[0]));
		int numMovies = 0;
		for (Document doc : cursor) {
			numMovies++;
		}

		assertEquals(33, numMovies, "Number of movies expected does not match. Check your query filter");

		assertEquals(expectedCount, dao.getCastSearchCount(cast.toArray(new String[0])),
				"Total count of movies with cast does not match. Check your query filter");

		Document movie = cursor.iterator().next();
		assertEquals("Pretty Woman", movie.getString("title"), "Expected title does not match. Check your sort filter");
	}

	@Test
	public void testMultipleGenreSearch() {
		ArrayList<String> genres = new ArrayList<>();
		genres.add("Action");
		genres.add("Adventure");
		int expectedCount = 3805;
		int limit = 25;
		int skip = 0;
		String[] garray = genres.toArray(new String[0]);
		Iterable<Document> movies = dao.getMoviesByGenre(sortKey, limit, skip, garray);
		int numMovies = 0;
		for (Document doc : movies) {
			numMovies++;
		}

		assertTrue(numMovies > 0, "getMoviesByGenre should be returning documents");
		assertEquals(expectedCount, dao.getGenresSearchCount(garray));
	}

	@Test
	public void testGenreSearch() {
		String genre = "Action";
		int expectedCount = 2539;
		int limit = 20;
		int skip = 0;
		Iterable<Document> movies = dao.getMoviesByGenre(sortKey, limit, skip, genre);
		int numMovies = 0;
		for (Document doc : movies) {
			numMovies++;
		}

		assertTrue(numMovies > 0, "getMoviesByGenre should be returning documents");
		assertEquals(expectedCount, dao.getGenresSearchCount(genre),
				"Number of total documents does not match expected. Check your dataset");
	}
}
