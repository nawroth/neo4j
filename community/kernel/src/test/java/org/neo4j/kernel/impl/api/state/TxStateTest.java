/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.kernel.impl.api.state;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.neo4j.graphdb.Direction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.api.TxState;
import org.neo4j.kernel.api.constraints.UniquenessConstraint;
import org.neo4j.kernel.api.index.IndexDescriptor;
import org.neo4j.kernel.impl.util.DiffSets;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import static org.neo4j.collection.primitive.PrimitiveLongCollections.iterator;
import static org.neo4j.graphdb.Direction.BOTH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.helpers.collection.IteratorUtil.asSet;
import static org.neo4j.kernel.api.properties.Property.noNodeProperty;
import static org.neo4j.kernel.api.properties.Property.stringProperty;
import static org.neo4j.kernel.impl.util.PrimitiveIteratorMatchers.containsLongs;

public class TxStateTest
{
    @Test
    public void shouldGetAddedLabels() throws Exception
    {
        // GIVEN
        state.nodeDoAddLabel( 1, 0 );
        state.nodeDoAddLabel( 1, 1 );
        state.nodeDoAddLabel( 2, 1 );

        // WHEN
        Set<Integer> addedLabels = state.nodeStateLabelDiffSets( 1 ).getAdded();

        // THEN
        assertEquals( asSet( 1, 2 ), addedLabels );
    }

    @Test
    public void shouldGetRemovedLabels() throws Exception
    {
        // GIVEN
        state.nodeDoRemoveLabel( 1, 0 );
        state.nodeDoRemoveLabel( 1, 1 );
        state.nodeDoRemoveLabel( 2, 1 );

        // WHEN
        Set<Integer> removedLabels = state.nodeStateLabelDiffSets( 1 ).getRemoved();

        // THEN
        assertEquals( asSet( 1, 2 ), removedLabels );
    }

    @Test
    public void removeAddedLabelShouldRemoveFromAdded() throws Exception
    {
        // GIVEN
        state.nodeDoAddLabel( 1, 0 );
        state.nodeDoAddLabel( 1, 1 );
        state.nodeDoAddLabel( 2, 1 );

        // WHEN
        state.nodeDoRemoveLabel( 1, 1 );

        // THEN
        assertEquals( asSet( 2 ), state.nodeStateLabelDiffSets( 1 ).getAdded() );
    }

    @Test
    public void addRemovedLabelShouldRemoveFromRemoved() throws Exception
    {
        // GIVEN
        state.nodeDoRemoveLabel( 1, 0 );
        state.nodeDoRemoveLabel( 1, 1 );
        state.nodeDoRemoveLabel( 2, 1 );

        // WHEN
        state.nodeDoAddLabel( 1, 1 );

        // THEN
        assertEquals( asSet( 2 ), state.nodeStateLabelDiffSets( 1 ).getRemoved() );
    }

    @Test
    public void shouldMapFromAddedLabelToNodes() throws Exception
    {
        // GIVEN
        state.nodeDoAddLabel( 1, 0 );
        state.nodeDoAddLabel( 2, 0 );
        state.nodeDoAddLabel( 1, 1 );
        state.nodeDoAddLabel( 3, 1 );
        state.nodeDoAddLabel( 2, 2 );

        // WHEN
        Set<Long> nodes = state.nodesWithLabelAdded( 2 );

        // THEN
        assertEquals( asSet( 0L, 2L ), asSet( nodes ) );
    }

    @Test
    public void shouldMapFromRemovedLabelToNodes() throws Exception
    {
        // GIVEN
        state.nodeDoRemoveLabel( 1, 0 );
        state.nodeDoRemoveLabel( 2, 0 );
        state.nodeDoRemoveLabel( 1, 1 );
        state.nodeDoRemoveLabel( 3, 1 );
        state.nodeDoRemoveLabel( 2, 2 );

        // WHEN
        Set<Long> nodes = state.nodesWithLabelChanged( 2 ).getRemoved();

        // THEN
        assertEquals( asSet( 0L, 2L ), asSet( nodes ) );
    }

    @Test
    public void shouldAddAndGetByLabel() throws Exception
    {
        // GIVEN
        int labelId = 2, labelId2 = 5, propertyKey = 3;

        // WHEN
        IndexDescriptor rule = new IndexDescriptor( labelId, propertyKey );
        state.indexRuleDoAdd( rule );
        state.indexRuleDoAdd( new IndexDescriptor( labelId2, propertyKey ) );

        // THEN
        assertEquals( asSet( rule ), state.indexDiffSetsByLabel( labelId ).getAdded() );
    }

