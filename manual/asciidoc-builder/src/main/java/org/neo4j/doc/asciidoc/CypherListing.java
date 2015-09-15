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

public class CypherListing
{
    private CypherListing()
    {
        // not valid
    }

    public static Content with( final Lines code )
    {
        return SourceListing.with( "cypher", code );
    }

    public static Content with( final String prepend_cypher, final Lines cypher, final String append_cypher )
    {
        return SourceListing.with( 
                Attributes.with(
                    Attribute.with( "language", "cypher" ),
                    Attribute.with( "prepend_cypher", prepend_cypher ),
                    Attribute.with( "append_cypher", append_cypher ) ),
                cypher );
    }

    @Override
    public String toString()
    {
        throw new IllegalStateException( "Not supposed to call toString() on this class." );
    }
}
