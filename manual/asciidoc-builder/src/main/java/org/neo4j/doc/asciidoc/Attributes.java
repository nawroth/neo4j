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

public class Attributes
{
    private final Attribute[] attributes;

    private Attributes( final Attribute[] attributes )
    {
        this.attributes = attributes;
    }

    public static Attributes with( final Attribute... attributes )
    {
        return new Attributes( attributes );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( 1024 );
        for ( Attribute attribute : attributes )
        {
            sb.append( attribute.toString() );
        }
        sb.append( Document.NEWLINE );
        return sb.toString();
    }

    String asAttributeList()
    {
        StringBuilder sb = new StringBuilder( 1024 );
        String delimiter = "";
        for ( Attribute attribute : attributes )
        {            
            sb.append( delimiter ).append( attribute.asAttributeListItem() );
            delimiter = ",";
        }
        return sb.toString();
    }
}
