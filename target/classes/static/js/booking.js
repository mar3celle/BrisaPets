
document.addEventListener('DOMContentLoaded', function() {
    const services = document.querySelectorAll('.service-option');
    const days = document.querySelectorAll('.day-number:not(.inactive)');
    const times = document.querySelectorAll('.time-slot');
    const confirmBtn = document.getElementById('confirmAppointmentBtn');

    const petSelect = document.getElementById('petId'); // Adicionado

    const serviceInput = document.getElementById('serviceName');
    const dateInput = document.getElementById('selectedDate');
    const timeInput = document.getElementById('selectedTime');

    let selectedService = null;
    let selectedDate = null;
    let selectedTime = null;
    let selectedPetId = petSelect.value; // Inicializa com o valor selecionado

    function updateConfirmButton() {
        // Habilita o botão apenas se o Pet, Serviço, Data e Hora estiverem selecionados
        if (selectedService && selectedDate && selectedTime && selectedPetId) {
            confirmBtn.disabled = false;
        } else {
            confirmBtn.disabled = true;
        }
    }

    // --- 1. Lógica para selecionar o Pet ---
    petSelect.addEventListener('change', function() {
        selectedPetId = this.value;
        updateConfirmButton();
    });

    // --- 2. Lógica para selecionar o Serviço ---
    services.forEach(service => {
        service.addEventListener('click', function() {
            // Remove a seleção de todos
            services.forEach(s => s.classList.remove('is-selected'));

            // Adiciona a seleção no item clicado
            this.classList.add('is-selected');

            // Salva o valor no input oculto
            selectedService = this.getAttribute('data-service-name');
            serviceInput.value = selectedService;

            updateConfirmButton();
        });
    });

    // --- 3. Lógica para selecionar o Dia ---
    days.forEach(day => {
        day.addEventListener('click', function() {
            // Remove a seleção de todos os dias ativos
            days.forEach(d => d.classList.remove('is-selected'));

            // Adiciona a seleção no dia clicado
            this.classList.add('is-selected');

            // Salva o valor da data (YYYY-MM-DD) no input oculto
            selectedDate = this.getAttribute('data-date-value');
            dateInput.value = selectedDate;

            // Reseta a hora, forçando o usuário a re-selecionar
            selectedTime = null;
            timeInput.value = '';
            times.forEach(t => t.classList.remove('is-selected'));

            updateConfirmButton();
        });
    });

    // --- 4. Lógica para selecionar a Hora ---
    times.forEach(time => {
        time.addEventListener('click', function() {
            // Remove a seleção de todas as horas
            times.forEach(t => t.classList.remove('is-selected'));

            // Adiciona a seleção na hora clicada
            this.classList.add('is-selected');

            // Salva o valor da hora (HH:MM) no input oculto
            selectedTime = this.getAttribute('data-time-value');
            timeInput.value = selectedTime;

            updateConfirmButton();
        });
    });

    // Inicializa o estado do botão
    // Se a página for carregada com um pet já selecionado
    if (petSelect.value) {
        selectedPetId = petSelect.value;
    } else {
        selectedPetId = null;
    }

    // Adiciona uma opção default vazia se nenhum pet estiver selecionado, para forçar a escolha.
    if (!selectedPetId && petSelect.options.length > 1) {
        petSelect.value = "";
    }

    updateConfirmButton();
});