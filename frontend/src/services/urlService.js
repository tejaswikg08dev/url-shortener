import apiClient from "./apiClient";

const urlService = {
    create: (data) => apiClient.post('/urls', data),

    list: (page = 0,size = 20, search = '') =>
        apiClient.get('/urls', {params: {page, size, search}}),

    get: (shortKey) => apiClient.get(`/urls/${shortKey}`),

    update: (shortKey, data) => apiClient.put(`/urls/${shortKey}`, data),

    delete: (shortKey) => apiClient.delete(`/urls/${shortKey}`),

    bulkCreate: (urls) => apiClient.post('/urls/bulk', {urls}),

    getQrCode: (shortKey, size = 250) =>
        apiClient.get(`/urls/${shortKey}/qr`, {params: {size}, responseType: 'blob'}),

};

export default urlService;