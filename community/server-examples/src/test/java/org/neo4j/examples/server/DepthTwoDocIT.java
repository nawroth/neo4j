/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.examples.server;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.neo4j.examples.server.plugins.DepthTwo;
import org.neo4j.examples.server.plugins.GetAll;
import org.neo4j.graphdb.DynamicLabel;

public class DepthTwoDocIT extends AbstractPluginTestBase
{
    private static final String NODES_ON_DEPTH_TWO = "nodesOnDepthTwo";
    private static final String RELATIONSHIPS_ON_DEPTH_TWO = "relationshipsOnDepthTwo";
    private static final String PATHS_ON_DEPTH_TWO = "pathsOnDepthTwo";

    protected String getDocumentationSectionName()
    {
        return "rest-api";
    }

    @Test
    public void testName() throws Exception
    {
        long nodeId = helper.createNode(DynamicLabel.label("test"));
        Map map = getNodeLevelPluginMetadata(DepthTwo.class, nodeId);
        assertTrue(map.keySet().size() > 0);
    }
    
}
