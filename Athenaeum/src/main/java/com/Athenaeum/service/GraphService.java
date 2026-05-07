package com.Athenaeum.service;

import com.Athenaeum.entity.Document;
import com.Athenaeum.entity.Note;
import com.Athenaeum.entity.User;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    @Autowired
    private Driver neo4jDriver;

    @Autowired
    private NLPService nlpService;

    // Graph node creation and relationship management
    public void createDocumentNode(Document document) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (d:Document {id: $id, title: $title, type: 'document'}) " +
                      "SET d.keywords = $keywords, d.summary = $summary",
                    Map.of("id", document.getId(), 
                           "title", document.getTitle(),
                           "keywords", document.getKeywords() != null ? document.getKeywords() : "",
                           "summary", document.getSummary() != null ? document.getSummary() : ""));
                return null;
            });
        }
    }

    public void createNoteNode(Note note) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (n:Note {id: $id, title: $title, type: 'note'}) " +
                      "SET n.keywords = $keywords, n.summary = $summary",
                    Map.of("id", note.getId(), 
                           "title", note.getTitle(),
                           "keywords", note.getKeywords() != null ? note.getKeywords() : "",
                           "summary", note.getSummary() != null ? note.getSummary() : ""));
                return null;
            });
        }
    }

    public void createRelationships(Long itemId, String itemType, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                // Find related items based on common keywords
                for (String keyword : keywords) {
                    if (keyword.trim().length() > 2) {
                        String query = String.format(
                            "MATCH (a:%s {id: $itemId}) " +
                            "MATCH (b) WHERE b <> a AND (b.keywords CONTAINS $keyword OR b.title CONTAINS $keyword) " +
                            "MERGE (a)-[:RELATED_TO {keyword: $keyword}]-(b)",
                            itemType
                        );

                        tx.run(query, Map.of("itemId", itemId, "keyword", keyword.trim()));
                    }
                }
                return null;
            });
        }
    }

    public Map<String, Object> getKnowledgeGraph(User user) {
        Map<String, Object> graphData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            // Get all nodes
            Result nodeResult = session.readTransaction(tx -> 
                tx.run("MATCH (n) RETURN n.id as id, n.title as title, n.type as type, labels(n)[0] as label")
            );

            while (nodeResult.hasNext()) {
                Record record = nodeResult.next();
                Map<String, Object> node = new HashMap<>();
                node.put("id", record.get("id").asLong());
                node.put("title", record.get("title").asString());
                node.put("type", record.get("type").asString());
                node.put("label", record.get("label").asString());
                nodes.add(node);
            }

            // Get all relationships
            Result linkResult = session.readTransaction(tx ->
                tx.run("MATCH (a)-[r:RELATED_TO]-(b) RETURN a.id as source, b.id as target, r.keyword as keyword")
            );

            while (linkResult.hasNext()) {
                Record record = linkResult.next();
                Map<String, Object> link = new HashMap<>();
                link.put("source", record.get("source").asLong());
                link.put("target", record.get("target").asLong());
                link.put("keyword", record.get("keyword").asString());
                links.add(link);
            }
        } catch (Exception e) {
            // Fallback to simple graph generation
            return generateSimpleGraph(user);
        }

        graphData.put("nodes", nodes);
        graphData.put("links", links);
        return graphData;
    }

    private Map<String, Object> generateSimpleGraph(User user) {
        // Simple graph generation using Java HashMap
        Map<String, Object> graphData = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        // This would be implemented with document and note relationships
        // For MVP, return basic structure
        graphData.put("nodes", nodes);
        graphData.put("links", links);
        return graphData;
    }

    public List<Map<String, Object>> getRelatedItems(Long itemId, String itemType) {
        List<Map<String, Object>> relatedItems = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            Result result = session.readTransaction(tx -> {
                String query = String.format(
                    "MATCH (a:%s {id: $itemId})-[:RELATED_TO]-(b) " +
                    "RETURN b.id as id, b.title as title, b.type as type",
                    itemType
                );
                return tx.run(query, Map.of("itemId", itemId));
            });

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.get("id").asLong());
                item.put("title", record.get("title").asString());
                item.put("type", record.get("type").asString());
                relatedItems.add(item);
            }
        } catch (Exception e) {
            // Handle Neo4j connection issues
            System.err.println("Neo4j connection error: " + e.getMessage());
        }

        return relatedItems;
    }

    public void deleteNode(Long itemId, String itemType) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                String query = String.format("MATCH (n:%s {id: $itemId}) DETACH DELETE n", itemType);
                tx.run(query, Map.of("itemId", itemId));
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error deleting graph node: " + e.getMessage());
        }
    }
}