package org.es.ngram.index.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public final class NGramTokenFilter extends TokenFilter {
	public static final boolean DEFAULT_PRESERVE_ORIGINAL = false;
	public static final int DEFAULT_MIN_GRAMSIZE = 2;
	public static final int DEFAULT_MAX_GRAMSIZE = 100;

	private final int minGram;
	private final int maxGram;
	private final boolean preserveOriginal;

	private char[] curTermBuffer;
	private int curTermLength;
	private int curTermCodePointCount;
	private int curGramSize;
	private int curPos;
	private int curPosIncr;
	private int tokenStart;
	private int lastStartOffset;
	private State state;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	/**
	 * Creates an NGramTokenFilter that, for a given input term, produces all contained n-grams with
	 * lengths &gt;= minGram and &lt;= maxGram. Will optionally preserve the original term when its
	 * length is outside of the defined range.
	 *
	 * <p>Note: Care must be taken when choosing minGram and maxGram; depending on the input token
	 * size, this filter potentially produces a huge number of terms.
	 *
	 * @param input {@link TokenStream} holding the input to be tokenized
	 * @param minGram the minimum length of the generated n-grams
	 * @param maxGram the maximum length of the generated n-grams
	 * @param preserveOriginal Whether or not to keep the original term when it is shorter than
	 *     minGram or longer than maxGram
	 */
	public NGramTokenFilter(TokenStream input, int minGram, int maxGram, boolean preserveOriginal) {
		super(input);
		if (minGram < 1) {
			throw new IllegalArgumentException("minGram must be greater than zero");
		}
		if (minGram > maxGram) {
			throw new IllegalArgumentException("minGram must not be greater than maxGram");
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.preserveOriginal = preserveOriginal;
		this.lastStartOffset = -1;
	}

	public NGramTokenFilter(TokenStream input, int minGram, int maxGram) {
		this(input, minGram, maxGram, DEFAULT_PRESERVE_ORIGINAL);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		while (true) {
			if (curTermBuffer == null) {
				if (!input.incrementToken()) {
					return false;
				}
				state = captureState();
				curTermLength = termAtt.length();
				curTermCodePointCount = Character.codePointCount(termAtt, 0, termAtt.length());
				curPosIncr += posIncrAtt.getPositionIncrement();
				curPos = 0;
				tokenStart = offsetAtt.startOffset();
				if (curTermCodePointCount < minGram) {
					posIncrAtt.setPositionIncrement(curPosIncr);
					curPosIncr = 0;
					return true;
				}
				if (preserveOriginal && curTermCodePointCount < minGram) {
					// Token is shorter than minGram, but we'd still like to keep it.
					posIncrAtt.setPositionIncrement(curPosIncr);
					curPosIncr = 0;
					return true;
				}

				curTermBuffer = termAtt.buffer().clone();
				curGramSize = minGram;
			}
			if (curGramSize > maxGram) {
				restoreState(state);
				posIncrAtt.setPositionIncrement(curPosIncr);
				/*int curEndPos = curTermLength - curPos;
				int termLen = curEndPos - curPos;
				if(termLen <= maxGram) {
					termAtt.copyBuffer(curTermBuffer, curPos, curEndPos);
					this.lastStartOffset = tokenStart + curPos;
					int curEndOffset = this.lastStartOffset + curTermLength;
					*//*if(curEndOffset > curTermLength) {
						curEndOffset = curTermLength;
					}*//*
					offsetAtt.setOffset(this.lastStartOffset, curEndOffset);
					++curPos;
					curGramSize = minGram;
					return true;
				}*/
				++curPos;
				curGramSize = minGram;
				//return true;
			}
			if (curGramSize > maxGram || (curPos + curGramSize) > curTermCodePointCount) {
				++curPos;
				curGramSize = minGram;
			}
			if ((curPos + curGramSize) <= curTermCodePointCount) {
				restoreState(state);
				final int start = Character.offsetByCodePoints(curTermBuffer, 0, curTermLength, 0, curPos);
				final int end = Character.offsetByCodePoints(curTermBuffer, 0, curTermLength, start, curGramSize);
				int len = end - start;
				if (len > curTermLength) {
					len = curTermLength;
				}
				termAtt.copyBuffer(curTermBuffer, start, len);
				posIncrAtt.setPositionIncrement(curPosIncr);
				this.lastStartOffset = tokenStart + curPos;
				offsetAtt.setOffset(this.lastStartOffset, tokenStart + curPos + curGramSize);
				curPosIncr = 0;
				curGramSize++;
				return true;
			} else if (preserveOriginal && curTermCodePointCount > maxGram) {
				restoreState(state);
				if (this.lastStartOffset <= tokenStart) {
					posIncrAtt.setPositionIncrement(0);
					termAtt.copyBuffer(curTermBuffer, 0, curTermLength);
					offsetAtt.setOffset(tokenStart, curTermLength);
					this.lastStartOffset = tokenStart;
					curTermBuffer = null;
					return true;
				}
			}
			// Done with this input token, get next token on next iteration.
			curTermBuffer = null;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		curTermBuffer = null;
		curPosIncr = 0;
	}

	@Override
	public void end() throws IOException {
		super.end();
		posIncrAtt.setPositionIncrement(curPosIncr);
	}
}
