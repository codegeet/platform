# image

## Overview
Docker image to safely run code.

#### Coderunner
The [coderunner](https://github.com/codegeet/codegeet/tree/main/coderunner) installed inside a Docker container compiles and runs the code.

## Example

Pull Docker image:

```bash
docker pull codegeet/python:latest
```

Run code inside a Docker container:

python
```bash
echo '{       
  "language": "python",
  "code": "print(\"Hello World!!!\")"
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/python:latest
```

java
```bash
echo '{                   
  "language": "java",               
  "code": "class Main {    public static void main(String[] args) {    System.out.print(\"Hello World!!!\"); }   }"
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/java:latest
```

##### Result
```json
{"std_out":"Hello World!!!","std_err":"","error":""}
```
