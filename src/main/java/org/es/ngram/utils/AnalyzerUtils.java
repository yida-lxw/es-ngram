package org.es.ngram.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

/**
 * @author yida
 * @package org.es.ngram.utils
 * @date 2024-06-16 18:52
 * @description Type your description over here.
 */
public class AnalyzerUtils {

	public static void displayTokens(Analyzer analyzer, String text) throws IOException {
		TokenStream tokenStream = analyzer.tokenStream("text", text);
		displayTokens(tokenStream);
	}

	public static void displayTokens(TokenStream tokenStream) throws IOException {
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

		tokenStream.reset();
		int position = 0;
		while (tokenStream.incrementToken()) {
			int increment = positionIncrementAttribute.getPositionIncrement();
			if (increment > 0) {
				position = position + increment;
				System.out.print(position + ":");
			}
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString();
			System.out.println("[" + term + "]" + ":(" + startOffset + "-->" + endOffset + "):" + typeAttribute.type());
		}
		tokenStream.close();
	}
}
