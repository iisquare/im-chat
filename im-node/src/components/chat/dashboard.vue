<template>
  <div class="im-dashboard">
    <div class="im-toolbar">
      <b-container class="text-center im-toolbar-container">
        <b-row align-v="center">
          <b-col>
            <b-dropdown id="dropdown-dropright" dropright variant="dark" no-caret>
              <template slot="button-content"><i class="fa fa-user fa-lg"></i></template>
              <b-dropdown-item-button disabled>{{userId}}</b-dropdown-item-button>
              <b-dropdown-item-button @click="logout">退出登录</b-dropdown-item-button>
            </b-dropdown>
          </b-col>
        </b-row>
      </b-container>
    </div>
    <div class="im-contacts">
      <b-container class="im-contacts im-contacts-search">
        <b-row align-v="center">
          <b-col>
            <b-input-group size="sm">
              <b-form-input v-model="keyword" placeholder="search contacts"></b-form-input>
              <b-input-group-append><span class="input-group-text"><i class="fa fa-search"></i></span></b-input-group-append>
            </b-input-group>
          </b-col>
        </b-row>
      </b-container>
      <div class="im-contacts-container">
        <iscroll-view ref="contact" class="im-contacts-scroll" :options="{mouseWheel: true, scrollbars: true, fadeScrollbars: true}">
          <b-row align-v="center" class="im-contacts-item" @click="selectContact(index, item)" :key="index" v-for="(item, index) in contacts">
            <b-col>
              <b-media>
                <b-img slot="aside" width="35" height="35" rounded="circle" :src="'https://picsum.photos/125/125/?image=' + item.receiver.length"></b-img>
                <h6 class="mt-0 mb-0">{{item.receiver}}</h6>
                <p class="mb-0">{{item.content}}</p>
              </b-media>
            </b-col>
          </b-row>
        </iscroll-view>
      </div>
    </div>
    <div class="im-chat">
      <div class="im-chat-container" v-if="talk">
        <div class="im-chat-title">
          <h5>{{talk.receiver}}</h5>
          <i @click="fin" class="fa fa-times im-btn-close" aria-hidden="true"></i>
        </div>
        <div class="im-chat-message">
          <div class="im-message-container">
            <iscroll-view ref="chat" class="im-message-scroll" :options="{mouseWheel: true, scrollbars: true, fadeScrollbars: true}">
              <template v-for="(item, index) in messages.rows">
                <b-row :key="'time-' + index" align-v="center" align="center" class="im-message-item" v-if="item.timeText">
                  <b-col><b-badge variant="secondary">{{item.timeText}}</b-badge></b-col>
                </b-row>
                <b-row :key="'withdraw-' + index" align-v="center" align="center" class="im-message-item" v-if="item.withdraw">
                  <b-col><b-badge variant="secondary">消息已撤回</b-badge></b-col>
                </b-row>
                <b-row align-v="center" :key="'message-' + index" align="right" class="im-message-item" v-else-if="item.sender == userId">
                  <b-col>
                    <b-media class="im-message-item-right" right-align>
                      <b-img slot="aside" width="35" height="35" rounded="circle" src="https://avatars2.githubusercontent.com/u/5144531?s=35"></b-img>
                      <div class="im-message-arrow"></div>
                      <div class="im-message-body">{{item.content}}</div>
                    </b-media>
                  </b-col>
                </b-row>
                <b-row align-v="center" :key="'message-' + index" align="left" class="im-message-item" v-else>
                  <b-col>
                    <b-media class="im-message-item-left">
                      <b-img slot="aside" width="35" height="35" rounded="circle" :src="'https://picsum.photos/125/125/?image=' + item.sender.length"></b-img>
                      <div class="im-message-arrow"></div>
                      <div class="im-message-body">{{item.content}}</div>
                    </b-media>
                  </b-col>
                </b-row>
              </template>
            </iscroll-view>
          </div>
        </div>
        <div class="im-chat-input">
          <b-form-textarea v-model="message" placeholder="" no-resize></b-form-textarea>
        </div>
        <div class="im-chat-button">
          <b-button @click="send" variant="secondary" size="sm">{{sendText}}</b-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ImClient from '@/sdk'
