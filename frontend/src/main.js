import { createApp } from "vue";
import Keycloak from "keycloak-js";
import axios from "axios";
import VueAxios from "vue-axios";
import { createRouter, createWebHistory } from 'vue-router';
import { h, reactive } from "vue";

// Local routes and js
import App from "./App.vue";
import Home from "./routes/Home.vue"
import Templates from "./routes/Templates.vue"
import Consolidations from "./routes/Consolidations.vue"
import env from '@/env';

// CSS
import "./styles.css"
import '@fortawesome/fontawesome-free/css/all.css';

//==== Keycloack ======================
let initOptions = {
  url: env.get("AUTH_URL"),
  realm: env.get("REALM"),
  clientId: env.get("CLIENT_ID"),
  onLoad: "login-required"
};

let keycloak = new Keycloak(initOptions);
const props = reactive({ keycloak: keycloak, server_url: env.get("API_URL") })

const routes = [
    { path: '/', component:  Home, props: props },
    { path: '/templates', component:  Templates, props: props },
    { path: '/consolidations', component: Consolidations, props: props }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

keycloak
  .init({ onLoad: initOptions.onLoad })
  .then((auth) => {
    if (!auth) {
      window.location.reload();
    } else {
      console.log("Authenticated");

      const app = createApp({
        render: () => h(App, props)
      });

      app.use(VueAxios, axios);
      app.use(router);
      app.mount("#app");
    }

    //Token Refresh
    setInterval(() => {
      keycloak
        .updateToken(70)
        .then((refreshed) => {
          if (refreshed) {
            console.log("Token refreshed" + refreshed);
          } else {
            console.log(
              "Token not refreshed, valid for " +
              Math.round(
                keycloak.tokenParsed.exp +
                keycloak.timeSkew -
                new Date().getTime() / 1000
              ) +
              " seconds"
            );
          }
        })
        .catch(() => {
          console.log("Failed to refresh token");
        });
    }, 6000);
  })
  .catch(() => {
    console.log("Authenticated Failed");
  });


