<template>
  <b-container class="full">
    <b-row align-v="center" class="full">
      <b-col align="center">
        <b-input-group style="width: 300px;">
          <b-input-group-prepend><span class="input-group-text"><i class="fa fa-user fa-lg"></i></span></b-input-group-prepend>
          <b-form-input v-model="form.userId" placeholder="your user identification"></b-form-input>
          <b-input-group-append><b-button @click="submit" variant="outline-secondary" :class="'fa ' + (this.loading ? 'fa fa-spinner' : 'fa-arrow-circle-right') + ' fa-lg'"></b-button></b-input-group-append>
        </b-input-group>
      </b-col>
    </b-row>
  </b-container>
</template>
<script>
import wrapper from '@/core/RequestWrapper'
import DataUtil from '@/utils/data'
import userService from '@/service/user'
export default {
  data () {
    return {
      loading: false,
      form: {
        userId: ''
      }
    }
  },
  methods: {
    submit (ev) {
      this.loading = true
      wrapper.tips.bind(this)(userService.login(this.form)).then(response => {
        if (response.code === 0) {
          this.$store.commit('user/data', response.data)
          let url = this.$router.currentRoute.query.redirect
          if (DataUtil.empty(url)) url = '/'
          this.$router.push(url)
        }
        this.loading = false
      })
    }
  },
  mounted () {
    this.form.userId = this.$store.state.user.data.userId || ''
  }
}
</script>
<style lang="scss">
.full {
  width: 100%;
  height: 100%;
}
</style>
