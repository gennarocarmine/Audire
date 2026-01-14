document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('registrationForm');
    const errorContainer = document.getElementById('errorContainer');
    const roleSelect = document.getElementById('role');
    const performerFields = document.getElementById('performerFields');
    const passwordInput = document.getElementById('password');
    const confirmInput = document.getElementById('confirmPassword');

    FormUtils.initPasswordToggles();
    FormUtils.initPhoneInput('phoneNumber');

    const customValidationLogic = (input) => {
        if (input.id === 'confirmPassword') {
            return input.value === passwordInput.value;
        }

        if (input.id === 'phoneNumber') {
            return input.value.length === 10;
        }
        return true;
    };

    FormUtils.bindLiveValidation(form, customValidationLogic);

    form.addEventListener('submit', function(e) {
        FormUtils.clearErrors(errorContainer);
        let errorMessages = [];
        let formIsValid = true;

        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            let isFieldValid = input.checkValidity() && customValidationLogic(input);

            if(input.id === 'confirmPassword' && input.value !== passwordInput.value) isFieldValid = false;

            if (input.offsetParent !== null && !isFieldValid) {
                formIsValid = false;
                input.classList.add('invalid-field');
            }
        });


        if (!formIsValid) {
            errorMessages.push("Compila correttamente i campi evidenziati.");
        }

        if (passwordInput.value !== confirmInput.value) {
            errorMessages.push("Le password non coincidono.");
        }

        const phoneInput = document.getElementById('phoneNumber');
        if (phoneInput && phoneInput.value.length !== 10) {
            errorMessages.push("Il numero di telefono deve essere composto da 10 cifre.");
        }

        // Performer File Check (Opzionale/Warning)
        /* if (roleSelect.value === 'Performer') {
             // Logica specifica se vuoi forzare upload qui
        }
        */


        if (!formIsValid || errorMessages.length > 0) {
            e.preventDefault();
            // Rimuovi duplicati dai messaggi
            const uniqueErrors = [...new Set(errorMessages)];
            FormUtils.showErrors(errorContainer, uniqueErrors);
            return false;
        }

        const btn = form.querySelector('button[type="submit"]');
        FormUtils.setLoadingButton(btn, true);
    });


    function togglePerformer() {
        if (roleSelect.value === 'Performer') {
            performerFields.style.display = 'block';

            if(document.getElementById('cvFile')) {
                document.getElementById('cvFile').setAttribute('required', 'required');
            }

            setTimeout(() => performerFields.scrollIntoView({behavior: 'smooth', block: 'nearest'}), 100);
        } else {
            performerFields.style.display = 'none';

            if(document.getElementById('cvFile')) {
                document.getElementById('cvFile').removeAttribute('required');
            }

            const hiddenInputs = performerFields.querySelectorAll('input, select, textarea');
            hiddenInputs.forEach(i => i.value = '');
        }
    }
    roleSelect.addEventListener('change', togglePerformer);
    if (roleSelect.value === 'Performer') togglePerformer();
});