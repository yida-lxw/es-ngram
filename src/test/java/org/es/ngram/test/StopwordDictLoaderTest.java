package org.es.ngram.test;

import org.apache.lucene.analysis.CharArraySet;
import org.es.ngram.config.StopwordDictLoader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @author yida
 * @package org.es.ngram.test
 * @date 2024-04-12 14:03
 * @description Type your description over here.
 */
public class StopwordDictLoaderTest {
	public static void main(String[] args) throws IOException {
		String stopwordDictPath = "classpath:stopword.dic";
		CharArraySet stopwordSet = StopwordDictLoader.loadStopwordSet(stopwordDictPath);
		Iterator<Object> iterator = stopwordSet.iterator();
		while(iterator.hasNext()) {
			String word = String.valueOf((char[])(iterator.next()));
			System.out.println(word);
		}
	}
}
