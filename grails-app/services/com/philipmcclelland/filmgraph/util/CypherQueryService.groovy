package com.philipmcclelland.filmgraph.util

import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.RelationshipType



public class CypherQueryService {
    ///////////////////////////////////////////////////
    //  Dependency Injections
    ///////////////////////////////////////////////////

    def cypherExecutionService

    ///////////////////////////////////////////////////
    //  Properties
    ///////////////////////////////////////////////////

    static transactional = false

    ///////////////////////////////////////////////////
    //  Public Methods
    ///////////////////////////////////////////////////

    public void pageThroughInstancesOfLabel(String label, Closure customProcessor, Integer pageSize = 20) {
        log.trace("[pageThroughInstancesOfLabel()] params: [label: $label]")

        Integer instanceCount = countInstancesOfLabel(label)

        String cypherQueryTemplate = """
            MATCH
                (n:`$label`)
            RETURN
                n
            SKIP
                \$skip
            LIMIT
                \$limit
        """

        if( instanceCount > 0 ) {
            Integer skip = (pageSize * -1)

            while( (skip + pageSize) < instanceCount ) {
                skip += pageSize

                String queryInstance = cypherQueryTemplate.replace('$skip', skip.toString()).replace('$limit', pageSize.toString())

                ExecutionResult executionResult = cypherExecutionService.executeCypher(queryInstance)
                List<Node> nodes = getAllNodesFromExecutionResult(executionResult, "n")

                customProcessor.call(nodes)
            }
        }
    }

    public Node createInstanceOfLabel(String label, Map properties) {
        log.trace("[createInstanceOfLabel()] params: [label: $label, properties: $properties]")

        Node node = findNodeByLabelAndProperties(label, properties)

        if( !node ) {
            String cypherQuery = """
                CREATE
                    (n:`$label` { props })
                RETURN
                    n
            """

            ExecutionResult result = cypherExecutionService.executeCypher(cypherQuery, ['props': properties])

            node = getNodeFromFirstExecutionResult(result, "n")
        }


        return node
    }

    public Relationship createRelationship(Node sourceNode, Node targetNode, RelationshipType relationshipType, Map relationshipProperties, Direction direction = Direction.OUTGOING) {
        Relationship relationship = sourceNode.getSingleRelationship(relationshipType, direction)

        if( !relationship ) {

        }
    }

    public Node findNodeByLabelAndProperties(String label, Map properties) {
        Node node

        String cypherQuery = """
            MATCH
                (m:`$label`)
            ${buildWhereClauseForNodeProperties(properties, 'm')}
            RETURN
                m
        """

        ExecutionResult result = cypherExecutionService.executeCypher(cypherQuery, properties)

        node = getNodeFromFirstExecutionResult(result, "m")

        return node
    }

    public Integer countInstancesOfLabel(String label) {
        log.trace("[countInstancesOfLabel()] params: [label: $label]")

        Integer count = 0

        String cypherQuery = """
            MATCH
                (m:`$label`)
            RETURN
                count(m) as instanceCount
        """

        ExecutionResult result = cypherExecutionService.executeCypher(cypherQuery)

        count = (Integer)getFirstCypherIteratorResultColumnValue(result, "instanceCount") ?: 0

        return count
    }

    ///////////////////////////////////////////////////
    //  Private Methods
    ///////////////////////////////////////////////////

    private String buildWhereClauseForNodeProperties(Map nodeProperties, String nodeAlias) {
        String whereClause = nodeProperties.size() > 0 ? " WHERE " : " "

        List<String> whereClauseConditions = []

        nodeProperties.each({ String k, Object v ->
            whereClauseConditions.add(" $nodeAlias.$k = { $k } ")
        })

        whereClause += whereClauseConditions.join(" AND ")

        return whereClause
    }

    private Node getNodeFromFirstExecutionResult(ExecutionResult executionResult, String resultAlias) {
        Node result

        if( executionResult.iterator().hasNext() ) {
            result = (Node)executionResult.iterator().next()[resultAlias]
        }

        return result
    }

    private List<Node> getAllNodesFromExecutionResult(ExecutionResult executionResult, String resultAlias) {
        List<Node> nodes = []

        while (executionResult.iterator().hasNext()) {
            nodes.add((Node)executionResult.iterator().next()[resultAlias])
        }

        return nodes
    }

    public Object getFirstCypherIteratorResultColumnValue(ExecutionResult cypherResult, String columnAlias) {
        Object value = null

        Iterator<Map<String, Object>> iterator = cypherResult.iterator()
        if (iterator.hasNext()) {
            Map<String, Object> firstResultMap = iterator.next()
            value = firstResultMap[columnAlias]
        }

        return value
    }
}