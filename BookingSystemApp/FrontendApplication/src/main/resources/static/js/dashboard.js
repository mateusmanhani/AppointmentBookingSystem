// js/dashboard.js - Enhanced dashboard with profile editing
class Dashboard {
    constructor() {
        this.currentUser = null;
        this.appointments = [];
        this.currentFilter = 'upcoming';
        this.editProfileModal = null;
    }

    async init() {
        // Ensure user is authenticated
        const auth = AuthGuard.requireAuth();
        if (!auth) return;

        // auth.user may already be an object or a JSON string depending on AuthGuard implementation
        try {
            if (typeof auth.user === 'string') {
                this.currentUser = JSON.parse(auth.user);
            } else if (typeof auth.user === 'object' && auth.user !== null) {
                this.currentUser = auth.user;
            } else {
                // fallback: try localStorage
                const stored = localStorage.getItem('barberbook_user');
                this.currentUser = stored ? JSON.parse(stored) : {};
            }
        } catch (e) {
            console.warn('Failed to parse auth.user; falling back to localStorage or empty user', e);
            const stored = localStorage.getItem('barberbook_user');
            try {
                this.currentUser = stored ? JSON.parse(stored) : {};
            } catch (ee) {
                this.currentUser = {};
            }
        }
        this.editProfileModal = new bootstrap.Modal(document.getElementById('editProfileModal'));

        this.setupUserInfo();
        await this.loadUserAppointments();
        // Wire up UI event listeners (buttons, filters, form validation)
        this.setupEventListeners();
    }

    setupUserInfo() {
        // Display user information in profile card and header
        const firstName = this.currentUser.firstName || 'User';
        const lastName = this.currentUser.lastName || '';
        const fullName = `${firstName} ${lastName}`.trim();

        document.getElementById('userName').textContent = firstName;
        document.getElementById('userFullName').textContent = fullName;
        document.getElementById('userEmail').textContent = this.currentUser.email || '';
        document.getElementById('userPhone').textContent = this.currentUser.phone || 'Not provided';
        document.getElementById('userInitial').textContent = firstName[0]?.toUpperCase() || 'U';
    }

    // Show edit profile modal [web:586][web:587]
    showEditProfile() {
        // Ensure the user is authenticated before showing the modal
        const auth = (typeof AuthGuard !== 'undefined' && AuthGuard.requireAuth) ? AuthGuard.requireAuth() : null;
        if (!auth) {
            console.warn('User not authenticated - cannot open profile editor');
            return;
        }

        // Lazy-load currentUser if init() didn't run or returned early
        if (!this.currentUser) {
            try {
                if (auth && typeof auth.user === 'string') {
                    this.currentUser = JSON.parse(auth.user);
                } else if (auth && typeof auth.user === 'object') {
                    this.currentUser = auth.user;
                } else {
                    const stored = localStorage.getItem('barberbook_user');
                    this.currentUser = stored ? JSON.parse(stored) : {};
                }
            } catch (e) {
                console.warn('Unable to parse current user from auth/localStorage', e);
                this.currentUser = {};
            }
        }

        // Pre-fill the form with current user data (use safe access)
        document.getElementById('editFirstName').value = (this.currentUser && this.currentUser.firstName) ? this.currentUser.firstName : '';
        document.getElementById('editLastName').value = (this.currentUser && this.currentUser.lastName) ? this.currentUser.lastName : '';
        document.getElementById('editPhone').value = (this.currentUser && this.currentUser.phone) ? this.currentUser.phone : '';
        document.getElementById('editEmail').value = (this.currentUser && this.currentUser.email) ? this.currentUser.email : '';

        // Clear any previous messages
        const msgDiv = document.getElementById('profileUpdateMessage');
        if (msgDiv) msgDiv.innerHTML = '';

        // Ensure a bootstrap Modal instance exists; create it lazily if needed
        if (!this.editProfileModal) {
            const modalEl = document.getElementById('editProfileModal');
            if (modalEl && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                try {
                    this.editProfileModal = new bootstrap.Modal(modalEl);
                } catch (e) {
                    console.error('Failed to create bootstrap Modal instance:', e);
                }
            }
        }

        if (this.editProfileModal && typeof this.editProfileModal.show === 'function') {
            this.editProfileModal.show();
        } else {
            // Fallback: if modal instance couldn't be created, attempt to toggle the modal class
            const modalEl = document.getElementById('editProfileModal');
            if (modalEl) {
                modalEl.classList.add('show');
                modalEl.style.display = 'block';
                document.body.classList.add('modal-open');
            } else {
                console.error('Edit profile modal element not found');
            }
        }
    }

