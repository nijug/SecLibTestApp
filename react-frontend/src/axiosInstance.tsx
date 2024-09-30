import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'https://localhost/api',
    withCredentials: true,
});

axiosInstance.interceptors.request.use(config => {
    const session = localStorage.getItem('session');
    if (session) {
        config.headers.Authorization = `Bearer ${session}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

axiosInstance.interceptors.response.use(response => {
    return response;
}, error => {
    if (error.response && error.response.status === 401) {
        console.error('Unauthorized error:', error);
    }
    return Promise.reject(error);
});

export default axiosInstance;
