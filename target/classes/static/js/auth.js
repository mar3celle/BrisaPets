// auth.js
document.addEventListener('DOMContentLoaded', function () {
    const tabEntrar = document.getElementById('tab-entrar');
    const tabRegistro = document.getElementById('tab-registro');
    const loginForm = document.getElementById('login-form');
    const registroForm = document.getElementById('registro-form');

    function showLogin() {
        tabEntrar.classList.add('active');
        tabEntrar.setAttribute('aria-selected', 'true');
        tabRegistro.classList.remove('active');
        tabRegistro.setAttribute('aria-selected', 'false');

        loginForm.classList.remove('hidden');
        registroForm.classList.add('hidden');
    }

    function showRegister() {
        tabRegistro.classList.add('active');
        tabRegistro.setAttribute('aria-selected', 'true');
        tabEntrar.classList.remove('active');
        tabEntrar.setAttribute('aria-selected', 'false');

        registroForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
    }

    tabEntrar.addEventListener('click', showLogin);
    tabRegistro.addEventListener('click', showRegister);

    // Se houver um erro de login na query string (?error) mantemos a tab "Entrar" visível
    if (window.location.search.includes('error')) {
        showLogin();
    }

    // Se houver registro com sucesso (p.ex. registrationSuccess model attr mostrou mensagem), mostra login
});
