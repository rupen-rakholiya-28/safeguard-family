// ===== App State =====
let currentPage = 'dashboard';
let currentFamily = null;
let currentChildren = [];

// ===== Router =====
function showPage(page) {
    document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    
    const el = document.getElementById(`page-${page}`);
    if (el) el.classList.remove('hidden');
    
    const nav = document.querySelector(`[data-page="${page}"]`);
    if (nav) nav.classList.add('active');
    
    currentPage = page;
    
    switch(page) {
        case 'dashboard': loadDashboard(); break;
        case 'family': loadFamily(); break;
        case 'consents': loadConsents(); break;
        case 'controls': loadControls(); break;
        case 'alerts': loadAlerts(); break;
    }
}

// ===== Auth =====
function showAuth(type = 'login') {
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('app-screen').classList.add('hidden');
    document.getElementById('login-form').classList.toggle('hidden', type !== 'login');
    document.getElementById('signup-form').classList.toggle('hidden', type !== 'signup');
}

function showApp() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-screen').classList.remove('hidden');
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    document.getElementById('user-display-name').textContent = user.displayName || 'User';
    document.getElementById('user-display-email').textContent = user.email || '';
    document.getElementById('user-avatar-letter').textContent = (user.displayName || 'U')[0].toUpperCase();
    loadInitialData();
}

