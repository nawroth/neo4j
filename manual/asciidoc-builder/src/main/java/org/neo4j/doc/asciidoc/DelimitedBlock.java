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

import org.neo4j.doc.asciidoc.Style.StyleContent;

public enum DelimitedBlock
{
    COMMENT( "////" ),
    PASSTHROGH( "++++" ),
    LISTING( "----" ),
    LITERAL( "...." ),
    SIDEBAR( "****" ),
    QUOTE( "____" ),
    EXAMPLE( "====" ),
    OPEN( "--" );

    private String delimiter;

    private DelimitedBlock( final String delimiter )
    {
        this.delimiter = delimiter;
    }

    public Content with( final Content... text )
    {
        return new DelimitedBlockContent( Style.NONE.with(), Heading.NONE, text );
    }

    public Content with( final StyleContent style, final Content... text )
    {
        return new DelimitedBlockContent( style, Heading.NONE, text );
    }

    public Content with( final Heading heading, final Content... text )
    {
        return new DelimitedBlockContent( Style.NONE.with(), heading, text );
    }

    public Content with( final StyleContent style, final Heading heading, final Content... text )
    {
        return new DelimitedBlockContent( style, heading, text );
    }

    public class DelimitedBlockContent implements Content
    {
        private final StringBuilder sb = new StringBuilder( 512 );

        private DelimitedBlockContent( StyleContent style, Heading heading, Content... text )
        {
            sb.append( style.toString() )
            .append( heading.toString() )
            .append( delimiter )
            .append( Document.NEWLINE );
            for ( Content content : text )
            {
                sb.append( content.toString() );
            }
            sb.append( delimiter )
            .append( Document.NEWLINE )
            .append( Document.NEWLINE );
        }

        @Override
        public String toString()
        {
            return sb.toString();
        }
    }
}
