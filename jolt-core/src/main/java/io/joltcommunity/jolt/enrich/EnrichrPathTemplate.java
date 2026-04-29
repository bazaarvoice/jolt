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

import io.joltcommunity.jolt.exception.SpecException;
import io.joltcommunity.jolt.traversr.SimpleTraversr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parses and resolves enrich input and output paths.
 * <p>
 * Supported path forms are:
 * <ul>
 *     <li>Map keys such as {@code customer.id}</li>
 *     <li>Explicit array indices such as {@code customers.[0].id}</li>
 *     <li>Array wildcards such as {@code customers.[*].id}</li>
 *     <li>Output-only append segments such as {@code profiles.[]}</li>
 * </ul>
 * The template can both match input values and resolve output keys by substituting wildcard bindings
 * captured from an input match.
 */
final class EnrichrPathTemplate {

    private final String configuredPath;
    private final List<Segment> segments;
    private final int wildcardCount;
    private final boolean hasAppendSegment;
    private final SimpleTraversr<Object> traversr;

    /**
     * Create an immutable parsed path template.
     *
     * @param configuredPath original path string taken from the enrich spec
     * @param segments parsed path segments
     * @param wildcardCount number of {@code [*]} segments in the template
     * @param hasAppendSegment whether the template contains output append semantics via {@code []}
     */
    private EnrichrPathTemplate( String configuredPath, List<Segment> segments, int wildcardCount, boolean hasAppendSegment ) {
        this.configuredPath = configuredPath;
        this.segments = Collections.unmodifiableList( new ArrayList<>( segments ) );
        this.wildcardCount = wildcardCount;
        this.hasAppendSegment = hasAppendSegment;
        this.traversr = new SimpleTraversr<>( configuredPath );
    }

    /**
     * Parse a source path used to read values from the input document.
     * <p>
     * Input paths support fixed keys, explicit indices, and {@code [*]} wildcards. They intentionally do not
     * support {@code []} append semantics because append only makes sense on the write side.
     *
     * @param configuredPath source path declared in the enrich spec
     * @param index enrich rule index used in validation messages
     * @return parsed input path template
     */
    static EnrichrPathTemplate parseInput( String configuredPath, int index ) {
        EnrichrPathTemplate template = parse( configuredPath, index, "path" );
        if ( template.hasAppendSegment ) {
            throw new SpecException(
                    "Enrichr enrichment at index:" + index +
                            " does not support '[]' in 'path'. Use explicit indices like '[0]' or array wildcards like '[*]'."
            );
        }
        return template;
    }

    /**
     * Parse a destination path used when writing the enriched value back to the document.
     *
     * @param configuredPath output path declared in the enrich spec
     * @param index enrich rule index used in validation messages
     * @return parsed output path template
     */
    static EnrichrPathTemplate parseOutput( String configuredPath, int index ) {
        return parse( configuredPath, index, "outputPath" );
    }

    /**
     * Parse a human-readable dot path into typed path segments.
     *
     * @param configuredPath path string declared in the enrich spec
     * @param index enrich rule index used in validation messages
     * @param fieldName spec field currently being parsed, either {@code path} or {@code outputPath}
     * @return parsed path template
     */
    private static EnrichrPathTemplate parse( String configuredPath, int index, String fieldName ) {
        List<String> tokens = tokenize( configuredPath );
        List<Segment> segments = new ArrayList<>( tokens.size() );
        int wildcardCount = 0;
        boolean hasAppendSegment = false;

        for ( String token : tokens ) {
            if ( "[]".equals( token ) ) {
                segments.add( Segment.append() );
                hasAppendSegment = true;
                continue;
            }

            if ( token.startsWith( "[" ) && token.endsWith( "]" ) ) {
                String innerValue = token.substring( 1, token.length() - 1 ).trim();
                if ( innerValue.isEmpty() ) {
                    throw new SpecException(
                            "Enrichr enrichment at index:" + index + " has an invalid '" + fieldName + "' segment '" + token + "'."
                    );
                }

                if ( "*".equals( innerValue ) ) {
                    segments.add( Segment.wildcard() );
                    wildcardCount++;
                    continue;
                }

                try {
                    int arrayIndex = Integer.parseInt( innerValue );
                    if ( arrayIndex < 0 ) {
                        throw new NumberFormatException( "negative" );
                    }
                    segments.add( Segment.arrayIndex( innerValue ) );
                    continue;
                }
                catch ( NumberFormatException e ) {
                    throw new SpecException(
                            "Enrichr enrichment at index:" + index + " has an invalid '" + fieldName + "' segment '" + token +
                                    "'. Supported array syntax is '[0]', '[1]', or '[*]'."
                    );
                }
            }

            segments.add( Segment.mapKey( token ) );
        }

        return new EnrichrPathTemplate( configuredPath, segments, wildcardCount, hasAppendSegment );
    }

