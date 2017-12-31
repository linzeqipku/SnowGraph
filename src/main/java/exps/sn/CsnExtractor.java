package exps.sn;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.neo4j.graphdb.*;

public class CsnExtractor {

    public static String IS_A="isA";
    public static String HAS_ACTION="hasAction";
    public static String AGGREGATION="aggregation";
    public static String CALL="call";

    public static Graph extract(GraphDatabaseService db){
        Graph r=new Graph();
        try (Transaction tx=db.beginTx()){
            db.findNodes(Label.label(JavaCodeExtractor.CLASS)).stream().forEach(n->r.createVertex((String) n.getProperty(JavaCodeExtractor.SIGNATURE)));
            db.findNodes(Label.label(JavaCodeExtractor.INTERFACE)).stream().forEach(n->r.createVertex((String) n.getProperty(JavaCodeExtractor.SIGNATURE)));
            db.findNodes(Label.label(JavaCodeExtractor.METHOD)).stream().forEach(n->r.createVertex((String) n.getProperty(JavaCodeExtractor.SIGNATURE)));
            // IS_A relationship
            db.getAllRelationships().stream().filter(rel->rel.getType().equals(RelationshipType.withName(JavaCodeExtractor.EXTEND))||rel.getType().equals(RelationshipType.withName(JavaCodeExtractor.IMPLEMENT))).forEach(rel->{
                r.createEdge((String) rel.getStartNode().getProperty(JavaCodeExtractor.SIGNATURE),(String)rel.getEndNode().getProperty(JavaCodeExtractor.SIGNATURE),IS_A);
            });
            //HAS_ACTION relationship
            db.getAllRelationships().stream().filter(rel->rel.getType().equals(RelationshipType.withName(JavaCodeExtractor.HAVE_METHOD))).forEach(rel->{
                r.createEdge((String) rel.getStartNode().getProperty(JavaCodeExtractor.SIGNATURE),(String)rel.getEndNode().getProperty(JavaCodeExtractor.SIGNATURE),HAS_ACTION);
            });
            //AGGREGATION relationship
            db.findNodes(Label.label(JavaCodeExtractor.CLASS)).stream().forEach(owner->{
                owner.getRelationships(RelationshipType.withName(JavaCodeExtractor.HAVE_FIELD),Direction.OUTGOING).forEach(fieldRel->{
                    fieldRel.getEndNode().getRelationships(RelationshipType.withName(JavaCodeExtractor.TYPE),Direction.OUTGOING).forEach(typeRel->{
                        r.createEdge((String) typeRel.getEndNode().getProperty(JavaCodeExtractor.SIGNATURE),(String)owner.getProperty(JavaCodeExtractor.SIGNATURE),AGGREGATION);
                    });
                });
            });
            //CALL relationship
            db.getAllRelationships().stream().filter(rel->rel.getType().equals(RelationshipType.withName(JavaCodeExtractor.CALL_METHOD))).forEach(rel->{
                r.createEdge((String) rel.getStartNode().getProperty(JavaCodeExtractor.SIGNATURE),(String)rel.getEndNode().getProperty(JavaCodeExtractor.SIGNATURE),CALL);
            });
            tx.success();
        }
        r.weight();
        return r;
    }

}
