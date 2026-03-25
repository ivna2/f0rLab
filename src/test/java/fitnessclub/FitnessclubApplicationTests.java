package fitnessclub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "DB_URL=jdbc:h2:mem:testdb-context;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "DB_DRIVER=org.h2.Driver",
        "DB_USERNAME=sa",
        "DB_PASSWORD=",
        "DDL_AUTO=create-drop"
})
class FitnessclubApplicationTests {

    @Test
    void contextLoads() {
    }

}
