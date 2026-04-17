import axios from 'axios';

const api = axios.create();

api.interceptors.request.use((config) => {
    config.headers['Content-Type'] = 'application/json';
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    if (config.url && config.url.endsWith('/')) {
        config.url = config.url.slice(0, -1);
    }
    return config;
});

export default api;
