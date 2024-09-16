package org.es.ngram.config;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.es.ngram.utils.ResourceFileUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author yida
 * @package org.elasticsearch.config
 * @date 2024-04-12 13:33
 * @description 停用词词典加载器
 */
public class StopwordDictLoader {
	private static final int INITIAL_CAPACITY = 16;

	public static List<String> loadStopwordList(String dictPath) throws IOException {
		List<String> stopwordList = new ArrayList<>();
		CharArraySet charArraySet = loadStopwordSet(true, dictPath, "UTF-8");
		if(null == charArraySet || charArraySet.size() <= 0) {
			return stopwordList;
		}
		Iterator<Object> iterator = charArraySet.iterator();
		while(iterator.hasNext()) {
			String word = String.valueOf((char[])(iterator.next()));
			stopwordList.add(word);
		}
		return stopwordList;
	}

	public static CharArraySet loadStopwordSet(String dictPath) throws IOException {
		return loadStopwordSet(true, dictPath, "UTF-8");
	}

	public static CharArraySet loadStopwordSet(String dictPath, String charset) throws IOException {
		return loadStopwordSet(true, dictPath, charset);
	}

	public static CharArraySet loadStopwordSet(final boolean ignoreCase, String dictPath, String charset) throws IOException {
		Reader reader = null;
		CharArraySet charArraySet = new CharArraySet(INITIAL_CAPACITY, ignoreCase);
		try {
			reader = ResourceFileUtils.getReader(dictPath, charset);
			charArraySet = WordlistLoader.getWordSet(reader, charArraySet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(reader);
		}
		return charArraySet;
	}

	/**
	 * Creates a CharArraySet from a file.
	 *
	 * @param stopwords
	 *          the stopwords reader to load
	 *
	 * @return a CharArraySet containing the distinct stopwords from the given
	 *         reader
	 * @throws IOException
	 *           if loading the stopwords throws an {@link IOException}
	 */
	public static CharArraySet loadStopwordSet(Reader stopwords) throws IOException {
		try {
			return WordlistLoader.getWordSet(stopwords);
		} finally {
			IOUtils.close(stopwords);
		}
	}
}
