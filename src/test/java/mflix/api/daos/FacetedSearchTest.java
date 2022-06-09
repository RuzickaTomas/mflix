package mflix.api.daos;

import com.mongodb.client.MongoClient;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
	
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class FacetedSearchTest extends TicketTest {

	private MovieDao dao;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	@BeforeEach
	public void setup() {
		this.dao = new MovieDao(mongoClient, databaseName);
	}

	@Test
	public void testRatingRuntimeBuckets() {

		String cast = "Salma Hayek";

		List<Document> moviesInfo = dao.getMoviesCastFaceted(20, 0, cast);

		ArrayList<Document> allMovies = (ArrayList<Document>) moviesInfo.get(0).get("movies");
		assertEquals(20, allMovies.size(),
				"Check your movies sub-pipeline on getMoviesFaceted() for multiple cast in single cast");

		ArrayList rating = (ArrayList<Document>) moviesInfo.get(0).get("rating");
		assertEquals(3, rating.size(),
				"Check your $bucket rating sub-pipeline on getMoviesFaceted() for multiple cast in single cast");

		ArrayList runtime = (ArrayList<Document>) moviesInfo.get(0).get("runtime");
		assertEquals(3, runtime.size(),
				"Check your $bucket runtime sub-pipeline on getMoviesFaceted() for multiple cast in single cast");
	}

	@Test
	public void testFacetedSearchPaging() {

		String cast = "Paul Newman";

		List<Document> moviesInfo = dao.getMoviesCastFaceted(20, 2 * 20, cast);

		assertEquals(1, moviesInfo.size(), "Should return a list of one faceted document");

		ArrayList movies = (ArrayList<Document>) moviesInfo.get(0).get("movies");
		assertEquals(4, movies.size(),
				"Check your movies sub-pipeline on getMoviesFaceted() for multiple cast in paged results");

		ArrayList rating = (ArrayList<Document>) moviesInfo.get(0).get("rating");
		assertEquals(1, rating.size(),
				"Check your $bucket rating sub-pipeline on getMoviesFaceted() in multiple cast in paged results");

		ArrayList runtime = (ArrayList<Document>) moviesInfo.get(0).get("runtime");
		assertEquals(3, runtime.size(),
				"Check your $bucket runtime sub-pipeline on getMoviesFaceted() in paged results");
	}

	@Test
	public void testFacetedMultipleCast() {

		String[] cast = { "Brad Pitt", "Angelina Jolie" };

		List<Document> moviesInfo = dao.getMoviesCastFaceted(20, 2 * 20, cast);

		assertEquals(1, moviesInfo.size(), "Should return a list of one faceted document");

		ArrayList movies = (ArrayList<Document>) moviesInfo.get(0).get("movies");
		assertEquals(16, movies.size(),
				"Check your movies sub-pipeline on getMoviesFaceted() in multiple cast for multiple cast");

		ArrayList rating = (ArrayList<Document>) moviesInfo.get(0).get("rating");
		assertEquals(3, rating.size(), "Check your $bucket rating sub-pipeline on getMoviesFaceted() in multiple cast");

		ArrayList runtime = (ArrayList<Document>) moviesInfo.get(0).get("runtime");
		assertEquals(5, runtime.size(),
				"Check your $bucket runtime sub-pipeline on getMoviesFaceted() in multiple cast");
	}
}
