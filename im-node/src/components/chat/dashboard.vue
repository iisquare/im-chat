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
              <b-input-group-prepend><span class="input-group-text"><i class="fa fa-search"></i></span></b-input-group-prepend>
              <b-form-input placeholder="search contacts"></b-form-input>
              <b-input-group-append><b-button variant="outline-secondary" class="fa fa-plus"></b-button></b-input-group-append>
            </b-input-group>
          </b-col>
        </b-row>
      </b-container>
      <div class="im-contacts-container">
        <iscroll-view class="im-contacts-scroll" :options="{mouseWheel: true, scrollbars: true, fadeScrollbars: true}">
          <b-row align-v="center" class="im-contacts-item" :key="index" v-for="(item, index) in items">
            <b-col>
              <b-media>
                <b-img slot="aside" width="35" height="35" rounded="circle" :src="'https://picsum.photos/125/125/?image=' + item" :title="index"></b-img>
                <h6 class="mt-0 mb-0">Nested Media</h6>
                <p class="mb-0">xxxxxxxxxxxxxx</p>
              </b-media>
            </b-col>
          </b-row>
        </iscroll-view>
      </div>
    </div>
    <div class="im-chat">
      <div class="im-chat-container">
        <div class="im-chat-title">
          <h5>XXXXXXXXXXXXXXX</h5>
        </div>
        <div class="im-chat-message">aaa</div>
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
export default {
  data () {
    return {
      userId: '',
      items: [58, 1, 21, 33, 4, 57, 61, 7, 8, 9]
    }
  },
  methods: {
    logout () {
      this.$store.commit('user/token', null)
      this.$router.push('/user/login')
    }
  },
  mounted () {
    this.userId = this.$store.state.user.data.userId || ''
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
    margin: 0px;
    line-height: 60px;
    vertical-align: middle;
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
</style>
