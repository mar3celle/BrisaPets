// --- ESTADO GLOBAL ---
let currentPage = 'dashboard';
let currentYearMonth = new Date();
let selectedDate = null;
let chartInstance = null;

// --- MOCK DATA (Dados de Exemplo) ---
const mockAppointments = [
    { id: 1, date: '2025-10-28', time: '10:00', pet: 'Max', tutor: 'Ana Silva', service: 'Banho e Tosquia', status: 'Novo' },
    { id: 2, date: '2025-10-28', time: '14:30', pet: 'Bobi', tutor: 'João Santos', service: 'Banho Rápido', status: 'Confirmado' },
    { id: 3, date: '2025-10-29', time: '09:00', pet: 'Luna', tutor: 'Maria Gomes', service: 'Pet Sitting', period: '29/10 - 02/11', status: 'Novo' },
    { id: 4, date: '2025-10-29', time: '16:00', pet: 'Simba', tutor: 'Carla Dias', service: 'Banho e Tosquia', status: 'Confirmado' },
    { id: 5, date: '2025-10-30', time: '11:00', pet: 'Mel', tutor: 'Pedro Sousa', service: 'Banho Rápido', status: 'Novo' },
];

const mockStats = {
    totalAppointments: 45,
    totalRevenue: 2890.50,
    sittingRevenue: 650.00,
    servicesUsage: {
        'Banho e Tosquia': 20,
        'Banho Rápido': 15,
        'Pet Sitting': 5,
        'Hospedagem': 5
    }
};

const mockReports = [
     { date: '2025-10-15', service: 'Banho e Tosquia', pet: 'Toby', tutor: 'Ana Silva', value: 35.00, status: 'Concluído' },
     { date: '2025-10-10', service: 'Pet Sitting (5 dias)', pet: 'Rex', tutor: 'Pedro Sousa', value: 150.00, status: 'Concluído' },
     { date: '2025-10-05', service: 'Banho Rápido', pet: 'Max', tutor: 'Ana Silva', value: 20.00, status: 'Concluído' },
];

const mockSittings = [
    { id: 101, pet: 'Simba', tutor: 'Carla Dias', period: '25/10/2025 - 30/10/2025', status: 'Ativo', photoCount: 3, photos: ['placeholder.png', 'placeholder.png', 'placeholder.png'] },
    { id: 102, pet: 'Rex', tutor: 'Pedro Sousa', period: '01/10/2025 - 05/10/2025', status: 'Expirado', photoCount: 10, photos: ['placeholder.png', 'placeholder.png', 'placeholder.png'] },
    { id: 103, pet: 'Kika', tutor: 'Mariana Lima', period: '28/10/2025 - 05/11/2025', status: 'Ativo', photoCount: 1, photos: ['placeholder.png'] },
];

const mockUsers = [
    { id: 201, name: 'Ana Silva', phone: '911 223 344', email: 'ana@example.com', pets: ['Max', 'Toby'] },
    { id: 202, name: 'João Santos', phone: '934 567 890', email: 'joao@example.com', pets: ['Bobi'] },
    { id: 203, name: 'Maria Gomes', phone: '960 123 456', email: 'maria@example.com', pets: ['Luna'] },
];

// --- FUNÇÕES DE NAVEGAÇÃO E RENDERIZAÇÃO ---

function navigate(page) {
    currentPage = page;
    renderPage();

    // Atualiza o estado visual do menu
    document.querySelectorAll('.sidebar-item').forEach(item => {
        item.classList.remove('active');
    });
    const navId = page === 'appointment_form' ? 'nav-dashboard' : `nav-${page}`;
    const activeNav = document.getElementById(navId);
    if (activeNav) {
        activeNav.classList.add('active');
    }

    // Esconde o menu em mobile após a navegação
    const sidebar = document.getElementById('sidebar');
    if (sidebar && sidebar.classList.contains('active')) {
        sidebar.classList.remove('active');
    }
}