    /**
     * Split a dot path into raw tokens while preserving bracketed array segments as standalone elements.
     *
     * @param configuredPath path string declared in the enrich spec
     * @return tokenized path segments
     */
    private static List<String> tokenize( String configuredPath ) {
        String intermediatePath = configuredPath.replace( "[", ".[" ).replace( "..", "." );
        if ( intermediatePath.charAt( 0 ) == '.' ) {
            intermediatePath = intermediatePath.substring( 1 );
        }

        String[] rawKeys = intermediatePath.split( "\\." );
        List<String> keys = new ArrayList<>( rawKeys.length );
        Collections.addAll( keys, rawKeys );
        return keys;
    }

    /**
     * Resolve all input values that satisfy this template.
     *
     * @param input document to search
     * @return zero or more concrete path matches
     */
    List<EnrichrPathMatch> match( Object input ) {
        List<EnrichrPathMatch> matches = new ArrayList<>();
        collectMatches( input, 0, new ArrayList<String>(), new ArrayList<String>(), matches );
        return matches;
    }

    /**
     * Recursively walk the input document and collect every concrete path that matches this template.
     *
     * @param currentValue current node being inspected
     * @param segmentIndex current segment position within the template
     * @param resolvedKeys concrete path keys collected so far
     * @param wildcardBindings wildcard array indices collected so far
     * @param matches destination list for resolved matches
     */
    private void collectMatches(
            Object currentValue,
            int segmentIndex,
            List<String> resolvedKeys,
            List<String> wildcardBindings,
            List<EnrichrPathMatch> matches
    ) {
        if ( segmentIndex == segments.size() ) {
            matches.add( new EnrichrPathMatch( currentValue, resolvedKeys, wildcardBindings, resolvePath( wildcardBindings ) ) );
            return;
        }

        if ( currentValue == null ) {
            return;
        }

        Segment segment = segments.get( segmentIndex );
        if ( segment.type == SegmentType.MAP_KEY ) {
            if ( currentValue instanceof Map ) {
                @SuppressWarnings( "unchecked" )
                Map<String, Object> map = (Map<String, Object>) currentValue;
                if ( map.containsKey( segment.value ) ) {
                    resolvedKeys.add( segment.value );
                    collectMatches( map.get( segment.value ), segmentIndex + 1, resolvedKeys, wildcardBindings, matches );
                    resolvedKeys.remove( resolvedKeys.size() - 1 );
                }
            }
            return;
        }

        if ( segment.type == SegmentType.ARRAY_INDEX ) {
            if ( currentValue instanceof List ) {
                List<?> list = (List<?>) currentValue;
                int arrayIndex = Integer.parseInt( segment.value );
                if ( arrayIndex < list.size() ) {
                    resolvedKeys.add( segment.value );
                    collectMatches( list.get( arrayIndex ), segmentIndex + 1, resolvedKeys, wildcardBindings, matches );
                    resolvedKeys.remove( resolvedKeys.size() - 1 );
                }
            }
            return;
        }

        if ( segment.type == SegmentType.ARRAY_WILDCARD ) {
            if ( currentValue instanceof List ) {
                List<?> list = (List<?>) currentValue;
                for ( int index = 0; index < list.size(); index++ ) {
                    String resolvedIndex = String.valueOf( index );
                    resolvedKeys.add( resolvedIndex );
                    wildcardBindings.add( resolvedIndex );
                    collectMatches( list.get( index ), segmentIndex + 1, resolvedKeys, wildcardBindings, matches );
                    wildcardBindings.remove( wildcardBindings.size() - 1 );
                    resolvedKeys.remove( resolvedKeys.size() - 1 );
                }
            }
            return;
        }

        return;
    }

