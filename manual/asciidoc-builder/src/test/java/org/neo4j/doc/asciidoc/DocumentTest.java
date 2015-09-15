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

public class DocumentTest
{
    @Test
    public void document()
    {
        // Given
        Document document = Document.with( Header.with( "The Title" ), Paragraph.with( "Some text." ) );
        // When
        String output = document.toString();
        // Then
        assertThat( output, equalTo( "= The Title\n\nSome text.\n\n" ) );
    }

    @Test
    public void document_with_attributes()
    {
        // Given
        Document document = Document.with( Header.with( "The Title",
                Attributes.with( Attribute.with( "author", "The Neo4j Team" ),
                                 Attribute.with( "year", "2015" ) ) ),
                Paragraph.with( "Some text." ) );
        // When
        String output = document.toString();
        // Then
        assertThat( output, equalTo( "= The Title\n\n:author: The Neo4j Team\n:year: 2015\n\nSome text.\n\n" ) );
    }

    @Test
    public void document_with_sections()
    {
        // Given
        Document document = Document.with(
                Header.with( "The Title" ),
                Paragraph.with( "Some text first." ),
                Section.with( "Heading",
                    Paragraph.with( "Some text." ),
                    Section.with( "Heading2",
                        Paragraph.with( "Some more text." ) )
                ) );
        // When
        String output = document.toString();
        // Then
        assertThat( output, equalTo(
                  "= The Title\n"
                + "\n"
                + "Some text first.\n"
                + "\n"
                + "== Heading\n"
                + "\n"
                + "Some text.\n"
                + "\n"
                + "=== Heading2\n"
                + "\n"
                + "Some more text.\n"
                + "\n" ) );
    }
}
