document.addEventListener('DOMContentLoaded', function() {
    const deadlineInput = document.getElementById('deadline');
    const form = document.getElementById('castingForm');

    if (deadlineInput) {
        const minDateObj = new Date();
        minDateObj.setDate(minDateObj.getDate() + 7);

        // Formatta in YYYY-MM-DD
        const minDateIso = minDateObj.toISOString().split('T')[0];
        deadlineInput.setAttribute('min', minDateIso);
    }

    if (form && typeof FormUtils !== 'undefined') {
        FormUtils.bindLiveValidation(form);
    }
});