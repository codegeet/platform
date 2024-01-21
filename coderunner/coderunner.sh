#!/bin/bash

INPUT=""
while IFS= read -r LINE; do
    if [[ -z $LINE ]]; then
        break
    fi
    INPUT="${INPUT}${LINE}"$'\n'
done

CODE=$(jq -r '.code' <<< "$INPUT")
ARGS=$(jq -r '.args[]' <<< "$INPUT")
FILE_NAME=$(jq -r '.file_name' <<< "$INPUT")
COMPILE_CMD=$(jq -r '.instructions.compile' <<< "$INPUT")
EXEC_CMD=$(jq -r '.instructions.exec' <<< "$INPUT")

CURRENT_DIR=$(pwd)

COMPILE_LOG_FILE="$CURRENT_DIR/compile.txt"
EXECUTE_LOG_FILE="$CURRENT_DIR/execute.log"

# Save the code to a file
echo "$CODE" > "$FILE_NAME"

STD_OUT=""
STD_ERR=""
ERROR_MSG=""
EXEC_CODE=0

# Compile the code
eval $COMPILE_CMD 2>$COMPILE_LOG_FILE || {
    ERROR_MSG=$(<$COMPILE_LOG_FILE)
    EXEC_CODE=1
}

# Execute the code and capture the output and error
EXEC_TIME=0
if [ $EXEC_CODE -eq 0 ]; then
    START_TIME=$(date +%s%3N)

    { STD_OUT=$($EXEC_CMD "$ARGS" 2>$EXECUTE_LOG_FILE); } || {
        STD_ERR=$(<$EXECUTE_LOG_FILE)
        EXEC_CODE=1
    }

    END_TIME=$(date +%s%3N)
    EXEC_TIME=$((END_TIME - START_TIME))
fi

jq -n \
    --arg std_out "$STD_OUT" \
    --arg std_err "$STD_ERR" \
    --arg error "$ERROR_MSG" \
    --argjson exec_code $EXEC_CODE \
    --argjson exec_time $EXEC_TIME \
    '{
        "std_out": $std_out,
        "std_err": $std_err,
        "error": $error,
        "exec_code": $exec_code,
        "exec_time": $exec_time
    }'
