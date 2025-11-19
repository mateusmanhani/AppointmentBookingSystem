// Booking page functionality with API integration
class BookingPageManager {
    constructor() {
        this.shopId = null;
        this.serviceId = null;
        this.appointmentId = null; // for edit mode
        this.editMode = false;
        this.shop = null;
        this.service = null;
        this.employees = [];
        this.selectedEmployeeId = '';
        this.selectedSlot = null;
        this.SHOP_SERVICE_URL = 'http://localhost:8082';
        this.APPOINTMENT_SERVICE_URL = 'http://localhost:8083';
    }

    async init() {
        // Get params from URL
        const urlParams = new URLSearchParams(window.location.search);
        this.shopId = urlParams.get('shopId');
        this.serviceId = urlParams.get('serviceId');
        this.appointmentId = urlParams.get('appointmentId');
        this.editMode = urlParams.get('edit') === 'true' && !!this.appointmentId;

        if (!this.shopId || !this.serviceId) {
            this.showError('Missing shop or service information. Please start from the shop page.');
            return;
        }

        // Load data and setup page
        await this.loadShopAndService();

        // If editing, load the existing appointment first to prefill
        if (this.editMode) {
            await this.loadExistingAppointment();
        }

        await this.loadEmployees();
        this.setupEventListeners();
        
        // Set date input constraints: min = today, max = 60 days from now
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        const maxDate = new Date(today);
        maxDate.setDate(maxDate.getDate() + 60); // Allow booking up to 60 days ahead
        
        const dateInput = document.getElementById('appointmentDate');
        dateInput.min = today.toISOString().split('T')[0];
        dateInput.max = maxDate.toISOString().split('T')[0];
        
        if (this.editMode && this.existing) {
            dateInput.value = this.existing.appointmentDate;
        } else {
            dateInput.value = tomorrow.toISOString().split('T')[0];
        }
        
        // Initialize the detail date display
        const detailDate = document.getElementById('detailDate');
        if (detailDate) {
            const displayDate = this.editMode && this.existing 
                ? new Date(`${this.existing.appointmentDate}T00:00:00`)
                : tomorrow;
            detailDate.textContent = displayDate.toLocaleDateString('en-US', { 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
            });
        }
        
        // Load initial time slots
        await this.loadTimeSlots();

        // Auto-select existing slot in edit mode
        if (this.editMode && this.existing) {
            this.selectedSlot = this.existing.appointmentTime;
            const btn = document.querySelector(`.time-slot[data-time="${this.existing.appointmentTime}"]`);
            if (btn) {
                btn.click();
            } else {
                const sel = document.getElementById('selectedTime');
                if (sel) sel.textContent = this.prettyTime(this.selectedSlot);
            }
            // Update primary button label for edit mode
            const actionBtn = document.querySelector('button[onclick*="proceedToPayment"]');
            if (actionBtn) {
                actionBtn.innerHTML = '<i class="fas fa-arrow-repeat me-2"></i>Reschedule Appointment';
            }
        }
    }

