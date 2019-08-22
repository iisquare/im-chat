import LogicBase from '@/sdk/logic/base'

class UserLogic extends LogicBase {
  auth (token) {
    this.im.client.send({action: 'auth', token})
  }
}

export default UserLogic