    // Save profile changes [web:317][web:305]
    async saveProfile() {
        const saveBtn = document.getElementById('saveProfileBtn');
        const originalBtnContent = saveBtn.innerHTML;

        // Get form data
        const formData = {
            firstName: document.getElementById('editFirstName').value.trim(),
            lastName: document.getElementById('editLastName').value.trim(),
            phone: document.getElementById('editPhone').value.trim()
        };

        console.log('Updating profile with:', formData);

        // Basic validation
        if (!formData.firstName || !formData.lastName || !formData.phone) {
            this.showProfileMessage('Please fill in all required fields.', 'danger');
            return;
        }

        // Phone validation
        const phonePattern = /^[+]?[0-9]{10,15}$/;
        if (!phonePattern.test(formData.phone)) {
            this.showProfileMessage('Please enter a valid phone number (10-15 digits, optionally starting with +).', 'danger');
            return;
        }

        // Require country code (leading '+') to avoid backend parsing/validation issues
        if (!formData.phone.startsWith('+')) {
            this.showProfileMessage('Please include your country code (e.g. +1, +353). Use the format +<countrycode><number>.', 'danger');
            return;
        }

        // Show loading state
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

        try {
            // Call the API to update profile
            // NOTE: `apiCall` already prefixes CONFIG.API_BASE_URL which includes '/api',
            // so pass the endpoint without the leading '/api' to avoid duplicate '/api/api'.
            const updatedUser = await apiCall('/users/profile', {
                method: 'PUT',
                body: JSON.stringify(formData)
            });

            console.log('Profile updated successfully:', updatedUser);

            // Update local user data
            this.currentUser = { ...this.currentUser, ...updatedUser };

            // Update localStorage with new user data
            localStorage.setItem('barberbook_user', JSON.stringify(this.currentUser));

            // Update UI with new data
            this.setupUserInfo();

            // Show success message
            this.showProfileMessage('Profile updated successfully!', 'success');

            // Close modal after 1.5 seconds
            setTimeout(() => {
                this.editProfileModal.hide();
            }, 1500);

        } catch (error) {
            console.error('Profile update error:', error);
            // Show a friendly message to the user, prefer server-provided message
            const msg = (error && error.message) ? error.message : 'Update failed: An unexpected error occurred.';
            this.showProfileMessage(`Update failed: ${msg}`, 'danger');
        } finally {
            // Restore button state
            saveBtn.disabled = false;
            saveBtn.innerHTML = originalBtnContent;
        }
    }

    // Show message in profile modal
    showProfileMessage(message, type = 'danger') {
        const messageDiv = document.getElementById('profileUpdateMessage');
        messageDiv.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }

    async loadUserAppointments() {
        try {
            const appointmentsContainer = document.getElementById('appointmentsList');
            const loadingElement = document.getElementById('appointmentsLoading');
            const noAppointmentsElement = document.getElementById('noAppointments');

            // Simulate loading delay
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Hide loading
            loadingElement.classList.add('d-none');

            // For now, simulate empty appointments
            const appointments = []; // Empty for now

            if (appointments.length === 0) {
                noAppointmentsElement.classList.remove('d-none');
                this.updateStats(0, 0);
            } else {
                this.renderAppointments(appointments);
                this.updateStats(appointments);
            }

        } catch (error) {
            console.error('Error loading appointments:', error);
            this.showError('Failed to load appointments');
        }
    }

