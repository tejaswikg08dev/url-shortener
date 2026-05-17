import apiClient from "./apiClient";

const analyticsService = {

    getStats: (shortKey, days = 30) =>
        apiClient.get(`/urls/${shortKey}/stats`, {params: {days}}),

    getClicks: (shortKey, days = 30) => 
        apiClient.get(`/urls/${shortKey}/stats/clicks`, {params: {days}}),

    getReferrers: (shortKey, limit = 10) => 
        apiClient.get(`/urls/${shortKey}/stats/referrers`, {params: {limit}}),

    getCountries: (shortKey, limit = 10) => 
        apiClient.get(`/urls/${shortKey}/stats/countries`, {params: {limit}}),

    getDevices: (shortKey) =>
        apiClient.get(`/urls/${shortKey}/stats/devices`),

    exportCsv: (shortKey) =>
        apiClient.get(`/urls/${shortKey}/stats/export`, {responseType: 'blob'}),
};

export default analyticsService;