import wrapper from '@/core/RequestWrapper'
import userService from '@/service/user'
import DateUtil from '@/utils/date'
export default {
  data () {
    return {
      im: null,
      userId: '',
      keyword: '',
      keywording: false,
      contactRows: [],
      searchRows: [],
      talk: null,
      messages: { more: true, rows: [] },
      message: '',
      sendText: '发送'
    }
  },
  watch: {
    keyword (keyword) {
      if (!this.keywording) {
        this.keywording = true
        window.setTimeout(() => { this.search() }, 500)
      }
    }
  },
  computed: {
    contacts () {
      return this.keyword === '' ? this.contactRows : this.searchRows
    }
  },
  methods: {
    contact () {
      this.im.userLogic.contact().then(result => {
        this.contactRows = result
        if (result.length > 0 && this.talk && result[0].receiver === this.talk.receiver) {
          this.history(true)
        }
        this.$refs.contact.refresh()
      })
    },
    fin () {
      let reception = this.talk.reception
      let receiver = this.talk.receiver
      this.im.userLogic.fin(reception, receiver)
      let index = this.contactRows.findIndex((element, index, array) => {
        return element.reception === reception && element.receiver === receiver
      })
      index !== -1 && this.contactRows.splice(index, 1)
      this.talk = null
    },
    send () {
      if (this.message === '' || this.sendText === '发送中...') return
      this.sendText = '发送中...'
      this.im.messageLogic.pushTxt(this.talk.receiver, this.message).then(result => {
        this.sendText = '发送'
      }).catch(error => {
        this.sendText = '发送失败'
        this.$bvToast.toast(error.getMessage(), {
          title: '发送失败',
          toaster: 'b-toaster-top-center',
          variant: 'danger',
          solid: true
        })
      })
      this.message = ''
    },
    selectContact (index, item) {
      if (this.keyword !== '') {
        index = this.contactRows.findIndex((element, index, array) => {
          return element.receiver === item.receiver
        })
        if (index !== -1) {
          item = this.contactRows[index]
          this.contactRows.splice(index, 1)
        }
        this.contactRows.unshift(item)
        this.keyword = ''
      }
      if (this.talk && this.talk.receiver === item.receiver) return
      this.talk = item
      this.history(true)
    },
    history (renew) {
      renew && (this.messages = { more: true, rows: [] })
      if (!this.messages.more) return
      let length = this.messages.rows.length
      let version = length > 0 ? this.messages.rows[0].version : null
      this.im.messageLogic.history({receiver: this.talk.receiver, version}).then(result => {
        if (result.receiver !== this.talk.receiver) return
        this.messages.more = result.more
        let rows = []
        result.rows.concat(this.messages.rows).forEach(item => {
          let showTime = true
          if (rows.length > 0) {
            let row = rows[rows.length - 1]
            if (item.time - row.time < 900000) showTime = false
          }
          showTime && (item.timeText = DateUtil.format(item.time))
          rows.push(item)
        })
        this.messages.rows = rows
        this.$nextTick(() => {
          let chat = this.$refs.chat
          if (!chat) return
          chat.refresh()
          window.setTimeout(() => {
            chat.scrollTo(0, chat.iscroll.wrapperHeight - chat.iscroll.scrollerHeight, 300)
          }, 10)
        })
      })
    },
    search () {
      this.keywording = false
      wrapper.tips.bind(this)(userService.search({id: this.keyword, except: this.userId})).then(response => {
        if (response.code === 0) {
          this.searchRows = response.data.rows
        }
      })
    },
    logout () {
      this.$store.commit('user/token', null)
      this.$router.go(0)
    }
  },
  mounted () {
    let _this = this
    this.userId = this.$store.state.user.data.userId || ''
    this.im = new ImClient({
      uri: process.env.apiURL,
      onSync (sync) {
        _this.contact()
      },
      onAuth (result, auth) {
        if (auth) {
          _this.contact()
        } else {
          _this.$bvToast.toast(result.getMessage(), {
            title: '认证失败',
            toaster: 'b-toaster-top-center',
            variant: 'danger',
            solid: true
          })
          window.setTimeout(() => { _this.logout() }, 3000)
        }
      }
    })
    this.im.connect(this.$store.state.user.data.token)
  }
}
</script>