    /**
     * Return a traversr instance for writing values to this template after wildcard substitution.
     *
     * @return traversr configured for this path template
     */
    SimpleTraversr<Object> getTraversr() {
        return traversr;
    }

    /**
     * Return the number of {@code [*]} segments declared in this template.
     *
     * @return wildcard segment count
     */
    int getWildcardCount() {
        return wildcardCount;
    }

    /**
     * Indicate whether the template contains output append semantics via {@code []}.
     *
     * @return {@code true} when the template includes an append segment
     */
    boolean hasAppendSegment() {
        return hasAppendSegment;
    }

    /**
     * Resolve traversr keys for a concrete output path.
     *
     * @param wildcardBindings wildcard indices captured from the input path
     * @return keys suitable for {@link SimpleTraversr#set(Object, List, Object)}
     */
    List<String> resolveKeys( List<String> wildcardBindings ) {
        validateBindings( wildcardBindings );

        List<String> resolvedKeys = new ArrayList<>( segments.size() );
        int wildcardIndex = 0;
        for ( Segment segment : segments ) {
            if ( segment.type == SegmentType.MAP_KEY || segment.type == SegmentType.ARRAY_INDEX ) {
                resolvedKeys.add( segment.value );
            }
            else if ( segment.type == SegmentType.ARRAY_WILDCARD ) {
                resolvedKeys.add( wildcardBindings.get( wildcardIndex++ ) );
            }
            else {
                resolvedKeys.add( "[]" );
            }
        }
        return resolvedKeys;
    }

    /**
     * Render a concrete human-readable path by substituting wildcard bindings into this template.
     *
     * @param wildcardBindings wildcard indices captured from the input path
     * @return resolved path string
     */
    String resolvePath( List<String> wildcardBindings ) {
        validateBindings( wildcardBindings );

        StringBuilder pathBuilder = new StringBuilder();
        int wildcardIndex = 0;
        for ( int index = 0; index < segments.size(); index++ ) {
            if ( index > 0 ) {
                pathBuilder.append( '.' );
            }

            Segment segment = segments.get( index );
            if ( segment.type == SegmentType.MAP_KEY ) {
                pathBuilder.append( segment.value );
            }
            else if ( segment.type == SegmentType.ARRAY_INDEX ) {
                pathBuilder.append( '[' ).append( segment.value ).append( ']' );
            }
            else if ( segment.type == SegmentType.ARRAY_WILDCARD ) {
                pathBuilder.append( '[' ).append( wildcardBindings.get( wildcardIndex++ ) ).append( ']' );
            }
            else {
                pathBuilder.append( "[]" );
            }
        }
        return pathBuilder.toString();
    }

    /**
     * Ensure enough wildcard values were captured from the input path to resolve this template.
     *
     * @param wildcardBindings wildcard indices captured from the input path
     */
    private void validateBindings( List<String> wildcardBindings ) {
        if ( wildcardBindings.size() < wildcardCount ) {
            throw new IllegalArgumentException(
                    "Expected at least " + wildcardCount + " wildcard bindings for path '" + configuredPath + "', got " + wildcardBindings.size() + "."
            );
        }
    }

    /**
     * Internal representation of one parsed path segment.
     */
    private enum SegmentType {
        MAP_KEY,
        ARRAY_INDEX,
        ARRAY_WILDCARD,
        ARRAY_APPEND
    }

    private static final class Segment {
        private final SegmentType type;
        private final String value;

        private Segment( SegmentType type, String value ) {
            this.type = type;
            this.value = value;
        }

        private static Segment mapKey( String value ) {
            return new Segment( SegmentType.MAP_KEY, value );
        }

        private static Segment arrayIndex( String value ) {
            return new Segment( SegmentType.ARRAY_INDEX, value );
        }

        private static Segment wildcard() {
            return new Segment( SegmentType.ARRAY_WILDCARD, null );
        }

        private static Segment append() {
            return new Segment( SegmentType.ARRAY_APPEND, null );
        }
    }
}
