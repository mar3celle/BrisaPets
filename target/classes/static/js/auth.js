// auth.js - controla as abas de Entrar / Criar Conta
document.addEventListener('DOMContentLoaded', function () {
  const tabEntrar = document.getElementById('tab-entrar');
  const tabRegistro = document.getElementById('tab-registro');
  const formLogin = document.getElementById('login-form');
  const formRegistro = document.getElementById('registro-form');

  // Segurança: verifica que os elementos existem antes de manipular
  function safeGet(el) { return el || null; }
  if (!safeGet(tabEntrar) || !safeGet(tabRegistro) || !safeGet(formLogin) || !safeGet(formRegistro)) {
    // Se algum elemento estiver ausente, aborta silenciosamente
    return;
  }

  function showLogin() {
    tabEntrar.classList.add('active');
    tabEntrar.setAttribute('aria-selected', 'true');
    tabRegistro.classList.remove('active');
    tabRegistro.setAttribute('aria-selected', 'false');

    formLogin.classList.remove('hidden');
    formRegistro.classList.add('hidden');
  }

  function showRegister() {
    tabRegistro.classList.add('active');
    tabRegistro.setAttribute('aria-selected', 'true');
    tabEntrar.classList.remove('active');
    tabEntrar.setAttribute('aria-selected', 'false');

    formRegistro.classList.remove('hidden');
    formLogin.classList.add('hidden');
  }

  tabEntrar.addEventListener('click', showLogin);
  tabRegistro.addEventListener('click', showRegister);

  // Lógica para decidir qual aba mostrar inicialmente:
  // 1) Querystring: ?register ou ?error
  // 2) Se existir no DOM algum <p class="text-danger"> (erros de validação do Thymeleaf) -> mostra registo
  // 3) Se existir uma mensagem de sucesso de registo (ex.: elemento .alert-success com texto) -> mostrar login
  const urlParams = new URLSearchParams(window.location.search);
  const hasRegisterParam = urlParams.has('register');
  const hasErrorParam = urlParams.has('error'); // erro de login

  // Detecta erros de validação no form de registo (Thymeleaf coloca .text-danger)
  const hasFieldErrorsInRegister = !!formRegistro.querySelector('.text-danger');

  // Detecta mensagens de sucesso (ex.: registrationSuccess renderizado pelo backend)
  const registrationSuccess = document.querySelector('.alert-success');

  // Decisão:
  if (hasErrorParam) {
    showLogin();
  } else if (hasRegisterParam || hasFieldErrorsInRegister) {
    // Se veio com ?register ou há erros no registo -> mostra registo
    showRegister();
  } else if (registrationSuccess) {
    // Se há mensagem de sucesso do registo, mostra login para que o user entre
    showLogin();
  } else {
    // Padrão: login
    showLogin();
  }

  // Permitir navegação por teclado entre abas
  tabEntrar.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') { showLogin(); e.preventDefault(); } });
  tabRegistro.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') { showRegister(); e.preventDefault(); } });
});
