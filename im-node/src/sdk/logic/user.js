import LogicBase from '@/sdk/logic/base'
import UserPB from '@/sdk/protobuf/user_pb'

class UserLogic extends LogicBase {
  auth (token) {
    let parameter = new UserPB.Auth()
    parameter.setToken(token)
    this.im.client.send(this.directive('user.auth', parameter))
  }
}

export default UserLogic
