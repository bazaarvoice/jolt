/*
 * Copyright 2026 Jolt Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.joltcommunity.jolt;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.joltcommunity.jolt.chainr.spec.ChainrEntry;
import io.joltcommunity.jolt.enrich.EnrichrExternalApiTestHelper;
import io.joltcommunity.jolt.enrich.EnrichrTestHelper;
import io.joltcommunity.jolt.exception.SpecException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnrichrTest {

    @DataProvider
    public Object[][] getFixtureTestCases() {
        return new Object[][]{
                {"/json/enrich/classNameSync.json"},
                {"/json/enrich/asyncPublisher.json"},
                {"/json/enrich/contextKeySync.json"},
                {"/json/enrich/arrayIndexSync.json"},
                {"/json/enrich/arrayWildcardSync.json"},
                {"/json/enrich/arrayWildcardAppendSync.json"},
                {"/json/enrich/nestedArrayWildcardAsync.json"}
        };
    }

    @DataProvider
    public Object[][] getExternalApiFixtureTestCases() {
        return new Object[][]{
                {"/json/enrich/externalApiAsync.json"},
                {"/json/enrich/externalApiArrayAsync.json"}
        };
    }

    @Test(dataProvider = "getFixtureTestCases")
    @SuppressWarnings( "unchecked" )
    public void enrich_fixtureTests( String testFile ) throws IOException {
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testFile );
        Object spec = testUnit.get( "spec" );
        Object input = testUnit.get( "input" );
        Map<String, Object> context = fixtureContext(
                (Map<String, Object>) testUnit.get( "context" ),
                (List<String>) testUnit.get( "helperContextKeys" )
        );
        Object expected = testUnit.get( "expected" );

        Chainr chainr = Chainr.fromSpec( spec );

        Assert.assertTrue( chainr.hasContextualTransforms() );
        Assert.assertEquals( chainr.getContextualTransforms().size(), 1 );

        Object actual = chainr.transform( input, context );

        JoltTestUtil.runDiffy( "failed case " + testFile, expected, actual );
    }

    @Test(dataProvider = "getExternalApiFixtureTestCases")
    @SuppressWarnings( "unchecked" )
    public void enrich_fixtureExternalApiTest( String testFile ) throws Exception {
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testFile );

        HttpServer server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
        server.createContext( "/profiles", this::handleProfileLookup );
        server.start();

        try {
            Map<String, Object> context = new LinkedHashMap<>( (Map<String, Object>) testUnit.get( "context" ) );
            context.put( "customerLookupClient", new EnrichrExternalApiTestHelper( "http://127.0.0.1:" + server.getAddress().getPort() ) );

            Chainr chainr = Chainr.fromSpec( testUnit.get( "spec" ) );

            Assert.assertTrue( chainr.hasContextualTransforms() );
            Assert.assertEquals( chainr.getContextualTransforms().size(), 1 );

            Object actual = chainr.transform( testUnit.get( "input" ), context );

            JoltTestUtil.runDiffy( "failed case " + testFile, testUnit.get( "expected" ), actual );
        }
        finally {
            server.stop( 0 );
        }
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsNullSpec() {
        new Enrichr( null );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsNonMapSpec() {
        new Enrichr( "not-a-map" );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsNonListEnrichments() {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put( "enrichments", "not-a-list" );
        new Enrichr( spec );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsBlankExecutionMode() {
        new Enrichr( newEnrichSpec( "   ", enrichmentRule( "name", null, "uppercase" ) ) );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsNonStringExecutionMode() {
        Map<String, Object> spec = newEnrichSpec( null, enrichmentRule( "name", null, "uppercase" ) );
        spec.put( "executionMode", Boolean.TRUE );
        new Enrichr( spec );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsAppendSyntaxInInputPath() {
        new Enrichr( newEnrichSpec( null, enrichmentRule( "customers.[].id", null, "uppercase" ) ) );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsWildcardPathWithFixedOutputPath() {
        new Enrichr( newEnrichSpec( null, enrichmentRule( "customers.[*].id", "profiles.lookup", "uppercase" ) ) );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsWildcardBindingCountMismatch() {
        new Enrichr( newEnrichSpec( null, enrichmentRule( "orders.[*].items.[*].sku", "orders.[*].inventory", "uppercase" ) ) );
    }

    @Test
    public void enrich_itOverwritesTheSourceField() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "name", "alice" );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( null, enrichmentRule( "name", null, "uppercase" ) ) );

        Object output = chainr.transform( input, null );

        Assert.assertSame( output, input );
        Assert.assertEquals( input.get( "name" ), "ALICE" );
    }

    @Test
    public void enrich_itCanWriteToAnotherFieldAndUseContext() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put( "id", "cust-123" );

        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "customer", customer );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put( "tenant", "acme" );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( null, enrichmentRule( "customer.id", "customer.profile", "describe" ) ) );

        Object output = chainr.transform( input, context );

        Assert.assertSame( output, input );

        @SuppressWarnings( "unchecked" )
        Map<String, Object> profile = (Map<String, Object>) customer.get( "profile" );
        Assert.assertEquals( profile.get( "original" ), "cust-123" );
        Assert.assertEquals( profile.get( "inputType" ), "LinkedHashMap" );
        Assert.assertEquals( profile.get( "tenant" ), "acme" );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsMissingEnrichments() {
        new Enrichr( newEnrichSpec( null ) );
    }

    @Test
    public void enrich_itCanUseATargetResolvedFromContext() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put( "id", "cust-123" );

        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "customer", customer );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put( "tenant", "acme" );
        context.put( "lookupBean", new EnrichrTestHelper() );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( null, contextEnrichmentRule( "customer.id", "customer.profile", "lookupBean", "describeViaBean" ) ) );

        Object output = chainr.transform( input, context );

        Assert.assertSame( output, input );

        @SuppressWarnings( "unchecked" )
        Map<String, Object> profile = (Map<String, Object>) customer.get( "profile" );
        Assert.assertEquals( profile.get( "original" ), "cust-123" );
        Assert.assertEquals( profile.get( "tenant" ), "acme" );
    }

    @Test
    public void enrich_itResolvesCompletionStageResults() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "name", "alice" );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( "sync", enrichmentRule( "name", null, "asyncUppercase" ) ) );

        Object output = chainr.transform( input, null );

        Assert.assertSame( output, input );
        Assert.assertEquals( input.get( "name" ), "ALICE" );
    }

    @Test
    public void enrich_itResolvesPublisherResultsInAsyncMode() {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put( "id", "cust-123" );

        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "customer", customer );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put( "tenant", "acme" );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( "async", enrichmentRule( "customer.id", "customer.profile", "publisherDescribe" ) ) );

        Object output = chainr.transform( input, context );

        Assert.assertSame( output, input );

        @SuppressWarnings( "unchecked" )
        Map<String, Object> profile = (Map<String, Object>) customer.get( "profile" );
        Assert.assertEquals( profile.get( "original" ), "cust-123" );
        Assert.assertEquals( profile.get( "tenant" ), "acme" );
    }

    @Test
    public void enrich_itIgnoresMissingPathsInSyncMode() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "name", "alice" );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( "sync", enrichmentRule( "customer.id", "customer.profile", "describe" ) ) );

        Object output = chainr.transform( input, null );

        Assert.assertSame( output, input );
        Assert.assertEquals( input.size(), 1 );
        Assert.assertFalse( input.containsKey( "customer" ) );
    }

    @Test
    public void enrich_itIgnoresMissingPathsInAsyncMode() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put( "name", "alice" );

        Chainr chainr = Chainr.fromSpec( newChainrSpec( "async", enrichmentRule( "customer.id", "customer.profile", "publisherDescribe" ) ) );

        Object output = chainr.transform( input, null );

        Assert.assertSame( output, input );
        Assert.assertEquals( input.size(), 1 );
        Assert.assertFalse( input.containsKey( "customer" ) );
    }

    @Test( expectedExceptions = SpecException.class )
    public void enrich_itRejectsUnsupportedExecutionMode() {
        Chainr.fromSpec( newChainrSpec( "parallel", enrichmentRule( "name", null, "uppercase" ) ) );
    }

    @SafeVarargs
    private final Map<String, Object> newEnrichSpec( String executionMode, Map<String, Object>... rules ) {
        Map<String, Object> enrichSpec = new LinkedHashMap<>();
        if ( executionMode != null ) {
            enrichSpec.put( "executionMode", executionMode );
        }

        List<Map<String, Object>> enrichments = new ArrayList<>();
        for ( Map<String, Object> rule : rules ) {
            enrichments.add( rule );
        }
        enrichSpec.put( "enrichments", enrichments );
        return enrichSpec;
    }

    private List<Map<String, Object>> newChainrSpec( String executionMode, Map<String, Object> rule ) {
        List<Map<String, Object>> spec = new ArrayList<>();
        Map<String, Object> enrichOperation = new LinkedHashMap<>();
        enrichOperation.put( ChainrEntry.OPERATION_KEY, "enrich" );

        enrichOperation.put( ChainrEntry.SPEC_KEY, newEnrichSpec( executionMode, rule ) );
        spec.add( enrichOperation );
        return spec;
    }

    private Map<String, Object> enrichmentRule( String path, String outputPath, String methodName ) {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put( "path", path );
        if ( outputPath != null ) {
            rule.put( "outputPath", outputPath );
        }
        rule.put( "className", EnrichrTestHelper.class.getName() );
        rule.put( "method", methodName );
        return rule;
    }

    private Map<String, Object> contextEnrichmentRule( String path, String outputPath, String contextKey, String methodName ) {
        Map<String, Object> rule = new LinkedHashMap<>();
        rule.put( "path", path );
        if ( outputPath != null ) {
            rule.put( "outputPath", outputPath );
        }
        rule.put( "contextKey", contextKey );
        rule.put( "method", methodName );
        return rule;
    }

    private Map<String, Object> fixtureContext( Map<String, Object> rawContext, List<String> helperContextKeys ) {
        Map<String, Object> context = rawContext == null ? new LinkedHashMap<String, Object>() : new LinkedHashMap<>( rawContext );

        if ( helperContextKeys != null ) {
            for ( String helperContextKey : helperContextKeys ) {
                context.put( helperContextKey, new EnrichrTestHelper() );
            }
        }

        return context.isEmpty() ? null : context;
    }

    private void handleProfileLookup( HttpExchange exchange ) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String prefix = "/profiles/";

        if ( ! "GET".equals( exchange.getRequestMethod() ) || ! requestPath.startsWith( prefix ) ) {
            exchange.sendResponseHeaders( 404, -1 );
            exchange.close();
            return;
        }

        String customerId = requestPath.substring( prefix.length() );
        String tenant = extractQueryParam( exchange.getRequestURI().getRawQuery(), "tenant" );

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put( "customerId", customerId );
        responseBody.put( "tenant", tenant );
        responseBody.put( "segment", "gold" );
        responseBody.put( "source", "external-api" );

        byte[] responseBytes = JsonUtils.toJsonString( responseBody ).getBytes( StandardCharsets.UTF_8 );
        exchange.getResponseHeaders().add( "Content-Type", "application/json" );
        exchange.sendResponseHeaders( 200, responseBytes.length );

        try ( OutputStream outputStream = exchange.getResponseBody() ) {
            outputStream.write( responseBytes );
        }
    }

    private String extractQueryParam( String rawQuery, String key ) {
        if ( rawQuery == null || rawQuery.isEmpty() ) {
            return null;
        }

        for ( String entry : rawQuery.split( "&" ) ) {
            String[] keyValue = entry.split( "=", 2 );
            if ( key.equals( keyValue[0] ) ) {
                return keyValue.length == 2 ? URLDecoder.decode( keyValue[1], StandardCharsets.UTF_8 ) : "";
            }
        }

        return null;
    }
}
