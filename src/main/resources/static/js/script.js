/**
 * Brisa Pets - JavaScript Principal
 * Efeitos visuais delicados e interações padronizadas
 */

document.addEventListener('DOMContentLoaded', function () {
    // Inicializar todos os efeitos
    initMenuToggle();
    initButtonEffects();
    initCardEffects();
    initFormEffects();
    initLoadingAnimations();
});

// Menu toggle para mobile
function initMenuToggle() {
    const toggle = document.querySelector('.nav-toggle');
    const nav = document.querySelector('.nav-menu');

    if (!toggle || !nav) return;

    toggle.addEventListener('click', function (e) {
        const isOpen = nav.classList.toggle('open');
        toggle.classList.toggle('open', isOpen);
        toggle.setAttribute('aria-expanded', String(isOpen));
    });

    // Fecha o menu ao tocar fora
    document.addEventListener('click', function (e) {
        if (!nav.classList.contains('open')) return;
        const clickedInside = nav.contains(e.target) || toggle.contains(e.target);
        if (!clickedInside) {
            nav.classList.remove('open');
            toggle.classList.remove('open');
            toggle.setAttribute('aria-expanded', 'false');
        }
    });

    // Fecha com ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            nav.classList.remove('open');
            toggle.classList.remove('open');
            toggle.setAttribute('aria-expanded', 'false');
        }
    });
}

// Efeitos delicados nos botões
function initButtonEffects() {
    const buttons = document.querySelectorAll('.btn, button[type="submit"], .service-option, .time-slot');
    
    buttons.forEach(button => {
        // Efeito ripple ao clicar
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;
            
            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);
            
            setTimeout(() => ripple.remove(), 600);
        });
        
        // Efeito hover suave
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '';
        });
    });
}

// Efeitos nos cards
function initCardEffects() {
    const cards = document.querySelectorAll('.service-card, .pet-card, .card-large, .contact-card, .feature-card');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
            this.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.12)';
            this.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '';
        });
    });
}

// Efeitos nos formulários
function initFormEffects() {
    const inputs = document.querySelectorAll('input, select, textarea');
    
    inputs.forEach(input => {
        // Efeito focus
        input.addEventListener('focus', function() {
            this.style.borderColor = 'var(--color-primary)';
            this.style.boxShadow = '0 0 0 3px rgba(254, 135, 160, 0.1)';
            this.style.transform = 'scale(1.02)';
            this.style.transition = 'all 0.2s ease';
        });
        
        input.addEventListener('blur', function() {
            this.style.borderColor = '';
            this.style.boxShadow = '';
            this.style.transform = 'scale(1)';
        });
        
        // Validação visual
        input.addEventListener('input', function() {
            if (this.checkValidity()) {
                this.style.borderColor = '#28a745';
            } else if (this.value.length > 0) {
                this.style.borderColor = '#dc3545';
            }
        });
    });
}

// Animações de carregamento
function initLoadingAnimations() {
    // Fade in para elementos
    const elements = document.querySelectorAll('.section-padded, .service-card, .pet-card');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });
    
    elements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
}

// CSS para animação ripple
const style = document.createElement('style');
style.textContent = `
    @keyframes ripple {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .btn, button, .service-option, .time-slot {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    input, select, textarea {
        transition: all 0.2s ease;
    }
`;
document.head.appendChild(style);