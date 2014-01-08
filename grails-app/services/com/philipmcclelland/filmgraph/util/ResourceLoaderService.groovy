package com.philipmcclelland.filmgraph.util

import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource

class ResourceLoaderService {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def grailsApplication

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = false

    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public File loadFile(String resourcePath) {
        log.debug("[loadFile()] params: [resourcePath: $resourcePath]")

        return resolveResource(resourcePath).file
    }

    public boolean doesFileExist(String resourcePath) {
        log.debug("[doesFileExist()] params: [resourcePath: $resourcePath]")

        boolean fileExists = false

        try {
            loadFile(resourcePath)
            fileExists = true
        }
        catch(ex) {

        }

        return fileExists
    }

    ///////////////////////////////////////////////////
    //  Helper Methods
    ///////////////////////////////////////////////////

    private Resource resolveResource(String res) {
        log.debug("[resolveResource()] params: [res: $res]")

        // attempt to search for the resource a bit if its not found..
        ApplicationContext ctx = grailsApplication.mainContext
        List<String> resourcePaths = [
                res,                    // check the direct resource path
                "classpath:${res}",     // check the direct classpath
                "/WEB-INF/**/${res}",   // check the web-inf filesystem path
                "classpath*:**/${res}",   // check all the jars
                "/WEB-INF/lib/*.jar!/**/${res} " // check the direct jar files
        ]
        for (String resourcePath in resourcePaths) {
            try {
                List<Resource> resources = ctx.getResources(resourcePath) as List
                if (resources) {
                    if (resources.size() > 1) {
                        log.warn("[resourceResource()] Multiple resources found: ${resources}")
                    }
                    // return only the one that is an actual file..
                    Resource found = resources.find() { Resource r -> r.file?.isFile() }
                    if (found) {
                        return found
                    }
                }
            }
            catch (FileNotFoundException fnfe) {
                // ignore because we're working w/ a search path..
            }
        }
        // nothing was found so just throw..
        throw new FileNotFoundException(res)
    }
}