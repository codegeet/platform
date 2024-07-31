## Coderunner Job

**Coderunner** job has http endpoint to test docker configuration.

```bash
curl -X POST http://localhost:8080/api/executions \
     -H "Content-Type: application/json" \
     -d '{
  "code": "class Main { public static void main(String[] args) { System.out.print(args[0]); }}",
  "language": "java",
  "invocations": [
    {
      "args": ["one"]
    },
    {
      "args": ["another"]
    }
  ]
}'
````
