<script>
export default {
  name: "App",
  props: ["keycloak"],
  methods: {
    w3_close() {
      document.getElementById("mySidebar").style.display = "none";
      document.getElementById("myOverlay").style.display = "none";
    },
    w3_open() {
      document.getElementById("mySidebar").style.display = "block";
      document.getElementById("myOverlay").style.display = "block";
    },
  },
};

</script>

<template>
  <html>
  <head>
    <title>Xlsx Template Validator</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
  </head>
  <body class="w3-content" style="max-width:1200px">

  <!-- Sidebar/menu -->
  <nav class="w3-sidebar w3-bar-block w3-black w3-collapse w3-top" style="z-index:3;width:250px" id="mySidebar">
    <div class="w3-container w3-display-container w3-padding-16">
      <i @click="w3_close" class="fa fa-remove w3-hide-large w3-button w3-display-topright"></i>
      <h3 class="w3-wide"><b>Xlsx Template Validator</b></h3>
    </div>
    <div class="w3-padding-64 w3-large w3-text-grey" style="font-weight:bold">
      <router-link to="/" class="w3-bar-item w3-button">Home</router-link>
      <router-link to="/consolidations" class="w3-bar-item w3-button">Consolidated Files</router-link>
      <router-link to="/templates" class="w3-bar-item w3-button">Template Files</router-link>
    </div>
    <div class="w3-bar-item w3-button w3-large w3-text-grey" style="font-weight:bold" @click="keycloak.logout()">Logout</div>
  </nav>

  <!-- Top menu on small screens -->
  <header class="w3-bar w3-top w3-hide-large w3-black w3-xlarge">
    <div class="w3-bar-item w3-padding-24 w3-wide">Xlsx Template Validator</div>
    <a href="javascript:void(0)" class="w3-bar-item w3-button w3-padding-24 w3-right" @click="w3_open"><i class="fa fa-bars"></i></a>
  </header>

  <!-- Overlay effect when opening sidebar on small screens -->
  <div class="w3-overlay w3-hide-large" @click="w3_close" style="cursor:pointer" title="close side menu" id="myOverlay"></div>

  <!-- !PAGE CONTENT! -->
  <div class="w3-main" style="margin-left:250px">

    <!-- Push down content on small screens -->
    <div class="w3-hide-large" style="margin-top:83px"></div>
    <router-view></router-view>

    <!-- End page content -->
  </div>

  </body>
  </html>
</template>