<style lang="scss" scoped>
.im-dashboard {
  width: 100%;
  height: 100%;
}
.im-toolbar {
  width: 60px;
  height: 100%;
  float: left;
}
.im-contacts {
  width: 250px;
  height: 100%;
  float: left;
}
.im-chat {
  width: 100%;
  height: 100%;
  float: right;
  margin-left: -310px;
  padding-left: 310px;
}
.im-toolbar-container {
  background-color: #28292c;
  margin: 0px;
  padding: 15px 0px 15px 0px;
  width: 100%;
  height: 100%;
}
.im-contacts-search {
  height: 60px;
  background-color: #eeeae8;
  border-bottom: #e5e1df solid 1px;
  .row {
    height: 100%;
  }
}
.im-contacts-container {
  height: 100%;
  padding-top: 60px;
  background-color: #eeeae8;
}
.im-contacts-scroll {
  position: relative;
  overflow: hidden;
  height: 100%;
}
.im-contacts-item {
  height: 65px;
  margin: 0px;
  cursor: pointer;
  .media-body, p {
    width: 169px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
.im-contacts-item:hover {
  background-color: #dddede;
}
.im-chat-container {
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
}
.im-chat-title {
  height: 60px;
  padding: 0px 15px 0px 15px;
  border-bottom: #e7e7e7 solid 1px;
  h5 {
    float: left;
    width: 50%;
    margin: 0px;
    line-height: 60px;
    vertical-align: middle;
  }
  .im-btn-close {
    float: right;
    margin-top: 20px;
    cursor: pointer;
  }
}
.im-chat-message {
  width: 100%;
  height: 100%;
  margin: -60px 0px -100px 0px;
  padding: 60px 0px 100px 0px;
}
.im-chat-input {
  width: 100%;
  height: 55px;
  textarea {
    width: 100%;
    height: 100%;
    border: none;
    border-radius: 0px;
    outline: none !important;
  }
}
.im-chat-button {
  width: 100%;
  height: 45px;
  line-height: 45px;
  vertical-align: middle;
  text-align: right;
  padding: 0px 15px 0px 15px;
}
.im-message-container {
  width: 100%;
  height: 100%;
  border-bottom: #e7e7e7 solid 1px;
}
.im-message-scroll {
  position: relative;
  overflow: hidden;
  height: 100%;
}
.im-message-item {
  margin: 0px;
}
.im-message-arrow {
  width: 16px;
  position: relative;
}
.im-message-arrow:before {
  position: absolute;
  display: block;
  content: "";
  border-width: .5rem;
  border-style: solid;
  border-color: transparent;
}
.im-message-arrow:after {
  position: absolute;
  display: block;
  content: "";
  border-width: .5rem;
  border-style: solid;
  border-color: transparent;
}
.im-message-body {
  padding: 0.5rem 0.75rem;
  color: #212529;
  background-clip: padding-box;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 0.3rem;
  width: fit-content;
  word-break: break-all;
  white-space: normal;
  text-align: left;
}
.im-message-item-left {
  padding: 10px 51px 10px 0px;
  .im-message-arrow {
    margin-left: -8px;
    top: 10px;
  }
  .im-message-arrow:before {
    left: 0px;
    border-left-width: 0;
    border-right-color: rgba(0, 0, 0, 0.2);
  }
  .im-message-arrow:after {
    left: 1px;
    border-left-width: 0;
    border-right-color: #fff;
  }
  .im-message-body {
    background-color: #fff;
  }
}
.im-message-item-right {
  padding: 10px 0px 10px 51px;
  .im-message-arrow {
    margin-right: -8px;
    top: 10px;
  }
  .im-message-arrow:before {
    right: 0px;
    border-right-width: 0;
    border-left-color: rgba(0, 0, 0, 0.2);
  }
  .im-message-arrow:after {
    right: 1px;
    border-right-width: 0;
    border-left-color: #9eea6a;
  }
  .im-message-body {
    background-color: #9eea6a;
  }
}
</style>
