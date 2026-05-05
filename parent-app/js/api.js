const API_BASE = 'http://localhost:8080/api/v1';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('accessToken');
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('accessToken', token);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    }

    async request(path, options = {}) {
        const headers = { 'Content-Type': 'application/json', ...options.headers };
        if (this.token) headers['Authorization'] = `Bearer ${this.token}`;
        
        const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
        const data = await res.json();
        
        if (!res.ok || !data.success) {
            throw new Error(data.message || 'Request failed');
        }
        return data;
    }

    get(path) { return this.request(path); }
    post(path, body) { return this.request(path, { method: 'POST', body: JSON.stringify(body) }); }
    put(path, body) { return this.request(path, { method: 'PUT', body: JSON.stringify(body) }); }
    del(path) { return this.request(path, { method: 'DELETE' }); }

    // Auth
    signup(data) { return this.post('/auth/signup', data); }
    login(data) { return this.post('/auth/login', data); }

    // Family
    createFamily(data) { return this.post('/families', data); }
    getMyFamilies() { return this.get('/families'); }
    getFamily(id) { return this.get(`/families/${id}`); }
    getFamilyMembers(id) { return this.get(`/families/${id}/members`); }

    // Consent
    getConsents(params) { 
        const q = new URLSearchParams(params).toString();
        return this.get(`/consents?${q}`); 
    }
    grantConsent(data) { return this.post('/consents/grant', data); }
    revokeConsent(data) { return this.post('/consents/revoke', data); }

    // Activity
    getScreenTime(childId, date) { return this.get(`/children/${childId}/screen-time?date=${date || new Date().toISOString().slice(0,10)}`); }
    getTimeline(childId, date) { return this.get(`/children/${childId}/timeline?date=${date || new Date().toISOString().slice(0,10)}`); }

    // Policies
    getPolicies(params) { 
        const q = new URLSearchParams(params).toString();
        return this.get(`/policies?${q}`); 
    }
    createPolicy(data) { return this.post('/policies', data); }
    deletePolicy(id) { return this.del(`/policies/${id}`); }

    // Alerts
    getAlerts(familyId, unacknowledgedOnly = false) { 
        return this.get(`/alerts?familyId=${familyId}&unacknowledgedOnly=${unacknowledgedOnly}`); 
    }
    acknowledgeAlert(id) { return this.put(`/alerts/${id}/acknowledge`); }

    // Risk & Intelligence (Phase 2)
    getRiskScore(childId) { return this.get(`/risk/score/${childId}`); }
    getRiskTrend(childId, days=7) { return this.get(`/risk/trend/${childId}?days=${days}`); }
    getSmartSuggestions(childId) { return this.get(`/risk/suggestions/${childId}`); }

    // Web Safety (Phase 2)
    getWebFilters(childId) { return this.get(`/web-safety/filters/${childId}`); }
    updateWebFilter(data) { return this.put(`/web-safety/filters/${data.id}`, data); }
    getWebHistory(childId) { return this.get(`/web-safety/history/${childId}`); }

    // Live Support & Assistance (Phase 3)
    getActiveSession(childId) { return this.get(`/support/sessions/active?childId=${childId}`); }
    initiateSession(data) { return this.post('/support/sessions', data); }
    endSession(sessionId) { return this.put(`/support/sessions/${sessionId}/end`, {}); }

    // Devices
    getDevicesByFamily(familyId) { return this.get(`/devices?familyId=${familyId}`); }
}

const api = new ApiClient();
