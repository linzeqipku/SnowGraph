package experiments.entity;

import java.util.ArrayList;
import java.util.List;

public class TestDataItem {

    private String query;
    private List<String> apis=new ArrayList<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getApis() {
        return apis;
    }

    public void setApis(List<String> apis) {
        this.apis = apis;
    }
}
