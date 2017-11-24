package graphdb.extractors.miners.apiusage.codeslice;

class Relevancy implements Comparable<Relevancy>
{
	private String invokedMethodKey;
	private String testMethodKey;
	private double nameRelevancy;
	private int callCount;
	private double tfidf;

	@Override
	public int compareTo(Relevancy r)
	{
		if (nameRelevancy == r.nameRelevancy)
			if (callCount == r.callCount)
				return 0;
			else
				return new Integer(callCount).compareTo(new Integer(r.callCount));
		else
			return new Double(nameRelevancy).compareTo(new Double(r.nameRelevancy));
	}

	public String getInvokedMethodKey()
	{
		return invokedMethodKey;
	}

	public void setInvokedMethodKey(String invokedMethodKey)
	{
		this.invokedMethodKey = invokedMethodKey;
	}

	public String getTestMethodKey()
	{
		return testMethodKey;
	}

	public void setTestMethodKey(String testMethodKey)
	{
		this.testMethodKey = testMethodKey;
	}

	public double getNameRelevancy()
	{
		return nameRelevancy;
	}

	public void setNameRelevancy(double nameRelevancy)
	{
		this.nameRelevancy = nameRelevancy;
	}

	private int getCallCount()
	{
		return callCount;
	}

	public void setCallCount(int callCount)
	{
		this.callCount = callCount;
	}

	private double getTfidf()
	{
		return tfidf;
	}

	public void setTfidf(double tfidf)
	{
		this.tfidf = tfidf;
	}

	public String toString()
	{
		return invokedMethodKey + "\t" + nameRelevancy + "\t" + testMethodKey;
	}

	public boolean hasSameRelevancyWith(Object o)
	{
		if (o instanceof Relevancy)
		{
			Relevancy r = (Relevancy) o;
			return (nameRelevancy == r.getNameRelevancy()) && (callCount == r.getCallCount())
					&& (tfidf == r.getTfidf());
		}
		else
			return false;
	}
}
