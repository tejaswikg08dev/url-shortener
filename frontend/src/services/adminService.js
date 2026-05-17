import apiClient from "./apiClient";

const adminService = {

    getAllUrls: (page = 0, size = 20) =>
        apiClient.get('/admin/urls', {params: {page, size}}),

    deleteUrl: (shortKey) => apiClient.delete(`/admin/urls/${shortKey}`),

    getAllUsers: (page = 0, size = 20 ) =>
        apiClient.get('/admin/users', {params: {page, size}}),

    updateUserRole: (userId, role) => 
        apiClient.put(`/admin/users/${userId}/role`, {role}),
};

export default adminService;