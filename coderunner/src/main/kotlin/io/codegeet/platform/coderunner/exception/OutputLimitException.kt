package io.codegeet.platform.coderunner.exception

class OutputLimitException(bytes: Int) :
    Exception("Process output exceeds limit of $bytes bytes")