import axios from 'axios';

export default {
    get(url, plainJson = false) {
        return axios(url, {
            headers: {
                'Accept': plainJson ? "application/json" : "application/ld+json"
            }
        }).then((res) => { return res.data; });
    },
    post(url, body) {
        return axios.post(url, body, {
            headers: {
                'Content-Type': "application/ld+json"
            }
        });
    }
};