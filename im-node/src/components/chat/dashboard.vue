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
        <iscroll-view class="im-contacts-scroll" :options="{mouseWheel: true, scrollbars: true, fadeScrollbars: true}">
          <b-row align-v="center" class="im-contacts-item" @click="selectContact(index, item)" :key="index" v-for="(item, index) in contacts">
            <b-col>
              <b-media>
                <b-img slot="aside" width="35" height="35" rounded="circle" :src="'https://picsum.photos/125/125/?image=' + item.id.length"></b-img>
                <h6 class="mt-0 mb-0">{{item.id}}</h6>
                <p class="mb-0">xxxxxxxxxxxxxx</p>
              </b-media>
            </b-col>
          </b-row>
        </iscroll-view>
      </div>
    </div>
    <div class="im-chat">
      <div class="im-chat-container" v-if="talk">
        <div class="im-chat-title">
          <h5>{{talk.id}}</h5>
          <i class="fa fa-times im-btn-close" aria-hidden="true"></i>
        </div>
        <div class="im-chat-message">
          <div class="im-message-container">
            <iscroll-view class="im-message-scroll" :options="{mouseWheel: true, scrollbars: true, fadeScrollbars: true}">
              <b-row align-v="center" align="center" class="im-message-item">
                <b-col><b-badge variant="secondary">2019-01-03 13:25:22</b-badge></b-col>
              </b-row>
              <b-row align-v="center" :align="index % 2 == 0 ? 'left' : 'right'" class="im-message-item" :key="index" v-for="(item, index) in items">
                <b-col>
                  <b-media class="im-message-item-left" v-if="index % 2 == 0">
                    <b-img :id="'im-' + index" slot="aside" width="35" height="35" rounded="circle" :src="'https://picsum.photos/125/125/?image=' + item" :title="index"></b-img>
                    <div class="im-message-arrow"></div>
                    <div class="im-message-body">posuereposuereposueresuereosuereposuere</div>
                  </b-media>
                  <b-media class="im-message-item-right" right-align v-if="index % 2 != 0">
                    <b-img slot="aside" width="35" height="35" rounded="circle" src="https://avatars2.githubusercontent.com/u/5144531?s=35" :title="index"></b-img>
                    <div class="im-message-arrow"></div>
                    <div class="im-message-body">SedSedSedSedSedSedSedSedSedSedSSeSedSedSedSedSedSedSedSedSedSed</div>
                  </b-media>
                </b-col>
              </b-row>
            </iscroll-view>
          </div>
        </div>
        <div class="im-chat-input">
          <b-form-textarea placeholder="" no-resize></b-form-textarea>
        </div>
        <div class="im-chat-button">
          <b-button variant="secondary" size="sm">发送(S)</b-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ImClient from '@/sdk'
import wrapper from '@/core/RequestWrapper'
import userService from '@/service/user'
export default {
  data () {
    return {
      im: null,
      userId: '',
      keyword: '',
      keywording: false,
      recent: [],
      searchRows: [],
      talk: null,
      items: [58, 1, 21, 33, 4, 57, 61, 7, 8, 9]
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
      return this.keyword === '' ? this.recent : this.searchRows
    }
  },
  methods: {
    selectContact (index, item) {
      if (this.keyword !== '') {
        index = this.recent.findIndex((element, index, array) => {
          return element.id === item.id
        })
        if (index !== -1) {
          item = this.recent[index]
          this.recent.splice(index, 1)
        }
        this.recent.unshift(item)
        this.keyword = ''
      }
      this.talk = item
    },
    search () {
      this.keywording = false
      wrapper.tips.bind(this)(userService.search({id: this.keyword})).then(response => {
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
    this.userId = this.$store.state.user.data.userId || ''
    this.im = new ImClient(process.env.apiURL)
    this.im.connect(this.$store.state.user.data.token).then(result => {
      console.log('result', result)
    }).catch(error => {
      console.log(error)
      this.$bvToast.toast(error.getMessage(), {
        title: '认证失败',
        toaster: 'b-toaster-top-center',
        variant: 'danger',
        solid: true
      })
      window.setTimeout(() => { this.logout() }, 3000)
    })
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