    async fetchUserAppointments() {
        // TODO: Implement when appointment service API is ready
        try {
            const response = await apiCall(`/api/appointments/user/${this.currentUser.id}`);
            return response;
        } catch (error) {
            console.error('Error fetching appointments:', error);
            return [];
        }
    }

    renderAppointments(appointments) {
        const container = document.getElementById('appointmentsList');

        if (appointments.length === 0) {
            document.getElementById('noAppointments').classList.remove('d-none');
            return;
        }

        // TODO: Implement appointment rendering when appointments are available
        container.innerHTML = '<p class="text-muted">No appointments to display</p>';
    }

    updateStats(upcoming, completed) {
        document.getElementById('upcomingCount').textContent = upcoming;
        document.getElementById('completedCount').textContent = completed;
    }

    setupEventListeners() {
        // Filter buttons for appointments
        document.querySelectorAll('[data-filter]').forEach(button => {
            button.addEventListener('click', (e) => {
                // Update active button
                document.querySelectorAll('[data-filter]').forEach(btn => btn.classList.remove('active'));
                e.target.classList.add('active');

                // Update filter
                this.currentFilter = e.target.dataset.filter;
                this.renderAppointments(this.appointments);
            });
        });

        // Form validation for profile editing [web:599]
        document.getElementById('editPhone').addEventListener('blur', function() {
            const phone = this.value.trim();
            const pattern = /^[+]?[0-9]{10,15}$/;

            if (phone && !pattern.test(phone)) {
                this.classList.add('is-invalid');
                if (!this.nextElementSibling || !this.nextElementSibling.classList.contains('invalid-feedback')) {
                    const feedback = document.createElement('div');
                    feedback.className = 'invalid-feedback';
                    feedback.textContent = 'Please enter a valid phone number (10-15 digits)';
                    this.parentNode.parentNode.appendChild(feedback);
                }
            } else {
                this.classList.remove('is-invalid');
                const feedback = this.parentNode.parentNode.querySelector('.invalid-feedback');
                if (feedback) feedback.remove();
            }
        });
    }

    showError(message) {
        const container = document.getElementById('appointmentsList');
        container.innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>${message}
            </div>
        `;
    }
}

// Initialize dashboard when page loads
let dashboard;
document.addEventListener('DOMContentLoaded', async function() {
    dashboard = new Dashboard();
    await dashboard.init();

    // Expose to global scope so inline onclick handlers in HTML can call methods
    try {
        window.dashboard = dashboard;
    } catch (e) {
        // If window is not writable for some reason, silently ignore (very unlikely in browser)
        console.warn('Unable to attach dashboard to window:', e);
    }

    // Ensure Edit and Save buttons are wired even if init() returned early
    const editBtn = document.getElementById('editProfileBtn');
    if (editBtn) {
        editBtn.addEventListener('click', function() {
            if (dashboard && typeof dashboard.showEditProfile === 'function') {
                dashboard.showEditProfile();
            } else {
                console.warn('Dashboard not initialized yet; attempting to open modal directly');
                // Try to open modal directly as a last resort
                const modalEl = document.getElementById('editProfileModal');
                if (modalEl && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                    try {
                        const m = new bootstrap.Modal(modalEl);
                        m.show();
                    } catch (e) {
                        console.error('Failed to show modal directly:', e);
                    }
                }
            }
        });
    }

    const saveBtn = document.getElementById('saveProfileBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', function() {
            if (dashboard && typeof dashboard.saveProfile === 'function') {
                dashboard.saveProfile();
            } else {
                console.warn('Dashboard.saveProfile is not available');
            }
        });
    }
});
