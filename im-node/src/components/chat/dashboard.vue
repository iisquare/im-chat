<template>
  <div class="im-dashboard">
    <div class="im-chat">
      xxxxx
    </div>
    <b-container class="im-toolbar text-center">
      <b-row>
        <b-col>
          <b-dropdown id="dropdown-dropright" dropright variant="dark" no-caret>
            <template slot="button-content"><i class="fa fa-user fa-lg"></i></template>
            <b-dropdown-item-button disabled>{{userId}}</b-dropdown-item-button>
            <b-dropdown-item-button @click="logout">退出登录</b-dropdown-item-button>
          </b-dropdown>
        </b-col>
      </b-row>
    </b-container>
    <div class="im-contacts">asfa</div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      userId: ''
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
.im-chat {
  width: 100%;
  height: 100%;
  float: right;
  margin-left: -310px;
  padding-left: 310px;
  background-color: #f5f5f5;
}
</style>