function renderPage() {
    const mainContent = document.getElementById('main-content');
    let templateId;
    let title;

    switch (currentPage) {
        case 'dashboard':
            templateId = 'dashboard-template';
            title = 'Dashboard';
            break;
        case 'reports':
            templateId = 'reports-template';
            title = 'Relatórios de Serviços';
            break;
        case 'sitting':
            templateId = 'sitting-template';
            title = 'Diário Pet Sitting';
            break;
        case 'appointment_form':
            templateId = 'appointment-form-template';
            title = 'Adicionar Agendamento';
            break;
        case 'clients':
            templateId = 'clients-template';
            title = 'Clientes e Pets';
            break;
        default:
            templateId = 'dashboard-template';
            title = 'Dashboard';
    }

    // Define o título da página
    const pageTitleEl = document.getElementById('page-title');
    if (pageTitleEl) pageTitleEl.textContent = title;

    // Clona o template e renderiza
    const template = document.getElementById(templateId);
    if (template) {
        mainContent.innerHTML = '';
        const content = template.content.cloneNode(true);
        mainContent.appendChild(content);

        // Chama funções de inicialização específicas
        if (currentPage === 'dashboard') {
            initDashboard();
        } else if (currentPage === 'reports') {
            initReports();
        } else if (currentPage === 'sitting') {
            initSitting();
        } else if (currentPage === 'appointment_form') {
            initAppointmentForm();
        } else if (currentPage === 'clients') {
            initClients();
        }
    }
}

// --- LÓGICA ESPECÍFICA DAS PÁGINAS ---

// ########################### 1. DASHBOARD ###########################
function initDashboard() {
    renderAppointments();
    renderStats();
    createServicesChart();
}

function renderAppointments() {
    const list = document.getElementById('appointment-list');
    if (!list) return;
    list.innerHTML = '';

    const today = new Date().toISOString().split('T')[0];

    // Filtra agendamentos de hoje/futuro e ordena
    const upcoming = mockAppointments
        .filter(a => a.date >= today)
        .sort((a, b) => new Date(`${a.date} ${a.time}`) - new Date(`${b.date} ${b.time}`));

    if (upcoming.length === 0) {
         list.innerHTML = `<p class="text-muted italic p-4 card">Sem agendamentos futuros registados.</p>`;
         return;
    }

    upcoming.forEach(appt => {
        const isNew = appt.status === 'Novo';
        const badgeClass = isNew ? 'badge-new' : 'badge-confirmed';
        const badgeText = appt.status;

        const item = document.createElement('div');
        item.className = 'appointment-item';
        item.innerHTML = `
            <div class="left">
                <div class="title">${appt.time} - ${appt.pet} (<span style="font-weight:600;color:var(--color-primary)">${appt.tutor}</span>)</div>
                <div class="meta">${appt.service} | ${new Date(appt.date).toLocaleDateString('pt-PT', {day: '2-digit', month: 'short'})}</div>
            </div>
            <div class="right" style="display:flex;align-items:center;gap:0.75rem">
                <span class="badge ${badgeClass}">${badgeText}</span>
                ${isNew ? `<button onclick="confirmAppointment(${appt.id})" class="btn-confirm">Confirmar</button>` : ''}
            </div>
        `;
        list.appendChild(item);
    });
}

function confirmAppointment(id) {
    const appt = mockAppointments.find(a => a.id === id);
    if (appt && appt.status === 'Novo') {
        appt.status = 'Confirmado';
        renderAppointments(); // Redesenha a lista
        // substitui alerta por pequena notificação possivel (por enquanto alert)
        alert('Agendamento confirmado para o Pet: ' + appt.pet);
    }
}

function renderStats() {
    const statAppointments = document.getElementById('stat-appointments');
    const statRevenue = document.getElementById('stat-revenue');
    const statSitting = document.getElementById('stat-sitting-revenue');

    if (statAppointments) statAppointments.textContent = mockStats.totalAppointments;
    if (statRevenue) statRevenue.textContent = `€ ${mockStats.totalRevenue.toFixed(2)}`;
    if (statSitting) statSitting.textContent = `€ ${mockStats.sittingRevenue.toFixed(2)}`;
}

