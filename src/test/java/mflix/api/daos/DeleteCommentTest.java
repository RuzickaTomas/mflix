package mflix.api.daos;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import org.bson.conversions.Bson;
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

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class DeleteCommentTest extends TicketTest {

	private CommentDao dao;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	private String commentId;

	private String ownerEmail = "owner@email.com";

	@BeforeEach
	public void setUp() {

		this.dao = new CommentDao(mongoClient, databaseName);

		Document commentDoc = new Document("email", ownerEmail);
		commentDoc.append("date", new Date());
		commentDoc.append("text", "some text");
		commentDoc.append("name", "user name");

		this.mongoClient.getDatabase(databaseName).getCollection("comments").insertOne(commentDoc);

		commentId = commentDoc.getObjectId("_id").toHexString();
	}

	@Test
	public void testDeleteOfOwnedComment() {
		assertTrue(dao.deleteComment(commentId, ownerEmail), "Should be able to delete owns comments: Check your deleteComment() method");
	}

	@Test
	public void testDeleteNotOwnedComment() {
		assertFalse(dao.deleteComment(commentId, "some@email.com"),
				"Should not be able to delete not matching owner comment: Check your delete filter on deleteComment() method");
	}

	@Test
	public void testDeleteNonExistingComment() {
		String nonExistingCommentId = new ObjectId().toHexString();
		assertFalse(dao.deleteComment(nonExistingCommentId, ""),
				"Deleting non-existing comment should return " + "false: Check your deleteComment() method");
	}

	@Test
	public void testDeleteIncorrectCommentId() {
		String nonExistingCommentId = new ObjectId().toHexString();
		assertFalse(dao.deleteComment(nonExistingCommentId, ownerEmail), "Deleting comment where _id value does not exist should not return true");
	}

	@Test
	public void testDeleteInvalidCommentId() {
		assertThrows(IllegalArgumentException.class, () -> dao.deleteComment("", ownerEmail));
	}

	@AfterEach
	public void tearDown() {
		Bson deleteFiler = Filters.eq("_id", new ObjectId(this.commentId));
		this.mongoClient.getDatabase(databaseName).getCollection("comments").deleteOne(deleteFiler);
	}
}
