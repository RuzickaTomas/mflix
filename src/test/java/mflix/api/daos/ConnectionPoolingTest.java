package mflix.api.daos;

import com.mongodb.ConnectionString;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ConnectionPoolingTest extends TicketTest {

  ConnectionString connectionString;

  @BeforeEach
  public void setUp() throws IOException {
    connectionString = new ConnectionString(getProperty("spring.mongodb.uri"));
  }

  @Test
  public void testConnectionPoolSize() {
    assertNotNull(connectionString.getMaxConnectionPoolSize(),
        "Do not forget to set the maxPoolSize parameter "
            + "in your spring.mongodb.uri key in the properties file"
        );
    Integer expectedMaxPoolSize = 50;
    assertEquals( 
        expectedMaxPoolSize,
        connectionString.getMaxConnectionPoolSize(),
        "The connection pool size should be set to 50");
  }
}
