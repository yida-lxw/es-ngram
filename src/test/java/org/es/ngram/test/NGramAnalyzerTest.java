package org.es.ngram.test;

import org.apache.lucene.analysis.Analyzer;
import org.es.ngram.index.analysis.NGramAnalyzer;
import org.es.ngram.index.analysis.STConvertType;
import org.es.ngram.utils.AnalyzerUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author yida
 * @package org.es.ngram.test
 * @date 2024-06-16 18:51
 * @description Type your description over here.
 */
public class NGramAnalyzerTest {
	public static void main(String[] args) throws IOException {
		String sourceFilePath = "F:/tmp/tmp_index.txt";
		List<String> lines =  Files.readAllLines(Paths.get(sourceFilePath));
		StringBuilder stringBuilder = new StringBuilder();
		for (String line : lines) {
			stringBuilder.append(line).append("\n");
		}
		Analyzer analyzer = new NGramAnalyzer(2, 20, false, STConvertType.TRADITIONAL_2_SIMPLE);
		String text = stringBuilder.toString();
		AnalyzerUtils.displayTokens(analyzer, text);
	}
}
