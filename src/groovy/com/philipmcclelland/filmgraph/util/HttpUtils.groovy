package com.philipmcclelland.filmgraph.util

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.ccil.cowan.tagsoup.Parser

public class HttpUtils {

    private static final Log log = LogFactory.getLog(HttpUtils.class)
    private static final USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:25.0) Gecko/20100101 Firefox/25.0"


    public static InputStream loadHttpGetResponseInputStream(String urlString) {
        log.debug("[loadHttpGetResponse()] params: [urlString: $urlString]")

        URLConnection connection = urlString.toURL().openConnection()

        connection.addRequestProperty("User-Agent", USER_AGENT)

        return connection.getInputStream()
    }

    public static def loadHtmlParser(InputStream inputStream) {
        log.debug("[loadHtmlParser()] params: [inputStream: N/A]")

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        Parser parser = new Parser()
        XmlSlurper slurper = new XmlSlurper(parser)
        def htmlParser = slurper.parse(bufferedReader)

        bufferedReader.close()

        return htmlParser
    }
}