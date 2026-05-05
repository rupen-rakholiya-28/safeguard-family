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
    
        case 'dashboard': loadDashboard(); break;
        case 'family': loadFamily(); break;
        case 'consents': loadConsents(); break;
        case 'controls': loadControls(); break;
        case 'intelligence': loadIntelligence(); break;
        case 'websafety': loadWebSafety(); break;
        case 'livesupport': loadLiveSupport(); break;
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
            <div class="quick-actions">
                <div class="quick-action-card" onclick="showPage('family')">
                    <div class="quick-action-icon">👨‍👩‍👧‍👦</div>
                    <div class="quick-action-text">
                        <div class="quick-action-title">Create Family</div>
                        <div class="quick-action-desc">Set up your family to start protecting</div>
                    </div>
                    <div class="quick-action-arrow">→</div>
                </div>
            </div>
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
    let activePolicies = 0;
    try {
        const alertRes = await api.getAlerts(currentFamily.id, true);
        alertCount = alertRes.data ? alertRes.data.length : 0;
        if (currentChildren.length > 0) {
            const policyRes = await api.getPolicies({ childId: currentChildren[0].id });
            activePolicies = policyRes.data ? policyRes.data.length : 0;
        }
    } catch(e) {}

    container.innerHTML = `
        <div class="quick-actions">
            <div class="quick-action-header">
                <h3>⚡ Quick Actions</h3>
            </div>
            <div class="quick-action-grid">
                <div class="quick-action-card" onclick="showPage('family')">
                    <div class="quick-action-icon" style="background: linear-gradient(135deg, #6366f1, #8b5cf6);">👨‍👩‍👧‍👦</div>
                    <div class="quick-action-text">
                        <div class="quick-action-title">Family</div>
                        <div class="quick-action-desc">Manage members & invites</div>
                    </div>
                    <div class="quick-action-arrow">→</div>
                </div>
                <div class="quick-action-card" onclick="showPage('controls')">
                    <div class="quick-action-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">⚙️</div>
                    <div class="quick-action-text">
                        <div class="quick-action-title">Controls</div>
                        <div class="quick-action-desc">${activePolicies} active rules</div>
                    </div>
                    <div class="quick-action-arrow">→</div>
                </div>
                <div class="quick-action-card" onclick="showPage('consents')">
                    <div class="quick-action-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">📋</div>
                    <div class="quick-action-text">
                        <div class="quick-action-title">Consents</div>
                        <div class="quick-action-desc">Feature permissions</div>
                    </div>
                    <div class="quick-action-arrow">→</div>
                </div>
                <div class="quick-action-card" onclick="showPage('alerts')">
                    <div class="quick-action-icon" style="background: linear-gradient(135deg, #ef4444, #f87171);">🔔</div>
                    <div class="quick-action-text">
                        <div class="quick-action-title">Alerts</div>
                        <div class="quick-action-desc">${alertCount} pending alerts</div>
                    </div>
                    <div class="quick-action-arrow">→</div>
                </div>
            </div>
        </div>
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
}

// ===== Phase 2: Intelligence =====
async function loadIntelligence() {
    const container = document.getElementById('intelligence-content');
    if (!currentFamily || currentChildren.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="icon">🧠</div><p>Link a child device first</p></div>';
        return;
    }
    const childId = currentChildren[0].id;
    let score = null, trend = [], suggestions = [];
    try {
        const scoreRes = await api.getRiskScore(childId);
        score = scoreRes.data;
        const trendRes = await api.getRiskTrend(childId);
        trend = trendRes.data || [];
        const suggRes = await api.getSmartSuggestions(childId);
        suggestions = suggRes.data || [];
    } catch(e) {}

    container.innerHTML = `
        <div class="grid-2">
            <div class="card">
                <div class="card-title">🛡️ Current Risk Score</div>
                <div style="text-align:center; margin: 32px 0;">
                    <div style="font-size: 64px; font-weight: 800; background: var(--accent-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">
                        ${score ? score.overallScore : 0}
                    </div>
                    <div style="color: var(--text-secondary); text-transform: uppercase; font-weight: 600; font-size: 14px; margin-top: 8px;">
                        Status: <span class="badge badge-${score && score.riskLevel === 'LOW' ? 'granted' : 'pending'}">${score ? score.riskLevel : 'Unknown'}</span>
                    </div>
                </div>
                <div class="grid-2">
                    <div style="text-align:center; background:var(--bg-secondary); padding: 16px; border-radius:var(--radius-md);">
                        <div style="font-size:24px;">⏱️</div>
                        <div style="font-weight:600; font-size:18px;">${score ? score.lateNightMinutes : 0}m</div>
                        <div style="font-size:12px; color:var(--text-muted);">Late Night Usage</div>
                    </div>
                    <div style="text-align:center; background:var(--bg-secondary); padding: 16px; border-radius:var(--radius-md);">
                        <div style="font-size:24px;">🔔</div>
                        <div style="font-weight:600; font-size:18px;">${score ? score.eventCount : 0}</div>
                        <div style="font-size:12px; color:var(--text-muted);">Today's Events</div>
                    </div>
                </div>
            </div>
            <div class="card">
                <div class="card-title">📈 7-Day Trend</div>
                <canvas id="riskChart"></canvas>
            </div>
        </div>
        <div class="card" style="margin-top: 24px;">
            <div class="card-title">💡 AI Smart Suggestions</div>
            ${suggestions.length === 0 ? '<p style="color:var(--text-secondary)">No urgent suggestions right now.</p>' : ''}
            ${suggestions.map(s => `
                <div class="alert-item severity-${s.level}">
                    <div class="alert-content">
                        <div class="alert-title">${s.title}</div>
                        <div class="alert-msg">${s.description}</div>
                        <div class="alert-time">Action: <strong>${s.recommendedAction}</strong></div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;

    if (trend.length > 0) {
        const ctx = document.getElementById('riskChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: trend.map(t => new Date(t.scoreDate).toLocaleDateString(undefined, {weekday:'short'})),
                datasets: [{
                    label: 'Risk Score',
                    data: trend.map(t => t.overallScore),
                    borderColor: '#8b5cf6',
                    backgroundColor: 'rgba(139, 92, 246, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, max: 100, grid: { color: 'rgba(255,255,255,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    }
}

// ===== Phase 2: Web Safety =====
async function loadWebSafety() {
    const container = document.getElementById('websafety-content');
    if (!currentFamily || currentChildren.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="icon">🌐</div><p>Link a child device first</p></div>';
        return;
    }
    const childId = currentChildren[0].id;
    let filters = null;
    try {
        const res = await api.getWebFilters(childId);
        filters = res.data;
    } catch(e) {}

    if (!filters) {
        container.innerHTML = '<div class="empty-state"><p>No web filter configuration found.</p></div>';
        return;
    }

    container.innerHTML = `
        <div class="grid-2">
            <div class="card">
                <div class="card-title">🔒 Category Blocking</div>
                <div class="consent-item" onclick="toggleFilter('${filters.id}', 'blockAdultContent', ${!filters.blockAdultContent})">
                    <div>
                        <div class="feature-name">🔞 Adult Content</div>
                        <div class="feature-desc">Block explicit websites and searches</div>
                    </div>
                    <span class="badge badge-${filters.blockAdultContent ? 'granted' : 'revoked'}">${filters.blockAdultContent ? 'ON' : 'OFF'}</span>
                </div>
                <div class="consent-item" onclick="toggleFilter('${filters.id}', 'blockViolence', ${!filters.blockViolence})">
                    <div>
                        <div class="feature-name">⚔️ Violence / Weapons</div>
                        <div class="feature-desc">Block violent content and imagery</div>
                    </div>
                    <span class="badge badge-${filters.blockViolence ? 'granted' : 'revoked'}">${filters.blockViolence ? 'ON' : 'OFF'}</span>
                </div>
                <div class="consent-item" onclick="toggleFilter('${filters.id}', 'blockGambling', ${!filters.blockGambling})">
                    <div>
                        <div class="feature-name">🎰 Gambling</div>
                        <div class="feature-desc">Block gambling and betting sites</div>
                    </div>
                    <span class="badge badge-${filters.blockGambling ? 'granted' : 'revoked'}">${filters.blockGambling ? 'ON' : 'OFF'}</span>
                </div>
            </div>
            <div class="card">
                <div class="card-title">🔍 Safe Search</div>
                <div class="consent-item" onclick="toggleFilter('${filters.id}', 'enforceSafeSearch', ${!filters.enforceSafeSearch})">
                    <div>
                        <div class="feature-name">Search Engines</div>
                        <div class="feature-desc">Force SafeSearch on Google, Bing, YouTube</div>
                    </div>
                    <span class="badge badge-${filters.enforceSafeSearch ? 'granted' : 'revoked'}">${filters.enforceSafeSearch ? 'ON' : 'OFF'}</span>
                </div>
            </div>
        </div>
    `;
}

async function toggleFilter(filterId, field, newValue) {
    try {
        const payload = { id: filterId };
        payload[field] = newValue;
        await api.updateWebFilter(payload);
        showToast('Web filter updated', 'success');
        loadWebSafety();
    } catch (e) { showToast(e.message, 'error'); }
}

// ===== Phase 3: Live Support =====
async function loadLiveSupport() {
    const container = document.getElementById('livesupport-content');
    if (!currentFamily || currentChildren.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="icon">🎧</div><p>Link a child device first</p></div>';
        return;
    }
    const child = currentChildren[0];
    let activeSession = null;
    try {
        const res = await api.getActiveSession(child.id);
        activeSession = res.data;
    } catch(e) {}

    if (activeSession) {
        container.innerHTML = `
            <div class="card" style="border-color: var(--success); box-shadow: 0 0 20px rgba(16,185,129,0.2);">
                <div class="card-title">🟢 Active Session with ${child.displayName}</div>
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <p style="font-size:18px; margin-bottom:8px;">Type: <strong>${activeSession.sessionType}</strong></p>
                        <p style="color:var(--text-secondary); font-size:14px;">Started: ${new Date(activeSession.startedAt).toLocaleString()}</p>
                    </div>
                    <button class="btn btn-danger" onclick="endSession('${activeSession.id}')">End Session</button>
                </div>
            </div>
        `;
    } else {
        container.innerHTML = `
            <div class="grid-2">
                <div class="card">
                    <div class="card-title">Start Live Session</div>
                    <p style="color:var(--text-secondary); margin-bottom:24px; font-size:14px;">
                        Initiate a fully transparent voice or screen sharing session. The child device will display a persistent notification while active.
                    </p>
                    <div style="display:flex; gap:16px; flex-direction:column;">
                        <button class="btn btn-primary" onclick="initiateSession('${child.id}', 'VOICE_CALL')" style="background:linear-gradient(135deg, #3b82f6, #2563eb);">
                            📞 Start Voice Support
                        </button>
                        <button class="btn btn-primary" onclick="initiateSession('${child.id}', 'SCREEN_SHARE')" style="background:linear-gradient(135deg, #10b981, #059669);">
                            📺 Request Screen Share
                        </button>
                        <button class="btn btn-primary" onclick="initiateSession('${child.id}', 'GUIDED_VIEW')" style="background:linear-gradient(135deg, #f59e0b, #d97706);">
                            🧭 Start Guided View
                        </button>
                    </div>
                </div>
                <div class="card">
                    <div class="card-title">Transparency Logs</div>
                    <p style="color:var(--text-secondary); font-size:13px;">All live sessions are logged for audit and consent tracking.</p>
                </div>
            </div>
        `;
    }
}

async function initiateSession(childId, type) {
    try {
        await api.initiateSession({ childId, type });
        showToast('Session initiated', 'success');
        loadLiveSupport();
    } catch(e) { showToast(e.message, 'error'); }
}

async function endSession(sessionId) {
    try {
        await api.endSession(sessionId);
        showToast('Session ended', 'success');
        loadLiveSupport();
    } catch(e) { showToast(e.message, 'error'); }
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
