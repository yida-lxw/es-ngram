package org.es.ngram.plugin.analysis.ik;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.es.ngram.index.analysis.EdgeNGramAnalyzerProvider;
import org.es.ngram.index.analysis.NGramAnalyzerProvider;
import org.es.ngram.index.analysis.STConvertTokenFilterFactory;
import org.es.ngram.index.analysis.StopwordTokenFilterFactory;

import java.util.HashMap;
import java.util.Map;

public class EdgeNGramAnalysisPlugin extends Plugin implements AnalysisPlugin {
    public static String PLUGIN_NAME = "edge-ngram";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new HashMap<>();
        extra.put("stopword-filter", StopwordTokenFilterFactory::getStopwordTokenFilterFactory);
        extra.put("tsconvert-filter", STConvertTokenFilterFactory::getSTConvertTokenFilterFactory);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();
        extra.put("edge-ngram-analyzer", EdgeNGramAnalyzerProvider::getEdgeNGramAnalyzerProvider);
        extra.put("ngram-analyzer", NGramAnalyzerProvider::getNGramAnalyzerProvider);
        return extra;
    }
}
