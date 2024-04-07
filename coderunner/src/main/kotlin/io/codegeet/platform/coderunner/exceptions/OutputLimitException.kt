package io.codegeet.platform.coderunner.exceptions

class OutputLimitException(bytes: Int) :
    Exception("Process output exceeds limit of $bytes bytes")