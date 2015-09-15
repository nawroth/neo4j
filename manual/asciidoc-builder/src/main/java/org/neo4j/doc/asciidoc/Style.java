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

public enum Style implements Content
{
    LITERAL( "literal" ),
    VERSE( "verse" ),
    QUOTE( "quote" ),
    LISTING( "listing" ),
    TIP( "TIP" ),
    NOTE( "NOTE" ),
    IMPORTANT( "IMPORTANT" ),
    WARNING( "WARNING" ),
    CAUTION( "CAUTION" ),
    ABSTRACT( "abstract" ),
    PARTINTRO( "partintro" ),
    COMMENT( "comment" ),
    EXAMPLE( "example" ),
    SIDEBAR( "sidebar" ),
    SOURCE( "source" ),
    MUSIC( "music" ),
    LATEX( "latex" ),
    GRAPHVIZ( "graphviz" ),
    DOT( "dot" ), // Neo specific
    NONE( null );

    private String name;

    private Style( final String name )
    {
        this.name = name;
    }

    public StyleContent with( Attributes attributes )
    {
        return new StyleContent( attributes );
    }

    public StyleContent with()
    {
        return new StyleContent();
    }

    @Override
    public String toString()
    {
        if ( name == null )
        {
            return "";
        }
        else
        {
            return "[\"" + name + "\"]" + Document.NEWLINE;
        }
    }
    
    public class StyleContent implements Content
    {
        private final StringBuilder sb = new StringBuilder( 512 );

        private StyleContent()
        {
            sb.append( Style.this.toString() );
        }

        private StyleContent( Attributes attributes )
        {
            sb.append( "[" );
            if ( !(Style.this == NONE) )
            {
                sb.append( '"' ).append( name ).append( '"' );
            }
            if ( attributes != null )
            {
                sb.append( ',' ).append( attributes.asAttributeList() );
            }
            sb.append( "]" ).append( Document.NEWLINE );
        }

        @Override
        public String toString()
        {
            return sb.toString();
        }
    }
}