    async loadExistingAppointment() {
        try {
            const token = localStorage.getItem('barberbook_token');
            const response = await fetch(`${this.APPOINTMENT_SERVICE_URL}/api/appointments/${this.appointmentId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Failed to load appointment');
            this.existing = await response.json();

            // Pre-select employee if any
            if (this.existing.employeeId) {
                this.selectedEmployeeId = String(this.existing.employeeId);
            }
        } catch (e) {
            console.error('Error loading existing appointment:', e);
            this.showError('Unable to load appointment for editing.');
        }
    }

    async loadShopAndService() {
        try {
            // Fetch shop details
            const shopResponse = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}`);
            if (!shopResponse.ok) throw new Error('Failed to load shop');
            this.shop = await shopResponse.json();

            // Fetch service details
            const serviceResponse = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/services/${this.serviceId}`);
            if (!serviceResponse.ok) throw new Error('Failed to load service');
            this.service = await serviceResponse.json();

            this.renderServiceHeader();
        } catch (error) {
            console.error('Error loading shop/service:', error);
            this.showError('Unable to load booking information. Please try again.');
        }
    }

    async loadEmployees() {
        const select = document.getElementById('employeeSelect');
        if (!select) return;
        try {
            const response = await fetch(`${this.SHOP_SERVICE_URL}/api/shops/${this.shopId}/employees`);
            if (!response.ok) throw new Error('Failed to load employees');
            this.employees = await response.json();

            // Populate dropdown
            select.innerHTML = '';
            const anyOpt = document.createElement('option');
            anyOpt.value = '';
            anyOpt.textContent = 'Any available';
            select.appendChild(anyOpt);

            this.employees.forEach(emp => {
                const opt = document.createElement('option');
                opt.value = emp.id;
                opt.textContent = emp.name || `Employee ${emp.id}`;
                select.appendChild(opt);
            });

            // If previously selected, restore
            if (this.selectedEmployeeId) select.value = this.selectedEmployeeId;

            // Update summary
            const selected = this.employees.find(e => String(e.id) === String(select.value));
            const detailEmployee = document.getElementById('detailEmployee');
            if (detailEmployee) detailEmployee.textContent = selected ? (selected.name || `Employee ${selected.id}`) : 'Any available';

        } catch (e) {
            console.error('Error loading employees:', e);
            // Fallback to Any available
            select.innerHTML = '<option value="">Any available</option>';
            const detailEmployee = document.getElementById('detailEmployee');
            if (detailEmployee) detailEmployee.textContent = 'Any available';
        }
    }

    renderServiceHeader() {
        document.title = `Book ${this.service.name} - BarberBook`;
        
        document.querySelector('.service-header h2').textContent = this.service.name;
        document.querySelector('.service-header p.text-muted').textContent = this.service.description || 'Professional service';
        document.querySelector('.service-header .duration').textContent = `${this.service.duration || 30} minutes`;
        document.querySelector('.price-tag span').textContent = `$${(this.service.price || 0).toFixed(2)}`;
        
        // Update details summary
        const detailService = document.getElementById('detailServiceName');
        const detailDuration = document.getElementById('detailDuration');
        const detailPrice = document.getElementById('detailPrice');
        if (detailService) detailService.textContent = this.service.name;
        if (detailDuration) detailDuration.textContent = `${this.service.duration || 30} minutes`;
        if (detailPrice) detailPrice.textContent = `$${(this.service.price || 0).toFixed(2)}`;
    }

    async loadTimeSlots() {
        const date = document.getElementById('appointmentDate').value;
        if (!date) return;

        // Update the header with the selected date
        this.updateSlotsHeader(date);

        const slotsContainer = document.querySelector('.time-slots-grid');
        slotsContainer.innerHTML = '<div class="col-12 text-center py-3"><div class="spinner-border text-primary"></div><p class="mt-2 text-muted">Loading available slots...</p></div>';

        try {
            const employeeQuery = this.selectedEmployeeId ? `?employeeId=${encodeURIComponent(this.selectedEmployeeId)}` : '';
            const response = await fetch(`${this.APPOINTMENT_SERVICE_URL}/api/availability/shop/${this.shopId}/date/${date}${employeeQuery}`);
            if (!response.ok) throw new Error('Failed to load time slots');
            
            const slots = await response.json();
            this.renderTimeSlots(slots);
        } catch (error) {
            console.error('Error loading time slots:', error);
            slotsContainer.innerHTML = '<div class="col-12"><div class="alert alert-warning">Unable to load available times. Please try again.</div></div>';
        }
    }

    updateSlotsHeader(dateString) {
        const headerElement = document.getElementById('slotsDateHeader');
        if (!headerElement) return;

        try {
            const dateObj = new Date(dateString + 'T00:00:00');
            const formatted = dateObj.toLocaleDateString('en-US', { 
                weekday: 'long',
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
            });
            headerElement.textContent = `Available Times for ${formatted}`;
        } catch (e) {
            headerElement.textContent = 'Available Times';
        }
    }

    renderTimeSlots(slots) {
        const container = document.querySelector('.time-slots-grid');
        container.innerHTML = '';
        
        // Normalize slot format: support ["09:00","09:30"] or [{time:"09:00", available:true}, ...]
        const normalized = (slots || []).map(s => typeof s === 'string' ? ({ time: s, available: true }) : s);
        
        // Filter out past slots if today is selected
        const selectedDate = document.getElementById('appointmentDate').value;
        const today = new Date().toISOString().split('T')[0];
        const isToday = selectedDate === today;
        
        let relevantSlots = normalized;
        if (isToday) {
            const now = new Date();
            const currentTime = now.getHours() * 60 + now.getMinutes();
            
            relevantSlots = normalized.filter(slot => {
                const timeStr = slot.time;
                if (!timeStr) return false;
                
                const [hours, minutes] = timeStr.split(':').map(Number);
                const slotTime = hours * 60 + minutes;
                return slotTime > currentTime; // Only future slots
            });
            
            console.log(`Filtered ${normalized.length - relevantSlots.length} past time slots`);
        }
        
        // Filter to only available slots
        const availableSlots = relevantSlots.filter(s => s && (s.available === undefined ? true : !!s.available));
        
        if (availableSlots.length === 0) {
            const message = isToday 
                ? 'No available time slots remaining for today. Please select another date.'
                : 'No available time slots for this date. Please select another date.';
            container.innerHTML = `<div class="col-12"><div class="alert alert-info">${message}</div></div>`;
            return;
        }

        let currentRow = null;
        availableSlots.forEach((slot, index) => {
            if (index % 4 === 0) {
                currentRow = document.createElement('div');
                currentRow.className = 'row g-2 mb-3';
                container.appendChild(currentRow);
            }

            const col = document.createElement('div');
            col.className = 'col-6 col-md-3';
            col.innerHTML = `
                <button type="button" class="btn btn-outline-primary w-100 time-slot" data-time="${slot.time}">
                    ${this.prettyTime(slot.time)}
                </button>
            `;
            currentRow.appendChild(col);
        });

        this.setupTimeSlotHandlers();
    }

    setupTimeSlotHandlers() {
        document.querySelectorAll('.time-slot').forEach(slot => {
            slot.addEventListener('click', () => {
                document.querySelectorAll('.time-slot').forEach(s => {
                    s.classList.remove('selected', 'btn-primary');
                    s.classList.add('btn-outline-primary');
                });
                
                slot.classList.remove('btn-outline-primary');
                slot.classList.add('btn-primary', 'selected');
                
                this.selectedSlot = slot.dataset.time;
                document.getElementById('selectedTime').textContent = this.prettyTime(slot.dataset.time);
            });
        });
    }

    setupEventListeners() {
        const dateInput = document.getElementById('appointmentDate');
        if (!dateInput) return;
        dateInput.addEventListener('change', () => {
            this.selectedSlot = null;
            document.getElementById('selectedTime').textContent = 'Not selected';
            this.loadTimeSlots();
            
            // Update summary
            const date = new Date(dateInput.value);
            const detailDate = document.getElementById('detailDate');
            if (detailDate && !isNaN(date.getTime())) {
                detailDate.textContent = date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
            }
        });

        const employeeSelect = document.getElementById('employeeSelect');
        if (employeeSelect) {
            employeeSelect.addEventListener('change', () => {
                this.selectedEmployeeId = employeeSelect.value;
                const selected = this.employees.find(e => String(e.id) === String(employeeSelect.value));
                const detailEmployee = document.getElementById('detailEmployee');
                if (detailEmployee) detailEmployee.textContent = selected ? (selected.name || `Employee ${selected.id}`) : 'Any available';
                // Reload time slots when employee changes
                this.selectedSlot = null;
                document.getElementById('selectedTime').textContent = 'Not selected';
                this.loadTimeSlots();
            });
        }
    }

    async bookAppointment() {
        const token = localStorage.getItem('barberbook_token');
        if (!token) {
            alert('Please login to book an appointment');
            window.location.href = 'login.html?redirect=' + encodeURIComponent(window.location.href);
            return;
        }

        if (!this.selectedSlot) {
            alert('Please select a time slot');
            return;
        }

    const date = document.getElementById('appointmentDate').value;
    const appointmentDateTime = `${date}T${this.formatToHHMMSS(this.selectedSlot)}`;

        const bookingData = {
            shopId: parseInt(this.shopId),
            serviceId: parseInt(this.serviceId),
            ...(this.selectedEmployeeId ? { employeeId: parseInt(this.selectedEmployeeId) } : {}),
            appointmentDateTime: appointmentDateTime,
            notes: this.selectedEmployeeId ? `Preferred employee: ${this.getSelectedEmployeeName()} (${this.selectedEmployeeId})` : ''
        };

        const btn = document.querySelector('button[onclick*="proceedToPayment"]');
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Booking...';

        try {
            const response = await fetch(`${this.APPOINTMENT_SERVICE_URL}/api/appointments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(bookingData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Booking failed');
            }

            const appointment = await response.json();
            console.log('Appointment created:', appointment);
            
            // Show success and redirect to dashboard
            alert('Appointment booked successfully!');
            window.location.href = 'dashboard.html';
            
        } catch (error) {
            console.error('Booking error:', error);
            alert('Failed to book appointment: ' + error.message);
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    }

    async rescheduleAppointment() {
        const token = localStorage.getItem('barberbook_token');
        if (!token) {
            alert('Please login to manage your appointment');
            window.location.href = 'login.html?redirect=' + encodeURIComponent(window.location.href);
            return;
        }

        if (!this.selectedSlot) {
            alert('Please select a time slot');
            return;
        }

        const date = document.getElementById('appointmentDate').value;
        const newDateTime = `${date}T${this.formatToHHMMSS(this.selectedSlot)}`;

        const payload = {
            newDateTime,
            employeeId: this.selectedEmployeeId ? parseInt(this.selectedEmployeeId) : null,
            notes: undefined
        };

        const btn = document.querySelector('button[onclick*="proceedToPayment"]');
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Rescheduling...';

        try {
            const response = await fetch(`${this.APPOINTMENT_SERVICE_URL}/api/appointments/${this.appointmentId}/reschedule`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const err = await response.json().catch(() => ({}));
                throw new Error(err.message || 'Failed to reschedule appointment');
            }

            alert('Appointment rescheduled successfully!');
            window.location.href = 'dashboard.html';
        } catch (e) {
            console.error('Reschedule error:', e);
            alert('Failed to reschedule appointment: ' + e.message);
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    }

    async submit() {
        if (this.editMode) {
            return this.rescheduleAppointment();
        }
        return this.bookAppointment();
    }

    getSelectedEmployeeName() {
        const selected = this.employees.find(e => String(e.id) === String(this.selectedEmployeeId));
        return selected ? (selected.name || `Employee ${selected.id}`) : '';
    }

    // Ensure time in HH:mm:ss for backend
    formatToHHMMSS(timeStr) {
        if (!timeStr) return null;
        const s = String(timeStr).trim();
        // Already HH:mm:ss
        if (/^\d{1,2}:\d{2}:\d{2}$/.test(s)) return s;
        // 12-hour like h:mm AM/PM
        const ampm = s.match(/^(\d{1,2}):(\d{2})\s*(AM|PM)$/i);
        if (ampm) {
            let hh = parseInt(ampm[1], 10);
            const mm = ampm[2];
            const mer = ampm[3].toUpperCase();
            if (mer === 'PM' && hh !== 12) hh += 12;
            if (mer === 'AM' && hh === 12) hh = 0;
            return `${String(hh).padStart(2,'0')}:${mm}:00`;
        }
        // HH:mm → add seconds
        const short = s.match(/^(\d{1,2}):(\d{2})$/);
        if (short) {
            return `${short[1].padStart(2,'0')}:${short[2]}:00`;
        }
        // Fallback: if contains exactly two colons but ends with ':00:00', normalize first 2 parts
        const parts = s.split(':');
        if (parts.length >= 2) {
            const hh = String(parseInt(parts[0],10)).padStart(2,'0');
            const mm = parts[1].padStart(2,'0');
            const ss = parts[2] ? parts[2].padStart(2,'0') : '00';
            return `${hh}:${mm}:${ss}`;
        }
        // Last resort, return 00 seconds
        return `${s}:00`;
    }

    // Pretty print time for UI (remove trailing :00 seconds)
    prettyTime(timeStr) {
        const s = String(timeStr || '').trim();
        if (/^\d{1,2}:\d{2}:\d{2}$/.test(s)) {
            // strip seconds when :00
            if (s.endsWith(':00')) return s.slice(0,5);
            return s;
        }
        return s;
    }

    showError(message) {
        document.querySelector('.booking-container').innerHTML = `
            <div class="p-4">
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>${message}
                </div>
                <a href="index.html" class="btn btn-primary">
                    <i class="fas fa-home me-2"></i>Back to Home
                </a>
            </div>
        `;
    }
}

// Initialize
let bookingPageManager;
document.addEventListener('DOMContentLoaded', async function() {
    const auth = AuthGuard.requireAuth();
    if (!auth) return;

    bookingPageManager = new BookingPageManager();
    await bookingPageManager.init();
});

// Global function for button onclick
function proceedToPayment() {
    if (bookingPageManager) {
        bookingPageManager.submit();
    }
}
