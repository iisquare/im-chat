export default {
  tips (request, showSuccess = false, showWarning = true, showError = true) {
    return new Promise((resolve, reject) => {
      request.then(response => {
        if (response && response.code === 403 && response.message === 'required login') {
          window.open(window.location.href)
        }
        if (response && response.code === 0) {
          showSuccess && this.$bvToast.toast('消息:' + response.message, {
            title: '状态：' + response.code,
            variant: 'success',
            solid: true
          })
        } else if (showWarning) {
          this.$bvToast.toast('消息:' + response.message, {
            title: '状态：' + response.code,
            variant: 'warning',
            solid: true
          })
        }
        resolve(response)
      }).catch(error => {
        if (showError) {
          this.$bvToast.toast(error.message, {
            title: '请求异常',
            variant: 'danger',
            solid: true
          })
        }
        resolve({code: 500, message: error.message, data: error})
      })
    })
  }
}
