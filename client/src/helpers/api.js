import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': 'http://localhost:3001'
    },
})

api.interceptors.request.use(
    config => {
        const tokenId = localStorage.getItem('tokenId');
        if (tokenId) {
            config.headers['Authorization'] = `Bearer ${tokenId}`
        }
        return config
    },
    error => Promise.reject(error)
)

export default api;
