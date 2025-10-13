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

    // Get appointment details
    const appointmentData = {
        service: 'Haircut & Styling',
        employee: document.getElementById('employeeSelect').value,
        date: document.getElementById('appointmentDate').value,
        time: selectedTime.dataset.time,
        price: 45.00
    };

    // Store appointment data for payment page
    localStorage.setItem('pendingAppointment', JSON.stringify(appointmentData));

    // Redirect to payment page
    window.location.href = 'payment.html';
}
