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

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class ProjectionTest extends TicketTest {

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
	public void testFindMoviesByCountry() {
		int expectedSize = 2;
		String country = "Kosovo";
		Iterable<Document> cursor = dao.getMoviesByCountry(country);
		int actualSize = 0;
		for (Document d : cursor) {
			System.out.println(d);
			actualSize++;
		}

		assertEquals(expectedSize, actualSize,
				"Unexpected number of returned movie documents. Check your query filter");
	}

	@Test
	public void testProjectionShape() {
		Iterable<Document> cursor = dao.getMoviesByCountry("Russia", "Japan");
		for (Document doc : cursor) {
			assertEquals(2, doc.keySet().size(), "Document should have only two fields. Check your projection");

			assertTrue(doc.keySet().contains("_id"), "Should return `_id` field. Check your projection");
			assertTrue(doc.keySet().contains("title"), "Should return `title` field. Check your projection");
		}
	}
}
