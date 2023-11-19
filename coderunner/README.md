# coderunner

## Overview
**coderunner** is a command-line tool that reads code in JSON format from the standard input (stdin),
compiles and executes the code, and then writes the result in JSON format to the standard output (stdout).
At present, **coderunner** shares many similarities with [glot](https://github.com/glotcode/code-runner).

## Supported languages
- JAVA
- PYTHON
- ... more to come ;)

## Input (stdin)
```json
{
  "language_id": "python",
  "code": "print(\"Hello World!!!\")"
}
```

## Output (stdout)
```json
{
    "std_out":"Hello World!!!",
    "std_err":"",
    "error":""
}
```

## Test locally

Download `coderunner.jar` from [releases](https://github.com/codegeet/codegeet/releases).

Run:
```bash
echo '{
  "language_id": "python",
  "code": "print(\"Hello World!!!\")"
}' | java -jar coderunner.jar
```
