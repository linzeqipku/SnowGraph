import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import searcher.api.ApiLocator;
import webapp.SnowGraphContext;
import webapp.SnowView;

import java.io.IOException;
import java.util.Set;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = SnowView.class)
public class TestApiLocator {

    @Autowired
    private SnowGraphContext context;

    @Test
    public void testCase() throws IOException {

        String query="analyze";
        ApiLocator.SubGraph result=ApiLocator.query(query,context.getApiLocatorContext(),false);
        assert(checkNodeSetContainsSig(result.getNodes(),"org.apache.lucene.analysis.Analyzer"));

    }

    private boolean checkNodeSetContainsSig(Set<Long> nodeSet, String sig){
        for (long id:nodeSet)
            if (context.getApiLocatorContext().getId2Sig().get(id).equals(sig))
                return true;
        return false;
    }

}
