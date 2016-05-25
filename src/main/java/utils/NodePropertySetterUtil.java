package utils;

import java.util.Date;

import org.neo4j.graphdb.Node;

/*
 * A utility class to help set a node's property.
 */
public class NodePropertySetterUtil {
	private static final NodePropertySetterUtil SETTER = new NodePropertySetterUtil();
	
	private NodePropertySetterUtil(){ }
	
	public static NodePropertySetterUtil getInstance(){
		return SETTER;
	}
	
	/*
	 * Set a property (key,value) to given node, and do nothing if node, key or value is null. 
	 */
	public NodePropertySetterUtil setNodeProperty(Node node, String key, Object value){
		if(node == null || key == null || value == null){
			return SETTER;
		}
		
		node.setProperty(key, value);
		return SETTER;
	}
	
	/*
	 * Set a property whose value type is java.util.Date, specially handling Date value is for range query.  
	 */
	public NodePropertySetterUtil setNodeProperty(Node node,String key, Date dateValue){
		if(node == null || key == null || dateValue == null){
			return SETTER;
		}
		
//		//the milliseconds since January 1, 1970, 00:00:00 GMT
//		long dateAsLong = dateValue.getTime();
//		node.setProperty(key, dateAsLong);
		
		
		return SETTER;
	}
}
