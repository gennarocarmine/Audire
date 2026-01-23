document.addEventListener('DOMContentLoaded', function() {

    const searchInput = document.getElementById('cdSearchInput');
    const suggestionsBox = document.getElementById('suggestionsList');
    const hiddenIdInput = document.getElementById('selectedUserId');
    const form = document.getElementById('addMemberForm');
    const errorMsg = document.getElementById('selectionError');

    if (!searchInput || typeof availableDirectors === 'undefined') return;

    searchInput.addEventListener('input', function() {
        const query = this.value.toLowerCase().trim();

        hiddenIdInput.value = "";

        suggestionsBox.innerHTML = '';
        errorMsg.style.display = 'none';

        if (query.length === 0) {
            suggestionsBox.style.display = 'none';
            return;
        }

        const matches = availableDirectors.filter(director => {
            return director.fullName.toLowerCase().includes(query) ||
                director.email.toLowerCase().includes(query);
        });

        if (matches.length > 0) {
            suggestionsBox.style.display = 'block';

            matches.forEach(match => {
                const div = document.createElement('div');
                div.classList.add('suggestion-item');

                div.innerHTML = `
                    <span class="suggestion-name">${match.fullName}</span>
                    <span class="suggestion-email">${match.email}</span>
                `;

                div.addEventListener('click', function() {
                    searchInput.value = match.fullName;
                    hiddenIdInput.value = match.id;
                    suggestionsBox.style.display = 'none';
                    suggestionsBox.innerHTML = '';
                });

                suggestionsBox.appendChild(div);
            });
        } else {
            suggestionsBox.style.display = 'block';
            suggestionsBox.innerHTML = '<div class="suggestion-item" style="cursor:default; color:#999;">Nessun risultato</div>';
        }
    });

    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !suggestionsBox.contains(e.target)) {
            suggestionsBox.style.display = 'none';
        }
    });

    form.addEventListener('submit', function(e) {
        if (hiddenIdInput.value === "") {
            e.preventDefault();
            errorMsg.style.display = 'block';
            searchInput.focus();
        }
    });
});