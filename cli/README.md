Jolt Command Line Interface
========

# Purpose

The bin/ directory contains a command line interface (CLI) tool for using some of the functionality contained in Jolt. Three sub commands are available:

* transform: given a Jolt transform spec, runs the specified transforms on the input data.
* diffy: compare to json documents to see if there are any differences.
* sort: sort a json document.

The Jolt tool has the ability to accept input from standard in:

``` sh
curl -s "http://some.json.api.com/data/you/will/take/our/stupid/format/and/like/it" | jolt transform makeSaneSpec.json | jolt sort
curl -s "http://some.host.com/stuff/data.json" | jolt diffy moreData.json
```

# Setup

1. clone the project (e.g., to $JOLT_CHECKOUT)
1. cd $JOLT_CHECKOUT; git pull; mvn clean package
1. add $JOLT_CHECKOUT/bin/ to your PATH.

# Reference

## Transform Sub Command

The transform sub command will ingest a JSON spec file and an JSON input (from a file or standard input) and run the transforms specified in the spec file on the input. The program will return an exit code of 0 if the input is transformed successfully or a 1 if an error is encountered.

    usage: jolt transform [-h] [-u] spec [input]

positional arguments:

    spec               File path to Jolt Transform Spec to execute on the input. This file should contain valid JSON.
    input              File path to the input JSON for the Jolt  Transform  operation. This file should contain valid JSON. If this
                       argument is not specified then standard input will be used.

optional arguments:

    -h, --help         show this help message and exit
    -u                 Turns off pretty print for the output. Output will be raw json with no formatting. (default: false)

### Example

To run transforms specified in 'spec.json' on input 'input.json':

``` sh
jolt transform spec.json input.json
```

## Diffy Sub Command

Jolt CLI Diffy Tool. This tool will ingest two JSON inputs (from files or standard input) and perform the Jolt Diffy operation to detect any differences. The program will return an exit code of 0 if no differences are found or a 1 if a difference is found or an error is encountered.

    usage: jolt diffy [-h] [-s] [-a] [-i] filePath1 [filePath2]

positional arguments:

    filePath1          File path to feed to Input #1 for the Diffy operation. This file should contain valid JSON.
    filePath2          File path to feed to Input #2 for the  Diffy  operation.  This file should contain valid JSON. This argument
                       is mutually exclusive with -i; one or the other should be specified.

optional arguments:

    -h, --help         show this help message and exit
    -s                 Diffy will suppress output and run silently. (default: false)
    -a                 Diffy will not consider array order when detecting differences (default: false)

### Example

To find the differences between files 'input1.json' and 'input2.json':

    jolt diffy input1.json input2.json

## Sort Sub Command

The sort sub command will ingest one JSON input (from a file or standard input) and perform the Jolt sort operation on it. The sort order is standard alphabetical ascending, with a special case for "~" prefixed keys to be bumped to the top. The program will return an exit code of 0 if the sort operation is performed successfully or a 1 if an error is encountered.

    usage: usage: jolt sort [-h] [-u] [input]

positional arguments:

    input              File path to the input JSON that the sort  operation  should be performed on. This file should contain valid
                       JSON. If this argument is not specified then standard input will be used.

optional arguments:

    -h, --help         show this help message and exit
    -u                 Turns off pretty print for the output. Output will be raw json with no formatting. (default: false)

### Example

To sort input specified in 'input.json':

``` sh
jolt sort input.json
```
