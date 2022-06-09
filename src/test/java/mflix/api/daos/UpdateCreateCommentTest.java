package mflix.api.daos;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import mflix.api.models.Comment;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
	
import java.util.Date;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class UpdateCreateCommentTest extends TicketTest {

	private CommentDao dao;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	private String notValidEmail;
	private String validEmail;

	private String fakeCommentId;

	@BeforeEach
	public void setUp() {
		this.dao = new CommentDao(mongoClient, databaseName);

		this.notValidEmail = "hello@notvalid.io";
		this.validEmail = "hello@valid.io";
		this.fakeCommentId = this.dao.generateObjectId().toHexString();
		removeFakeComment(this.fakeCommentId);
	}

	@AfterEach
	public void tearDown() {
		removeFakeComment(this.fakeCommentId);
	}

	private void removeFakeComment(String id) {
		commentsCollection().deleteOne(Filters.eq("_id", id));
	}

	private Comment fakeCommentWithId() {
		Comment comment = fakeCommentNoId();
		comment.setId(this.fakeCommentId);
		return comment;
	}

	private Comment fakeCommentNoId() {
		String movieId = "573a1394f29313caabce0899";
		Comment comment = new Comment();
		comment.setEmail(validEmail);
		comment.setText(randomText(32));
		comment.setDate(new Date());
		comment.setName("some name");
		comment.setMovieId(movieId);
		return comment;
	}

	private MongoCollection commentsCollection() {
		return this.mongoClient.getDatabase(databaseName).getCollection(CommentDao.COMMENT_COLLECTION);
	}

	@Test
	public void testUserUpdatesOwnComments() {
		Comment fakeComment = fakeCommentWithId();
		dao.addComment(fakeComment);
		String expectedCommentText = randomText(20);

		assertTrue(dao.updateComment(fakeComment.getId(), expectedCommentText, validEmail),
				"Should be able to update his own comments. Check updateComment implementation");

		Document actualComment = (Document) commentsCollection().find(new Document("_id", new ObjectId(fakeCommentId)))
				.first();

		assertEquals(expectedCommentText, actualComment.getString("text"),
				"Comment text should match. Check updateComment implementation");

		assertEquals(validEmail, actualComment.getString("email"), "Commenter email should match the user email");
	}

	@Test
	public void testUserFailsUpdateOthersComments() {

		Comment fakeComment = fakeCommentWithId();
		dao.addComment(fakeComment);
		String newCommentText = randomText(20);

		assertTrue(!dao.updateComment(fakeComment.getId(), newCommentText, notValidEmail),
				"Cannot update comments not owned by user");
	}

	@Test
	public void testUserAddCommentWithNoID() {
		assertThrows(IncorrectDaoOperation.class, () -> {
			Comment actual = fakeCommentNoId();
			dao.addComment(actual);
		});
	}

	@Test
	public void testAddCommentWithId() {
		Comment expectedComment = fakeCommentWithId();
		assertNotNull(dao.addComment(expectedComment),
				"Comment should have been correctly added. Check your addComments method");

		Document actualComment = (Document) commentsCollection().find(Filters.eq("_id", expectedComment.getOid()))
				.first();

		assertNotNull(actualComment, "Comment should be found. Check your addComment method");

		assertEquals(actualComment.getString("email"), expectedComment.getEmail(),
				"Comment email not matching. Check your addComment method");

		assertEquals(actualComment.getString("text"), expectedComment.getText(),
				"Comment text not matching. Check your addComment method");

		assertEquals(actualComment.getDate("date"), expectedComment.getDate(),
				"Comment date not matching. Check your addComment method");
	}

	@Test
	public void testAddCommentUsingObjectId() {
		String id = "619e483309f8c99feb5c87a2";
		Comment comment = dao.getComment(id);

		assertNotNull(comment);
	}
}
