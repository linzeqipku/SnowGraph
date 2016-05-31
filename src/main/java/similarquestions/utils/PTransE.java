package similarquestions.utils;
import java.util.*;

public class PTransE {
	
	private static int NEG_SAMPLE_COUNT = 15;
	private static double BIAS = 7.0f;
		
	private int DIMENSION=100;
	private double STEP_SIZE=0.003;
	private int EPOCHES=100;
	private String NORM="L1";
	
	private List<String[]> trainExamples;
	
	private List<String> entityList;
	private List<String> headEntityList;
	private List<String> tailEntityList;
	private List<String> relationList;
	
	private HashMap<String, double[]> bestEntityEmbeddings;
	private HashMap<String, double[]> bestRelationEmbeddings;
	
	private HashMap<String, double[]> entityEmbeddings;
	private HashMap<String, double[]> relationEmbeddings;
	
	private HashSet<String> entitySet;
	private HashSet<String> headEntitySet;
	private HashSet<String> tailEntitySet;	
	private HashSet<String> relationSet;
	
	private Random rand;

	public PTransE(){	
		rand = new Random();
		trainExamples = new ArrayList<String[]>();
		entityEmbeddings = new HashMap<String, double[]>();
		relationEmbeddings = new HashMap<String, double[]>();
		bestEntityEmbeddings = new HashMap<String, double[]>();
		bestRelationEmbeddings = new HashMap<String, double[]>();
		entitySet = new HashSet<String>();
		headEntitySet = new HashSet<String>();
		tailEntitySet = new HashSet<String>();
		relationSet = new HashSet<String>();
		entityList = new ArrayList<String>();
		headEntityList = new ArrayList<String>();
		tailEntityList = new ArrayList<String>();
		relationList = new ArrayList<String>();
	}
	
	public void addTriple(String headEntity, String relation, String tailEntity){
		entitySet.add(headEntity);
		headEntitySet.add(headEntity);
		entitySet.add(tailEntity);
		tailEntitySet.add(tailEntity);
		relationSet.add(relation);
		String[] triple={headEntity,relation,tailEntity};
		trainExamples.add(triple);
	}
	
	public void init(){
		entityList.addAll(entitySet);
		headEntityList.addAll(headEntitySet);
		tailEntityList.addAll(tailEntitySet);
		relationList.addAll(relationSet);
		Iterator<String> entityIt = entitySet.iterator();
		while(entityIt.hasNext())
		{
			String entityKey = entityIt.next();
			
			double[] entityEmb = initEmb(); 
			entityEmbeddings.put(entityKey, entityEmb);
		}
		
		Iterator<String> relationIt = relationSet.iterator();
		
		while(relationIt.hasNext())
		{
			String relationKey = relationIt.next();
			
			double[] relationEmb = initEmb();
			normEmb(relationEmb);
			relationEmbeddings.put(relationKey, relationEmb);
		}
		
		bestEntityEmbeddings.clear();
		bestEntityEmbeddings.putAll(entityEmbeddings);
		bestRelationEmbeddings.clear();
		bestRelationEmbeddings.putAll(relationEmbeddings);
	}
	
	public Map<String, double[]> getBestEntityEmbeddings(){
		Map<String, double[]> r=new HashMap<String, double[]>();
		r.putAll(bestEntityEmbeddings);
		return r;
	}
	
	public Map<String, double[]> getBestRelationEmbeddings(){
		Map<String, double[]> r=new HashMap<String, double[]>();
		r.putAll(bestRelationEmbeddings);
		return r;
	}
	
	private double[] initEmb()
	{
		double[] embedding = new double[DIMENSION];
		for(int i = 0; i < DIMENSION; i++)
		{
			embedding[i] = (double) ((rand.nextDouble() - 0.5) / 0.5 * 6.0 / Math.sqrt(DIMENSION * 1.0));
		}
		return embedding;
	}
	
	private void normEmb(double[] emb)
	{
		double mode = 0.0f;
		for(int i = 0; i < DIMENSION; i++)
		{
			mode += emb[i] * emb[i];
		}
		mode = (double)Math.sqrt(mode);
		for(int i = 0; i < DIMENSION; i++)
			emb[i] /= mode;
	}
	private double logisticFunction(double param)
	{
		return (double) (1.0/(1.0 + Math.exp(-param)));
	}
	
