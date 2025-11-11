
document.addEventListener('DOMContentLoaded', function() {
    const services = document.querySelectorAll('.service-option');
    const days = document.querySelectorAll('.day-number:not(.inactive)');
    const times = document.querySelectorAll('.time-slot');
    const confirmBtn = document.getElementById('confirmAppointmentBtn');
    const confirmButtonText = document.getElementById('confirmButtonText');

    const petSelect = document.getElementById('petId');
    const dateTimeTitle = document.getElementById('dateTimeTitle');
    const dateRangeContainer = document.getElementById('dateRangeContainer');
    const observationsContainer = document.getElementById('observationsContainer');
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const calendarGrid = document.getElementById('calendarGrid');
    const timeSlots = document.getElementById('timeSlots');

    const serviceInput = document.getElementById('serviceName');
    const dateInput = document.getElementById('selectedDate');
    const timeInput = document.getElementById('selectedTime');

    let selectedService = null;
    let selectedDate = null;
    let selectedTime = null;
    let selectedPetId = petSelect.value;
    let isDateRangeService = false;

    function updateConfirmButton() {
        let isValid = false;
        
        if (selectedPetId && selectedService) {
            if (isDateRangeService) {
                // For date range services, only need start date
                const startDate = startDateInput.value;
                isValid = startDate !== '';
            } else {
                // For regular services, need date and time
                isValid = selectedDate && selectedTime;
            }
        }
        
        confirmBtn.disabled = !isValid;
    }
    
    function toggleServiceUI(serviceName) {
        isDateRangeService = serviceName === 'Pet Sitting' || serviceName === 'Hosting';
        
        if (isDateRangeService) {
            // Show date range and observations
            dateTimeTitle.innerHTML = '<i class="fas fa-calendar-alt"></i> Escolher Período';
            dateRangeContainer.style.display = 'block';
            observationsContainer.style.display = 'block';
            calendarGrid.style.display = 'none';
            timeSlots.style.display = 'none';
            confirmButtonText.textContent = 'Confirmar Reserva';
            
            // Set minimum date to today
            const today = new Date().toISOString().split('T')[0];
            startDateInput.min = today;
            endDateInput.min = today;
        } else {
            // Show regular calendar and time slots
            dateTimeTitle.innerHTML = '<i class="fas fa-calendar-alt"></i> Escolher Data e Hora';
            dateRangeContainer.style.display = 'none';
            observationsContainer.style.display = 'none';
            calendarGrid.style.display = 'grid';
            timeSlots.style.display = 'flex';
            confirmButtonText.textContent = 'Confirmar Agendamento';
        }
        
        // Reset selections
        selectedDate = null;
        selectedTime = null;
        dateInput.value = '';
        timeInput.value = '';
        startDateInput.value = '';
        endDateInput.value = '';
        days.forEach(d => d.classList.remove('is-selected'));
        times.forEach(t => t.classList.remove('is-selected'));
        
        updateConfirmButton();
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
            
            // Toggle UI based on service type
            toggleServiceUI(selectedService);

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
            
            // Fetch available time slots for selected date
            fetchAvailableSlots(selectedDate);

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

    // Date range validation
    startDateInput.addEventListener('change', function() {
        const startDate = this.value;
        if (startDate) {
            endDateInput.min = startDate;
            if (endDateInput.value && endDateInput.value < startDate) {
                endDateInput.value = startDate;
            }
        }
        updateConfirmButton();
    });
    
    endDateInput.addEventListener('change', function() {
        updateConfirmButton();
    });

    updateConfirmButton();
    
    // Function to fetch and display available time slots
    function fetchAvailableSlots(date) {
        fetch(`/api/availability?date=${date}`)
            .then(response => response.json())
            .then(availableSlots => {
                const timeSlotsContainer = document.getElementById('timeSlots');
                timeSlotsContainer.innerHTML = '';
                
                availableSlots.forEach(time => {
                    const button = document.createElement('button');
                    button.type = 'button';
                    button.className = 'time-slot active-time';
                    button.setAttribute('data-time-value', time);
                    button.textContent = time;
                    
                    button.addEventListener('click', function() {
                        document.querySelectorAll('.time-slot').forEach(t => t.classList.remove('is-selected'));
                        this.classList.add('is-selected');
                        selectedTime = this.getAttribute('data-time-value');
                        timeInput.value = selectedTime;
                        updateConfirmButton();
                    });
                    
                    timeSlotsContainer.appendChild(button);
                });
            })
            .catch(error => console.error('Error fetching available slots:', error));
    }
});