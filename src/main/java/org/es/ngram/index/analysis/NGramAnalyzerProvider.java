package org.es.ngram.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class NGramAnalyzerProvider extends AbstractIndexAnalyzerProvider<NGramAnalyzer> {
    private final NGramAnalyzer analyzer;

    public NGramAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings,name, settings);
        String minGramStr = settings.get("min_gram", "2");
        String maxGramStr = settings.get("max_gram", "100");
        String stConvertTypeStr = settings.get("st_convert_type", "t2s");
        boolean keepOrignalTerm = settings.get("keep_orignal_gram", "false").equals("true");
        STConvertType stConvertType = ("t2s".equals(stConvertTypeStr))? STConvertType.TRADITIONAL_2_SIMPLE : STConvertType.SIMPLE_2_TRADITIONAL;
        analyzer = new NGramAnalyzer(Integer.valueOf(minGramStr), Integer.valueOf(maxGramStr), keepOrignalTerm, stConvertType);
    }

    public static NGramAnalyzerProvider getNGramAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new NGramAnalyzerProvider(indexSettings, env, name, settings);
    }

    @Override public NGramAnalyzer get() {
        return this.analyzer;
    }
}
