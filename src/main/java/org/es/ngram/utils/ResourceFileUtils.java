package org.es.ngram.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * @author yida
 * @package com.hydangan.common.utils
 * @date 2023-12-06 15:36
 * @description 资源文件加载工具类
 */
public class ResourceFileUtils {
	public static final Log log = LogFactory.getLog(ResourceFileUtils.class);
	private static final String pattern = "^/[\\w]+:.*";

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	/** URL prefix for loading from the file system: "file:". */
	public static final String FILE_URL_PREFIX = "file:";

	/** URL prefix for loading from a jar file: "jar:". */
	public static final String JAR_URL_PREFIX = "jar:";

	/** URL prefix for loading from a jar file: "jar:". */
	public static final String JAR_FILE_URL_PREFIX = "jar:file:";

	/** URL prefix for loading from a war file on Tomcat: "war:". */
	public static final String WAR_URL_PREFIX = "war:";

	/** URL protocol for a file in the file system: "file". */
	public static final String URL_PROTOCOL_FILE = "file";

	/** URL protocol for an entry from a jar file: "jar". */
	public static final String URL_PROTOCOL_JAR = "jar";

	/** URL protocol for an entry from a war file: "war". */
	public static final String URL_PROTOCOL_WAR = "war";

	/** URL protocol for an entry from a zip file: "zip". */
	public static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar". */
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** URL protocol for an entry from a JBoss jar file: "vfszip". */
	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	/** URL protocol for a JBoss file system resource: "vfsfile". */
	public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

	/** URL protocol for a general JBoss VFS resource: "vfs". */
	public static final String URL_PROTOCOL_VFS = "vfs";

	/** File extension for a regular jar file: ".jar". */
	public static final String JAR_FILE_EXTENSION = ".jar";

	/** Separator between JAR URL and file path within the JAR: "!/". */
	public static final String JAR_URL_SEPARATOR = "!/";

	/** Special separator between WAR URL and jar part on Tomcat. */
	public static final String WAR_URL_SEPARATOR = "*/";

	public static String readResourceFile(String filePath) {
		File file = null;
		try {
			file = getFile("classpath:" + filePath);
			if (null == file) {
				log.info("kick off to read the resource file:["+filePath+"] in jar.");
				return readResourceFileInJar(filePath);
			}
			return new String(Files.readAllBytes(file.toPath()));
		} catch (Exception e) {
			log.error("Load resource file:["+filePath+"] from the classpath with the spring ResourceUtils occur exception:" + e.getMessage());
			return readResourceFileInJar(filePath);
		}

	}

