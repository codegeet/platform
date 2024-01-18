#!/bin/bash

# Initialize logs array
LOGS=()

log() {
    LOGS+=("$1")
}

# Read JSON from stdin
read -r -d '' JSON_CONTENT

log "Extracting data from JSON"

# Extract values from JSON using jq
CODE=$(jq -r '.code' <<< "$JSON_CONTENT")
ARGS=$(jq -r '.args[]' <<< "$JSON_CONTENT")
FILENAME=$(jq -r '.fileName' <<< "$JSON_CONTENT")
BUILD_CMD=$(jq -r '.instructions.build' <<< "$JSON_CONTENT")
EXEC_CMD=$(jq -r '.instructions.exec' <<< "$JSON_CONTENT")

# Get the current working directory
CURRENT_DIR=$(pwd)

BUILD_LOG_FILE="$CURRENT_DIR/build_error.txt"
EXEC_LOG_FILE="$CURRENT_DIR/exec_error.txt"

# Save the code to a file
echo "$CODE" > "$FILENAME"

log "Saved code to $CURRENT_DIR/$FILENAME"

# Initialize variables
STD_OUT=""
STD_ERR=""
ERROR_MSG=""
EXEC_CODE=0

# Build the code
log "Building the code with $BUILD_CMD"
eval $BUILD_CMD 2>$BUILD_LOG_FILE || {
    ERROR_MSG=$(<$BUILD_LOG_FILE)
    EXEC_CODE=1
    log "Build failed with error: $ERROR_MSG"
}

# Execute the command and capture the output and error
if [ $EXEC_CODE -eq 0 ]; then
    log "Executing code with $EXEC_CMD with arguments [$ARGS]"
    { STD_OUT=$($EXEC_CMD "$ARGS" 2>$EXEC_LOG_FILE); } || {
        STD_ERR=$(<$EXEC_LOG_FILE)
        EXEC_CODE=1
        log "Execution failed with error: $STD_ERR"
    }
else
    log "Skipping execution due to build failure"
fi

# Convert logs array to JSON array
LOGS_JSON=$(printf '%s\n' "${LOGS[@]}" | jq -R . | jq -s .)

# Format the output into JSON
jq -n \
    --arg std_out "$STD_OUT" \
    --arg std_err "$STD_ERR" \
    --arg error "$ERROR_MSG" \
    --argjson logs "$LOGS_JSON" \
    --argjson execCode $EXEC_CODE \
    '{
        "std_out": $std_out,
        "std_err": $std_err,
        "error": $error,
        "logs": $logs,
        "execCode": $execCode
    }'