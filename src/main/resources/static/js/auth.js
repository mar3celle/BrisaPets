/**
 * Brisa Pets - Authentication JavaScript
 * Funcionalidades específicas para login e registro
 */

document.addEventListener('DOMContentLoaded', function() {
    initAuthForms();
    initPasswordToggle();
    initFormValidation();
});

// Inicializar formulários de autenticação
function initAuthForms() {
    const loginForm = document.querySelector('form[action*="login"]');
    const registerForm = document.querySelector('form[action*="register"]');
    
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<span class="loading-spinner"></span> Entrando...';
                submitBtn.disabled = true;
            }
        });
    }
    
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<span class="loading-spinner"></span> Registrando...';
                submitBtn.disabled = true;
            }
        });
    }
}

// Toggle de visibilidade da password (apenas para registro)
function initPasswordToggle() {
    const registerForm = document.querySelector('form[action*="register"]');
    
    if (registerForm) {
        const passwordInputs = registerForm.querySelectorAll('input[type="password"]');
        
        passwordInputs.forEach(input => {
            // Verificar se já tem wrapper para evitar duplicação
            if (input.parentNode.classList && input.parentNode.classList.contains('password-wrapper')) {
                return;
            }
            
            const wrapper = document.createElement('div');
            wrapper.className = 'password-wrapper';
            wrapper.style.position = 'relative';
            wrapper.style.display = 'inline-block';
            wrapper.style.width = '100%';
            
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);
            
            const toggleBtn = document.createElement('button');
            toggleBtn.type = 'button';
            toggleBtn.innerHTML = '<i class="fas fa-eye"></i>';
            toggleBtn.style.cssText = `
                position: absolute;
                right: 12px;
                top: 50%;
                transform: translateY(-50%);
                background: none;
                border: none;
                cursor: pointer;
                color: #666;
                padding: 5px;
                z-index: 10;
            `;
            
            toggleBtn.addEventListener('click', function() {
                const isPassword = input.type === 'password';
                input.type = isPassword ? 'text' : 'password';
                this.innerHTML = isPassword ? '<i class="fas fa-eye-slash"></i>' : '<i class="fas fa-eye"></i>';
            });
            
            wrapper.appendChild(toggleBtn);
        });
    }
}

// Validação de formulários
function initFormValidation() {
    const registerForm = document.querySelector('form[action*="register"]');
    
    if (registerForm) {
        const emailInputs = registerForm.querySelectorAll('input[type="email"]');
        const passwordInputs = registerForm.querySelectorAll('input[type="password"]');
        
        emailInputs.forEach(input => {
            input.addEventListener('blur', function() {
                const isValid = this.checkValidity();
                this.style.borderColor = isValid ? '#28a745' : '#dc3545';
            });
        });
        
        passwordInputs.forEach(input => {
            if (input.name === 'password' || input.id === 'password') {
                input.addEventListener('input', function() {
                    const strength = calculatePasswordStrength(this.value);
                    updatePasswordStrengthIndicator(this, strength);
                });
            }
        });
    }
}

function calculatePasswordStrength(password) {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    return strength;
}

function updatePasswordStrengthIndicator(input, strength) {
    let indicator = input.parentNode.querySelector('.password-strength');
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.className = 'password-strength';
        indicator.style.cssText = `
            margin-top: 5px;
            height: 4px;
            background: #e0e0e0;
            border-radius: 2px;
            overflow: hidden;
        `;
        input.parentNode.appendChild(indicator);
    }
    
    const colors = ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#20c997'];
    const widths = ['20%', '40%', '60%', '80%', '100%'];
    
    indicator.innerHTML = `<div style="height: 100%; background: ${colors[strength - 1] || '#e0e0e0'}; width: ${widths[strength - 1] || '0%'}; transition: all 0.3s ease;"></div>`;
}