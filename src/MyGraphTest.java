import org.junit.Test;

import static org.junit.Assert.*;

public class MyGraphTest {

    @Test
    public void testQueryBridgeWords() {
        MyGraph graph = new MyGraph("1.txt");
        String result = graph.queryBridgeWords("scientist", "analyzed");
        assertEquals("The bridge words from scientist to analyzed are: carefully.", result);
    }
}
