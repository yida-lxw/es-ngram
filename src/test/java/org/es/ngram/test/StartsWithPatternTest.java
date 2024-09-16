package org.es.ngram.test;

/**
 * @author yida
 * @package org.es.ngram.test
 * @date 2024-04-18 10:42
 * @description Type your description over here.
 */
public class StartsWithPatternTest {
	public static void main(String[] args) {
		String input = "/D:example"; // 你要检查的字符串

		// 定义正则表达式模式，匹配以 "/D:" 开头的字符串
		String pattern = "^/[\\w]+:.*";

		// 使用 matches() 方法匹配字符串
		boolean isMatch = input.matches(pattern);

		// 输出匹配结果
		if (isMatch) {
			System.out.println("字符串以\"/D:\"开头");
		} else {
			System.out.println("字符串不以\"/D:\"开头");
		}
	}
}