	private void updateGradient(HashMap<String, double[]> gradientEmbHashMap, String key, double[] gradientEmb)
	{
		if(gradientEmbHashMap.containsKey(key))
		{
			double[] tmpEmb = gradientEmbHashMap.get(key); 
			gradientEmbHashMap.put(key, embCalculator(tmpEmb, "+", gradientEmb));
		}
		else {
			double[] tmpEmb = new double[DIMENSION];
			for(int i = 0; i < tmpEmb.length; i++)
				tmpEmb[i] = 0.0f;
			gradientEmbHashMap.put(key, embCalculator(tmpEmb, "+", gradientEmb));
		}
	}
	
	public void train()
	{
		for(int i = 0; i < EPOCHES; i++)
		{				
			Collections.shuffle(trainExamples);
			double totalSum = 0.0f;
			
			for(int j = 0; j < trainExamples.size(); j++)
			{
				HashMap<String, double[]> gradientEntityEmbHashMap = new HashMap<String, double[]>();
				HashMap<String, double[]> gradientRelationEmbHashMap = new HashMap<String, double[]>();
				
				String[] posTriplet = trainExamples.get(j);
				
				String posHeadEntity = posTriplet[0];
				String posTailEntity = posTriplet[2];
				String posRelation = posTriplet[1];
				
				double[] posHeadEmb = entityEmbeddings.get(posHeadEntity);
				
				double[] posTailEmb = entityEmbeddings.get(posTailEntity);
				
				double[] posRelationEmb = relationEmbeddings.get(posRelation);
				
				double[] posDistanceEmb = getDistanceEmb(posHeadEmb, posRelationEmb, posTailEmb); //h + r - t
				
				double posDistance = norm(posDistanceEmb); //让他小
				double posLogisticDistance = logisticFunction(BIAS - 0.5f * posDistance);
				//System.out.println("POS: " + posDistance);
				//System.out.println(Math.log(posLogisticDistance));
				totalSum += Math.log(posLogisticDistance);
				
				double posGradientParam = 1.0f - posLogisticDistance;	
					
				double[] posUpdatedGradientEmb = embCalculator(posDistanceEmb, "*", STEP_SIZE * posGradientParam);
				
				double[] posHeadGradientEmb = embCalculator(posUpdatedGradientEmb, "*", -3.0f);
				double[] posRelationGradientEmb = embCalculator(posUpdatedGradientEmb, "*", -3.0f);
				double[] posTailGradientEmb = embCalculator(posUpdatedGradientEmb, "*", 3.0f);
				

				updateGradient(gradientEntityEmbHashMap, posHeadEntity, posHeadGradientEmb);
				updateGradient(gradientRelationEmbHashMap, posRelation, posRelationGradientEmb);
				updateGradient(gradientEntityEmbHashMap, posTailEntity, posTailGradientEmb);
				
				
				for(int k = 0; k < NEG_SAMPLE_COUNT; k++)
				{
					String[] headNegTriplet = getHeadNegTriplet(posTriplet);				
					String negHeadEntity = headNegTriplet[0];
					
					double[] negHeadEmb = entityEmbeddings.get(negHeadEntity);
					double[] negDistanceEmb = getDistanceEmb(negHeadEmb, posRelationEmb, posTailEmb);
					double negDistance = norm(negDistanceEmb);
					double negLogisticDistance = logisticFunction(BIAS - 0.5f * negDistance);	
					//System.out.println("NEG: " + negDistance);
					totalSum += Math.log(1.0f - negLogisticDistance);
					
					double negGradientParam = - negLogisticDistance;					
					double[] negGradientEmb = getGradientEmb(negDistanceEmb);
					
					double[] negUpdatedGradientEmb = embCalculator(negGradientEmb, "*", STEP_SIZE * negGradientParam);

					double[] negHeadGradientEmb = embCalculator(negUpdatedGradientEmb, "*", -1.0f);
					double[] negRelationGradientEmb = embCalculator(negUpdatedGradientEmb, "*", -1.0f);
					double[] negTailGradientEmb = embCalculator(negUpdatedGradientEmb, "*", +1.0f);
					
					updateGradient(gradientEntityEmbHashMap, negHeadEntity, negHeadGradientEmb);
					updateGradient(gradientRelationEmbHashMap, posRelation, negRelationGradientEmb);
					updateGradient(gradientEntityEmbHashMap, posTailEntity, negTailGradientEmb);
									
					String[] tailNegTriplet = getTailNegTriplet(posTriplet);				
					String negTailEntity = tailNegTriplet[2];
					
					double[] negTailEmb = entityEmbeddings.get(negTailEntity);
					negDistanceEmb = getDistanceEmb(posHeadEmb, posRelationEmb, negTailEmb);
					negDistance = norm(negDistanceEmb);
					negLogisticDistance = logisticFunction(BIAS - 0.5f * negDistance);					
					totalSum += Math.log(1.0f - negLogisticDistance);
					
					negGradientParam = - negLogisticDistance;					
					negGradientEmb = getGradientEmb(negDistanceEmb);
					
					negUpdatedGradientEmb = embCalculator(negGradientEmb, "*", STEP_SIZE * negGradientParam);

					negHeadGradientEmb = embCalculator(negUpdatedGradientEmb, "*", -1.0f);
					negRelationGradientEmb = embCalculator(negUpdatedGradientEmb, "*", -1.0f);
					negTailGradientEmb = embCalculator(negUpdatedGradientEmb, "*", 1.0f);
					
					updateGradient(gradientEntityEmbHashMap, posHeadEntity, negHeadGradientEmb);
					updateGradient(gradientRelationEmbHashMap, posRelation, negRelationGradientEmb);
					updateGradient(gradientEntityEmbHashMap, negTailEntity, negTailGradientEmb);
				
				}
				
				String[] relationNegTriplet = getRelationNegTriplet(posTriplet);				
				String negRelation = relationNegTriplet[1];
				
				double[] negRelationEmb = relationEmbeddings.get(negRelation);
				double[] negDistanceEmb = getDistanceEmb(posHeadEmb, negRelationEmb, posTailEmb);
				double negDistance = norm(negDistanceEmb);
				double negLogisticDistance = logisticFunction(BIAS - 0.5f * negDistance);					
				totalSum += Math.log(1.0f - negLogisticDistance);
				
				double negGradientParam = - negLogisticDistance;					
				double[] negGradientEmb = getGradientEmb(negDistanceEmb);
				
				double[] negUpdatedGradientEmb = embCalculator(negGradientEmb, "*", STEP_SIZE * negGradientParam);

				double[] negHeadGradientEmb = embCalculator(negUpdatedGradientEmb, "*", -1.0f);
				double[] negRelationGradientEmb = embCalculator(negUpdatedGradientEmb, "*", -1.0f);
				double[] negTailGradientEmb = embCalculator(negUpdatedGradientEmb, "*", 1.0f);
				
				updateGradient(gradientEntityEmbHashMap, posHeadEntity, negHeadGradientEmb);
				updateGradient(gradientRelationEmbHashMap, negRelation, negRelationGradientEmb);
				updateGradient(gradientEntityEmbHashMap, posTailEntity, negTailGradientEmb);
				
				Iterator<String> entityKeys = gradientEntityEmbHashMap.keySet().iterator();
				while(entityKeys.hasNext())
				{
					String entityKey = entityKeys.next();
//					System.out.println(entityKey);
//					displayEmb(gradientEntityEmbHashMap.get(entityKey));
					
					double[] tmpEmb = entityEmbeddings.get(entityKey);
					//displayEmb(tmpEmb);
					entityEmbeddings.put(entityKey, embCalculator(tmpEmb, "+", gradientEntityEmbHashMap.get(entityKey)));	 
					
				}
				
				Iterator<String> relationKeys = gradientRelationEmbHashMap.keySet().iterator();
				while(relationKeys.hasNext())
				{
					String relationKey = relationKeys.next();
//					System.out.println(relationKey);
//					displayEmb(gradientRelationEmbHashMap.get(relationKey));	
					
					double[] tmpEmb = relationEmbeddings.get(relationKey);
					relationEmbeddings.put(relationKey, embCalculator(tmpEmb, "+", gradientRelationEmbHashMap.get(relationKey)));	 				
				}
				
			}
			System.out.println(i);
			
			System.out.println("TRAIN AVG LOSS: " + totalSum / trainExamples.size());
			bestEntityEmbeddings.clear();
			bestEntityEmbeddings.putAll(entityEmbeddings);
			bestRelationEmbeddings.clear();
			bestRelationEmbeddings.putAll(relationEmbeddings);
		}
	}
			
