package webapp.resource;

import java.util.ArrayList;
import java.util.List;

public class NavResult {

    public NavResult(int propertyTypeCount, int propertyCount) {
        this.propertyTypeCount = propertyTypeCount;
        this.propertyCount = propertyCount;
    }

    public class NavNode{
        private final long id;
        private final String label;
        private final int count;
        public NavNode(long id,String label,int count){
            this.id=id;
            this.label=label;
            this.count=count;
        }
        public long getId() {
            return id;
        }
        public String getLabel() {
            return label;
        }
        public int getCount() {
            return count;
        }
    }

    public class NavRelation{
        private final long id;
        private final long startNode;
        private final long endNode;
        private final int count;
        private final String type;
        public NavRelation(long id, long startNode, long endNode, int count, String type) {
            this.id = id;
            this.startNode = startNode;
            this.endNode = endNode;
            this.count = count;
            this.type = type;
        }
        public long getId() {
            return id;
        }
        public long getStartNode() {
            return startNode;
        }
        public long getEndNode() {
            return endNode;
        }
        public int getCount() {
            return count;
        }
        public String getType() {
            return type;
        }
    }

    private final List<NavNode> nodes=new ArrayList<>();
    private final List<NavRelation> relationships=new ArrayList<>();
    private final int propertyTypeCount,propertyCount;

    public int getPropertyTypeCount() {
        return propertyTypeCount;
    }

    public int getPropertyCount() {
        return propertyCount;
    }

    public void addNode(long id, String label, int count){
        nodes.add(new NavNode(id,label,count));
    }

    public void addRelation(long id, long startNode, long endNode, int count, String type){
        relationships.add(new NavRelation(id,startNode,endNode,count,type));
    }

    public List<NavNode> getNodes() {
        return nodes;
    }
    public List<NavRelation> getRelationships() {
        return relationships;
    }
}
