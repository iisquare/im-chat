import imPB from '@/sdk/protobuf/IM_pb'
import uuidv1 from 'uuid/v1'

class LogicBase {
  constructor (im) {
    this.im = im
  }
  sequence () {
    return uuidv1().replace(/-/g, '')
  }
  directive (command, parameter) {
    let directive = new imPB.Directive()
    directive.setSequence(this.sequence())
    directive.setCommand(command)
    directive.setParameter(parameter.serializeBinary())
    return directive.serializeBinary()
  }
}

export default LogicBase
