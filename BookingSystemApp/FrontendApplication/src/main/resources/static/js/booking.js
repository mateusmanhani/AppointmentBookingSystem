// Booking page functionality
document.addEventListener('DOMContentLoaded', function() {
    // Time slot selection
    const timeSlots = document.querySelectorAll('.time-slot');
    const selectedTimeElement = document.getElementById('selectedTime');

    timeSlots.forEach(slot => {
        slot.addEventListener('click', function() {
            // Remove selected class from all slots
            timeSlots.forEach(s => s.classList.remove('selected', 'btn-primary'));
            timeSlots.forEach(s => s.classList.add('btn-outline-primary'));

            // Add selected class to clicked slot
            this.classList.remove('btn-outline-primary');
            this.classList.add('btn-primary', 'selected');

            // Update selected time display
            selectedTimeElement.textContent = this.dataset.time;
        });
    });

    // Mini calendar functionality
    const miniDays = document.querySelectorAll('.mini-day');
    miniDays.forEach(day => {
        day.addEventListener('click', function() {
            miniDays.forEach(d => d.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Date change handler
    document.getElementById('appointmentDate').addEventListener('change', function() {
        // Update available time slots based on selected date
        console.log('Date changed to:', this.value);
        // Add your logic to fetch available slots for the selected date
    });

    // Employee change handler
    document.getElementById('employeeSelect').addEventListener('change', function() {
        // Update available time slots based on selected employee
        console.log('Employee changed to:', this.value);
        // Add your logic to fetch available slots for the selected employee
    });
});

// Proceed to payment
function proceedToPayment() {
    const selectedTime = document.querySelector('.time-slot.selected');

    if (!selectedTime) {
        alert('Please select a time slot');
        return;
    }


    // Helper: convert 12-hour time (e.g. "7:30 PM") to 24-hour "HH:mm"
    function to24Hour(time12) {
        if (!time12) return null;
        const m = time12.match(/^(\d{1,2}):(\d{2})\s*(AM|PM)?$/i);
        if (!m) return time12; // assume already HH:mm
        let hh = parseInt(m[1], 10);
        const mm = m[2];
        const ampm = m[3] ? m[3].toUpperCase() : null;
        if (ampm) {
            if (ampm === 'PM' && hh !== 12) hh += 12;
            if (ampm === 'AM' && hh === 12) hh = 0;
        }
        return String(hh).padStart(2, '0') + ':' + mm;
    }

    // Get appointment details and build ISO LocalDateTime string
    const date = document.getElementById('appointmentDate').value; // YYYY-MM-DD
    const time24 = to24Hour(selectedTime.dataset.time); // HH:mm
    const appointmentDateTime = date + 'T' + time24 + ':00'; // e.g. 2025-11-17T19:30:00

    const appointmentData = {
        service: 'Haircut & Styling',
        employee: document.getElementById('employeeSelect').value,
        appointmentDateTime: appointmentDateTime,
        price: 45.00
    };

    // Store appointment data for payment page
    localStorage.setItem('pendingAppointment', JSON.stringify(appointmentData));

    // Redirect to payment page
    window.location.href = 'payment.html';
}
