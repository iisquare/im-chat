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
}

export default LogicBase
