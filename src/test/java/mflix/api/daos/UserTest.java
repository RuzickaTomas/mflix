package mflix.api.daos;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import mflix.api.models.Session;
import mflix.api.models.User;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class UserTest extends TicketTest {

	private UserDao dao;

	private static String email = "gryffindor@hogwarts.edu";
	private User testUser;
	private String jwt;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	@BeforeEach
	public void setup() {

		this.dao = new UserDao(mongoClient, databaseName);
		this.testUser = new User();
		this.testUser.setName("Hermione Granger");
		this.testUser.setEmail(email);
		this.testUser.setHashedpw("somehashedpw");
		this.jwt = "somemagicjwt";
		mongoClient.getDatabase(databaseName).getCollection("users").deleteOne(new Document("email", "log@out.com"));
	}

	@AfterEach
	public void tearDownClass() {
		MongoDatabase db = mongoClient.getDatabase(databaseName);
		db.getCollection("users").deleteMany(new Document("email", email));
		db.getCollection("users").deleteMany(new Document("email", "log@out.com"));
		db.getCollection("sessions").deleteMany(new Document("user_id", "log@out" + ".com"));
	}

	@Test
	public void testRegisterUser() {

		assertTrue(dao.addUser(testUser), "Should have correctly created the user - check your write user method"); // add
																													// string
		// explanation

		User user = dao.getUser(testUser.getEmail());
		assertEquals(testUser.getName(), user.getName());
		assertEquals(testUser.getEmail(), user.getEmail());
		assertEquals(testUser.getHashedpw(), user.getHashedpw());
	}

	@Test
	public void testLogin() {
		dao.addUser(testUser);
		boolean result = dao.createUserSession(testUser.getEmail(), jwt);
		assertTrue(result, "Should be able to create user sesssion.");
		Session session = dao.getUserSession(testUser.getEmail());
		assertEquals(testUser.getEmail(), session.getUserId(),
				"The user email needs to match the `session` user_id field");
		assertEquals(jwt, session.getJwt(), "jwt key needs to match the session `jwt`");
	}

	@Test
	public void testLogout() {
		String email = "log@out.com";
		Document logOutUser = new Document("email", email);
		mongoClient.getDatabase(databaseName).getCollection("users").insertOne(logOutUser);
		Document logOutUserSession = new Document("user_id", email);
		mongoClient.getDatabase(databaseName).getCollection("sessions").insertOne(logOutUserSession);

		assertTrue(dao.deleteUser(email),
				"Should have deleted user from sessions collection - check your logout method");
		Session session = dao.getUserSession(email);
		assertNull(session, "All sessions for user should have been deleted after logout");
	}

	@Test
	public void testDeleteUser() {
		dao.addUser(testUser);
		assertTrue(dao.deleteUser(testUser.getEmail()),
				"You should be able to delete correctly the testDb user. Check your delete filter");

		assertNull(dao.getUserSession(testUser.getEmail()),
				"Should not find any sessions after deleting a user. deleteUser() method needs to remove the user sessions data!");

		assertNull(dao.getUser(testUser.getEmail()),
				"User data should not be found after user been deleted. Make sure you delete data from users collection");
	}
}
