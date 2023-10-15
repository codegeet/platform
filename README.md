# Codegeet sandbox
Here we develop open-source online code execution system.

# Interface

`POST /execution`

**Body**

```json
{
  "code": "class Main {    public static void main(String[] args) {    System.out.print(\"Hello Jesus!\"); }   }",
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
"code": "class Main {    public static void main(String[] args) {    System.out.print(\"Hello Jesus!\"); }   }",
"std_out": "Hello Jesus!",
"std_err": "",
"error": "",
"exit_code": 0
}
```
