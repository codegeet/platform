# CodeGeet
Here we build open-source online code execution system.

### Components

- [Sandbox](https://github.com/codegeet/sandbox/tree/main/api) API
- [Docker Images](https://github.com/codegeet/images) with [Code Runner](https://github.com/codegeet/sandbox/tree/main/coderunner) that is used by Sandbox  
  
### Overview

```mermaid
flowchart TD
A[CodeGeet Sandbox API] --> L{Language?}

L -->|Java| J(Docker Image)
L -->|Python| P(Docker Image)
L -->|...| O(Docker Image)

J --> RJ[Code Runner]
P --> RP[Code Runner]
O --> RO[Code Runner]
```

# Interface

`POST /execution`

**Body**

```json
{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello Jesus!\"); } }",
  "language_id": "java"
}
```

**Response**

```json
{
  "execution_id": " ... "
}
```

`GET /execution/{execution_id}`

**Response**
```json
{
  "execution_id": "0f98b086-a060-4a78-a80e-4beb59460225",
  "language_id": "JAVA",
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello Java!\"); } }",
  "std_out": "Hello Java!",
  "std_err": "",
  "error": "",
  "exit_code": 0
}
```

# Coderunner

See [coderunner](https://github.com/codegeet/codegeet/tree/main/coderunner)

# Special Thanks
Special thanks for the inspiration to open-source projects:
- [glot](https://github.com/glotcode)
- [Judge0](https://github.com/judge0)
