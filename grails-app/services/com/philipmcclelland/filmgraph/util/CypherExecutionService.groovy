package com.philipmcclelland.filmgraph.util

import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.Transaction

public class CypherExecutionService {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def graphDatabaseService

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = false
    ExecutionEngine executionEngine

    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public ExecutionResult executeCypher(String cypherQuery) {
        ExecutionResult result

        Transaction tx = graphDatabaseService.beginTx()

        try {

            log.trace("[executeCypher] executing cypher: $cypherQuery")
            result = getExecutionEngine().execute(cypherQuery)
            log.trace("[executeCypher] cypher result: ${result}")

            tx.success()
        }
        finally {
            tx.finish()
        }

        return result
    }

    public ExecutionResult executeCypher(String cypherQuery, Map params) {
        ExecutionResult result

        Transaction tx = graphDatabaseService.beginTx()

        try {

            log.trace("[executeCypher] executing cypher: $cypherQuery; params: $params")
            result = getExecutionEngine().execute(cypherQuery, params)
            log.trace("[executeCypher] cypher result: ${result}")

            tx.success()
        }
        finally {
            tx.finish()
        }

        return result
    }

    ///////////////////////////////////////////////////
    //  Private Methods
    ///////////////////////////////////////////////////

    private ExecutionEngine getExecutionEngine() {
        if( !executionEngine && graphDatabaseService) {
            executionEngine = new ExecutionEngine( graphDatabaseService )
        }

        return executionEngine
    }


}