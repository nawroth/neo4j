/*
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.doc.asciidoc;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;

public class CypherListingTest
{
    @Test
    public void test()
    {
        // Given
        Content cypher = CypherListing.with( Lines.with( "MATCH (n) RETURN n" ) );
        // When
        String output = cypher.toString();
        // Then
        assertThat( output, equalTo( "[\"source\",language=\"cypher\"]\n----\nMATCH (n) RETURN n\n----\n\n" ) );
    }

    @Test
    public void with_prepend_and_append()
    {
        // Given
        Content cypher = CypherListing.with(
                        "MATCH n RETURN",
            Lines.with( "CASE n.eyes",
                        " WHEN 'blue' THEN 1",
                        " WHEN 'brown' THEN 2",
                        " ELSE 3",
                        "END"),
                        "AS result" );
        // When
        String output = cypher.toString();
        // Then
        assertThat( output, equalTo(
                "[\"source\",language=\"cypher\","
                + "prepend_cypher=\"MATCH n RETURN\","
                + "append_cypher=\"AS result\"]\n"
                + "----\nCASE n.eyes\n WHEN 'blue' THEN 1\n WHEN 'brown' THEN 2\n ELSE 3\nEND\n----\n\n" ) );
    }
}
