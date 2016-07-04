import axios from 'axios';
import bluebird from 'bluebird';

axios.defaults.headers.common['Accept'] = "application/ld+json";
axios.defaults.headers.common['Content-Type'] = "application/ld+json";

export default {
    get(url) {
        return axios(url).then((res) => { return res.data; });
    }
};