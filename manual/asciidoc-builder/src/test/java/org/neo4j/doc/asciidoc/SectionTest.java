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

public class SectionTest
{
    @Test
    public void section()
    {
        // Given
        Section section = Section.with( "Heading", Paragraph.with( "Some text." ) );
        // When
        String output = section.toString();
        // Then
        assertThat( output, equalTo( "== Heading\n\nSome text.\n\n" ) );
    }

    @Test
    public void nested_section()
    {
        // Given
        Section section = Section.with( "Heading", Paragraph.with( "Some text." ),
                Section.with( "Heading2", Paragraph.with( "Some more text." ) ) );
        // When
        String output = section.toString();
        // Then
        assertThat( output, equalTo( "== Heading\n\nSome text.\n\n=== Heading2\n\nSome more text.\n\n" ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void subsections_folloed_by_content_is_illegal()
    {
        // Given
        Section.with( "Heading", Paragraph.with( "Some text." ),
                Section.with( "Heading2", Paragraph.with( "Some more text." ) ),
                Paragraph.with( "Some text at the wrong place." ) );
    }
}
