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

            loadingElement.classList.remove('d-none');
            appointmentsContainer.innerHTML = '';
            noAppointmentsElement.classList.add('d-none');

            // Fetch appointments from API
            const token = localStorage.getItem('barberbook_token');
            const response = await fetch('http://localhost:8083/api/appointments/my-appointments', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to load appointments');
            }

            this.appointments = await response.json();
            console.log('Loaded appointments:', this.appointments);

            // Hide loading
            loadingElement.classList.add('d-none');

            if (this.appointments.length === 0) {
                noAppointmentsElement.classList.remove('d-none');
                this.updateStats(0, 0);
            } else {
                this.renderAppointments(this.appointments);
                this.calculateAndUpdateStats();
            }

        } catch (error) {
            console.error('Error loading appointments:', error);
            document.getElementById('appointmentsLoading').classList.add('d-none');
            this.showError('Failed to load appointments');
        }
    }

    calculateAndUpdateStats() {
        const now = new Date();
        let upcoming = 0;
        let completed = 0;

        this.appointments.forEach(apt => {
            const aptDate = new Date(`${apt.appointmentDate}T${apt.appointmentTime}`);
            if (aptDate > now && (apt.status === 'PENDING' || apt.status === 'CONFIRMED')) {
                upcoming++;
            } else if (apt.status === 'COMPLETED') {
                completed++;
            }
        });

        this.updateStats(upcoming, completed);
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
        container.innerHTML = '';

        appointments.forEach(appointment => {
            const appointmentCard = this.createAppointmentCard(appointment);
            container.appendChild(appointmentCard);
        });
    }

    createAppointmentCard(appointment) {
        const card = document.createElement('div');
        card.className = 'col-md-6 col-lg-4 mb-3';

        // Format date
        const dateObj = new Date(`${appointment.appointmentDate}T${appointment.appointmentTime}`);
        const formattedDate = dateObj.toLocaleDateString('en-US', { 
            weekday: 'short', 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric' 
        });
        const formattedTime = appointment.appointmentTime; // Already in HH:mm format

        // Get status badge class and text
        const statusInfo = this.getStatusBadge(appointment.status, dateObj);

        card.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="card-title mb-0">${appointment.shopName || 'Shop'}</h5>
                        <span class="badge ${statusInfo.class}">${statusInfo.text}</span>
                    </div>
                    
                    <h6 class="text-muted mb-3">${appointment.serviceName || 'Service'}</h6>
                    
                    <div class="appointment-details">
                        <div class="d-flex align-items-center mb-2">
                            <i class="bi bi-calendar-event me-2 text-primary"></i>
                            <span>${formattedDate}</span>
                        </div>
                        <div class="d-flex align-items-center mb-2">
                            <i class="bi bi-clock me-2 text-primary"></i>
                            <span>${formattedTime}</span>
                        </div>
                        ${appointment.employeeName ? `
                        <div class="d-flex align-items-center mb-2">
                            <i class="bi bi-person me-2 text-primary"></i>
                            <span>${appointment.employeeName}</span>
                        </div>
                        ` : ''}
                        ${appointment.price ? `
                        <div class="d-flex align-items-center">
                            <i class="bi bi-currency-euro me-2 text-primary"></i>
                            <span><strong>€${appointment.price.toFixed(2)}</strong></span>
                        </div>
                        ` : ''}
                    </div>
                </div>
                
                ${this.shouldShowActions(appointment.status, dateObj) ? `
                <div class="card-footer bg-transparent">
                    <button class="btn btn-sm btn-outline-primary me-2" onclick="dashboard.goToReschedule(${appointment.id}, ${appointment.shopId}, ${appointment.serviceId})">
                        <i class="bi bi-arrow-repeat me-1"></i>Reschedule
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="dashboard.cancelAppointment(${appointment.id})">
                        <i class="bi bi-x-circle me-1"></i>Cancel
                    </button>
                </div>
                ` : ''}
            </div>
        `;

        return card;
    }

    getStatusBadge(status, dateObj) {
        const now = new Date();
        
        switch(status) {
            case 'PENDING':
                return { class: 'bg-warning text-dark', text: 'Pending' };
            case 'CONFIRMED':
                if (dateObj < now) {
                    return { class: 'bg-secondary', text: 'Past' };
                }
                return { class: 'bg-success', text: 'Confirmed' };
            case 'COMPLETED':
                return { class: 'bg-info', text: 'Completed' };
            case 'CANCELLED':
                return { class: 'bg-danger', text: 'Cancelled' };
            default:
                return { class: 'bg-secondary', text: status };
        }
    }

    shouldShowActions(status, dateObj) {
        const now = new Date();
        // Only show actions for future appointments that are pending or confirmed
        return dateObj > now && (status === 'PENDING' || status === 'CONFIRMED');
    }

    async cancelAppointment(appointmentId) {
        if (!confirm('Are you sure you want to cancel this appointment?')) {
            return;
        }

        try {
            const token = localStorage.getItem('barberbook_token');
            const response = await fetch(`http://localhost:8083/api/appointments/${appointmentId}/cancel`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to cancel appointment');
            }

            this.showSuccess('Appointment cancelled successfully');
            // Reload appointments
            await this.loadUserAppointments();

        } catch (error) {
            console.error('Error cancelling appointment:', error);
            this.showError('Failed to cancel appointment');
        }
    }

    goToReschedule(appointmentId, shopId, serviceId) {
        window.location.href = `booking.html?shopId=${encodeURIComponent(shopId)}&serviceId=${encodeURIComponent(serviceId)}&appointmentId=${encodeURIComponent(appointmentId)}&edit=true`;
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

    showSuccess(message) {
        // Create temporary success alert at the top of the page
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3';
        alertDiv.style.zIndex = '9999';
        alertDiv.innerHTML = `
            <i class="bi bi-check-circle me-2"></i>${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        document.body.appendChild(alertDiv);

        // Auto dismiss after 3 seconds
        setTimeout(() => {
            alertDiv.classList.remove('show');
            setTimeout(() => alertDiv.remove(), 150);
        }, 3000);
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
