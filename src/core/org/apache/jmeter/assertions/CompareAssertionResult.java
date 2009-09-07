package org.apache.jmeter.assertions;


public class CompareAssertionResult extends AssertionResult {
	private static final long serialVersionUID = 1;
	
	private transient final ResultHolder comparedResults = new ResultHolder();

	/**
	 * For testing only
	 * @deprecated Use the other ctor
	 */
	@Deprecated
    public CompareAssertionResult() { // needs to be public for testing
		super();
	}

	public CompareAssertionResult(String name) {
		super(name);
	}
	
	public void addToBaseResult(String resultData)
	{
		comparedResults.addToBaseResult(resultData);
	}
	
	public void addToSecondaryResult(String resultData)
	{
		comparedResults.addToSecondaryResult(resultData);
	}
	
	public String getBaseResult()
	{
		return comparedResults.baseResult;
	}
	
	public String getSecondaryResult()
	{
		return comparedResults.secondaryResult;
	}

	private static class ResultHolder
	{
		private String baseResult;
		private String secondaryResult;
		
		public ResultHolder()
		{
			
		}
		
		public void addToBaseResult(String r)
		{
			if(baseResult == null)
			{
				baseResult = r;
			}
			else
			{
				baseResult = baseResult + "\n\n" + r;
			}
		}
		
		public void addToSecondaryResult(String r)
		{
			if(secondaryResult == null)
			{
				secondaryResult = r;
			}
			else
			{
				secondaryResult = secondaryResult + "\n\n" + r;
			}
		}
	}
}
