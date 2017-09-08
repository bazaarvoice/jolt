# Getting Started

## 1 Add Jolt maven dependencies to your pom file

Maven Dependency to Add to your pom file
``` xml
<dependency>
    <groupId>com.bazaarvoice.jolt</groupId>
    <artifactId>jolt-core</artifactId>
    <version>${latest.jolt.version}</version>
</dependency>
<dependency>
    <groupId>com.bazaarvoice.jolt</groupId>
    <artifactId>json-utils</artifactId>
    <version>${latest.jolt.version}</version>
</dependency>
```

Where `latest.jolt.version` looks like `0.0.16`, and can be found by looking at the [project's releases](https://github.com/bazaarvoice/jolt/releases).

The two maven artifacts are:

1. `jolt-core` : only one dependency on apache.commons for StringUtils
    * The goal is for the `jolt-core` artifact to be pure Java, so that it does not cause any dependency issues.
2. `json-utils` : Jackson wrapper and testing utilities.   Used by jolt-core as a test dependency.
    * If you are willing to pull in Jackson 2, this artifact provides nice utility methods.


## 2 Code and Sample Data

1. Copy-paste this code and sample data.
2. Get it to run
3. Replace the input and spec file with your own

### JoltSample.java

Available [here](https://github.com/bazaarvoice/jolt/tree/master/jolt-core/src/test/java/com/bazaarvoice/jolt/sample/JoltSample.java).

``` java
package com.bazaarvoice.jolt.sample;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.io.IOException;
import java.util.List;

public class JoltSample {

    public static void main(String[] args) throws IOException {

        // How to access the test artifacts, i.e. JSON files
        //  JsonUtils.classpathToList : assumes you put the test artifacts in your class path
        //  JsonUtils.filepathToList : you can use an absolute path to specify the files

        List chainrSpecJSON = JsonUtils.classpathToList( "/json/sample/spec.json" );
        Chainr chainr = Chainr.fromSpec( chainrSpecJSON );

        Object inputJSON = JsonUtils.classpathToObject( "/json/sample/input.json" );

        Object transformedOutput = chainr.transform( inputJSON );
        System.out.println( JsonUtils.toJsonString( transformedOutput ) );
    }
}
```

### /json/sample/input.json
Available [here](https://github.com/bazaarvoice/jolt/tree/master/jolt-core/src/test/resources/json/sample/input.json).

``` json
{
    "rating": {
        "primary": {
            "value": 3
        },
        "quality": {
            "value": 3
        }
    }
}
```

### /json/sample/spec.json
Available [here](https://github.com/bazaarvoice/jolt/tree/master/jolt-core/src/test/resources/json/sample/spec.json).

``` json
[
    {
        "operation": "shift",
        "spec": {
            "rating": {
                "primary": {
                    "value": "Rating"
                },
                "*": {
                    "value": "SecondaryRatings.&1.Value",
                    "$": "SecondaryRatings.&.Id"
                }
            }
        }
    },
    {
        "operation": "default",
        "spec": {
            "Range" : 5,
            "SecondaryRatings" : {
                "*" : {
                    "Range" : 5
                }
            }
        }
    }
]
```

### Output

With pretty formatting, looks like:

``` json
{
    "Rating": 3,
    "Range": 5,
    "SecondaryRatings": {
        "quality": {
            "Id": "quality",
            "Value": 3,
            "Range": 5
        }
    }
}
```
