import axios from 'axios'

// @link:https://www.npmjs.com/package/axios
let $axios = axios.create({
  baseURL: process.env.apiURL,
  withCredentials: true
})

// axios.interceptors.response.use((response) => {
//   return Promise.resolve(response)
// }, (error) => {
//   return Promise.reject(error)
// })

export default {
  $axios,
  request (config) {
    return new Promise((resolve, reject) => {
      $axios.request(config).then((response) => {
        resolve(response.data)
      }).catch((error) => {
        reject(error)
      })
    })
  },
  get (url, config = {}) {
    return this.request(Object.assign(config, {method: 'get', url}))
  },
  post (url, data = null, config = {}) {
    return this.request(Object.assign(config, {method: 'post', url, data}))
  }
}
