package org.es.ngram.index.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * @author yida
 * @package org.es.ngram.index.analysis
 * @date 2024-04-12 16:26
 * @description Type your description over here.
 */
public class StopwordTokenFilterFactory extends AbstractTokenFilterFactory {

	private String stopwordDictPath;

	public StopwordTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
		super(indexSettings, name, settings);
		this.stopwordDictPath = settings.get("stopword_dict_path", (String)null);
	}

	public static StopwordTokenFilterFactory getStopwordTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
		return new StopwordTokenFilterFactory(indexSettings, env, name, settings);
	}

	@Override
	public TokenFilter create(TokenStream input) {
		return new StopwordTokenFilter(input, this.stopwordDictPath);
	}

	public String getStopwordDictPath() {
		return stopwordDictPath;
	}

	public void setStopwordDictPath(String stopwordDictPath) {
		this.stopwordDictPath = stopwordDictPath;
	}
}
