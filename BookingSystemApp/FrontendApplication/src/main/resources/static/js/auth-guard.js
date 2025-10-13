// frontend/src/main/resources/static/js/auth-guard.js

// Authentication Guard for Protected Routes
class AuthGuard {
    static checkAuth() {
        const token = localStorage.getItem('barberbook_token');
        const user = localStorage.getItem('barberbook_user');

        if (!token || !user) {
            return null;
        }

        // Check if token is expired (optional)
        try {
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));
            if (tokenPayload.exp * 1000 < Date.now()) {
                this.logout();
                return null;
            }
        } catch (e) {
            this.logout();
            return null;
        }

        return { token, user: JSON.parse(user) };
    }

    static requireAuth() {
        const auth = this.checkAuth();
        if (!auth) {
            // Store the intended destination
            localStorage.setItem('redirectAfterLogin', window.location.pathname);
            window.location.href = 'login.html';
            return false;
        }
        return auth;
    }

    static redirectBasedOnRole(user) {
        switch (user.role) {
            case 'CUSTOMER':
                return 'dashboard.html';
            case 'STAFF':
                return 'dashboard.html'; // Staff dashboard
            case 'SHOP_OWNER':
                return 'owner-dashboard.html'; // Owner dashboard
            default:
                return 'dashboard.html';
        }
    }

    static logout() {
        localStorage.removeItem('barberbook_token');
        localStorage.removeItem('barberbook_user');
        window.location.href = 'index.html';
    }
}

async function refreshTokenIfNeeded() {
    const token = localStorage.getItem('barberbook_token');
    if (!token) return false;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expiresIn = payload.exp * 1000 - Date.now();

        // Refresh if token expires in less than 5 minutes
        if (expiresIn < 5 * 60 * 1000) {
            const refreshToken = localStorage.getItem('barberbook_refresh_token');

            const response = await apiCall('/users/auth/refresh', {
                method: 'POST',
                body: JSON.stringify({ refreshToken })
            });

            localStorage.setItem('barberbook_token', response.accessToken);
            return true;
        }
    } catch (e) {
        console.error('Token refresh failed:', e);
        return false;
    }
}
