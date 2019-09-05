import base from '@/core/ServiceBase'
import { MESSAGE_RECEPTION_PERSON } from '@/sdk/constants'

export default {
  login (param) {
    if (!param) param = {}
    return base.post('/user/token', param)
  },
  logout () {
    return new Promise(() => {
      this.$store.commit('user/token', null)
    })
  },
  search (param) {
    if (!param) param = {}
    return base.post('/user/search', param).then(response => {
      response.data.rows = response.data.rows.map(item => {
        item.reception = MESSAGE_RECEPTION_PERSON
        item.receiver = item.id
        return item
      })
      return response
    })
  }
}