function createServicesChart() {
    if (chartInstance) {
        chartInstance.destroy();
    }

    const ctx = document.getElementById('servicesChart');
    if (!ctx) return;

    const data = mockStats.servicesUsage;
    const labels = Object.keys(data);
    const values = Object.values(data);

    chartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    '#fe87a0', // primary
                    '#ffb1c3', // secondary
                    '#a0d8ff',
                    '#f7a379'
                ],
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                },
                title: {
                    display: false,
                }
            }
        }
    });
}

// ########################### 2. RELATÓRIOS ###########################
function initReports() {
    const tableBody = document.getElementById('reports-table-body');
    if (!tableBody) return;
    tableBody.innerHTML = '';

    mockReports.forEach(report => {
        const row = document.createElement('tr');
        const statusColor = report.status === 'Concluído' ? 'text-green-600' : 'text-gray-500';

        row.innerHTML = `
            <td class="py-3 px-4 text-sm">${new Date(report.date).toLocaleDateString('pt-PT')}</td>
            <td class="py-3 px-4 text-sm">${report.service}</td>
            <td class="py-3 px-4 text-sm font-medium">${report.pet}</td>
            <td class="py-3 px-4 text-sm">${report.tutor}</td>
            <td class="py-3 px-4 text-right text-sm font-semibold text-gray-700">€ ${report.value.toFixed(2)}</td>
            <td class="py-3 px-4 text-center text-sm ${statusColor}">${report.status}</td>
        `;
        tableBody.appendChild(row);
    });
}

// ########################### 3. PET SITTING DIARY ###########################
function initSitting() {
    const filter = document.getElementById('sitting-filter');
    renderSittingSessions(filter ? filter.value : 'ativo');
    if (filter) {
        filter.addEventListener('change', (e) => {
            renderSittingSessions(e.target.value);
        });
    }
}

function renderSittingSessions(filter) {
    const list = document.getElementById('sitting-sessions-list');
    if (!list) return;
    list.innerHTML = '';

    const filteredSessions = mockSittings.filter(s => filter === 'all' || s.status.toLowerCase() === filter.toLowerCase());

    if (filteredSessions.length === 0) {
        list.innerHTML = `<p class="text-muted italic p-4 card">Nenhuma sessão ${filter} encontrada.</p>`;
        return;
    }

    filteredSessions.forEach(session => {
        const isExpired = session.status === 'Expirado';
        const statusClass = isExpired ? 'bg-gray-200 text-gray-700' : 'bg-green-100 text-green-700';

        const sessionElement = document.createElement('div');
        sessionElement.className = 'card p-4 border border-gray-100';
        sessionElement.innerHTML = `
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-4 border-b pb-3">
                <div>
                    <span class="px-3 py-1 text-xs font-semibold rounded-full ${statusClass}">${session.status}</span>
                    <h3 class="text-xl font-bold text-gray-800 mt-1">${session.pet} (<span class="font-normal" style="color:var(--color-primary)">${session.tutor}</span>)</h3>
                    <p class="text-sm text-muted"><i class="fas fa-calendar-alt mr-1"></i> Período: ${session.period}</p>
                </div>
                <div class="text-sm font-medium mt-3 sm:mt-0">${session.photoCount} Fotos</div>
            </div>

            <div class="gallery-grid mb-4" id="gallery-${session.id}">
            </div>

            <button onclick="deletePhotos(${session.id})" class="btn" style="background:#ef4444;color:#fff">
                <i class="fas fa-trash-alt mr-2"></i> Excluir Fotos Anteriores
            </button>
        `;
        list.appendChild(sessionElement);

        const gallery = sessionElement.querySelector(`#gallery-${session.id}`);
        session.photos.forEach(photoName => {
             const img = document.createElement('img');
             img.src = 'https://placehold.co/300x200/ffb1c3/333?text=Foto';
             img.alt = 'Foto do Pet';
             img.style.borderRadius = '8px';
             gallery.appendChild(img);
        });
    });
}

