<template>
  <div class="im-dashboard">
    <div class="im-chat">
      <b-container>
        <b-row align-v="center" class="im-chat-title">
          <b-col><span>XXXXXXXXXXXXXXX</span></b-col>
        </b-row>
      </b-container>
    </div>
    <b-container class="im-toolbar text-center">
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
    <b-container class="im-contacts">
      <b-row align-v="center" class="im-contacts-search">
        <b-col>
          <b-input-group size="sm">
            <b-input-group-prepend><span class="input-group-text"><i class="fa fa-search"></i></span></b-input-group-prepend>
            <b-form-input placeholder="search cantacts"></b-form-input>
            <b-input-group-append><b-button variant="outline-secondary" class="fa fa-plus"></b-button></b-input-group-append>
          </b-input-group>
        </b-col>
      </b-row>
      <b-row align-v="center" class="im-contacts-scroll">
        <b-col>
          <iscroll-view>
            <b-row align-v="center" class="im-contacts-item" :key="index" v-for="(item, index) in items">
              <b-col>
                <b-media>
                  <b-img slot="aside" width="35" height="35" rounded="circle" src="https://picsum.photos/125/125/?image=58" :title="index"></b-img>
                  <h6 class="mt-0 mb-0">Nested Media</h6>
                  <p class="mb-0">xxxxxxxxxxxxxx</p>
                </b-media>
              </b-col>
            </b-row>
          </iscroll-view>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
export default {
  data () {
    return {
      userId: '',
      items: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
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
  background-color: #28292c;
  padding: 15px 0px 15px 0px;
}
.im-contacts {
  width: 250px;
  height: 100%;
  float: left;
  background-color: #eeeae8;
}
.im-contacts-search {
  height: 60px;
  border-bottom: #e5e1df solid 1px;
}
.im-contacts-scroll {
  height: 100%;
  overflow: hidden;
  margin-top: -65px;
  padding-top: 65px;
}
.im-contacts-item {
  height: 65px;
  cursor: pointer;
}
.im-contacts-item:hover {
  background-color: #dddede;
}
.im-chat {
  width: 100%;
  height: 100%;
  float: right;
  margin-left: -310px;
  padding-left: 310px;
  background-color: #f5f5f5;
}
.im-chat-title {
  height: 60px;
  border-bottom: #e7e7e7 solid 1px;
}
</style>
