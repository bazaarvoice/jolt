# Getting Started

## 1 Add Jolt maven dependencies to your pom file

Maven Dependency to Add to your pom file
```
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

Where `latest.jolt.version` looks like `0.0.5`, and can be found by looking at the [project's tags](https://github.com/bazaarvoice/jolt/tags).

The two maven artifacts are:

1. `jolt-core` : only one dependency on apache.commons for StringUtils
    * The goal is for the `jolt-core` artifact to be pure Java, so that it does not cause any dependency issues.
2. `json-utils` : Jackson wrapper and testing utilities.   Used by jolt-core as a test dependency.
    * If you are willing to pull in Jackson 2, this artifact provides nice utility methods.


## 2 Code and Sample Data

1. Copy - paste this code and sample data.
2. Get it to run
3. Replace the input and spec file with your own

### JoltSample.java
```
import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.io.IOException;

public class JoltSample {

    public static void main(String[] args) throws IOException {

        Object chainrSpecJSON = JsonUtils.jsonToObject( JoltSample.class.getResourceAsStream( "chainrSpec.json" ) );
        Chainr chainr = new Chainr( chainrSpecJSON );

        Object inputJSON = JsonUtils.jsonToObject( JoltSample.class.getResourceAsStream( "input.json" ) );

        Object transformedOutput = chainr.transform( inputJSON );
        System.out.println( JsonUtils.toJsonString( transformedOutput ) );
    }
}
```

### input.json
```
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

### chainrSpec.json
```
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

Minus the pretty formatting, looks like :
```
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