    @Test
    public void shouldAddAndGetByRuleId() throws Exception
    {
        // GIVEN
        int labelId = 2, propertyKey = 3;

        // WHEN
        IndexDescriptor rule = new IndexDescriptor( labelId, propertyKey );
        state.indexRuleDoAdd( rule );

        // THEN
        assertEquals( asSet( rule ), state.indexChanges().getAdded() );
    }

    @Test
    public void shouldIncludeAddedNodesWithCorrectProperty() throws Exception
    {
        // Given
        long nodeId = 1337l;
        int propertyKey = 2;
        String propValue = "hello";

        state.nodeDoReplaceProperty( nodeId, noNodeProperty( nodeId, propertyKey ), stringProperty(
                propertyKey, propValue ) );

        // When
        DiffSets<Long> diff = state.nodesWithChangedProperty( propertyKey, propValue );

        // Then
        assertThat( diff.getAdded(), equalTo( asSet( nodeId ) ) );
        assertThat( diff.getRemoved(), equalTo( emptySet ) );
    }

    @Test
    public void shouldExcludeNodesWithCorrectPropertyRemoved() throws Exception
    {
        // Given
        long nodeId = 1337l;
        int propertyKey = 2;
        String propValue = "hello";

        state.nodeDoRemoveProperty( nodeId, stringProperty( propertyKey, propValue ) );

        // When
        DiffSets<Long> diff = state.nodesWithChangedProperty( propertyKey, propValue );

        // Then
        assertThat( diff.getAdded(), equalTo( emptySet ) );
        assertThat( diff.getRemoved(), equalTo( asSet( nodeId ) ) );
    }

    @Test
    public void shouldListNodeAsDeletedIfItIsDeleted() throws Exception
    {
        // Given

        // When
        long nodeId = 1337l;
        state.nodeDoDelete( nodeId );

        // Then
        assertThat( asSet( state.addedAndRemovedNodes().getRemoved() ), equalTo( asSet( nodeId ) ) );
    }

    @Test
    public void shouldAddUniquenessConstraint() throws Exception
    {
        // when
        UniquenessConstraint constraint = new UniquenessConstraint( 1, 17 );
        state.constraintDoAdd( constraint, 7 );

        // then
        DiffSets<UniquenessConstraint> diff = state.constraintsChangesForLabel( 1 );
        assertEquals( Collections.singleton( constraint ), diff.getAdded() );
        assertTrue( diff.getRemoved().isEmpty() );
    }

    @Test
    public void addingUniquenessConstraintShouldBeIdempotent() throws Exception
    {
        // given
        UniquenessConstraint constraint1 = new UniquenessConstraint( 1, 17 );
        state.constraintDoAdd( constraint1, 7 );

        // when
        UniquenessConstraint constraint2 = new UniquenessConstraint( 1, 17 );
        state.constraintDoAdd( constraint2, 19 );

        // then
        assertEquals( constraint1, constraint2 );
        assertEquals( Collections.singleton( constraint1 ), state.constraintsChangesForLabel( 1 ).getAdded() );
    }

    @Test
    public void shouldDifferentiateBetweenUniquenessConstraintsForDifferentLabels() throws Exception
    {
        // when
        UniquenessConstraint constraint1 = new UniquenessConstraint( 1, 17 );
        state.constraintDoAdd( constraint1, 7 );
        UniquenessConstraint constraint2 = new UniquenessConstraint( 2, 17 );
        state.constraintDoAdd( constraint2, 19 );

        // then
        assertEquals( Collections.singleton( constraint1 ), state.constraintsChangesForLabel( 1 ).getAdded() );
        assertEquals( Collections.singleton( constraint2 ), state.constraintsChangesForLabel( 2 ).getAdded() );
    }

    @Test
    public void shouldListRelationshipsAsCreatedIfCreated() throws Exception
    {
        // When
        long relId = 10;
        state.relationshipDoCreate( relId, 0, 1, 2 );

        // Then
        assertTrue( state.hasChanges() );
        assertTrue( state.relationshipIsAddedInThisTx( relId ) );
    }

    @Test
    public void shouldAugmentWithAddedRelationships() throws Exception
    {
        // When
        int startNode = 1, endNode = 2, relType = 0;
        long relId = 10;
        state.relationshipDoCreate( relId, relType, startNode, endNode );

        // Then
        long otherRel = relId + 1;
        assertTrue( state.hasChanges() );
        assertThat( state.augmentRelationships( startNode, OUTGOING, iterator( otherRel ) ),
                containsLongs( relId, otherRel ) );
        assertThat( state.augmentRelationships( startNode, BOTH, iterator( otherRel ) ),
                containsLongs( relId, otherRel ) );
        assertThat( state.augmentRelationships( endNode, INCOMING, iterator( otherRel ) ),
                containsLongs( relId, otherRel ) );
        assertThat( state.augmentRelationships( endNode, BOTH, iterator( otherRel ) ),
                containsLongs( relId, otherRel ) );
        assertThat( state.addedRelationships( endNode, new int[]{relType}, BOTH ),
                containsLongs( relId ) );
        assertThat( state.addedRelationships( endNode, new int[]{relType+1}, BOTH ),
                containsLongs() );
    }

