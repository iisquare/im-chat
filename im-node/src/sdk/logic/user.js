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
}

export default UserLogic
