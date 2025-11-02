document.addEventListener('DOMContentLoaded', () => {
    // -------------------------------------------------------------------------
    // LÓGICA DO MODAL DE EXCLUSÃO DE CONTA
    // -------------------------------------------------------------------------
    const modal = document.getElementById('delete-modal');
    const showModalBtn = document.getElementById('show-modal-btn');
    const cancelBtn = document.getElementById('cancel-btn');

    // Se os elementos existirem, configuramos os listeners
    if (modal && showModalBtn && cancelBtn) {
        // Função para mostrar o modal
        const showModal = () => {
            modal.classList.add('is-active');
        }

        // Função para esconder o modal
        const hideModal = () => {
            modal.classList.remove('is-active');
        }

        // Abrir modal ao clicar no botão de exclusão
        showModalBtn.addEventListener('click', showModal);

        // Fechar modal ao clicar no botão de cancelar
        cancelBtn.addEventListener('click', hideModal);

        // Fechar modal ao clicar fora dele
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                hideModal();
            }
        });

        // Fechar modal com a tecla ESC
        document.addEventListener('keydown', (e) => {
            // Verifica se a tecla é 'Escape' E se o modal está ativo
            if (e.key === 'Escape' && modal.classList.contains('is-active')) {
                hideModal();
            }
        });
    }

    // -------------------------------------------------------------------------
    // LÓGICA DO BOTÃO DE EDIÇÃO DE PERFIL
    // -------------------------------------------------------------------------
    const editButton = document.getElementById('edit-profile-btn');
    if (editButton) {
        // Adiciona um listener para o botão de edição
        editButton.addEventListener('click', () => {
            // Feedback temporário na consola, visto que 'alert()' é proibido.
            console.log('Botão de Edição de Perfil clicado. Funcionalidade em desenvolvimento.');
        });
    }
});
