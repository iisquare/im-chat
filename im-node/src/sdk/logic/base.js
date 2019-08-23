import IMPB from '@/sdk/protobuf/IM_pb'
import uuidv1 from 'uuid/v1'
// import {Any} from 'google-protobuf/google/protobuf/any_pb.js'
class LogicBase {
  constructor (im) {
    this.im = im
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
  send (command, parameter) {
    return this.im.client.send(this.directive(command, parameter))
  }
  result (T, result) {
    return T.deserializeBinary(result.getData())
  }
}

export default LogicBase
