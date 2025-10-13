// js/dashboard.js - User-specific dashboard functionality
class Dashboard {
    constructor() {
        this.currentUser = null;
        this.appointments = [];
        this.currentFilter = 'upcoming';
    }

    async init() {
        // Ensure user is authenticated
        const auth = AuthGuard.requireAuth();
        if (!auth) return;

        this.currentUser = JSON.parse(auth.user);
        this.setupUserInfo();
        await this.loadUserAppointments();
        this.setupEventListeners();
    }

    setupUserInfo() {
        // Display user information
        document.getElementById('userName').textContent = this.currentUser.firstName || 'User';
        document.getElementById('userInitial').textContent = (this.currentUser.firstName?.[0] || 'U').toUpperCase();
    }

    async loadUserAppointments() {
        try {
            // For now, show loading state and simulate data
            // TODO: Replace with actual API call when appointment service is ready
            const appointmentsContainer = document.getElementById('appointmentsList');
            const loadingElement = document.getElementById('appointmentsLoading');
            const noAppointmentsElement = document.getElementById('noAppointments');

            // Simulate loading delay
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Hide loading
            loadingElement.classList.add('d-none');

            // For now, simulate empty appointments
            // TODO: Implement actual API call
            // const appointments = await this.fetchUserAppointments();
            const appointments = []; // Empty for now

            if (appointments.length === 0) {
                noAppointmentsElement.classList.remove('d-none');
                this.updateStats(0, 0, 0, 0);
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

        const filteredAppointments = this.filterAppointments(appointments);

        container.innerHTML = filteredAppointments.map(appointment => `
            <div class="appointment-card border rounded-3 p-3 mb-3">
                <div class="row align-items-center">
                    <div class="col-md-2">
                        <div class="appointment-date text-center">
                            <div class="date-day fw-bold text-primary">${this.formatDate(appointment.date, 'DD')}</div>
                            <div class="date-month small text-muted">${this.formatDate(appointment.date, 'MMM')}</div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="appointment-time">
                            <i class="fas fa-clock text-muted me-2"></i>
                            <span class="fw-bold">${appointment.time}</span>
                        </div>
                        <div class="appointment-duration text-muted small">
                            ${appointment.duration} minutes
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="appointment-details">
                            <div class="service-name fw-bold">${appointment.serviceName}</div>
                            <div class="barber-shop text-muted small">
                                <i class="fas fa-cut me-1"></i>${appointment.barberName} at ${appointment.shopName}
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2">
                        <div class="appointment-price text-end">
                            <div class="price fw-bold">$${appointment.price}</div>
                        </div>
                    </div>
                    <div class="col-md-1">
                        <div class="appointment-actions">
                            <div class="dropdown">
                                <button class="btn btn-outline-secondary btn-sm dropdown-toggle" data-bs-toggle="dropdown">
                                    <i class="fas fa-ellipsis-h"></i>
                                </button>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#" onclick="dashboard.rescheduleAppointment('${appointment.id}')">
                                        <i class="fas fa-calendar me-2"></i>Reschedule
                                    </a></li>
                                    <li><a class="dropdown-item text-danger" href="#" onclick="dashboard.cancelAppointment('${appointment.id}')">
                                        <i class="fas fa-times me-2"></i>Cancel
                                    </a></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    filterAppointments(appointments) {
        const now = new Date();

        switch (this.currentFilter) {
            case 'upcoming':
                return appointments.filter(apt => new Date(apt.dateTime) > now);
            case 'past':
                return appointments.filter(apt => new Date(apt.dateTime) < now);
            default:
                return appointments;
        }
    }

    updateStats(upcoming, completed, favorites, totalHours) {
        document.getElementById('upcomingCount').textContent = upcoming;
        document.getElementById('completedCount').textContent = completed;
        document.getElementById('favoriteBarbers').textContent = favorites;
        document.getElementById('totalHours').textContent = totalHours;
    }

    setupEventListeners() {
        // Filter buttons
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
    }

    formatDate(dateStr, format) {
        const date = new Date(dateStr);
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                       'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

        switch (format) {
            case 'DD':
                return date.getDate().toString().padStart(2, '0');
            case 'MMM':
                return months[date.getMonth()];
            default:
                return date.toLocaleDateString();
        }
    }

    showError(message) {
        const container = document.getElementById('appointmentsList');
        container.innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>${message}
            </div>
        `;
    }

    async cancelAppointment(appointmentId) {
        if (confirm('Are you sure you want to cancel this appointment?')) {
            try {
                // TODO: Implement API call to cancel appointment
                console.log('Canceling appointment:', appointmentId);
                await this.loadUserAppointments(); // Reload appointments
            } catch (error) {
                console.error('Error canceling appointment:', error);
                alert('Failed to cancel appointment. Please try again.');
            }
        }
