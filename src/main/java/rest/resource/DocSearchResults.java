package rest.resource;

import searcher.doc.DocSearcher;

import java.util.ArrayList;
import java.util.List;

public class DocSearchResults {

    private final String query;

    private final List<DocSearcher.DocSearchResult> rankedResults;

    public DocSearchResults(String query, List<DocSearcher.DocSearchResult> rankedResults) {
        this.query = query;
        this.rankedResults=rankedResults;
    }

    public List<DocSearcher.DocSearchResult> getRankedResults() {
        return rankedResults;
    }

    public String getQuery() {
        return query;
    }
}
