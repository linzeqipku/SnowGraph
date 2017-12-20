import de.parsemis.graph.Graph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import searcher.codepattern.CodePatternSearcher;
import searcher.codepattern.code.mining.MiningNode;
import webapp.SnowGraphContext;
import webapp.SnowView;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SnowView.class)
public class TestCodePatternSearcher {

    @Autowired
    private SnowGraphContext context;

    @Test
    public void testCodePatternSearcher() throws IOException {
        String query="create index";
        List<Graph<MiningNode, Integer>> graphs= CodePatternSearcher.run(query,context);
        assert(graphs.size()>0);
    }

}
