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
package io.joltcommunity.jolt.enrich;

import io.joltcommunity.jolt.JsonUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class EnrichrExternalApiTestHelper {

    private final String baseUrl;
    private final HttpClient httpClient;

    public EnrichrExternalApiTestHelper( String baseUrl ) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletionStage<Object> lookupProfile( Object value, Object input, Map<String, Object> context ) {
        String tenant = context == null || context.get( "tenant" ) == null ? "" : String.valueOf( context.get( "tenant" ) );
        String uri = baseUrl + "/profiles/" + value + "?tenant=" + URLEncoder.encode( tenant, StandardCharsets.UTF_8 );

        HttpRequest request = HttpRequest.newBuilder( URI.create( uri ) ).GET().build();

        return httpClient.sendAsync( request, HttpResponse.BodyHandlers.ofString() )
                .thenApply( response -> {
                    if ( response.statusCode() >= 400 ) {
                        throw new IllegalStateException( "Unexpected HTTP status: " + response.statusCode() );
                    }
                    return JsonUtils.jsonToObject( response.body() );
                } );
    }
}
