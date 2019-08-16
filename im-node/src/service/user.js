import base from '@/core/ServiceBase'

export default {
  login (param) {
    if (!param) param = {}
    return base.post('/user/token', param)
  },
  logout () {
    return new Promise(() => {
      this.$store.commit('user/token', null)
    })
  }
}
