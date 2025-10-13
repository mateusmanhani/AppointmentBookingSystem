// Payment form handling
document.addEventListener('DOMContentLoaded', function() {
    // Card number formatting
    const cardNumberInput = document.getElementById('cardNumber');
    cardNumberInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\s/g, '').replace(/[^0-9]/gi, '');
        let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value;
        e.target.value = formattedValue;
    });

    // Expiry date formatting
    const expiryInput = document.getElementById('expiryDate');
    expiryInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        let formattedValue = value.replace(/(\d{2})(\d{2})/, '$1/$2');
        e.target.value = formattedValue;
    });

    // CVC formatting
    const cvcInput = document.getElementById('cvc');
    cvcInput.addEventListener('input', function(e) {
        e.target.value = e.target.value.replace(/[^0-9]/g, '');
    });

    // Payment method selection
    const paymentOptions = document.querySelectorAll('.payment-option');
    paymentOptions.forEach(option => {
        option.addEventListener('click', function() {
            paymentOptions.forEach(opt => opt.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Form submission
    document.getElementById('paymentForm').addEventListener('submit', function(e) {
        e.preventDefault();

        // Simulate payment processing
        const submitBtn = this.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Processing...';

        setTimeout(() => {
            alert('Payment processed successfully! Redirecting to dashboard...');
            window.location.href = 'dashboard.html';
        }, 3000);
    });
});
