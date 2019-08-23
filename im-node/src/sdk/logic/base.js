import IMPB from '@/sdk/protobuf/IM_pb'
import uuidv1 from 'uuid/v1'

class LogicBase {
  constructor (im) {
    this.im = im
  }
  static readAsArrayBuffer (blob) {
    return new Promise((resolve, reject) => {
      var reader = new FileReader()
      reader.readAsArrayBuffer(event.data)
      reader.onload = () => {
        resolve(reader.result)
      }
      reader.onerror = (e) => {
        reject(e)
      }
    })
  }
  sequence () {
    return uuidv1().replace(/-/g, '')
  }
  directive (command, parameter) {
    let directive = new IMPB.Directive()
    directive.setSequence(this.sequence())
    directive.setCommand(command)
    directive.setParameter(parameter.serializeBinary())
    return directive
  }
}

export default LogicBase
