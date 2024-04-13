package io.codegeet.platform.coderunner.exception

import java.util.concurrent.TimeUnit

class TimeLimitException(timeout: Long, timeunit: TimeUnit) :
    Exception("Process did not finish within $timeout ${timeunit.name.lowercase()}")