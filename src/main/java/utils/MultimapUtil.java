package utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class MultimapUtil {
	/*
	 * 删除所有具有多个值的键值对
	 */
	public static <K,V> void removeAllKeysWithMultiValues(Multimap<K,V> map){
		Set<K> duplicateKeys = new HashSet<K>();
		
		for(K key:map.keySet()){
			//一个key对应具有多个values
			if(map.get(key).size() > 1){
				duplicateKeys.add(key);
			}
		}
		
		//删除所有具有多个值的键
		for(K duplicateKey:duplicateKeys){
			map.removeAll(duplicateKey);
		}
	}
	
	public static <K,V> V getUniqueValue(Multimap<K,V> map, K key){
		Collection<V> values = map.get(key);
		if(values == null){
			return null;
		}
		
		Iterator<V> iter = values.iterator();
		V value = null;
		if(iter.hasNext()){
			value = iter.next();
		}
		
		if(iter.hasNext()){//More than one value
			return null;
		}else{
			return value;
		}
	}
	
	public static void main(String[] args) {
		Multimap<String,String> map = ArrayListMultimap.create();
		map.put("1", "1");
		map.put("1", "01");
		map.put("2", "2");
		map.put("4", "4");
		map.put("3", "3");
		map.put("3", "11");
		
		System.out.println(getUniqueValue(map,"1"));
		System.out.println(getUniqueValue(map,"2"));
		System.out.println(getUniqueValue(map,"3"));
		System.out.println(getUniqueValue(map,"4"));
		
		removeAllKeysWithMultiValues(map);
		
		for(String key:map.keySet()){
			System.out.print(key + "\t");				
			for(String value:map.get(key)){
				System.out.print(value + "\t");
			}
			System.out.println();
		}
	}
}
