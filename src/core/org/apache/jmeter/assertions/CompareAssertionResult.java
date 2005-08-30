package org.apache.jmeter.assertions;


public class CompareAssertionResult extends AssertionResult {
	private static final long serialVersionUID = 1;
	
	private ResultHolder comparedResults = new ResultHolder();

	public CompareAssertionResult() {
		super();
	}

	public CompareAssertionResult(boolean failure, boolean error, String message) {
		super(failure, error, message);
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

	private class ResultHolder
	{
		String baseResult;
		String secondaryResult;
		
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
