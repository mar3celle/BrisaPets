document.addEventListener('DOMContentLoaded', () => {





    // --- FUNÇÕES DO DASHBOARD (renderCharts e initDashboard) ---

    function renderCharts(data) {
        const ctx = document.getElementById('myChart');

        // Destruir a instância anterior do gráfico, se existir
        if (chartInstance) {
            chartInstance.destroy();
        }

        chartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'],
                datasets: [{
                    label: 'Serviços Diários',
                    data: data.dailyServices,
                    backgroundColor: 'rgba(254, 135, 160, 0.7)', // Cor primária do tema
                    borderColor: 'rgba(254, 135, 160, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    title: {
                        display: true,
                        text: 'Serviços Agendados na Última Semana'
                    }
                }
            }
        });
    }

    async function initDashboard() {
        const data = await fetchData('dashboard');

        // 1. Atualizar métricas (se os elementos HTML existirem)
        const metricUsers = document.getElementById('metric-users');
        if (metricUsers) metricUsers.textContent = data.users.toLocaleString('pt-PT');

        const metricAppointments = document.getElementById('metric-appointments');
        if (metricAppointments) metricAppointments.textContent = data.appointments.toLocaleString('pt-PT');

        const metricRevenue = document.getElementById('metric-revenue');
        if (metricRevenue) metricRevenue.textContent = `€ ${data.revenue.toLocaleString('pt-PT', { minimumFractionDigits: 2 })}`;

        // 2. Renderizar Gráfico
        if (document.getElementById('myChart')) {
            renderCharts(data);
        }
    }


    // --- FUNÇÕES DO CALENDÁRIO (Appointments/Sitting) ---

    // Polyfill simples para YearMonth (para simular a funcionalidade de um objeto real)
    function YearMonth(year, month) {
        this.date = new Date(year, month - 1, 1);
        this.year = this.date.getFullYear();
        this.month = this.date.getMonth() + 1; // 1-12

        this.toLocaleString = function(locale, options) {
            return this.date.toLocaleString(locale, options);
        };
        this.lengthOfMonth = function() {
            return new Date(this.year, this.month, 0).getDate();
        };
        this.atDay = function(day) {
            // Retorna um objeto com getDay() que simula LocalDate.getDayOfWeek().getValue() de 0 (Dom) a 6 (Sáb)
            return { getDay: () => new Date(this.year, this.month - 1, day).getDay() };
        };
        this.plusMonths = function(months) {
            const newDate = new Date(this.date);
            newDate.setMonth(newDate.getMonth() + months);
            return new YearMonth(newDate.getFullYear(), newDate.getMonth() + 1);
        };
        this.minusMonths = function(months) {
            return this.plusMonths(-months);
        };
    }

    // Corrigir a inicialização do estado global para usar o YearMonth "polyfill"
    currentYearMonth = new YearMonth(currentYearMonth.getFullYear(), currentYearMonth.getMonth() + 1);


    function initCalendar() {
        // Inicializa o calendário com o mês atual
        renderCalendar(currentYearMonth);
    }

    function renderCalendar(yearMonth) {
        // Atualiza o estado global
        currentYearMonth = yearMonth;

        const calendarElement = document.getElementById('calendar-grid');
        const monthYearDisplay = document.getElementById('month-year-display');

        if (!calendarElement || !monthYearDisplay) {
            return; // Sai se os elementos não existirem na página atual
        }

        // Formato do mês: "Novembro 2025"
        monthYearDisplay.textContent = yearMonth.toLocaleString('pt-PT', { month: 'long', year: 'numeric' });

        // Calcula os dias do calendário (simulação simples)
        const daysInMonth = yearMonth.lengthOfMonth();
        const firstDayOfMonth = yearMonth.atDay(1).getDay(); // 0 (Dom) a 6 (Sáb)

        // Limpa o calendário anterior
        calendarElement.innerHTML = '';

        // Adiciona células vazias para o padding (garante que 1º dia comece no dia da semana correto)
        // Adjust for Monday start (0=Sun, 1=Mon... 6=Sat). We want Monday=0, Sunday=6 padding.
        const startDay = (firstDayOfMonth === 0 ? 6 : firstDayOfMonth - 1);

        for (let i = 0; i < startDay; i++) {
            const emptyDay = document.createElement('div');
            emptyDay.classList.add('calendar-day-box', 'empty');
            calendarElement.appendChild(emptyDay);
        }

        // Adiciona as células do dia
        for (let day = 1; day <= daysInMonth; day++) {
            const dayElement = document.createElement('div');
            dayElement.classList.add('calendar-day-box');
            dayElement.innerHTML = `<div class="day-number">${day}</div><div class="appointment-list"></div>`;

            // Simulação: Adiciona um evento em alguns dias para demonstração
            if (day % 5 === 0) {
                 const list = dayElement.querySelector('.appointment-list');
                 list.innerHTML += `<div class="appointment-item bg-primary-light">Banho - Tobby</div>`;
            }
            if (day % 7 === 1) {
                 const list = dayElement.querySelector('.appointment-list');
                 list.innerHTML += `<div class="appointment-item bg-secondary-light">Pet Sit - Kiko</div>`;
            }


            calendarElement.appendChild(dayElement);
        }
    }

    function nextMonth() {
        const nextMonthYear = currentYearMonth.plusMonths(1);
        renderCalendar(nextMonthYear);
    }

    function prevMonth() {
        const prevMonthYear = currentYearMonth.minusMonths(1);
        renderCalendar(prevMonthYear);
    }

    // --- INICIALIZAÇÃO DE LISTENERS ---




    // -------------------------------------------------------------------------
    // --- LÓGICA DE UI GERAL (Sidebar Toggle, Margin, Search Dropdown) ---
    // -------------------------------------------------------------------------

    // Toggle do menu em mobile/tablet
    const sidebarToggle = document.getElementById('sidebar-toggle');
    const sidebar = document.getElementById('sidebar');
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // Ajusta margem do conteúdo principal conforme largura
    const mainWrapper = document.querySelector('.flex-1') || document.querySelector('.main-wrapper') || document.getElementById('main-content')?.parentElement;
    function adjustMainMargin(){
        if (!mainWrapper) return;
        // Se a sidebar estiver visível na desktop (>= 1024px), adiciona a margem
        if(window.innerWidth >= 1024){
            mainWrapper.style.marginLeft = '280px';
        } else {
            mainWrapper.style.marginLeft = '0';
        }
    }
    adjustMainMargin();
    window.addEventListener('resize', adjustMainMargin);

    // Search dropdown: observa alterações no container de resultados para mostrar/ocultar
    const petSearch = document.getElementById('pet-search');
    const resultsDiv = document.getElementById('search-results');
    if (resultsDiv && petSearch) {
        // Inicializa o dropdown com display: none (será controlado pelo observer/classe 'show')
        resultsDiv.style.display = 'none';

        // Observador para verificar se há resultados e mostrar/esconder o dropdown
        const observer = new MutationObserver(() => {
            // Verifica se há elementos filhos DENTRO do resultsDiv E se o input tem pelo menos 2 caracteres
            if(resultsDiv.children.length > 0 && petSearch.value.trim().length >= 2){
                resultsDiv.classList.add('show');
                resultsDiv.style.display = 'block';
            } else {
                resultsDiv.classList.remove('show');
                resultsDiv.style.display = 'none';
            }
        });
        observer.observe(resultsDiv, { childList: true, subtree: true });

        // Fechar dropdown quando clicam fora
        document.addEventListener('click', (e) => {
            // Se o clique não foi dentro da div de resultados E não foi no input de pesquisa
            if (!resultsDiv.contains(e.target) && e.target !== petSearch) {
                resultsDiv.classList.remove('show');
                resultsDiv.style.display = 'none';
            }
        });

        // Adiciona um listener para o input para simular a pesquisa
        petSearch.addEventListener('input', async (e) => {
            const query = e.target.value.trim();
            resultsDiv.innerHTML = ''; // Limpa resultados

            if (query.length >= 2) {
                // Simulação de pesquisa de pets
                // TODO: Chamar API real para pesquisa
                const pets = [
                    { id: 1, name: 'Tobby' },
                    { id: 2, name: 'Max' },
                    { id: 3, name: 'Kiko' }
                ];

                const filteredPets = pets.filter(pet =>
                    pet.name.toLowerCase().includes(query.toLowerCase())
                );

                filteredPets.forEach(pet => {
                    const resultItem = document.createElement('a');
                    resultItem.href = `/admin/pets/${pet.id}`;
                    resultItem.classList.add('search-result-item', 'p-3', 'hover:bg-gray-50', 'block');
                    resultItem.textContent = pet.name;
                    resultsDiv.appendChild(resultItem);
                });
            }
        });
    }


    // -------------------------------------------------------------------------
    // --- FUNÇÃO DE ALERTA (Para mensagens flash do servidor) ---
    // -------------------------------------------------------------------------

    /**
     * Exibe uma mensagem de alerta temporária no topo do dashboard.
     */
    function alertMessage(msg, type) {
        const container = document.querySelector('.dashboard-container') || document.querySelector('.main-content-wrapper');
        if (!container) return;

        let messageBox = document.querySelector('#js-admin-alert');
        if (!messageBox) {
            messageBox = document.createElement('div');
            messageBox.id = 'js-admin-alert';
            messageBox.style.cssText = `
                padding: 15px;
                margin-bottom: 20px;
                border-radius: 8px;
                font-weight: 600;
                display: none;
                text-align: center;
                transition: opacity 0.3s;
                opacity: 0;
            `;
            // Tenta adicionar logo abaixo do cabeçalho principal
            const contentArea = document.getElementById('main-content');
            if(contentArea && contentArea.parentElement) {
                 contentArea.parentElement.insertBefore(messageBox, contentArea);
            } else {
                 container.prepend(messageBox);
            }
        }

        messageBox.textContent = msg;
        messageBox.style.opacity = 0;
        messageBox.style.display = 'block';

        if (type === 'success') {
            messageBox.style.backgroundColor = '#d4edda';
            messageBox.style.color = '#155724';
            messageBox.style.border = '1px solid #c3e6cb';
        } else if (type === 'error') {
            messageBox.style.backgroundColor = '#f8d7da';
            messageBox.style.color = '#721c24';
            messageBox.style.border = '1px solid #f5c6cb';
        } else { // info ou default
             messageBox.style.backgroundColor = '#cce5ff';
             messageBox.style.color = '#004085';
             messageBox.style.border = '1px solid #b8daff';
        }

        // Fade in
        setTimeout(() => {
            messageBox.style.opacity = 1;
        }, 10);

        // Esconde a mensagem após 4 segundos
        setTimeout(() => {
            messageBox.style.opacity = 0;
            // Espera a transição acabar para esconder
            setTimeout(() => {
                 messageBox.style.display = 'none';
            }, 300);
        }, 4000);
    }

    // Processa mensagens flash
    const adminMessageElement = document.getElementById('flash-admin-message');
    if (adminMessageElement && adminMessageElement.value) {
        alertMessage(adminMessageElement.value, 'success');
    }
    const adminErrorElement = document.getElementById('flash-admin-error');
    if (adminErrorElement && adminErrorElement.value) {
        alertMessage(adminErrorElement.value, 'error');
    }

});