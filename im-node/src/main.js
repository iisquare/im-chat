// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import store from './store'
import App from './App'
import router from './router'
import DateUtil from './utils/date'

import BootstrapVue from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import 'font-awesome/css/font-awesome.min.css'

import IScrollView from 'vue-iscroll-view'
import IScroll from 'iscroll'

Vue.use(BootstrapVue, IScrollView, IScroll)

Vue.config.productionTip = false

store.dispatch('user/loadConfig')

Vue.filter('date', DateUtil.format)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
