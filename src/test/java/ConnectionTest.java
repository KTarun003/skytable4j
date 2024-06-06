import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.novoess.skytable4j.*;


public class ConnectionTest {

    @Test
    public void BasicConnect(){
        Connection connection = new Connection("127.0.0.1",2456);
        Assertions.assertTrue(connection.IsConnected);
    }
}
