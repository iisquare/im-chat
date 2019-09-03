const LEVEL_TRACE = 1
const LEVEL_DEBUG = 2
const LEVEL_INFO = 4
const LEVEL_WARN = 8
const LEVEL_ERROR = 16

class Log {
  constructor (identify, level = 'trace') {
    this.identify = identify
    switch (level) {
      case 'error':
        this.level = LEVEL_ERROR
        break
      case 'warn':
        this.level = LEVEL_WARN | LEVEL_ERROR
        break
      case 'info':
        this.level = LEVEL_INFO | LEVEL_WARN | LEVEL_ERROR
        break
      case 'debug':
        this.level = LEVEL_DEBUG | LEVEL_INFO | LEVEL_WARN | LEVEL_ERROR
        break
      case 'trace':
        this.level = LEVEL_TRACE | LEVEL_DEBUG | LEVEL_INFO | LEVEL_WARN | LEVEL_ERROR
        break
      default:
        this.level = 0
    }
  }

  text (level) {
    switch (level) {
      case 1: return 'trace'
      case 2: return 'debug'
      case 4: return 'info'
      case 8: return 'warn'
      case 16: return 'error'
      default: return 'unknown'
    }
  }

  log (level, message, cause) {
    console.log(this.identify, this.text(level), message, cause)
  }

  isTraceEnabled () {
    return (this.level & LEVEL_TRACE) === LEVEL_TRACE
  }

  isDebugEnabled () {
    return (this.level & LEVEL_DEBUG) === LEVEL_DEBUG
  }

  isInfoEnabled () {
    return (this.level & LEVEL_INFO) === LEVEL_INFO
  }

  isWarnEnabled () {
    return (this.level & LEVEL_WARN) === LEVEL_WARN
  }

  isErrorEnabled () {
    return (this.level & LEVEL_ERROR) === LEVEL_ERROR
  }

  trace (message, cause) {
    if (!this.isTraceEnabled()) return
    this.log(LEVEL_TRACE, message, cause)
  }

  debug (message, cause) {
    if (!this.isDebugEnabled()) return
    this.log(LEVEL_DEBUG, message, cause)
  }

  info (message, cause) {
    if (!this.isInfoEnabled()) return
    this.log(LEVEL_INFO, message, cause)
  }

  warn (message, cause) {
    if (!this.isWarnEnabled()) return
    this.log(LEVEL_WARN, message, cause)
  }

  error (message, cause) {
    if (!this.isErrorEnabled()) return
    this.log(LEVEL_ERROR, message, cause)
  }
}
export default Log
