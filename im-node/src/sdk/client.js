import IMPB from '@/sdk/protobuf/IM_pb'

let promises = {}
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
  async onMessage (event) {
    let result = IMPB.Result.deserializeBinary(await this.readAsArrayBuffer(event.data))
    if (result === null) return
    this.config.onMessage && this.config.onMessage(result)
    let sequence = result.getSequence()
    let promise = promises[sequence]
    if (!promise) return
    if (result.getCode() === 0) {
      promise.resolve(result)
    } else {
      promise.reject(result)
    }
    delete promises[sequence]
  }
  onError (event) {
    this.config.onError && this.config.onError(event)
  }
  onSend (directive) {
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
