// initial state
const state = {
  ready: false,
  data: null
}

// getters
const getters = {

}

// actions
const actions = {
  loadConfig ({ commit }) {
    let data = window.localStorage ? window.localStorage.getItem('userInfo') : null
    commit('ready')
    commit('data', data ? JSON.parse(data) : {})
  }
}

// mutations
const mutations = {
  ready (state, ready = true) {
    state.ready = ready
  },
  data (state, data) {
    state.data = data
    window.localStorage && window.localStorage.setItem('userInfo', JSON.stringify(data))
  },
  token (state, token) {
    state.data.token = token
    window.localStorage && window.localStorage.setItem('userInfo', JSON.stringify(state.data))
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
