#!/bin/bash

read_input() {
    INPUT=""
    while IFS= read -r LINE; do
        if [[ -z $LINE ]]; then
            break
        fi
        INPUT="${INPUT}${LINE}"$'\n'
    done
}

compile_code() {
    eval $COMPILE_CMD 2>$COMPILE_LOG_FILE || {
        COMPILE_ERROR_MSG=$(<$COMPILE_LOG_FILE)
        GLOBAL_EXEC_CODE=1
        return 1
    }
}

execute_code() {
    local args=$1
    local std_in=$2

    {
        STD_OUT=$(echo "$std_in" | eval $EXEC_CMD $args 2>$EXECUTE_LOG_FILE);
    } || {
        STD_ERR=$(<$EXECUTE_LOG_FILE)
        LOCAL_EXEC_CODE=1
        GLOBAL_EXEC_CODE=1
    }

    jq -n \
        --arg std_out "$STD_OUT" \
        --arg std_err "$STD_ERR" \
        --argjson exec_code $LOCAL_EXEC_CODE \
        '{
            "std_out": $std_out,
            "std_err": $std_err,
            "exec_code": $exec_code
        }'
}

read_input

CODE=$(jq -r '.code' <<< "$INPUT")
FILE_NAME=$(jq -r '.file_name' <<< "$INPUT")
COMPILE_CMD=$(jq -r '.instructions.compile // ""' <<< "$INPUT")
EXEC_CMD=$(jq -r '.instructions.exec' <<< "$INPUT")

CURRENT_DIR=$(pwd)
COMPILE_LOG_FILE="$CURRENT_DIR/compile.log"
EXECUTE_LOG_FILE="$CURRENT_DIR/execute.log"
GLOBAL_EXEC_CODE=0
COMPILE_ERROR_MSG=""

# Save the code to a file
echo "$CODE" > "$FILE_NAME"

# Compile the code
compile_code

# If compilation fails, output the error in JSON and exit
if [[ $GLOBAL_EXEC_CODE -ne 0 ]]; then
    jq -n \
        --arg error "$COMPILE_ERROR_MSG" \
        --argjson exec_code $GLOBAL_EXEC_CODE \
        '{
            "executions": [],
            "error": $error,
            "exec_code": $exec_code
        }'
    exit 1
fi

# Execute the code and gather results
EXECUTIONS=$(jq -r '.executions // [{"args": [], "std_in": ""}]' <<< "$INPUT")

EXECUTION_RESULTS=()
executions_length=$(jq 'length' <<< "$EXECUTIONS")
for (( i=0; i < "$executions_length"; i++ )); do
    args=$(jq -r ".executions[$i].args | @sh" <<< "$INPUT")
    std_in=$(jq -r ".[$i].std_in" <<< "$EXECUTIONS")
    LOCAL_EXEC_CODE=0

    result=$(execute_code "$args" "$std_in")

    EXECUTION_RESULTS+=("$result")
done

jq -n \
    --argjson results "$(jq -s . <<< "${EXECUTION_RESULTS[@]}")" \
    --arg error "" \
    --argjson exec_code $GLOBAL_EXEC_CODE \
    '{
        "executions": $results,
        "error": $error,
        "exec_code": $exec_code
    }'
