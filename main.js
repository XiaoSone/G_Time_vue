import App from './App'
import axios from 'axios'

// Axios 全局配置
axios.defaults.baseURL = 'http://127.0.0.1:8080' // 设置后端接口的基础地址
axios.defaults.headers.common['Content-Type'] = 'application/json' // 设置默认的请求头

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'

// 将 axios 挂载到 Vue 原型上，方便在组件中调用
Vue.prototype.$http = axios

Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'

export function createApp() {
  const app = createSSRApp(App)

  // 将 axios 挂载到全局属性中，方便在组件中调用
  app.config.globalProperties.$http = axios

  return {
    app
  }
}
// #endif
