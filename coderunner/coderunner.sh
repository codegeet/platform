#!/bin/bash

JSON_CONTENT=""
while IFS= read -r line; do
    # Exit the loop if the line is empty
    if [[ -z $line ]]; then
        break
    fi
    JSON_CONTENT="${JSON_CONTENT}${line}"$'\n'
done

CODE=$(jq -r '.code' <<< "$JSON_CONTENT")
ARGS=$(jq -r '.args[]' <<< "$JSON_CONTENT")
FILE_NAME=$(jq -r '.file_name' <<< "$JSON_CONTENT")
BUILD_CMD=$(jq -r '.instructions.build' <<< "$JSON_CONTENT")
EXEC_CMD=$(jq -r '.instructions.exec' <<< "$JSON_CONTENT")

CURRENT_DIR=$(pwd)

BUILD_LOG_FILE="$CURRENT_DIR/build_error.txt"
EXEC_LOG_FILE="$CURRENT_DIR/exec_error.txt"

# Save the code to a file
echo "$CODE" > "$FILE_NAME"

STD_OUT=""
STD_ERR=""
ERROR_MSG=""
EXEC_CODE=0

# Build the code
eval $BUILD_CMD 2>$BUILD_LOG_FILE || {
    ERROR_MSG=$(<$BUILD_LOG_FILE)
    EXEC_CODE=1
}

# Execute the code and capture the output and error
EXEC_TIME=0
if [ $EXEC_CODE -eq 0 ]; then
    START_TIME=$(date +%s%3N)

    { STD_OUT=$($EXEC_CMD "$ARGS" 2>$EXEC_LOG_FILE); } || {
        STD_ERR=$(<$EXEC_LOG_FILE)
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
