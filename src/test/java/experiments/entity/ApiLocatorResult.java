package experiments.entity;

import java.util.ArrayList;
import java.util.List;

public class ApiLocatorResult {

    private TestDataItem item;
    private List<String> res=new ArrayList<>();

    public TestDataItem getItem() {
        return item;
    }

    public void setItem(TestDataItem item) {
        this.item = item;
    }

    public List<String> getRes() {
        return res;
    }

    public void setRes(List<String> res) {
        this.res = res;
    }

    public void add(String sig){
        res.add(sig);
    }

}
