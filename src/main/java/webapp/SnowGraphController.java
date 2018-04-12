package webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webapp.resource.*;
import searcher.api.ApiLocator;

import java.util.List;

@CrossOrigin
@RestController
public class SnowGraphController {

    @Autowired
    private SnowGraphContext context;

    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jSubGraph apiLocation(@RequestParam(value="query", defaultValue="") String query){
        return new Neo4jSubGraph(ApiLocator.query(query,context.getApiLocatorContext(),true),context);
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jRelation> relationList(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jRelation.getNeo4jRelationList(id,context);
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jNode node(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jNode.get(id,context);
    }

    @RequestMapping(value = "/nav", method = {RequestMethod.GET,RequestMethod.POST})
    public NavResult nav(){
        return context.getNav();
    }

}
