import HttpClient from '@/sdk/http'
import WebSocketClient from '@/sdk/websocket'
import CometClient from '@/sdk/comet'
import UserLogic from '@/sdk/logic/user'
import MessageLogic from '@/sdk/logic/message'
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
        let config = Object.assign(this.config, {
          onOpen (event) {
            _this.userLogic.auth(_this.token).then(result => resolve(result)).catch(error => reject(error))
          }
        })
        this.client = window.WebSocket ? new WebSocketClient(Object.assign(config, {ws: route.ws})) : new CometClient(Object.assign(config, route.comet))
        this.client.connect()
      }).catch(error => reject(error))
    })
  }
  disconnect () {
    this.client.disconnect()
  }
}

export default ImClient
