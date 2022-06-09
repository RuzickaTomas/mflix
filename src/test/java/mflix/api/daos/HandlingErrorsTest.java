package mflix.api.daos;

import com.mongodb.client.MongoClient;
import mflix.api.models.User;
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

@SpringBootTest(classes = {MongoDBConfiguration.class})
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class HandlingErrorsTest extends TicketTest {

  private MovieDao mDao;
  private User testUser;
  private UserDao uDao;
  @Autowired MongoClient mongoClient;

  @Value("${spring.mongodb.database}")
  String databaseName;

  @BeforeEach
  public void setup() {
    this.mDao = new MovieDao(mongoClient, databaseName);
    this.uDao = new UserDao(mongoClient, databaseName);
    this.testUser = new User();
    this.testUser.setName("Hermione Granger");
    this.testUser.setHashedpw("somehashedpw");
  }

  @Test
  public void testAccessExistingDocument() {
    String validId = "573a1394f29313caabce0899";
    Document movie = mDao.getMovie(validId);
    assertEquals(movie.getObjectId("_id").toHexString(), validId);
  }

  @Test
  public void testAccessInvalidIdDocument() {
    String notValidId = "573a1394f29313caabce9999";
    Document movie = mDao.getMovie(notValidId);
    assertNull(movie);
  }

  @Test
  public void testNoUserDups() {
	assertThrows(IncorrectDaoOperation.class, () -> {  
    // creates the user for the first time
    uDao.addUser(testUser);
    // checks if user was able to be created again
    uDao.addUser(testUser);
    });
  }
}
