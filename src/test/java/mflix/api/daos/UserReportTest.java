package mflix.api.daos;

import com.mongodb.client.MongoClient;
import mflix.api.models.Critic;
import mflix.config.MongoDBConfiguration;
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

import java.util.List;

@SpringBootTest(classes = { MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class UserReportTest extends TicketTest {

	private CommentDao dao;
	@Autowired
	MongoClient mongoClient;

	@Value("${spring.mongodb.database}")
	String databaseName;

	@BeforeEach
	public void setUp() {
		this.dao = new CommentDao(mongoClient, databaseName);
	}

	@Test
	public void testMostActiveUserComments() {
		String mostActiveCommenter = "roger_ashton-griffiths@gameofthron.es";
		List<Critic> mostActive = this.dao.mostActiveCommenters();
		int expectedListSize = 20;
		assertEquals(expectedListSize, mostActive.size(), "mostActiveComments() should return 20 results");

		assertEquals(mostActiveCommenter, mostActive.get(0).getId(),
				"The top comments user email does not match. Check your mostActiveCommenters() method");

		int expectedNumComments = 277;
		assertEquals(expectedNumComments, mostActive.get(0).getNumComments(), "The top comments count does not match.");
	}
}
