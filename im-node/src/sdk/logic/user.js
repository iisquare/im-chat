import LogicBase from '@/sdk/logic/base'
import IMUserPB from '@/sdk/protobuf/IMUser_pb'

class UserLogic extends LogicBase {
  auth (token) {
    let parameter = new IMUserPB.Auth()
    parameter.setToken(token)
    return this.send('user.auth', parameter).then(result => {
      return this.result(IMUserPB.AuthResult, result)
    })
  }
  contact () {
    return this.send('user.contact').then(result => {
      return this.result(IMUserPB.Contact, result).toObject().rowsList.sort((a, b) => {
        return b.time - a.time
      })
    })
  }
  fin (reception, receiver) {
    let parameter = new IMUserPB.Fin()
    parameter.setReception(reception)
    parameter.setReceiver(receiver)
    return this.send('user.fin', parameter)
  }
}

export default UserLogic
