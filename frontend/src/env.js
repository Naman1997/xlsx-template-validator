// env.js
import envData from './env.json';

const env = {
    // Function to get the value of an environment variable
    get(key) {
        return envData[key];
    },
};

export default env;
