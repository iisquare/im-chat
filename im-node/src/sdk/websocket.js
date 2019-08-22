import Client from '@/sdk/client'

class WebSocketClient extends Client {
  constructor (config) {
    super(config)
    this.client = null
  }
  connect () {
    this.client = new WebSocket(this.config.ws)
    this.client.addEventListener('open', event => { this.onOpen(event) })
    this.client.addEventListener('close', event => { this.onClose(event) })
    this.client.addEventListener('message', event => { this.onMessage(event) })
    this.client.addEventListener('error', event => { this.onError(event) })
  }
  disconnect () {
    this.client.close()
    this.client = null
  }
  send (message) {
    this.client.send(message)
  }
}

export default WebSocketClient
