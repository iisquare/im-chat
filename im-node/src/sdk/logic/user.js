import LogicBase from '@/sdk/logic/base'
import IMUserPB from '@/sdk/protobuf/IMUser_pb'

class UserLogic extends LogicBase {
  contact () {
    return this.send('user.contact').then(result => {
      return this.result(IMUserPB.Contact, result).toObject().rowsList.sort((a, b) => {
        return b.time - a.time
      })
    })
  }
  detach (reception, receiver) {
    let parameter = new IMUserPB.Detach()
    parameter.setReception(reception)
    parameter.setReceiver(receiver)
    return this.send('user.detach', parameter)
  }
}

export default UserLogic
