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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.neo4j.server.helpers.FunctionalTestHelper;
import org.neo4j.server.plugins.PluginFunctionalTestHelper;
import org.neo4j.server.plugins.PluginFunctionalTestHelper.RegExp;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.rest.AbstractRestFunctionalTestBase;
import org.neo4j.server.rest.domain.GraphDbHelper;
import org.neo4j.server.rest.domain.JsonParseException;

public class AbstractPluginTestBase extends AbstractRestFunctionalTestBase
{

    private static FunctionalTestHelper functionalTestHelper;
    protected static GraphDbHelper helper;

    @BeforeClass
    public static void setupServer() throws IOException
    {
        functionalTestHelper = new FunctionalTestHelper( server() );
        helper = functionalTestHelper.getGraphDbHelper();
    }

    public void checkExtensionMetadata( Class<? extends ServerPlugin> klass, String name, String pattern )
            throws Exception
    {
        Map<String, Object> map = getDatabaseLevelPluginMetadata(klass);

        assertThat( (String) map.get( name ), RegExp.endsWith( String.format( pattern, klass.getSimpleName(), name ) ) );
    }

    @SuppressWarnings( "unchecked" )
    protected Map<String, Object> getDatabaseLevelPluginMetadata(Class<? extends ServerPlugin> klass) throws JsonParseException
    {
        Map<String, Object> map = PluginFunctionalTestHelper.makeGet( functionalTestHelper.dataUri() );
        assertThat( "Could not get server metadata.", map, notNullValue() );
        map = (Map<String, Object>) map.get( "extensions" );
        assertThat( "Missing extensions key in server metadata.", map, notNullValue() );
        map = (Map<String, Object>) map.get( klass.getSimpleName() );
        assertThat( "Missing '" + klass.getSimpleName() + "' key in extensions.", map, notNullValue() );
        return map;
    }

    protected Map<String, Object> getNodeLevelPluginMetadata(Class<? extends ServerPlugin> klass, long nodeId) throws JsonParseException
    {
        Map<String, Object> map = PluginFunctionalTestHelper.makeGet( functionalTestHelper.nodeUri(nodeId) );
        assertThat( "Could not get server metadata.", map, notNullValue() );
        map = (Map<String, Object>) map.get( "extensions" );
        assertThat( "Missing extensions key in server metadata.", map, notNullValue() );
        map = (Map<String, Object>) map.get( klass.getSimpleName() );
        assertThat( "Missing '" + klass.getSimpleName() + "' key in extensions.", map, notNullValue() );
        return map;
    }

    protected String performPost( String uri )
    {
        String result = gen.get()
                .noGraph()
                .expectedStatus( 200 )
                .post( uri )
                .entity();
        return result;
    }
}