async function handleSignup(e) {
    e.preventDefault();
    const errEl = document.getElementById('signup-error');
    errEl.style.display = 'none';
    
    try {
        const data = {
            email: document.getElementById('signup-email').value,
            password: document.getElementById('signup-password').value,
            displayName: document.getElementById('signup-name').value,
        };
        const res = await api.signup(data);
        api.setToken(res.data.accessToken);
        localStorage.setItem('refreshToken', res.data.refreshToken);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        showApp();
        showToast('Account created!', 'success');
    } catch(err) {
        errEl.textContent = err.message;
        errEl.style.display = 'block';
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const errEl = document.getElementById('login-error');
    errEl.style.display = 'none';
    
    try {
        const data = {
            email: document.getElementById('login-email').value,
            password: document.getElementById('login-password').value,
        };
        const res = await api.login(data);
        api.setToken(res.data.accessToken);
        localStorage.setItem('refreshToken', res.data.refreshToken);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        showApp();
        showToast('Welcome back!', 'success');
    } catch(err) {
        errEl.textContent = err.message;
        errEl.style.display = 'block';
    }
}

function handleLogout() {
    api.clearToken();
    showAuth('login');
}

// ===== Initial Data Load =====
async function loadInitialData() {
    try {
        const res = await api.getMyFamilies();
        if (res.data && res.data.length > 0) {
            currentFamily = res.data[0];
            const membersRes = await api.getFamilyMembers(currentFamily.id);
            currentChildren = membersRes.data.filter(m => m.role === 'CHILD');
        }
    } catch(e) { /* first time, no family */ }
    showPage('dashboard');
}

// ===== Dashboard =====
async function loadDashboard() {
    const container = document.getElementById('dashboard-content');
    
    if (!currentFamily) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="icon">👨‍👩‍👧‍👦</div>
                <h3>Welcome! Create your family first</h3>
                <p>Set up a family to start protecting your children</p>
                <button class="btn btn-primary" style="max-width:240px;margin:20px auto" onclick="showPage('family')">
                    Create Family
                </button>
            </div>`;
        return;
    }

    let alertCount = 0;
    try {
        const alertRes = await api.getAlerts(currentFamily.id, true);
        alertCount = alertRes.data ? alertRes.data.length : 0;
    } catch(e) {}

    container.innerHTML = `
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">👨‍👩‍👧‍👦</div>
                <div class="stat-value">${currentFamily.name}</div>
                <div class="stat-label">Family</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">👶</div>
                <div class="stat-value">${currentChildren.length}</div>
                <div class="stat-label">Children</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🔔</div>
                <div class="stat-value">${alertCount}</div>
                <div class="stat-label">Active Alerts</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">✅</div>
                <div class="stat-value">Active</div>
                <div class="stat-label">Protection Status</div>
            </div>
        </div>
        <div class="grid-2">
            <div class="card">
                <div class="card-title">📋 Invite Code</div>
                <div class="invite-code-display">
                    <p>Share this code with your child's device</p>
                    <div class="code">${currentFamily.inviteCode}</div>
                    <p>Enter this in the child app to link the device</p>
                </div>
            </div>
            <div class="card">
                <div class="card-title">👶 Children</div>
                ${currentChildren.length === 0 
                    ? '<div class="empty-state"><p>No children linked yet. Share your invite code!</p></div>'
                    : currentChildren.map(c => `
                        <div class="member-item">
                            <div class="avatar child">${(c.displayName||'C')[0]}</div>
                            <div class="info">
                                <div class="name">${c.displayName}</div>
                                <div class="role">Child</div>
                            </div>
                        </div>`).join('')
                }
            </div>
        </div>`;
}

// ===== Family =====
async function loadFamily() {
    const container = document.getElementById('family-content');
    
    if (!currentFamily) {
        container.innerHTML = `
            <div class="card" style="max-width:480px">
                <h3 style="margin-bottom:20px">Create Your Family</h3>
                <div class="form-group">
                    <label>Family Name</label>
                    <input type="text" id="family-name" placeholder="e.g. The Smiths">
                </div>
                <button class="btn btn-primary" onclick="createFamily()">Create Family</button>
            </div>`;
        return;
    }

    let members = [];
    try {
        const res = await api.getFamilyMembers(currentFamily.id);
        members = res.data || [];
    } catch(e) {}

    container.innerHTML = `
        <div class="grid-2">
            <div class="card">
                <div class="card-title">👨‍👩‍👧‍👦 Family Details</div>
                <p><strong>Name:</strong> ${currentFamily.name}</p>
                <div class="invite-code-display" style="margin-top:16px">
                    <p>Invite Code</p>
                    <div class="code">${currentFamily.inviteCode}</div>
                </div>
            </div>
            <div class="card">
                <div class="card-title">👥 Members (${members.length})</div>
                ${members.map(m => `
                    <div class="member-item">
                        <div class="avatar ${m.role.toLowerCase()}">${(m.displayName||'?')[0]}</div>
                        <div class="info">
                            <div class="name">${m.displayName}</div>
                            <div class="role">${m.role}</div>
                        </div>
                    </div>`).join('')}
            </div>
        </div>`;
}

async function createFamily() {
    try {
        const name = document.getElementById('family-name').value;
        if (!name) return showToast('Enter a family name', 'error');
        const res = await api.createFamily({ name });
        currentFamily = res.data;
        showToast('Family created!', 'success');
        showPage('family');
    } catch(err) { showToast(err.message, 'error'); }
}

// ===== Consents =====
async function loadConsents() {
    const container = document.getElementById('consents-content');
    
    if (!currentFamily || currentChildren.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="icon">📋</div><p>Link a child device first to manage consents</p></div>';
        return;
    }

    const child = currentChildren[0];
    let consents = [];
    try {
        const res = await api.getConsents({ childId: child.id });
        consents = res.data || [];
    } catch(e) {}

    const features = [
        { key: 'SCREEN_TIME_TRACKING', label: 'Screen Time Tracking', desc: 'Track daily screen time usage', icon: '⏱️' },
        { key: 'APP_USAGE_TRACKING', label: 'App Usage Tracking', desc: 'See which apps are used', icon: '📱' },
        { key: 'LOCATION_SHARING', label: 'Location Sharing', desc: 'Share device location', icon: '📍' },
        { key: 'WEB_PROTECTION', label: 'Web Protection', desc: 'Safe browsing filters', icon: '🌐' },
        { key: 'EMERGENCY_CONTACT_SHARING', label: 'Emergency Contacts', desc: 'Share emergency info', icon: '🆘' },
    ];

    container.innerHTML = `
        <div class="card">
            <div class="card-title">Consent Status for ${child.displayName}</div>
            ${features.map(f => {
                const consent = consents.find(c => c.featureName === f.key);
                const status = consent ? consent.status : 'NOT_SET';
                return `
                <div class="consent-item">
                    <div>
                        <div class="feature-name">${f.icon} ${f.label}</div>
                        <div class="feature-desc">${f.desc}</div>
                    </div>
                    <div style="display:flex;align-items:center;gap:8px">
                        <span class="badge badge-${status === 'GRANTED' ? 'granted' : status === 'REVOKED' ? 'revoked' : 'pending'}">${status}</span>
                        ${status !== 'GRANTED' 
                            ? `<button class="btn btn-success btn-sm" onclick="grantConsent('${child.id}','${f.key}')">Grant</button>`
                            : `<button class="btn btn-danger btn-sm" onclick="revokeConsent('${child.id}','${f.key}')">Revoke</button>`
                        }
                    </div>
                </div>`;
            }).join('')}
        </div>`;
}

async function grantConsent(childId, feature) {
    try {
        await api.grantConsent({ childId, featureName: feature, policyVersion: '1.0' });
        showToast('Consent granted', 'success');
        loadConsents();
    } catch(err) { showToast(err.message, 'error'); }
}

async function revokeConsent(childId, feature) {
    try {
        await api.revokeConsent({ childId, featureName: feature });
        showToast('Consent revoked', 'success');
        loadConsents();
    } catch(err) { showToast(err.message, 'error'); }
}

// ===== Controls =====
async function loadControls() {
    const container = document.getElementById('controls-content');
    
    if (!currentFamily || currentChildren.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="icon">⚙️</div><p>Link a child device first to set controls</p></div>';
        return;
    }

    const child = currentChildren[0];
    let policies = [];
    try {
        const res = await api.getPolicies({ childId: child.id });
        policies = res.data || [];
    } catch(e) {}

    container.innerHTML = `
        <div class="grid-2">
            <div class="card">
                <div class="card-title">➕ Add New Rule</div>
                <div class="form-group">
                    <label>Rule Type</label>
                    <select id="policy-type">
                        <option value="SCREEN_TIME_LIMIT">Screen Time Limit</option>
                        <option value="APP_BLOCK">Block Apps</option>
                        <option value="BEDTIME_MODE">Bedtime Mode</option>
                        <option value="STUDY_MODE">Study Mode</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Daily Limit (minutes)</label>
                    <input type="number" id="policy-limit" placeholder="e.g. 120">
                </div>
                <div class="form-group">
                    <label>Start Time</label>
                    <input type="time" id="policy-start">
                </div>
                <div class="form-group">
                    <label>End Time</label>
                    <input type="time" id="policy-end">
                </div>
                <button class="btn btn-primary" onclick="createPolicy('${child.id}')">Create Rule</button>
            </div>
            <div class="card">
                <div class="card-title">📋 Active Rules (${policies.length})</div>
                ${policies.length === 0 
                    ? '<div class="empty-state"><p>No rules set yet</p></div>'
                    : policies.map(p => `
                        <div class="consent-item">
                            <div>
                                <div class="feature-name">${getPolicyIcon(p.policyType)} ${formatPolicyType(p.policyType)}</div>
                                <div class="feature-desc">${getPolicyDesc(p)}</div>
                            </div>
                            <button class="btn btn-danger btn-sm" onclick="deletePolicy('${p.id}')">Remove</button>
                        </div>`).join('')
                }
            </div>
        </div>`;
}

async function createPolicy(childId) {
    try {
        const data = {
            childId,
            policyType: document.getElementById('policy-type').value,
            dailyLimitMinutes: parseInt(document.getElementById('policy-limit').value) || null,
            startTime: document.getElementById('policy-start').value || null,
            endTime: document.getElementById('policy-end').value || null,
        };
        await api.createPolicy(data);
        showToast('Rule created!', 'success');
        loadControls();
    } catch(err) { showToast(err.message, 'error'); }
}

async function deletePolicy(id) {
    try {
        await api.deletePolicy(id);
        showToast('Rule removed', 'success');
        loadControls();
    } catch(err) { showToast(err.message, 'error'); }
}

function getPolicyIcon(type) {
    const icons = { SCREEN_TIME_LIMIT: '⏱️', APP_BLOCK: '🚫', BEDTIME_MODE: '🌙', STUDY_MODE: '📚', APP_ALLOW: '✅', REWARD_TIME: '🎁' };
    return icons[type] || '⚙️';
}

function formatPolicyType(type) {
    return type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

function getPolicyDesc(p) {
    let parts = [];
    if (p.dailyLimitMinutes) parts.push(`${p.dailyLimitMinutes} min/day`);
    if (p.startTime && p.endTime) parts.push(`${p.startTime} - ${p.endTime}`);
    if (p.appPackages) parts.push(`Apps: ${p.appPackages}`);
    return parts.join(' • ') || 'Active';
}

// ===== Alerts =====
async function loadAlerts() {
    const container = document.getElementById('alerts-content');
    
    if (!currentFamily) {
        container.innerHTML = '<div class="empty-state"><div class="icon">🔔</div><p>Create a family first</p></div>';
        return;
    }

    let alerts = [];
    try {
        const res = await api.getAlerts(currentFamily.id);
        alerts = res.data || [];
    } catch(e) {}

    container.innerHTML = `
        <div class="card">
            <div class="card-title">🔔 Alerts (${alerts.length})</div>
            ${alerts.length === 0 
                ? '<div class="empty-state"><p>No alerts yet — everything looks safe!</p></div>'
                : alerts.map(a => `
                    <div class="alert-item severity-${a.severity}">
                        <div class="alert-content">
                            <div class="alert-title">${a.title}</div>
                            <div class="alert-msg">${a.message || ''}</div>
                            <div class="alert-time">${new Date(a.createdAt).toLocaleString()} • ${a.severity}</div>
                        </div>
                        ${!a.acknowledged ? `<button class="btn btn-sm btn-secondary" onclick="ackAlert('${a.id}')">Acknowledge</button>` : '<span class="badge badge-granted">Done</span>'}
                    </div>`).join('')
            }
        </div>`;
}

async function ackAlert(id) {
    try {
        await api.acknowledgeAlert(id);
        showToast('Alert acknowledged', 'success');
        loadAlerts();
    } catch(err) { showToast(err.message, 'error'); }
}

// ===== Toast =====
function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
    if (api.token) {
        showApp();
    } else {
        showAuth('login');
    }
});
