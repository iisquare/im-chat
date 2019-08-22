import imPB from '@/sdk/protobuf/im_pb'

class LogicBase {
  constructor (im) {
    this.im = im
  }
  directive (command, parameter) {
    let directive = new imPB.Directive()
    directive.setCommand(command)
    directive.setParameter(parameter.serializeBinary())
    return directive.serializeBinary()
  }
}

export default LogicBase