    @Test
    public void addedAndThenRemovedRelShouldNotShowUp() throws Exception
    {
        // Given
        int startNode = 1, endNode = 2, relType = 0;
        long relId = 10;
        state.relationshipDoCreate( relId, relType, startNode, endNode );

        // When
        state.relationshipDoDelete( relId, relType, startNode, endNode );

        // Then
        long otherRel = relId + 1;
        assertThat( state.augmentRelationships( startNode, OUTGOING, iterator( otherRel ) ),
                containsLongs( otherRel ) );
        assertThat( state.augmentRelationships( startNode, BOTH, iterator( otherRel ) ),
                containsLongs( otherRel ) );
        assertThat( state.augmentRelationships( endNode, INCOMING, iterator( otherRel ) ),
                containsLongs( otherRel ) );
        assertThat( state.augmentRelationships( endNode, BOTH, iterator( otherRel ) ),
                containsLongs( otherRel ) );
    }

    @Test
    public void shouldGiveCorrectDegreeWhenAddingAndRemovingRelationships() throws Exception
    {
        // Given
        int startNode = 1, endNode = 2, relType = 0;

        // When
        state.relationshipDoCreate( 10, relType, startNode, endNode );
        state.relationshipDoCreate( 11, relType, startNode, endNode );
        state.relationshipDoCreate( 12, relType + 1, startNode, endNode );
        state.relationshipDoCreate( 13, relType + 1, endNode, startNode );

        state.relationshipDoDelete( 1337, relType, startNode, endNode );
        state.relationshipDoDelete( 1338, relType + 1, startNode, startNode );

        // Then
        assertEquals( 12, state.augmentNodeDegree( startNode, 10, Direction.BOTH ) );
        assertEquals( 10, state.augmentNodeDegree( startNode, 10, Direction.INCOMING ) );
        assertEquals( 11, state.augmentNodeDegree( startNode, 10, Direction.BOTH, relType ) );
    }

    @Test
    public void shouldGiveCorrectRelationshipTypesForNode() throws Exception
    {
        // Given
        int startNode = 1, endNode = 2, relType = 0;

        // When
        long relA = 10, relB = 11, relC = 12;
        state.relationshipDoCreate( relA, relType, startNode, endNode );
        state.relationshipDoCreate( relB, relType, startNode, endNode );
        state.relationshipDoCreate( relC, relType + 1, startNode, endNode );

        state.relationshipDoDelete( relB, relType, startNode, endNode );
        state.relationshipDoDelete( relC, relType + 1, startNode, endNode );

        // Then
        assertThat( IteratorUtil.asList( state.nodeRelationshipTypes( startNode ) ), equalTo( Arrays.asList(relType)));
    }

    @Test
    public void shouldNotChangeRecordForCreatedAndDeletedNode() throws Exception
    {
        // GIVEN
        state.nodeDoCreate( 0 );
        state.nodeDoDelete( 0 );
        state.nodeDoCreate( 1 );

        // WHEN
        state.accept( new TxState.VisitorAdapter()
        {
            @Override
            public void visitCreatedNode( long id )
            {
                assertEquals( "Should not create any other node than 1", 1, id );
            }

            @Override
            public void visitDeletedNode( long id )
            {
                fail( "Should not delete any node" );
            }
        } );
    }

    @Test
    public void shouldNotChangeRecordForCreatedAndDeletedRelationship() throws Exception
    {
        // GIVEN
        state.relationshipDoCreate( 0, 0, 1, 2 );
        state.relationshipDoDelete( 0, 0, 1, 2 );
        state.relationshipDoCreate( 1, 0, 2, 3 );

        // WHEN
        state.accept( new TxState.VisitorAdapter()
        {
            @Override
            public void visitCreatedRelationship( long id, int type, long startNode, long endNode )
            {
                assertEquals( "Should not create any other relationship than 1", 1, id );
            }

            @Override
            public void visitDeletedRelationship( long id )
            {
                fail( "Should not delete any relationship" );
            }
        } );
    }

    private TxState state;
    private final Set<Long> emptySet = Collections.emptySet();

    @Before
    public void before() throws Exception
    {
        state = new TxStateImpl( mock( LegacyIndexTransactionState.class )
        );
    }
}
