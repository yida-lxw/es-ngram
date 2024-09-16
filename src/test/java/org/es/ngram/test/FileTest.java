package org.es.ngram.test;

import org.es.ngram.utils.ResourceFileUtils;

import java.io.File;
import java.net.URL;

/**
 * @author yida
 * @package org.es.ngram.test
 * @date 2024-04-18 09:44
 * @description Type your description over here.
 */
public class FileTest {
	public static void main(String[] args) throws Exception {
		String urlString = "file:/D:/elasticsearch-7.11.2/plugins/es-ngram/es-ngram-1.0.jar!/stopword.dic";
		URL resourceUrl = new URL(urlString);
		File file = new File(urlString);
		String fileName = file.getName();
		System.out.println("fileName:" + fileName);
	}
}
