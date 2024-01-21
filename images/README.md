# Images

## Overview
**CodeGeet** executes code within Docker containers, using a separate Docker image for each supported language. 
Typically, each docker image is based on a standard base image for the respective language 
and includes coderunner for compiling and executing code.

### Pull Docker image:

```bash
docker pull codegeet/python:latest
```

### Run Docker container:

#### Python
```bash
 echo '{
  "code": "print(\"Hello, CodeGeet!\")",
  "args": [],
  "file_name": "app.py",
  "instructions": {
    "compile": "",
    "exec": "python app.py"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/python:latest
```

#### Java
```bash
echo '{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, CodeGeet!\"); }}",
  "args": [],
  "file_name": "Main.java",
  "instructions": {
    "compile": "javac Main.java",
    "exec": "java Main"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/java:latest
```

#### Output
```json
{
  "std_out": "Hello, CodeGeet!",
  "std_err": "",
  "error": "",
  "exec_code": 0,
  "exec_time": 16
}
```
or
```json
{
  "std_out": "",
  "std_err": "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1\n\tat Main.main(Main.java:1)",
  "error": "",
  "exec_code": 1,
  "exec_time": 16
}
```