<script>
import axios from 'axios';

export default {
  props: ["keycloak", "server_url"],
  data() {
    return {
      files: [],
      showPopup: false,
    };
  },
  computed: {
    isSmallDevice() {
      return window.innerWidth <= 768;
    }
  },
  mounted() {
    this.fetchFiles();
  },
  methods: {
    fetchFiles() {
      axios.get(this.server_url + '/template/list', {
        headers: {
          Authorization: "Bearer " + this.keycloak.token,
        },
      }).then(response => {
        this.files = response.data.sort();
      }).catch(error => {
        this.handleError(error);
      });
    },
    handleFileUpload() {
      const files = this.$refs.fileInput.files;
      const formData = new FormData();

      // Append each selected file to the FormData
      Array.from(files).forEach((file) => {
        formData.append('file', file);
      });

      // Make the API call to upload the files
      axios.post(`${this.server_url}/template/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: 'Bearer ' + this.keycloak.token,
        },
      }).then((response) => {
        // Handle success
        console.log('Files uploaded successfully:', response.data);
        this.fetchFiles(); // Refresh the file list after successful upload
      }).catch((error) => {
        this.handleError(error);
      }).finally(() => {
        this.closePopup(); // Close the popup after the upload operation completes
      });
    },
    downloadFile(filename) {
      axios.get(this.server_url + `/template/download/${filename}`, {
        headers: {
          Authorization: "Bearer " + this.keycloak.token,
        },
        responseType: 'blob'
      })
          .then(response => {
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
          })
          .catch(error => {
            this.handleError(error);
          });
    },
    deleteFile(filename) {
      axios.delete(this.server_url + `/template/delete/${filename}`, {
        headers: {
          Authorization: "Bearer " + this.keycloak.token,
        }
      }).then(response => {
        // Handle success
        console.log('Files uploaded successfully:', response.data);
        this.fetchFiles(); // Refresh the file list after successful delete
      }).catch(error => {
        this.handleError(error);
      });
    },
    handleError(error) {
      if (error.response) {
        alert(error.response.data);
      } else {
        alert(error.message);
      }
      console.log(error);
    },
    openPopup() {
      this.showPopup = true;
    },
    closePopup() {
      this.showPopup = false;
    }
  }
};
</script>

<template>
  <div class="new-consolidation-button">
    <button @click="openPopup" class="btn-new-consolidation">
      + New Template
    </button>
  </div>
  <table class="file-table">
    <thead>
    <tr>
      <th>Filename</th>
      <th class="options-header">Options</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="file in files" :key="file">
      <td>{{ file }}</td>
      <td class="options-cell">
        <button @click="downloadFile(file)" class="btn-download">
          <span class="option-text">Download</span>
        </button>
        <button @click="deleteFile(file)" class="btn-delete">
          <span class="option-text">Delete</span>
        </button>
      </td>
    </tr>
    </tbody>
  </table>

  <!-- Popup -->
  <div v-if="showPopup" class="popup-overlay">
    <div class="popup-container">
      <div class="file-input-container">
        <h2 class="w3-text-black">Upload Template(s)</h2>
        <input ref="fileInput" type="file" multiple>
      </div>
      <button @click="handleFileUpload" class="btn-upload">Upload</button>
      <button @click="closePopup" class="btn-cancel">Cancel</button>
    </div>
  </div>
</template>

<style scoped>

.new-consolidation-button {
  text-align: right;
  margin-bottom: 10px;
  padding-right: 10px;
}

.btn-new-consolidation {
  background-color: #2196f3;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
}

.file-table {
  width: 100%;
  border-collapse: collapse;
}

.file-table th,
.file-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.file-table th {
  background-color: #f5f5f5;
}

.options-header {
  text-align: right;
}

.options-cell {
  text-align: right;
}

.options-cell button {
  margin-right: 5px;
}

.btn-download {
  background-color: #2196f3;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
}

.btn-delete {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 3px;
  cursor: pointer;
}

.btn-download:hover,
.btn-delete:hover {
  opacity: 0.8;
}

.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.popup-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  border: 1px solid #ccc;
  max-width: 400px;
  width: 90%;
}

.popup-container h2 {
  margin-top: 0;
  color: #333;
  font-size: 18px;
  margin-bottom: 10px;
}

.popup-container select {
  margin-bottom: 10px;
  width: 100%;
}

.popup-container button {
  margin-left: 5px;
  padding: 6px 12px;
  border-radius: 3px;
  font-size: 14px;
  font-weight: bold;
  text-transform: uppercase;
  cursor: pointer;
}

.popup-container button.btn-upload {
  background-color: #4caf50;
  color: #fff;
}

.popup-container button.btn-cancel {
  background-color: #f44336;
  color: #fff;
}

.popup-container button:hover {
  opacity: 0.8;
}

.file-input-container {
  padding-bottom: 20px;
}
</style>

