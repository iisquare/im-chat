import LogicBase from '@/sdk/logic/base'
import IMMessagePB from '@/sdk/protobuf/IMMessage_pb'

const MESSAGE_TYPE_TXT = 'txt'
const MESSAGE_RECEPTION_PERSON = 'person'
// const MESSAGE_RECEPTION_GROUP = 'group'
// const MESSAGE_RECEPTION_OFFICIAL = 'official'

class MessageLogic extends LogicBase {
  pushTxt (receiver, content) {
    return this.push({receiver, type: MESSAGE_TYPE_TXT, content})
  }
  push ({receiver, type, content}) {
    let parameter = new IMMessagePB.Push()
    parameter.setReception(MESSAGE_RECEPTION_PERSON)
    parameter.setReceiver(receiver)
    parameter.setType(type)
    parameter.setContent(content)
    return this.send('message.push', parameter).then(result => {
      return this.result(IMMessagePB.PushResult, result)
    })
  }
  sync () {
    return this.send('message.sync').then(result => {
      return this.result(IMMessagePB.Sync, result)
    })
  }
  pull (param) {
    let parameter = new IMMessagePB.Pull()
    parameter.setPage(param.page)
    parameter.setPageSize(param.pageSize)
    parameter.setSort(param.sort)
    parameter.setReception(param.reception)
    parameter.setReceiver(param.receiver)
    parameter.setMinVersion(param.minVersion)
    parameter.setMaxVersion(param.maxVersion)
    parameter.setMinTime(param.minTime)
    parameter.setMaxTime(param.maxTime)
    return this.send('message.pull', parameter).then(result => {
      return this.result(IMMessagePB.PullResult, result)
    })
  }
  history ({receiver, version}) {
    let param = {
      reception: MESSAGE_RECEPTION_PERSON,
      receiver,
      maxVersion: version ? version - 1 : version,
      sort: 'version.desc'
    }
    return this.pull(param).then(result => {
      result = result.toObject()
      return {
        receiver,
        more: result.total / result.pageSize > 1,
        rows: result.rowsList.sort((a, b) => { return a.version - b.version })
      }
    })
  }
}

export default MessageLogic
