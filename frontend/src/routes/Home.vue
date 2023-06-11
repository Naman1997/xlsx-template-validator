<template>
  <div id="app">
    <div class="image-container">
      <img src="../assets/Mountains.jpg" class="responsive-image" alt="Mountains">
    </div>
    <h1>{{ msg + keycloak.idTokenParsed.preferred_username + "!" }}</h1>
    <div>
      <button class="btn" @click="keycloak.logout()">Logout</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'app',
  props: ['keycloak'],
  data () {
    return {
      msg: 'Welcome '
    }
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

h1, h2 {
  font-weight: normal;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: block;
  margin: 0 10px;
  color: #333;
  font-size: 20px;
}

a {
  color: #42b983;
}

.btn {
  color: #fff;
  background-color: #0088ce;
  border-color: #00659c;
  padding: 6px 10px;
  font-size: 14px;
  line-height: 1.3333333;
  border-radius: 1px;
}

.image-container {
  max-width: 100%;
  height: auto;
  display: flex;
  justify-content: center;
}

.responsive-image {
  max-width: 100%;
  height: auto;
}

</style>