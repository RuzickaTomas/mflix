package mflix.api.daos;

import com.mongodb.client.MongoClient;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
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

import java.util.Date;
import java.util.List;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class GetCommentsTest extends TicketTest {

	private MovieDao dao;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	private String existingMovieId = "573a13c7f29313caabd73ea7";
	private String commentId;

	private void InsertComment() {
		Document comment = new Document("movie_id", new ObjectId(existingMovieId));
		comment.append("text", "hello world").append("email", "yulia@mflix.com").append("date", new Date())
				.append("name", "Yulia");

		mongoClient.getDatabase(databaseName).getCollection("comments").insertOne(comment);
		commentId = comment.getObjectId("_id").toHexString();
	}

	@AfterEach
	public void tearDown() {
		Document filter = new Document("_id", commentId);
		mongoClient.getDatabase(databaseName).getCollection("comments").deleteMany(filter);
	}

	@BeforeEach
	public void setUp() {

		this.dao = new MovieDao(mongoClient, databaseName);
		InsertComment();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetMovieComments() {
		String movieId = "573a13b5f29313caabd42c2f";
		Document movieDocument = dao.getMovie(movieId);
		assertNotNull(movieDocument, "Should not return null. Check getMovie()");

		List<Document> commentDocs = (List<Document>) movieDocument.get("comments");
		int expectedSize = 147;
		assertEquals(expectedSize, commentDocs.size(), "Comments list size does not match expected");

		String expectedName = "Arya Stark";
		assertEquals(expectedName, commentDocs.get(1).getString("name"),
				"Expected `name` field does match: check your " + "getMovie() comments sort order.");
	}

	@Test
	public void testCommentsMovieIdNonExisting() {
		String nonExistingMovieId = "a73a1396559313caabc14181";

		assertNull(dao.getMovie(nonExistingMovieId),
				"Non-existing movieId should return null document. " + "Check your getMovie() method");
	}

	@Test
	public void testInsertedComment() {
		Document movie = dao.getMovie(existingMovieId);
		List<Document> commentDocs = (List<Document>) movie.get("comments");
		boolean foundDocument = false;
		for (Document c : commentDocs) {
			if (c.getObjectId("_id").toHexString().equals(commentId)) {
				foundDocument = true;
				break;
			}
		}
		assertTrue(foundDocument,
				"Did not find the expected comment in the comments " + "array: Check your buildLookupStage method");
	}

}
