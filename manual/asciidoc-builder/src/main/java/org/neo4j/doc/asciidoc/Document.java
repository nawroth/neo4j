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

public class Document
{
    static final String NEWLINE = "\n";
    private final StringBuilder sb = new StringBuilder( 8 * 1024 );

    private Document()
    {
        // wouldn't be valid
    }

    private Document( final Header header )
    {
        sb.append( header.toString() );
    }

    private Document( final Header header, final Content... contents )
    {
        this( header );
        for ( Content content : contents )
        {
            sb.append( content.toString() );
        }
    }

    public static Document with( final Header header, final Content... contents )
    {
        return new Document( header, contents );
    }

    @Override
    public String toString()
    {
        return sb.toString();
    }
}