	private String[] getHeadNegTriplet(String[] posTriplet)
	{
		String[] negTriplet = new String[3];
		String corruptedHeadEntity = posTriplet[0];
		while(corruptedHeadEntity.equals(posTriplet[0]))
		{
			int corruptedHeadIdx = rand.nextInt(headEntityList.size());
			corruptedHeadEntity = headEntityList.get(corruptedHeadIdx);
		}
		negTriplet[0] = corruptedHeadEntity;
		negTriplet[1] = posTriplet[1];
		negTriplet[2] = posTriplet[2];
		return negTriplet;
	}
		
	private String[] getTailNegTriplet(String[] posTriplet)
	{
		String[] negTriplet = new String[3];
		String corruptedTailEntity = posTriplet[2];
		while(corruptedTailEntity.equals(posTriplet[2]))
		{
			int corruptedTailIdx = rand.nextInt(tailEntityList.size());
			corruptedTailEntity = tailEntityList.get(corruptedTailIdx);
		}
		negTriplet[0] = posTriplet[0];
		negTriplet[1] = posTriplet[1];
		negTriplet[2] = corruptedTailEntity;
		return negTriplet;
	}
	
	private String[] getRelationNegTriplet(String[] posTriplet)
	{
		String[] negTriplet = new String[3];
		String corruptedRelationEntity = posTriplet[1];
		while(corruptedRelationEntity.equals(posTriplet[1]))
		{
			int corruptedRelationIdx = rand.nextInt(relationList.size());
			corruptedRelationEntity = relationList.get(corruptedRelationIdx);
		}
		negTriplet[0] = posTriplet[0];
		negTriplet[1] = corruptedRelationEntity;
		negTriplet[2] = posTriplet[2];
		return negTriplet;
	}
	
