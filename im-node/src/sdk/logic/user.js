import LogicBase from '@/sdk/logic/base'
import IMUserPB from '@/sdk/protobuf/IMUser_pb'

class UserLogic extends LogicBase {
  auth (token) {
    let parameter = new IMUserPB.Auth()
    parameter.setToken(token)
    return this.im.client.send(this.directive('user.auth', parameter))
  }
}

export default UserLogic