function deletePhotos(sessionId) {
    // Lógica para excluir as fotos (apenas mock aqui)
    alert(`Atenção: A função para excluir as fotos da sessão ${sessionId} foi acionada. (Implementação real de back-end necessária)`);
}

// ########################### 4. AGENDAR / ADICIONAR AGENDAMENTO ###########################
function initAppointmentForm() {
    renderCalendar();
    const serviceType = document.getElementById('service-type');
    const form = document.getElementById('appointment-details-form');
    if (serviceType) serviceType.addEventListener('change', toggleServiceType);
    if (form) form.addEventListener('submit', handleAppointmentSubmit);

    // se houver wrapper de busca, ativar event listeners
    const petSearch = document.getElementById('pet-search');
    const resultsDiv = document.getElementById('search-results');
    if (petSearch && resultsDiv) {
        petSearch.addEventListener('input', searchClient);
    }
}

function toggleServiceType(event) {
    const type = event.target.value;
    const timeSlot = document.getElementById('time-slot-container');
    const period = document.getElementById('period-container');
    if (timeSlot) timeSlot.classList.toggle('hidden', type === 'sitting');
    if (period) period.classList.toggle('hidden', type !== 'sitting');
}

function renderCalendar() {
    const today = new Date();
    const year = currentYearMonth.getFullYear();
    const month = currentYearMonth.getMonth(); // 0-11
    const monthNames = ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];

    const titleEl = document.getElementById('calendar-title');
    if (titleEl) titleEl.textContent = `${monthNames[month]} ${year}`;
    const calendarGrid = document.getElementById('calendar-grid');
    if (!calendarGrid) return;
    // remove only day elements (safer)
    calendarGrid.querySelectorAll('.day').forEach(d => d.remove());

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    // Padding (0=Dom, 1=Seg, ... 6=Sáb)
    let padding = firstDayOfMonth.getDay();

    // Adiciona espaços vazios
    for (let i = 0; i < padding; i++) {
        const empty = document.createElement('div');
        empty.className = 'calendar-day-box day day-disabled';
        calendarGrid.appendChild(empty);
    }

    // Adiciona dias
    for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
        const date = new Date(year, month, day);
        const dateString = date.toISOString().split('T')[0];
        const isPast = date < new Date(today.getFullYear(), today.getMonth(), today.getDate());
        const isSelected = selectedDate === dateString;

        let classes = 'calendar-day-box day text-lg font-medium';
        if (isPast) classes += ' day-disabled';
        if (isSelected) classes += ' day-selected';

        const newDayDiv = document.createElement('div');
        newDayDiv.className = classes;
        newDayDiv.dataset.date = dateString;
        newDayDiv.textContent = day;

        if (!isPast) {
            newDayDiv.onclick = () => selectDay(dateString);
        }
        calendarGrid.appendChild(newDayDiv);
    }
}

function changeMonth(delta) {
    currentYearMonth.setMonth(currentYearMonth.getMonth() + delta);
    renderCalendar();
}

function selectDay(dateString) {
    selectedDate = dateString;

    // Remove seleção anterior
    document.querySelectorAll('.day-selected').forEach(d => d.classList.remove('day-selected'));

    // Adiciona nova seleção
    const selectedElement = document.querySelector(`.day[data-date="${dateString}"]`);
    if (selectedElement) {
        selectedElement.classList.add('day-selected');
    }

    const date = new Date(dateString);
    const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    const selectedDisplay = document.getElementById('selected-date-display');
    if (selectedDisplay) {
        selectedDisplay.textContent =
            `Data Selecionada: ${date.toLocaleDateString('pt-PT', dateOptions)}`;
    }
}

