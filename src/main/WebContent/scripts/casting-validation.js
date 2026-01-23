document.addEventListener('DOMContentLoaded', function() {
    const deadlineInput = document.getElementById('deadline');
    const form = document.getElementById('castingForm');

    let minDateIso = '';
    if (deadlineInput) {
        const minDateObj = new Date();
        minDateObj.setDate(minDateObj.getDate() + 7);
        minDateIso = minDateObj.toISOString().split('T')[0];
        deadlineInput.setAttribute('min', minDateIso);
    }

    const validateCastingRules = function(input) {
        const val = input.value.trim();

        if (input.id === 'title') {
            if (val.length === 0) {
                return { isValid: false, message: "Il titolo è obbligatorio." };
            }
            if (val.length < 5) {
                return { isValid: false, message: "Il titolo deve contenere almeno 5 caratteri." };
            }
        }

        if (input.id === 'deadline') {
            if (!val) {
                return { isValid: false, message: "Inserisci una data di scadenza." };
            }
            if (val < minDateIso) {
                return { isValid: false, message: "La scadenza deve essere almeno tra 7 giorni." };
            }
        }

        if (input.id === 'description') {
            if (val.length === 0) {
                return { isValid: false, message: "La descrizione è obbligatoria." };
            }
            if (val.length < 20) {
                return { isValid: false, message: `Descrizione troppo breve (mancano ${20 - val.length} caratteri).` };
            }
        }

        if (input.required && val === '') {
            return { isValid: false, message: "Questo campo è obbligatorio." };
        }

        return { isValid: true };
    };

    if (form && typeof FormUtils !== 'undefined') {
        FormUtils.bindLiveValidation(form, validateCastingRules);
    }
});