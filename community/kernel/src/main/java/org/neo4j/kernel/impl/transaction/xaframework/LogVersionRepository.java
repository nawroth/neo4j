/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.kernel.impl.transaction.xaframework;

import java.io.IOException;

/*
 * TODO 2.2-future This should not be required. Please remove - log versions is a knowledge best shared by as
 * few as possible
 */
public interface LogVersionRepository
{
	/**
	 * Returns the current log version. It is non blocking.
	 */
    long getCurrentLogVersion();

    /**
     * Increments (making sure it is persisted on disk) and returns the latest log version for this repository.
     * It does so atomically and can potentially block.
     */
    long incrementAndGetVersion() throws IOException;
}
