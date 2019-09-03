import IMPB from '@/sdk/protobuf/IM_pb'
import IMMessagePB from '@/sdk/protobuf/IMMessage_pb'
import IMUserPB from '@/sdk/protobuf/IMUser_pb'
import { SEQUENCE_SYNC, SEQUENCE_AUTH } from '@/sdk/constants'

let promises = {}
let debug = true
class Client {
  constructor (config) {
    this.config = config
  }
  onChange (event) {
    debug && console.log('onChange', event)
    this.config.event && this.config.event(event)
  }
  onOpen (event) {
    debug && console.log('onOpen', event)
    this.config.onOpen && this.config.onOpen(event)
  }
  onClose (event) {
    debug && console.log('onClose', event)
    this.config.onClose && this.config.onClose(event)
  }
  onSync (result) {
    debug && console.log('onSync', event)
    let sync = IMMessagePB.Sync.deserializeBinary(result.getData())
    this.config.onSync && this.config.onSync(sync)
  }
  onAuth (result) {
    debug && console.log('onAuth', event)
    let auth = result.getCode() === 0 ? IMUserPB.AuthResult.deserializeBinary(result.getData()) : null
    this.config.onAuth && this.config.onAuth(result, auth)
  }
  async onMessage (event) {
    debug && console.log('onMessage', event)
    let result = IMPB.Result.deserializeBinary(await this.readAsArrayBuffer(event.data))
    if (result === null) return
    this.config.onMessage && this.config.onMessage(result)
    let sequence = result.getSequence()
    switch (sequence) {
      case SEQUENCE_SYNC:
        this.onSync(result)
        break
      case SEQUENCE_AUTH:
        this.onAuth(result)
        break
      default:
        let promise = promises[sequence]
        if (!promise) return
        if (result.getCode() === 0) {
          promise.resolve(result)
        } else {
          promise.reject(result)
        }
        delete promises[sequence]
    }
  }
  onError (event) {
    debug && console.log('onError', event)
    this.config.onError && this.config.onError(event)
  }
  onSend (directive) {
    debug && console.log('onSend', event)
    this.config.onSend && this.config.onSend(directive)
    return new Promise((resolve, reject) => {
      promises[directive.getSequence()] = {resolve, reject}
    })
  }
  readAsArrayBuffer (blob) {
    return new Promise((resolve, reject) => {
      var reader = new FileReader()
      reader.readAsArrayBuffer(blob)
      reader.onload = () => {
        resolve(reader.result)
      }
      reader.onerror = (e) => {
        reject(e)
      }
    })
  }
}

export default Client
