import HttpClient from '@/sdk/http'
import WebSocketClient from '@/sdk/websocket'
import CometClient from '@/sdk/comet'
import UserLogic from '@/sdk/logic/user'
import MessageLogic from '@/sdk/logic/message'
import * as constants from '@/sdk/constants'

class ImClient {
  constructor (config) {
    this.config = config
    this.http = new HttpClient(config.uri)
    this.userLogic = new UserLogic(this)
    this.messageLogic = new MessageLogic(this)
  }
  connect (token) {
    let _this = this
    this.token = token
    return new Promise((resolve, reject) => {
      this.http.get('/gate/route').then(response => {
        let node = response.data.nodes.pop()
        let route = response.data.routes[node]
        let ws = route.ws + '?token=' + _this.token
        this.client = window.WebSocket ? new WebSocketClient(Object.assign(this.config, {ws})) : new CometClient(Object.assign(this.config, route.comet))
        this.client.connect()
        resolve(response)
      }).catch(error => reject(error))
    })
  }
  disconnect () {
    this.client.disconnect()
  }
}

Object.assign(ImClient, constants)

export default ImClient
