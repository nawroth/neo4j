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

public class Section implements Content
{
    private final StringBuilder sb = new StringBuilder( 8 * 1024 );
    private final String heading;
    private final Content[] contents;
    private int level = 2;

    private Section( final String heading, final Content[] contents )
    {
        this.heading = heading;
        this.contents = contents;
    }

    public static Section with( final String heading, final Content... contents )
    {
        boolean foundSections = false;
        for ( Content content : contents )
        {
            if ( content instanceof Section )
            {
                foundSections = true;
            }
            else if ( foundSections == true )
            {
                throw new IllegalArgumentException( "Subsections can not be followed by additional content." );
            }
        }
        return new Section( heading, contents );
    }

    private void setLevel( int newLevel )
    {
        this.level = newLevel;
    }

    @Override
    public String toString()
    {
        for ( int i = 1; i <= level; i++ )
        {
            sb.append( '=' );
        }
        sb.append( ' ' );
        sb.append( heading ).append( Document.NEWLINE ).append( Document.NEWLINE );
        for ( Content content : contents )
        {
            if ( content instanceof Section )
            {
                ((Section) content).setLevel( level + 1 );
            }
            sb.append( content.toString() );
        }
        return sb.toString();
    }
}
