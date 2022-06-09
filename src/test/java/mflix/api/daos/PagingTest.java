package mflix.api.daos;

import com.mongodb.client.MongoClient;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class PagingTest extends TicketTest {

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
	public void testPagingByCast() {
		String cast = "Michael Caine";
		int skip = 0;
		int countPage1 = 0;
		List<Document> movieDocs = dao.getMoviesByCast(sortKey, 20, skip, cast);
		assertEquals("The Dark Knight", movieDocs.get(0).getString("title"),
				"Expected `title` field does match: Please check your \" + \"getMoviesByCast() movies sort order.");

		countPage1 = movieDocs.size();
		assertEquals(20, countPage1, "Check the query used in getMoviesByCast() in MoviesDao.java");

		int countPage2 = 0;
		movieDocs = dao.getMoviesByCast(sortKey, 20, countPage1, cast);
		assertEquals("Educating Rita", movieDocs.get(0).getString("title"),
				"Expected `title` field does match: Please check your \" + \"getMoviesByCast() movies sort order.");
		countPage2 = movieDocs.size();
		assertEquals(20, countPage2, "Incorrect count in page 2. Check your query implementation");

		int countPage3 = 0;
		movieDocs = dao.getMoviesByCast(sortKey, 20, countPage1 + countPage2, cast);
		assertEquals("X, Y and Zee", movieDocs.get(0).getString("title"),
				"Expected `title` field does match: Please check your \" + \"getMoviesByCast() movies sort order.");
		countPage3 = movieDocs.size();

		assertEquals(12, countPage3, "Incorrect count in page 3");

		assertEquals(52, countPage1 + countPage2 + countPage3, "Total document count does not match");
	}

	@Test
	public void testPagingByGenre() {
		String genre = "History";
		int skip = 0;
		int countPage1 = 0;

		List<Document> movieDocs = dao.getMoviesByGenre(sortKey, 20, skip, genre);
		assertEquals("Braveheart", movieDocs.get(0).getString("title"),
				"Expected `title` field does match: Please check your \" + \"getMoviesByGenre() movies sort order.");
		countPage1 = movieDocs.size();
		int expectedTotal = 999;
		assertEquals(20, countPage1, "Check the query used in () in MoviesDao.java");

		assertEquals(expectedTotal, dao.getGenresSearchCount(genre),
				"Total document count does not match expected. Review " + "getGenreSearchCount()");

		// jump to last page

		int lastPage = expectedTotal / 20;
		skip = lastPage * 20;
		int countPageFinal = 0;
		movieDocs = dao.getMoviesByGenre(sortKey, 20, skip, genre);
		assertEquals("Only the Dead", movieDocs.get(0).getString("title"),
				"Expected `title` field does match: Please check your " + "getMoviesByGenre() movies sort order.");
		countPageFinal = movieDocs.size();

		assertEquals(expectedTotal % 20, countPageFinal,
				"Last page count does not match expected. Check dataset and getGenreSearchCount()");
	}

	@Test
	public void testPagingByText() {
		String keywords = "bank robbery";
		int expectedCount = 475;
		int count = 0;
		List<Document> movieDocs = dao.getMoviesByText(20, 0, keywords);
		assertEquals(
				"The Bank", movieDocs.get(0).getString("title"), "Expected `title` field does match: Please check your \" + \"getMoviesByText() movies sort order.");

		count = movieDocs.size();
		assertEquals(20, count, "Check the query used in getMoviesByText() in MoviesDao.java");
		assertEquals(expectedCount, dao.getTextSearchCount(keywords), "Check your count method");

	}

	@Test
	public void testPagingByTextAndSkip() {

		String keywords = "magic";
		int limit = 20;
		int skip = 280;
		int expectedCount = 296;
		String expectedTitle = "Estomago: A Gastronomic Story";
		int finalCount = 0;
		List<Document> movieDocs = dao.getMoviesByText(limit, skip, keywords);
		assertEquals(
				expectedTitle, movieDocs.get(0).getString("title"), "Expected `title` field does match: Please check your \" + \"getMoviesByText() movies sort order.");
		finalCount = movieDocs.size();

		assertEquals(16, finalCount, "Check your getMoviesByText method.");

		assertEquals(expectedCount % 20, finalCount, "Check the query used in getMovies() in MoviesDao.java");
	}
}
