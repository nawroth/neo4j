/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.tooling;

import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.api.TokenAccess;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;

/**
 * A tool for doing global operations, for example {@link #getAllNodes()}.
 * @deprecated use methods on {@link GraphDatabaseService} instead
 */
@Deprecated
public class GlobalGraphOperations
{
    private final ThreadToStatementContextBridge statementCtxSupplier;
    private final GraphDatabaseService db;

    private GlobalGraphOperations( GraphDatabaseService db )
    {
        this.db = db;
        GraphDatabaseAPI dbApi = (GraphDatabaseAPI) db;
        DependencyResolver resolver = dbApi.getDependencyResolver();
        this.statementCtxSupplier = resolver.resolveDependency( ThreadToStatementContextBridge.class );
    }

    /**
     * Get a {@link GlobalGraphOperations} for the given {@code db}.
     *
     * @param db the {@link GraphDatabaseService} to get global operations for.
     * @return {@link GlobalGraphOperations} for the given {@code db}.
     */
    public static GlobalGraphOperations at( GraphDatabaseService db )
    {
        return new GlobalGraphOperations( db );
    }

    /**
     * Returns all nodes in the graph.
     *
     * @return all nodes in the graph.
     * @deprecated use {@link GraphDatabaseService#getAllNodes()} instead
     */
    @Deprecated
    public ResourceIterable<Node> getAllNodes()
    {
        return db.getAllNodes();
    }

    /**
     * Returns all relationships in the graph.
     *
     * @return all relationships in the graph.
     * @deprecated use {@link GraphDatabaseService#getAllRelationships()} instead
     */
    @Deprecated
    public Iterable<Relationship> getAllRelationships()
    {
        return db.getAllRelationships();
    }

    /**
     * Returns all relationship types currently in the underlying store. Relationship types are
     * added to the underlying store the first time they are used in a successfully committed
     * {@link Node#createRelationshipTo node.createRelationshipTo(...)}. Note that this method is
     * guaranteed to return all known relationship types, but it does not guarantee that it won't
     * return <i>more</i> than that (e.g. it can return "historic" relationship types that no longer
     * have any relationships in the graph).
     *
     * @return all relationship types in the underlying store
     * @deprecated use {@link GraphDatabaseService#getAllRelationshipTypes()} instead
     */
    @Deprecated
    public Iterable<RelationshipType> getAllRelationshipTypes()
    {
        return all( TokenAccess.RELATIONSHIP_TYPES );
    }

    /**
     * Returns all relationship types currently in use in the underlying store. Relationship types are
     * added to the underlying store the first time they are used in a successfully committed
     * {@link Node#createRelationshipTo node.createRelationshipTo(...)}. Note that this method is
     * guaranteed to return all known relationship types, and it guarantees that it won't return
     * "historic" relationship types that no longer have any relationships in the graph.
     *
     * @return all relationship types in use in the underlying store
     * @deprecated use {@link GraphDatabaseService#getAllRelationshipTypes()} instead
     */
    @Deprecated
    public Iterable<RelationshipType> getAllRelationshipTypesInUse()
    {
        return db.getAllRelationshipTypes();
    }

    /**
     * Returns all labels currently in the underlying store. Labels are added to the store the first time
     * they are used. This method guarantees that it will return all labels currently in use. However,
     * it may also return <i>more</i> than that (e.g. it can return "historic" labels that are no longer used).
     *
     * Please take care that the returned {@link ResourceIterable} is closed correctly and as soon as possible
     * inside your transaction to avoid potential blocking of write operations.
     *
     * @return all labels in the underlying store.
     * @deprecated use {@link GraphDatabaseService#getAllLabels()} instead
     */
    @Deprecated
    public ResourceIterable<Label> getAllLabels()
    {
        return all( TokenAccess.LABELS );
    }

    /**
     * Returns all labels currently in use in the underlying store. Labels are added to the store the first time
     * they are used. This method guarantees that it will return all labels currently in use by filtering out
     * "historic" labels that are no longer used.
     *
     * Please take care that the returned {@link ResourceIterable} is closed correctly and as soon as possible
     * inside your transaction to avoid potential blocking of write operations.
     *
     * @return all labels in use in the underlying store.
     * @deprecated use {@link GraphDatabaseService#getAllLabels()} instead
     */
    @Deprecated
    public ResourceIterable<Label> getAllLabelsInUse()
    {
        return db.getAllLabels();
    }

    /**
     * Returns all property keys currently in the underlying store. This method guarantees that it will return all
     * property keys currently in use. However, it may also return <i>more</i> than that (e.g. it can return "historic"
     * labels that are no longer used).
     *
     * Please take care that the returned {@link ResourceIterable} is closed correctly and as soon as possible
     * inside your transaction to avoid potential blocking of write operations.
     *
     * @return all property keys in the underlying store.
     * @deprecated use {@link GraphDatabaseService#getAllPropertyKeys()} instead
     */
    @Deprecated
    public ResourceIterable<String> getAllPropertyKeys()
    {
        return all( TokenAccess.PROPERTY_KEYS );
    }

    /**
     * Returns all {@link Node nodes} with a specific {@link Label label}.
     *
     * Please take care that the returned {@link ResourceIterable} is closed correctly and as soon as possible
     * inside your transaction to avoid potential blocking of write operations.
     *
     * @param label the {@link Label} to return nodes for.
     * @return {@link Iterable} containing nodes with a specific label.
     * @deprecated Use {@link GraphDatabaseService#findNodes(Label)} instead
     */
    @Deprecated
    public ResourceIterable<Node> getAllNodesWithLabel( final Label label )
    {
        statementCtxSupplier.assertInUnterminatedTransaction();
        return () -> db.findNodes( label );
    }

    private <T> ResourceIterable<T> all( final TokenAccess<T> tokens )
    {
        statementCtxSupplier.assertInUnterminatedTransaction();
        return () -> tokens.all( statementCtxSupplier.get() );
    }
}