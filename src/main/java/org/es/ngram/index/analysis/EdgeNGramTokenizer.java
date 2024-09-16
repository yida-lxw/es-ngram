package org.es.ngram.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * @author yida
 * @package org.es.ngram.index.analysis
 * @date 2024-04-12 15:51
 * @description Type your description over here.
 */
public final class EdgeNGramTokenizer extends Tokenizer {
	public static final Side DEFAULT_SIDE = Side.FRONT;
	public static final int DEFAULT_MAX_GRAM_SIZE = 2;
	public static final int DEFAULT_MIN_GRAM_SIZE = 100;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	/** Specifies which side of the input the n-gram should be generated from */
	public static enum Side {

		/** Get the n-gram from the front of the input */
		FRONT {
			@Override
			public String getLabel() { return "front"; }
		},

		/** Get the n-gram from the end of the input */
		BACK  {
			@Override
			public String getLabel() { return "back"; }
		};

		public abstract String getLabel();

		// Get the appropriate Side from a string
		public static Side getSide(String sideName) {
			if (FRONT.getLabel().equals(sideName)) {
				return FRONT;
			}
			if (BACK.getLabel().equals(sideName)) {
				return BACK;
			}
			return null;
		}
	}

	private int minGram;
	private int maxGram;
	private int gramSize;
	private Side side;
	private boolean started = false;
	private int inLen;
	private String inStr;


	/**
	 * Creates EdgeNGramTokenizer that can generate n-grams in the sizes of the given range
	 *
	 * @param factory {@link Reader} holding the input to be tokenized
	 * @param side the {@link Side} from which to chop off an n-gram
	 * @param minGram the smallest n-gram to generate
	 * @param maxGram the largest n-gram to generate
	 */
	public EdgeNGramTokenizer(AttributeFactory factory, Side side, int minGram, int maxGram) {
		super(factory);
		init(side, minGram, maxGram);
	}

	/**
	 * Creates EdgeNGramTokenizer that can generate n-grams in the sizes of the given range
	 *
	 * @param factory {@link org.apache.lucene.util.AttributeFactory} to use
	 * @param sideLabel the name of the {@link Side} from which to chop off an n-gram
	 * @param minGram the smallest n-gram to generate
	 * @param maxGram the largest n-gram to generate
	 */
	public EdgeNGramTokenizer(AttributeFactory factory, String sideLabel, int minGram, int maxGram) {
		this(factory, Side.getSide(sideLabel), minGram, maxGram);
	}

	private void init(Side side, int minGram, int maxGram) {
		if (side == null) {
			throw new IllegalArgumentException("sideLabel must be either front or back");
		}

		if (minGram < 1) {
			throw new IllegalArgumentException("minGram must be greater than zero");
		}

		if (minGram > maxGram) {
			throw new IllegalArgumentException("minGram must not be greater than maxGram");
		}

		this.minGram = minGram;
		this.maxGram = maxGram;
		this.side = side;
	}

	/** Returns the next token in the stream, or null at EOS. */
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		// if we are just starting, read the whole input
		if (!started) {
			started = true;
			char[] chars = new char[1024];
			int charsRead = input.read(chars);
			inStr = new String(chars, 0, charsRead).trim();  // remove any leading or trailing spaces
			inLen = inStr.length();
			gramSize = minGram;
		}

		// if the remaining input is too short, we can't generate any n-grams
		if (gramSize > inLen) {
			return false;
		}

		// if we have hit the end of our n-gram size range, quit
		if (gramSize > maxGram) {
			return false;
		}

		// grab gramSize chars from front or back
		int start = side == Side.FRONT ? 0 : inLen - gramSize;
		int end = start + gramSize;
		termAtt.setEmpty().append(inStr, start, end);
		offsetAtt.setOffset(correctOffset(start), correctOffset(end));
		gramSize++;
		return true;
	}

	@Override
	public final void end() {
		final int finalOffset = inLen;
		this.offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		started = false;
	}
}
