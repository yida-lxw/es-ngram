package org.es.ngram.index.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.es.ngram.config.StopwordDictLoader;

import java.util.regex.Pattern;

/**
 * @author yida
 * @package org.es.ngram.analyzer
 * @date 2024-04-12 15:19
 * @description Type your description over here.
 */
public class NGramAnalyzer extends Analyzer {
	public static final Log log = LogFactory.getLog(NGramAnalyzer.class);

	public static final int DEFAULT_MIN_GRAM_SIZE = 2;
	public static final int DEFAULT_MAX_GRAM_SIZE = 100;
	public static final boolean DEFAULT_KEEP_ORIGNAL_TERM = false;

	private final static String stopwordDictPath = "classpath:stopword.dic";

	private Pattern pattern = Pattern.compile("(?i)[^a-zA-Z0-9\\u4E00-\\u9FA5]");

	private int minGram;
	private int maxGram;
	private boolean keepOrignalTerm;
	private STConvertType stConvertType;

	/**
	 * An immutable stopword set
	 */
	protected final CharArraySet stopwords;

	/**
	 * Returns the analyzer's stopword set or an empty set if the analyzer has no
	 * stopwords
	 *
	 * @return the analyzer's stopword set or an empty set if the analyzer has no
	 * stopwords
	 */
	public CharArraySet getStopwordSet() {
		return stopwords;
	}

	/**
	 * Creates a new instance initialized with the given stopword set
	 */
	public NGramAnalyzer(int minGram, int maxGram, boolean keepOrignalTerm, STConvertType stConvertType) {
		CharArraySet charArraySet = null;
		try {
			charArraySet = StopwordDictLoader.loadStopwordSet(stopwordDictPath);
		} catch (Exception e) {
			log.error("Load stopword dictionary file occr exception:\n" + e.getMessage());
		}
		this.stopwords = (charArraySet == null) ? CharArraySet.EMPTY_SET : CharArraySet
				.unmodifiableSet(CharArraySet.copy(charArraySet));
		if (minGram <= 0) {
			minGram = DEFAULT_MIN_GRAM_SIZE;
		}
		if (maxGram <= 0 || maxGram > 255) {
			maxGram = DEFAULT_MAX_GRAM_SIZE;
		}
		if (null == stConvertType) {
			stConvertType = STConvertType.TRADITIONAL_2_SIMPLE;
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.keepOrignalTerm = keepOrignalTerm;
		this.stConvertType = stConvertType;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		//Reader reader = new BufferedReader(new StringReader(fieldName));
		final Tokenizer source = new WhitespaceTokenizer();
		//PatternReplaceFilter patternReplaceCharFilter = new PatternReplaceFilter(source, pattern, "", true);
		//StopFilter stopFilter = new StopFilter(patternReplaceCharFilter, this.stopwords);
		LowerCaseFilter lowerCaseFilter = new LowerCaseFilter(source);
		STConvertTokenFilter stConvertTokenFilter = new STConvertTokenFilter(lowerCaseFilter, this.stConvertType, ",", false);
		org.es.ngram.index.analysis.NGramTokenFilter nGramTokenFilter = new org.es.ngram.index.analysis.NGramTokenFilter(stConvertTokenFilter, minGram, maxGram, this.keepOrignalTerm);
		return new TokenStreamComponents(source, nGramTokenFilter);
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		return new LowerCaseFilter(in);
	}

	public int getMinGram() {
		return minGram;
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public int getMaxGram() {
		return maxGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

	public boolean isKeepOrignalTerm() {
		return keepOrignalTerm;
	}

	public void setKeepOrignalTerm(boolean keepOrignalTerm) {
		this.keepOrignalTerm = keepOrignalTerm;
	}

	public CharArraySet getStopwords() {
		return stopwords;
	}

	public STConvertType getStConvertType() {
		return stConvertType;
	}

	public void setStConvertType(STConvertType stConvertType) {
		this.stConvertType = stConvertType;
	}
}