	private double norm(double[] embedding)
	{
		double mode = 0.0f;
		
		if(NORM.equals("L1"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				mode += Math.abs(embedding[i]);
			}
			
		}
		else if(NORM.equals("L2"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				mode += embedding[i] * embedding[i];
			}
		}
		else {
			
		}
	
		return mode;
	}
	
	private double[] embCalculator(double[] firstEmb, String operator, double[] secondEmb)
	{
		double[] resultEmb = new double[DIMENSION];
		
		if(operator.equals("+"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				resultEmb[i] = firstEmb[i] + secondEmb[i];
			}
		}
		
		else if (operator.equals("-"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				resultEmb[i] = firstEmb[i] - secondEmb[i];
			}
		}
		else
		{
			
		}
		return resultEmb;
			
	}
	
	private double[] embCalculator(double[] firstEmb, String operator, double second)
	{
		double[] resultEmb = new double[DIMENSION];
		
		if(operator.equals("*"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				resultEmb[i] = firstEmb[i] * second;
			}
		}
		
		else if (operator.equals("/"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				resultEmb[i] = firstEmb[i] / second;
			}
		}
		else
		{
			
		}
		return resultEmb;
	}
	
	private double[] getGradientEmb(double[] distanceEmb)
	{
		double[] gradientEmb = new double[DIMENSION];
		if(NORM.equals("L1"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				if(distanceEmb[i] > 0.0f)
					gradientEmb[i] = 1.0f;
				else 
					gradientEmb[i] = -1.0f;
			}
		}
		else if(NORM.equals("L2"))
		{
			for(int i = 0; i < DIMENSION; i++)
			{
				gradientEmb[i] = distanceEmb[i];
			}
		}
		return gradientEmb;
	}
	
	private double[] getDistanceEmb(double[] headEmb, double[] relationEmb, double[] tailEmb)
	{
		return embCalculator(embCalculator(headEmb, "+", relationEmb), "-", tailEmb);
	}
	
	public static void main(String[] args) throws Exception
	{
		PTransE ptranse = new PTransE();
		//ptranse.addTriple(...);
		ptranse.init();
		ptranse.train();
	}
	
}