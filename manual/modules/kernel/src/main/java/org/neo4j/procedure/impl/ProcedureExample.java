/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.procedure.impl;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.Resource;

public class ProcedureExample
{
    @Resource
    public GraphDatabaseService db;

    /**
     * Finds all nodes in the database with more relationships than the specified threshold.
     * @param threshold only include nodes with at least this many relationships
     * @return a stream of records describing supernodes in this database
     */
    @Procedure
    public Stream<SuperNode> findSuperNodes( @Name("threshold") long threshold )
    {
        return db.getAllNodes().stream()
                .filter( (node) -> node.getDegree() > threshold )
                .map( SuperNode::new );
    }

    /**
     * Output record for {@link #findSuperNodes(long)}.
     */
    public static class SuperNode
    {
        public long nodeId;
        public long degree;

        public SuperNode( Node node )
        {
            this.nodeId = node.getId();
            this.degree = node.getDegree();
        }
    }
}
