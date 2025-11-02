/**
 * Lógica JavaScript para a página de Relatórios de Faturação (reports.html).
 * Este script gere a interatividade dos filtros e simula a funcionalidade de exportação.
 */
document.addEventListener('DOMContentLoaded', function() {
    // Referências dos elementos principais
    const dateRangeSelect = document.getElementById('date-range');
    const exportButton = document.querySelector('.export-button');
    const filterSection = document.querySelector('.filter-export-section');
    
    // Validação de elementos essenciais
    if (!dateRangeSelect || !exportButton || !filterSection) {
        console.error('Elementos essenciais não encontrados na página');
        return;
    }

    // ----------------------------------------------------------------------
    // 1. Gestão do Filtro de Intervalo de Datas
    // Objetivo: Adicionar campos de data de início e fim quando "Personalizado" for escolhido.
    // ----------------------------------------------------------------------

    /**
     * Adiciona ou remove os campos de data de início e fim.
     */
    function toggleCustomDateInputs(selectedValue) {
        const existingCustomInputs = document.querySelectorAll('.custom-date-group');
        existingCustomInputs.forEach(el => el.remove()); // Remove existentes

        if (selectedValue === 'custom') {
            const startGroup = createDateFilter('date-start', 'Data de Início');
            const endGroup = createDateFilter('date-end', 'Data de Fim');

            // Encontra o grupo de intervalo de datas (o pai) para inserir os novos inputs a seguir
            const dateRangeGroup = dateRangeSelect.closest('.filter-group');
            if (dateRangeGroup) {
                dateRangeGroup.insertAdjacentElement('afterend', endGroup);
                dateRangeGroup.insertAdjacentElement('afterend', startGroup);
            }
        }
        applyFilters(); // Aplica os filtros após a alteração
    }

    /**
     * Cria um novo grupo de filtro de data personalizado.
     */
    function createDateFilter(id, labelText) {
        const group = document.createElement('div');
        group.className = 'filter-group custom-date-group';

        const label = document.createElement('label');
        label.setAttribute('for', id);
        label.textContent = labelText;

        const input = document.createElement('input');
        input.id = id;
        input.type = 'date';
        input.placeholder = 'dd/mm/aaaa';

        // Adiciona listener para aplicar filtros após a alteração da data
        input.addEventListener('change', applyFilters);

        group.appendChild(label);
        group.appendChild(input);
        return group;
    }

    // Listener para o dropdown de intervalo de datas
    dateRangeSelect.addEventListener('change', function() {
        toggleCustomDateInputs(this.value);
    });

    // Inicializa a lógica de customização na carga da página
    toggleCustomDateInputs(dateRangeSelect.value);


    // ----------------------------------------------------------------------
    // 2. Simulação de Aplicação de Filtros
    // ----------------------------------------------------------------------

    /**
     * Simula a recolha e aplicação dos filtros selecionados.
     */
    function applyFilters() {
        try {
            const range = dateRangeSelect?.value || '';
            const serviceElement = document.getElementById('service-type');
            const statusElement = document.getElementById('status');
            
            if (!serviceElement || !statusElement) {
                console.warn('Elementos de filtro não encontrados');
                return;
            }
            
            const service = serviceElement.value;
            const status = statusElement.value;

            // Sanitização básica dos valores
            const sanitizedRange = range.replace(/[<>"'&]/g, '');
            const sanitizedService = service.replace(/[<>"'&]/g, '');
            const sanitizedStatus = status.replace(/[<>"'&]/g, '');

            let logMessage = `Filtros aplicados: [Intervalo: ${sanitizedRange}, Serviço: ${sanitizedService}, Estado: ${sanitizedStatus}]`;

            if (range === 'custom') {
                const dateStart = document.getElementById('date-start')?.value || 'N/A';
                const dateEnd = document.getElementById('date-end')?.value || 'N/A';
                logMessage += ` (Início: ${dateStart}, Fim: ${dateEnd})`;
            }

            console.log(logMessage);

        // Numa aplicação real, esta função faria um fetch(POST/GET) para o Controller Java
        // com estes parâmetros para atualizar os dados (métricas e tabela) via AJAX.

            // Por enquanto, atualiza apenas o log da consola para demonstrar que a lógica funciona.
            updateDashboardSimulation(range);
        } catch (error) {
            console.error('Erro ao aplicar filtros:', error);
        }
    }

    // Adiciona listener de mudança a todos os filtros (exceto o de data, já coberto)
    filterSection.querySelectorAll('select').forEach(select => {
        if (select.id !== 'date-range') {
            select.addEventListener('change', applyFilters);
        }
    });


    // ----------------------------------------------------------------------
    // 3. Simulação da Lógica de Exportação
    // ----------------------------------------------------------------------

    /**
     * Simula o download do ficheiro de exportação e mostra uma mensagem de sucesso temporária.
     */
    function handleExport() {
        try {
            if (!exportButton) return;
            
            exportButton.disabled = true;
            exportButton.textContent = 'A gerar ficheiro...';

            // Simula o tempo de processamento de exportação
            setTimeout(() => {
                alertMessage('Exportação de dados concluída!', 'success');
                exportButton.textContent = 'Exportar Dados';
                exportButton.disabled = false;
            }, 1500); // 1.5 segundos de espera
        } catch (error) {
            console.error('Erro durante exportação:', error);
            if (exportButton) {
                exportButton.textContent = 'Exportar Dados';
                exportButton.disabled = false;
            }
        }
    }

    exportButton.addEventListener('click', handleExport);


    // ----------------------------------------------------------------------
    // 4. Funções de Utilidade (Simulação de Atualização e Mensagem)
    // ----------------------------------------------------------------------

    /**
     * Simula a atualização de algumas métricas no dashboard.
     */
    function updateDashboardSimulation(range) {
        // Apenas um exemplo visual de que a filtragem "aconteceu"
        const revenueValue = document.querySelector('.metric-card.revenue .value');
        if (revenueValue) {
             // Simula uma mudança de valor com base no range
            if (range === 'this-month') {
                revenueValue.textContent = 'R$ 7.150,00';
            } else if (range === 'last-month') {
                revenueValue.textContent = 'R$ 5.900,00';
            } else {
                 revenueValue.textContent = 'R$ 6.500,00';
            }
        }
    }


    /**
     * Exibe uma mensagem de alerta (em vez de usar alert() nativo).
     * Nota: Esta função precisa que o HTML tenha um elemento para a mensagem.
     */
    function alertMessage(msg, type) {
        const container = document.querySelector('.dashboard-container');
        if (!container) return;

        let messageBox = document.querySelector('#js-alert-message');
        if (!messageBox) {
            messageBox = document.createElement('div');
            messageBox.id = 'js-alert-message';
            messageBox.style.cssText = `
                padding: 15px;
                margin-bottom: 20px;
                border-radius: 8px;
                font-weight: 600;
                display: none;
                text-align: center;
            `;
            container.prepend(messageBox);
        }

        messageBox.textContent = msg;
        messageBox.style.display = 'block';

        if (type === 'success') {
            messageBox.style.backgroundColor = '#d4edda';
            messageBox.style.color = '#155724';
            messageBox.style.border = '1px solid #c3e6cb';
        } else if (type === 'error') {
            messageBox.style.backgroundColor = '#f8d7da';
            messageBox.style.color = '#721c24';
            messageBox.style.border = '1px solid #f5c6cb';
        }

        // Esconde a mensagem após 4 segundos
        setTimeout(() => {
            messageBox.style.display = 'none';
        }, 4000);
    }

    // Inicia a aplicação dos filtros na carga (para mostrar o estado inicial)
    applyFilters();
});
