package similarquestions.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class Features implements Serializable{

	public Map<Pair<Long, Long>, Double> surfaceFeature=new HashMap<Pair<Long, Long>, Double>();
	public Map<Pair<Long, Long>, Double> topicFeature=new HashMap<Pair<Long, Long>, Double>();
	public Map<Pair<Long, Long>, Double> word2vecFeature=new HashMap<Pair<Long, Long>, Double>();
	public Map<Pair<Long, Long>, Double> code2vecFeature=new HashMap<Pair<Long, Long>, Double>();
	public Set<Pair<Long, Long>> standards=new HashSet<Pair<Long, Long>>();
	
}
