package org.es.ngram.index.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.es.ngram.config.StopwordDictLoader;

import java.util.Arrays;
import java.util.List;

/**
 * @author yida
 * @package org.es.ngram.index.analysis
 * @date 2024-04-12 16:00
 * @description 停用词过滤器
 */
public class StopwordTokenFilter extends FilteringTokenFilter {
	public static final Log log = LogFactory.getLog(StopwordTokenFilter.class);

	private final static String DEFAULT_STOPWORD_DICTPATH = "classpath:stopword.dic";

	private final CharArraySet stopWords;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	private String stopwordDictPath;
	/**
	 * Constructs a filter which removes words from the input TokenStream that are
	 * named in the Set.
	 *
	 * @param in
	 *          Input stream
	 * @see #makeStopSet(java.lang.String...)
	 */
	public StopwordTokenFilter(TokenStream in, String stopwordDictPath) {
		super(in);
		if(null == stopwordDictPath || stopwordDictPath.length() <= 0) {
			stopwordDictPath = DEFAULT_STOPWORD_DICTPATH;
		}
		this.stopwordDictPath = stopwordDictPath;
		CharArraySet charArraySet = null;
		try {
			charArraySet = StopwordDictLoader.loadStopwordSet(this.stopwordDictPath);
		} catch (Exception e) {
			log.error("Load stopword dictionary file occr exception:\n" + e.getMessage());
		}
		this.stopWords = (charArraySet == null) ? CharArraySet.EMPTY_SET : CharArraySet
				.unmodifiableSet(CharArraySet.copy(charArraySet));
	}

	/**
	 * Builds a Set from an array of stop words,
	 * appropriate for passing into the StopFilter constructor.
	 * This permits this stopWords construction to be cached once when
	 * an Analyzer is constructed.
	 *
	 * @param stopWords An array of stopwords
	 * @see #makeStopSet(java.lang.String[], boolean) passing false to ignoreCase
	 */
	public static CharArraySet makeStopSet(String... stopWords) {
		return makeStopSet(stopWords, false);
	}

	/**
	 * Builds a Set from an array of stop words,
	 * appropriate for passing into the StopFilter constructor.
	 * This permits this stopWords construction to be cached once when
	 * an Analyzer is constructed.
	 *
	 * @param stopWords A List of Strings or char[] or any other toString()-able list representing the stopwords
	 * @return A Set ({@link CharArraySet}) containing the words
	 * @see #makeStopSet(java.lang.String[], boolean) passing false to ignoreCase
	 */
	public static CharArraySet makeStopSet(List<?> stopWords) {
		return makeStopSet(stopWords, false);
	}

	/**
	 * Creates a stopword set from the given stopword array.
	 *
	 * @param stopWords An array of stopwords
	 * @param ignoreCase If true, all words are lower cased first.
	 * @return a Set containing the words
	 */
	public static CharArraySet makeStopSet(String[] stopWords, boolean ignoreCase) {
		CharArraySet stopSet = new CharArraySet(stopWords.length, ignoreCase);
		stopSet.addAll(Arrays.asList(stopWords));
		return stopSet;
	}

	/**
	 * Creates a stopword set from the given stopword list.
	 * @param stopWords A List of Strings or char[] or any other toString()-able list representing the stopwords
	 * @param ignoreCase if true, all words are lower cased first
	 * @return A Set ({@link CharArraySet}) containing the words
	 */
	public static CharArraySet makeStopSet(List<?> stopWords, boolean ignoreCase){
		CharArraySet stopSet = new CharArraySet(stopWords.size(), ignoreCase);
		stopSet.addAll(stopWords);
		return stopSet;
	}

	/**
	 * Returns the next input Token whose term() is not a stop word.
	 */
	@Override
	protected boolean accept() {
		return !stopWords.contains(termAtt.buffer(), 0, termAtt.length());
	}

	public String getStopwordDictPath() {
		return stopwordDictPath;
	}

	public void setStopwordDictPath(String stopwordDictPath) {
		this.stopwordDictPath = stopwordDictPath;
	}
}
