import axios from 'axios'

class HttpClient {
  constructor (baseURL) {
    this.$axios = axios.create({
      baseURL: baseURL,
      withCredentials: true
    })
  }
  wrap (request) {
    return new Promise((resolve, reject) => {
      request.then(response => {
        if (response && response.code === 0) {
          resolve(response)
        } else {
          reject(resolve(response))
        }
      }).catch(error => {
        resolve({code: -1, message: error.message, data: error})
      })
    })
  }
  request (config) {
    return this.wrap(new Promise((resolve, reject) => {
      this.$axios.request(config).then((response) => {
        resolve(response.data)
      }).catch((error) => {
        reject(error)
      })
    }))
  }
  get (url, config = {}) {
    return this.request(Object.assign(config, {method: 'get', url}))
  }
  post (url, data = null, config = {}) {
    return this.request(Object.assign(config, {method: 'post', url, data}))
  }
}

export default HttpClient