	public static String readResourceFileInJar(String filePath) {
		ClassLoader classLoader = ResourceFileUtils.class.getClassLoader();
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			inputStream = classLoader.getResourceAsStream(filePath);
			if (null == inputStream) {
				inputStream = classLoader.getResourceAsStream("/" + filePath);
				if (null == inputStream) {
					inputStream = ResourceFileUtils.class.getResourceAsStream(filePath);
					if (null == inputStream) {
						inputStream = ResourceFileUtils.class.getResourceAsStream("/" + filePath);
					}
				}
			}
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
		} catch (Exception e) {
			log.error("Read resource file:["+filePath+"] in jar occur exception.");
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (Exception e) {
				}
			}
			if (null != bufferedReader) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
				}
			}
			return stringBuilder.toString();
		}
	}

	public static InputStream loadResourceFileAsInputStreamInJar(String filePath) {
		ClassLoader classLoader = ResourceFileUtils.class.getClassLoader();
		InputStream inputStream = null;
		try {
			inputStream = classLoader.getResourceAsStream(filePath);
			if (null == inputStream) {
				inputStream = classLoader.getResourceAsStream("/" + filePath);
				if (null == inputStream) {
					inputStream = ResourceFileUtils.class.getResourceAsStream(filePath);
					if (null == inputStream) {
						inputStream = ResourceFileUtils.class.getResourceAsStream("/" + filePath);
					}
				}
			}
		} catch (Exception e) {
			log.error("Load resource file:["+filePath+"] in jar as InputStream occur exception.");
		} finally {
			return inputStream;
		}
	}

	public static File getFile(String resourceLocation) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			String description = "class path resource [" + path + "]";
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
			if (url == null) {
				throw new FileNotFoundException(description +
						" cannot be resolved to absolute file path because it does not exist");
			}
			return getFile(url, description);
		}
		try {
			// try URL
			return getFile(new URL(resourceLocation));
		}
		catch (MalformedURLException ex) {
			// no URL -> treat as file path
			return new File(resourceLocation);
		}
	}

	public static File getFile(URL resourceUrl) throws FileNotFoundException {
		return getFile(resourceUrl, resourceUrl.getPath());
	}

	public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		log.info("[File]resource url:["+resourceUrl.getPath()+"], protocol:["+resourceUrl.getProtocol()+"]");
		String protocol = resourceUrl.getProtocol();
		if (!URL_PROTOCOL_FILE.equals(protocol)) {
			if(protocol.startsWith(JAR_FILE_URL_PREFIX)) {
				String urlString = resourceUrl.toString();
				log.info("orignal resourceUrl:" + urlString);
				urlString = urlString.replace(JAR_FILE_URL_PREFIX, FILE_URL_PREFIX);
				log.info("replaced resourceUrl:" + urlString);
				try {
					resourceUrl = new URL(urlString);
				} catch (Exception e) {
				    log.error("build URL instance with urlString:"+urlString+" but occur exception:" + e.getMessage());
					throw new FileNotFoundException(
							description + " cannot be resolved to absolute file path " +
									"because it does not reside in the file system: " + urlString);
				}
			} else if(URL_PROTOCOL_JAR.equals(resourceUrl.getProtocol())) {
				String urlString = resourceUrl.toString();
				log.info("[jar]orignal resourceUrl:" + urlString);
				if(urlString.startsWith(JAR_URL_PREFIX)) {
					urlString = urlString.replace(JAR_URL_PREFIX, "");
					log.info("[jar]replaced resourceUrl:" + urlString);
				}
				try {
					resourceUrl = new URL(urlString);
				} catch (Exception e) {
					log.error("build URL instance with urlString:"+urlString+" but occur exception:" + e.getMessage());
					throw new FileNotFoundException(
							description + " cannot be resolved to absolute file path " +
									"because it does not reside in the file system: " + urlString);
				}
			} else {
				throw new FileNotFoundException(
						description + " cannot be resolved to absolute file path " +
								"because it does not reside in the file system: " + resourceUrl);
			}
		}
		try {
			URI uri = toURI(resourceUrl);
			log.info("[before toURI(resourceUrl)]resourceUrl:[" + resourceUrl.toString() + "],scheme:" + uri.getScheme());
			String filePath = uri.getSchemeSpecificPart();
			if(filePath.matches(pattern)) {
				filePath = FILE_URL_PREFIX + filePath;
			}
			log.info("[toURI(resourceUrl).getSchemeSpecificPart()]:" + filePath);
			File file = new File(filePath);
			log.info("dict file name:" + file.getName());
			return file;
		} catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			String filePath = resourceUrl.getFile();
			log.info("[Exception][toURI(resourceUrl).getSchemeSpecificPart()]:" + filePath);
			if(filePath.matches(pattern)) {
				filePath = FILE_URL_PREFIX + filePath;
			}
			return new File(filePath);
		}
	}

	public static Reader getReader(String resourceLocation, String charset) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		log.info("resource resourceLocation:["+resourceLocation+"]");
		String path = resourceLocation;
		String description = resourceLocation;
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			description = "class path resource [" + path + "]";
		}
		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		InputStream inputStream = ResourceFileUtils.class.getResourceAsStream(path);
		if(null == inputStream) {
			if(!path.startsWith("/")) {
				String filePath = "/" + path;
				inputStream = ResourceFileUtils.class.getResourceAsStream(filePath);
			}
			if(null == inputStream) {
				inputStream = ResourceFileUtils.class.getClassLoader().getResourceAsStream(path);
				if(null == inputStream) {
					if(!path.startsWith("/")) {
						String filePath = "/" + path;
						inputStream = ResourceFileUtils.class.getClassLoader().getResourceAsStream(filePath);
					}
				}
				if(null == inputStream) {
					inputStream = classLoader.getResourceAsStream(path);
					if(null == inputStream) {
						if (!path.startsWith("/")) {
							String filePath = "/" + path;
							inputStream = classLoader.getResourceAsStream(filePath);
						}
					}
					if(null == inputStream) {
						inputStream = ClassLoader.getSystemResourceAsStream(path);
						if(null == inputStream) {
							if (!path.startsWith("/")) {
								String filePath = "/" + path;
								inputStream = ClassLoader.getSystemResourceAsStream(filePath);
							}
						}
					}

				}
			}
		}
		if (inputStream == null) {
			throw new FileNotFoundException(description +
					" cannot be resolved to absolute file path because it does not exist");
		}
		return inputStream2Reader(inputStream, charset);
	}

	public static Reader getReader(String resourceLocation) throws FileNotFoundException {
		return getReader(resourceLocation, "UTF-8");
	}

	public static Reader getReaderOld(String resourceLocation, String charset) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		log.info("resource resourceLocation:["+resourceLocation+"]");
		File file = null;
		if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			String description = "class path resource [" + path + "]";
			ClassLoader cl = ClassUtils.getDefaultClassLoader();
			URL url = ResourceFileUtils.class.getResource(path);
			if(null == url) {
				if(!path.startsWith("/")) {
					path = "/" + path;
					url = ResourceFileUtils.class.getResource(path);
				}
				if(null == url) {
					url = ResourceFileUtils.class.getClassLoader().getResource(path);
					if(null == url) {
						if(!path.startsWith("/")) {
							path = "/" + path;
							url = ResourceFileUtils.class.getClassLoader().getResource(path);
						}
					}
					if(null == url) {
						url = ClassLoader.getSystemResource(path);
					}
				}
			}
			if (url == null) {
				throw new FileNotFoundException(description +
						" cannot be resolved to absolute file path because it does not exist");
			}
			log.info("[classpath]resource url:["+url.getPath()+"], protocol:["+url.getProtocol()+"]");
			file = getFile(url, description);
		} else {
			try {
				// try URL
				URL url = new URL(resourceLocation);
				log.info("[non-classpath]resource url:["+url.getPath()+"], protocol:["+url.getProtocol()+"]");
				file = getFile(url);
			} catch (MalformedURLException ex) {
				// no URL -> treat as file path
				file = new File(resourceLocation);
			}
		}
		return file2Reader(file, charset);
	}

	public static Reader getReader(URL resourceUrl, String charset) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		String protocol = resourceUrl.getProtocol();
		if (!URL_PROTOCOL_FILE.equals(protocol)) {
			if(resourceUrl.getProtocol().startsWith(JAR_FILE_URL_PREFIX)) {
				String urlString = resourceUrl.toString();
				log.info("orignal resourceUrl:" + urlString);
				urlString = urlString.replace(JAR_FILE_URL_PREFIX, FILE_URL_PREFIX);
				log.info("replaced resourceUrl:" + urlString);
				try {
					resourceUrl = new URL(urlString);
				} catch (Exception e) {
					log.error("build URL instance with urlString:"+urlString+" but occur exception:" + e.getMessage());
					throw new FileNotFoundException(
							resourceUrl.getPath() + " cannot be resolved to absolute file path " +
									"because it does not reside in the file system: " + urlString);
				}
			} else {
				throw new FileNotFoundException(
						resourceUrl.getPath() + " cannot be resolved to absolute file path " +
								"because it does not reside in the file system: " + resourceUrl);
			}
		}
		File file = null;
		try {
			file = new File(toURI(resourceUrl).getSchemeSpecificPart());
		}
		catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			file = new File(resourceUrl.getFile());
		}
		return file2Reader(file, charset);
	}

	private static Reader file2Reader(File file, String charset) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			log.info("FileInputStream is null?" + (null == fileInputStream));
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName(charset));
			log.info("InputStreamReader is null?" + (null == inputStreamReader));
			return new BufferedReader(inputStreamReader);
		} catch (Exception e) {
			log.error("file to reader occur exception:\n" + e.getMessage());
			return null;
		}
	}

	private static Reader inputStream2Reader(InputStream inputStream, String charset) {
		try {
			log.info("InputStream is null?" + (null == inputStream));
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(charset));
			log.info("InputStreamReader is null?" + (null == inputStreamReader));
			return new BufferedReader(inputStreamReader);
		} catch (Exception e) {
			log.error("file to reader occur exception:\n" + e.getMessage());
			return null;
		}
	}

	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * Create a URI instance for the given location String,
	 * replacing spaces with "%20" URI encoding first.
	 * @param location the location String to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the location wasn't a valid URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(StringUtils.replace(location, " ", "%20"));
	}
}
