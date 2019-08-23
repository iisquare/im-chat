let key = 'xxx'
let deley = {}
let promise = new Promise((resolve, reject) => {
  deley[key] = {resolve, reject}
})
promise.then(result => {
  console.log(result)
})
setTimeout(() => {
  deley[key].resolve(key)
}, 500)