function searchClient() {
    const petSearchEl = document.getElementById('pet-search');
    const resultsDiv = document.getElementById('search-results');
    if (!petSearchEl || !resultsDiv) return;

    const query = petSearchEl.value.trim().toLowerCase();
    resultsDiv.innerHTML = '';

    if (query.length < 2) {
        resultsDiv.classList.remove('show');
        return;
    }

    const results = mockUsers.filter(user =>
        user.name.toLowerCase().includes(query) ||
        user.pets.some(pet => pet.toLowerCase().includes(query))
    );

    if (results.length > 0) {
        resultsDiv.classList.add('show');
        results.forEach(user => {
            const resultItem = document.createElement('div');
            resultItem.className = 'p-2 border-b cursor-pointer hover:bg-gray-100 text-sm';
            resultItem.textContent = `${user.name} (Pets: ${user.pets.join(', ')})`;
            resultItem.onclick = () => selectClient(user);
            resultsDiv.appendChild(resultItem);
        });
    } else {
         resultsDiv.classList.add('show');
         resultsDiv.innerHTML = `<div class="p-2 text-sm text-muted italic">Nenhum cliente/pet encontrado.</div>`;
    }
}

function selectClient(user) {
    const petSearchEl = document.getElementById('pet-search');
    const resultsDiv = document.getElementById('search-results');
    if (petSearchEl) petSearchEl.value = user.name;
    if (resultsDiv) resultsDiv.classList.remove('show');
    // Aqui você poderia pré-preencher outros campos se necessário
    alert(`Cliente selecionado: ${user.name}.`);
}

function handleAppointmentSubmit(event) {
    event.preventDefault();

    if (!selectedDate) {
         alert('Por favor, selecione uma data no calendário.');
         return;
    }

    const petSearch = document.getElementById('pet-search') ? document.getElementById('pet-search').value : '';
    const serviceType = document.getElementById('service-type') ? document.getElementById('service-type').value : 'banho';

    // Mock de submissão
    alert(`Agendamento Criado!\nTipo: ${serviceType === 'banho' ? 'Banho/Tosquia' : 'Pet Sitting'}\nData: ${selectedDate}\nCliente: ${petSearch}`);

    // Navega de volta ao dashboard
    navigate('dashboard');
}

// ########################### 5. CLIENTES E PETS ###########################
function initClients() {
    const tableBody = document.getElementById('clients-table-body');
    if (!tableBody) return;
    tableBody.innerHTML = '';

    mockUsers.forEach(user => {
        const row = document.createElement('tr');
        row.className = 'border-b hover:bg-gray-50';
        row.innerHTML = `
            <td class="py-3 px-4 text-sm font-medium">${user.name}</td>
            <td class="py-3 px-4 text-sm">${user.email}</td>
            <td class="py-3 px-4 text-sm">${user.phone}</td>
            <td class="py-3 px-4 text-sm">${user.pets.join(', ')}</td>
            <td class="py-3 px-4 text-center text-sm">
                <button class="text-blue-500 hover:text-blue-700 mx-1"><i class="fas fa-edit"></i></button>
                <button class="text-red-500 hover:text-red-700 mx-1"><i class="fas fa-trash"></i></button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// --- INICIALIZAÇÃO E EVENT LISTENERS GERAIS ---
document.addEventListener('DOMContentLoaded', () => {
    // Inicializa a primeira página
    navigate('dashboard');

    // Toggle do menu mobile (robusto: verifica elementos)
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // Ajusta margem do conteúdo principal conforme largura
    const mainWrapper = document.querySelector('.flex-1') || document.querySelector('.main-wrapper') || document.getElementById('main-content').parentElement;
    function adjustMainMargin(){
        if (!mainWrapper) return;
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
        const observer = new MutationObserver(() => {
            if(resultsDiv.children.length && petSearch.value.trim().length >= 2){
                resultsDiv.classList.add('show');
            } else {
                resultsDiv.classList.remove('show');
            }
        });
        observer.observe(resultsDiv, { childList: true, subtree: true });

        // Fechar dropdown quando clicam fora
        document.addEventListener('click', (e) => {
            if (!resultsDiv.contains(e.target) && e.target !== petSearch) {
                resultsDiv.classList.remove('show');
            }
        });
    }
});
