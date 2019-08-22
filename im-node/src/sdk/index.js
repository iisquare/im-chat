import HttpClient from '@/sdk/http'
import WebSocketClient from '@/sdk/websocket'
import CometClient from '@/sdk/comet'
import UserLogic from '@/sdk/logic/user'

class ImClient {
  constructor (uri) {
    this.http = new HttpClient(uri)
    this.userLogic = new UserLogic(this)
  }
  connect (token) {
    let _this = this
    this.token = token
    this.http.get('/gate/route').then(response => {
      let node = response.data.nodes.pop()
      let route = response.data.routes[node]
      let config = {
        onOpen (event) {
          _this.userLogic.auth(_this.token)
        }
      }
      this.client = window.WebSocket ? new WebSocketClient(Object.assign(config, {ws: route.ws})) : new CometClient(Object.assign(config, route.comet))
      this.client.connect()
    })
  }
  disconnect () {
    this.client.disconnect()
  }
}

export default ImClient
