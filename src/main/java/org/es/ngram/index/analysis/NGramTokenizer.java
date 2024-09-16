package org.es.ngram.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

/**
 * @author yida
 * @package org.es.ngram.index.analysis
 * @date 2024-04-17 21:19
 * @description Type your description over here.
 */
public final class NGramTokenizer extends Tokenizer {
	public static final int DEFAULT_MIN_NGRAM_SIZE = 2;
	public static final int DEFAULT_MAX_NGRAM_SIZE = 100;

	private int minGram, maxGram;
	private int gramSize;
	private int pos = 0;
	private int inLen;
	private String inStr;
	private boolean started = false;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	/**
	 * Creates NGramTokenizer with given min and max n-grams.
	 * @param factory {@link AttributeFactory} holding the input to be tokenized
	 * @param minGram the smallest n-gram to generate
	 * @param maxGram the largest n-gram to generate
	 */
	public NGramTokenizer(AttributeFactory factory, int minGram, int maxGram) {
		super(factory);
		init(minGram, maxGram);
	}

	/**
	 * Creates NGramTokenizer with default min and max n-grams.
	 * @param factory {@link AttributeFactory} holding the input to be tokenized
	 */
	public NGramTokenizer(AttributeFactory factory) {
		this(factory, DEFAULT_MIN_NGRAM_SIZE, DEFAULT_MAX_NGRAM_SIZE);
	}

	private void init(int minGram, int maxGram) {
		if (minGram < 1) {
			throw new IllegalArgumentException("minGram must be greater than zero");
		}
		if (minGram > maxGram) {
			throw new IllegalArgumentException("minGram must not be greater than maxGram");
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
	}

	/** Returns the next token in the stream, or null at EOS. */
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		if (!started) {
			started = true;
			gramSize = minGram;
			char[] chars = new char[1024];
			input.read(chars);
			inStr = new String(chars).trim();  // remove any trailing empty strings
			inLen = inStr.length();
		}

		if (pos+gramSize > inLen) {            // if we hit the end of the string
			pos = 0;                           // reset to beginning of string
			gramSize++;                        // increase n-gram size
			if (gramSize > maxGram)            // we are done
				return false;
			if (pos+gramSize > inLen)
				return false;
		}

		int oldPos = pos;
		pos++;
		termAtt.setEmpty().append(inStr, oldPos, oldPos+gramSize);
		offsetAtt.setOffset(correctOffset(oldPos), correctOffset(oldPos+gramSize));
		return true;
	}

	@Override
	public final void end() {
		// set final offset
		final int finalOffset = inLen;
		this.offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		started = false;
		pos = 0;
	}
}
