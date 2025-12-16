/**
 * Brisa Pets - Booking JavaScript
 * Funcionalidades específicas para agendamento
 */

document.addEventListener('DOMContentLoaded', function() {
    initServiceSelection();
    initCalendarInteraction();
    initTimeSlotSelection();
    initFormValidation();
});

// Seleção de serviços
function initServiceSelection() {
    const serviceOptions = document.querySelectorAll('.service-option');
    const serviceNameInput = document.getElementById('serviceName');
    
    serviceOptions.forEach(option => {
        option.addEventListener('click', function() {
            // Remove seleção anterior
            serviceOptions.forEach(opt => opt.classList.remove('selected'));
            
            // Adiciona seleção atual
            this.classList.add('selected');
            
            // Define o serviço selecionado
            const serviceName = this.getAttribute('data-service-name');
            if (serviceNameInput) {
                serviceNameInput.value = serviceName;
            }
            
            // Efeito visual
            this.style.transform = 'scale(0.98)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
            
            updateFormValidation();
        });
    });
}

// Interação com calendário
function initCalendarInteraction() {
    const dayNumbers = document.querySelectorAll('.day-number:not(.inactive)');
    const selectedDateInput = document.getElementById('selectedDate');
    
    dayNumbers.forEach(day => {
        day.addEventListener('click', function() {
            // Remove seleção anterior
            dayNumbers.forEach(d => d.classList.remove('selected'));
            
            // Adiciona seleção atual
            this.classList.add('selected');
            
            // Define a data selecionada
            const dateValue = this.getAttribute('data-date-value');
            if (selectedDateInput) {
                selectedDateInput.value = dateValue;
            }
            
            // Efeito visual
            this.style.backgroundColor = 'var(--color-primary)';
            this.style.color = 'white';
            
            updateFormValidation();
        });
        
        // Efeito hover
        day.addEventListener('mouseenter', function() {
            if (!this.classList.contains('selected')) {
                this.style.backgroundColor = 'var(--color-secondary)';
            }
        });
        
        day.addEventListener('mouseleave', function() {
            if (!this.classList.contains('selected')) {
                this.style.backgroundColor = '';
            }
        });
    });
}

// Seleção de horários
function initTimeSlotSelection() {
    const timeSlots = document.querySelectorAll('.time-slot');
    const selectedTimeInput = document.getElementById('selectedTime');
    
    timeSlots.forEach(slot => {
        slot.addEventListener('click', function() {
            // Remove seleção anterior
            timeSlots.forEach(s => s.classList.remove('selected'));
            
            // Adiciona seleção atual
            this.classList.add('selected');
            
            // Define o horário selecionado
            const timeValue = this.getAttribute('data-time-value');
            if (selectedTimeInput) {
                selectedTimeInput.value = timeValue;
            }
            
            // Efeito visual
            this.style.backgroundColor = 'var(--color-primary)';
            this.style.color = 'white';
            
            updateFormValidation();
        });
    });
}

// Validação do formulário
function initFormValidation() {
    const form = document.getElementById('appointmentForm');
    const submitBtn = document.getElementById('confirmAppointmentBtn');
    
    if (form) {
        form.addEventListener('input', updateFormValidation);
        form.addEventListener('change', updateFormValidation);
    }
}

function updateFormValidation() {
    const petId = document.getElementById('petId')?.value;
    const serviceName = document.getElementById('serviceName')?.value;
    const selectedDate = document.getElementById('selectedDate')?.value;
    const selectedTime = document.getElementById('selectedTime')?.value;
    const submitBtn = document.getElementById('confirmAppointmentBtn');
    
    const isValid = petId && serviceName && selectedDate && selectedTime;
    
    if (submitBtn) {
        submitBtn.disabled = !isValid;
        submitBtn.style.opacity = isValid ? '1' : '0.6';
        
        if (isValid) {
            submitBtn.classList.add('pulse');
        } else {
            submitBtn.classList.remove('pulse');
        }
    }
}

// Adicionar estilos CSS específicos
const bookingStyles = document.createElement('style');
bookingStyles.textContent = `
    .service-option {
        cursor: pointer;
        transition: all 0.3s ease;
        border: 2px solid transparent;
    }
    
    .service-option:hover {
        border-color: var(--color-secondary);
        transform: translateY(-3px);
    }
    
    .service-option.selected {
        border-color: var(--color-primary);
        box-shadow: 0 0 0 3px rgba(254, 135, 160, 0.2);
    }
    
    .day-number {
        cursor: pointer;
        transition: all 0.2s ease;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .day-number.inactive {
        opacity: 0.3;
        cursor: not-allowed;
    }
    
    .time-slot {
        cursor: pointer;
        transition: all 0.2s ease;
        border: 2px solid #e0e0e0;
        background: white;
        color: #333;
    }
    
    .time-slot:hover {
        border-color: var(--color-primary);
        transform: translateY(-2px);
    }
    
    .time-slot.selected {
        background-color: var(--color-primary) !important;
        color: white !important;
        border-color: var(--color-primary);
    }
    
    .pulse {
        animation: pulse 2s infinite;
    }
    
    @keyframes pulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.05); }
        100% { transform: scale(1); }
    }
`;
document.head.appendChild(bookingStyles);