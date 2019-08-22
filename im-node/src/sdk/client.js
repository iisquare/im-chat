class Client {
  constructor (config) {
    this.config = config
  }
  onOpen (event) {
    this.config.onOpen && this.config.onOpen(event)
  }
  onClose (event) {
    this.config.onClose && this.config.onClose(event)
  }
  onMessage (event) {
    console.log(event)
    this.config.onMessage && this.config.onMessage(event)
  }
  onError (event) {
    this.config.onError && this.config.onError(event)
  }
}

export default Client
