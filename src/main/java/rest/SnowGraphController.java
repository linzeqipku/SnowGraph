package rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rest.resource.*;
import searcher.SnowGraphContext;
import searcher.api.ApiLocator;
import searcher.doc.DocSearcher;

import java.util.List;

@RestController
public class SnowGraphController {

    public SnowGraphController(){
        SnowGraphContext.init();
    }

    @RequestMapping(value = "/sampleQuestion", method = {RequestMethod.GET,RequestMethod.POST})
    public SampleQuestion sampleQuestion(){
        return SnowGraphContext.getStackOverflowExamples().getRandomExampleQuery();
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET,RequestMethod.POST})
    public DocSearchResults docSearch(@RequestParam(value="query", defaultValue="") String query){
        return new DocSearchResults(query, DocSearcher.search(query, SnowGraphContext.getDocSearcherContext()));
    }

    @RequestMapping(value = "/apiLocation", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jSubGraph apiLocation(@RequestParam(value="query", defaultValue="") String query){
        return new Neo4jSubGraph(ApiLocator.query(query,SnowGraphContext.getApiLocatorContext(),true));
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jRelation> relationList(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jRelation.getNeo4jRelationList(id);
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jNode node(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jNode.get(id);
    }

    @RequestMapping(value = "/nav", method = {RequestMethod.GET,RequestMethod.POST})
    public String nav(){
        return SnowGraphContext.getNav().toString();
    }


}
