package edu.buffalo.cse.irf14.analysis;

public class CapitalizationFilter extends TokenFilter {

	TokenStream tStream = null;

	public CapitalizationFilter(TokenStream stream) {
		super(stream);
		this.tStream = stream;
	}

	TokenStream tokenStream = new TokenStream();

	public void captilizationFilter(TokenStream tStream) throws FilterException {
		try {
			if (tStream != null && tStream.hasNext()) {
				tStream.next();
				Token tokens = tStream.getCurrent();
				if (tokens != null) {
					String token = tokens.getTermText();
					if (token != null) {
						Token token2 = new Token();
						token2.setTermText(token.toLowerCase());
						tokenStream.addTokenToStream(token2);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FilterException("Exception in Capitilization Filter");
		}
	}

	@Override
	public boolean increment() throws TokenizerException {
		try {
			captilizationFilter(tStream);
			if (tStream != null && tStream.hasNext())
				return true;
			else
				return false;
		} catch (FilterException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public TokenStream getStream() {
		return tokenStream;
	}

	@Override
	public void processThroughFilters() {
	}

}