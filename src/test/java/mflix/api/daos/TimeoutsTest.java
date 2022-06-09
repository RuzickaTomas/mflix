package mflix.api.daos;

import com.mongodb.ConnectionString;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import mflix.config.MongoDBConfiguration;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = { CommentDao.class, MongoDBConfiguration.class })
@EnableConfigurationProperties
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
public class TimeoutsTest extends TicketTest {

	@Autowired
	MongoClient mongoClient;
	private String mongoUri;

	@Value("${spring.mongodb.database}")
	String databaseName;

	private MovieDao movieDao;

	@BeforeEach
	public void setUp() throws IOException {
		this.movieDao = new MovieDao(mongoClient, databaseName);
		mongoUri = getProperty("spring.mongodb.uri");
		mongoClient = MongoClients.create(mongoUri);
	}

	@Test
	public void testConfiguredWtimeout() {
		WriteConcern wc = this.movieDao.mongoClient.getDatabase(databaseName).getWriteConcern();

		assertNotNull(wc);
		int actual = wc.getWTimeout(TimeUnit.MILLISECONDS);
		int expected = 2500;
		assertEquals(expected, actual, "Configured `wtimeoutms` not set has expected");
	}

	@Test
	public void testConfiguredConnectionTimeoutMs() {
		ConnectionString connectionString = new ConnectionString(mongoUri);
		int expected = 2000;
		int actual = connectionString.getConnectTimeout();

		assertEquals(expected, actual, "Configured `connectionTimeoutMS` does not match expected");
	}
}
