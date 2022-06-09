package mflix.api.daos;

import com.mongodb.client.MongoClient;
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

import java.util.HashMap;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class UserPreferencesTest extends TicketTest {

	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	private UserDao dao;
	private String email;

	@BeforeEach
	public void setup() {

		this.dao = new UserDao(mongoClient, databaseName);
		this.email = "user@preferences.email";
		Document userDoc = new Document("email", email);
		userDoc.put("name", "Preferencial");
		mongoClient.getDatabase(databaseName).getCollection("users").insertOne(userDoc);
	}

	@AfterEach
	public void tearDown() {
		Document userDoc = new Document("email", email);
		mongoClient.getDatabase(databaseName).getCollection("users").deleteOne(userDoc);
	}

	@Test
	public void testUpdateSinglePreferences() {

		String expected = "FC Porto";
		String key = "favorite_club";
		HashMap<String, String> userPrefs = new HashMap<>();
		userPrefs.put(key, expected);

		assertTrue(dao.updateUserPreferences(email, userPrefs),
				"The response of the updateUserPreferences should result in true. Check your implementation of this method");

		User user = dao.getUser(email);

		assertEquals(expected, user.getPreferences().get(key),
				"After an update, the user preferences should be found in the User object");

		assertEquals(1, user.getPreferences().keySet().size(),
				"The number of keys in the user preferences does not match the expected. Check update object");
	}

	@Test
	public void testMultiplePreferences() {
		String[] values = { "Francesinha", "Sunset in Lisbon" };
		String[] keys = { "favorite_dish", "favorite_movie" };

		HashMap<String, String> userPrefs = new HashMap<>();
		userPrefs.put(keys[0], values[0]);
		userPrefs.put(keys[1], values[1]);

		assertTrue(dao.updateUserPreferences(email, userPrefs),
				"The response of the updateUserPreferences should result in true. Check your implementation of this method");

		User user = dao.getUser(email);

		assertEquals(2, user.getPreferences().keySet().size(),
				"The number of keys in the user preferences does not match the expected. Check update object");

		assertEquals(values[1], user.getPreferences().get(keys[1]),
				"After an update, the user preferences should be found in the User object");

		assertNotEquals(values[1], user.getPreferences().get(keys[0]),
				"Looks like the user preferences got mixed up. Check your update method");
	}

	@Test
	public void testNullPreferences() {
		assertThrows(IncorrectDaoOperation.class, () -> dao.updateUserPreferences(email, null));
	}
}
