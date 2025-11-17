// profesional-registro.js
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar el selector de tipo de cuenta
    showTypeSelector();
    
    // Configurar validaciones para ambos formularios
    setupFormValidations();
});

function showTypeSelector() {
    document.getElementById('userForm').style.display = 'none';
    document.getElementById('proForm').style.display = 'none';
    document.querySelector('.account-type-selector').style.display = 'grid';
    document.querySelector('.login-link').style.display = 'block';
    document.querySelector('.back-home').style.display = 'block';
}

function showUserForm() {
    document.querySelector('.account-type-selector').style.display = 'none';
    document.querySelector('.login-link').style.display = 'none';
    document.querySelector('.back-home').style.display = 'none';
    document.getElementById('userForm').style.display = 'block';
    
    // Scroll suave al formulario
    document.getElementById('userForm').scrollIntoView({ behavior: 'smooth' });
}

function showProForm() {
    document.querySelector('.account-type-selector').style.display = 'none';
    document.querySelector('.login-link').style.display = 'none';
    document.querySelector('.back-home').style.display = 'none';
    document.getElementById('proForm').style.display = 'block';
    
    // Scroll suave al formulario
    document.getElementById('proForm').scrollIntoView({ behavior: 'smooth' });
}

function setupFormValidations() {
    // Validación para formulario de usuario
    const userForm = document.getElementById('userForm');
    if (userForm) {
        userForm.addEventListener('submit', function(e) {
            if (!validateUserForm()) {
                e.preventDefault();
            }
        });
    }

    // Validación para formulario de profesional
    const proForm = document.getElementById('proForm');
    if (proForm) {
        proForm.addEventListener('submit', function(e) {
            if (!validateProForm()) {
                e.preventDefault();
            }
        });
    }

    // Validación de contraseñas en tiempo real
    setupPasswordValidation();
}

function validateUserForm() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorElement = document.getElementById('errorMessage');

    if (password !== confirmPassword) {
        showError(errorElement, 'Las contraseñas no coinciden');
        return false;
    }

    if (password.length < 6) {
        showError(errorElement, 'La contraseña debe tener al menos 6 caracteres');
        return false;
    }

    return true;
}

function validateProForm() {
    const especialidad = document.getElementById('especialidad').value;
    const horario = document.getElementById('horarioDisponible').value;
    const password = document.getElementById('proPassword').value;
    const confirmPassword = document.getElementById('proConfirmPassword').value;
    const errorElement = document.getElementById('proErrorMessage');

    if (!especialidad) {
        showError(errorElement, 'Por favor selecciona una especialidad');
        return false;
    }

    if (!horario.trim()) {
        showError(errorElement, 'Por favor ingresa tu horario disponible');
        return false;
    }

    if (password !== confirmPassword) {
        showError(errorElement, 'Las contraseñas no coinciden');
        return false;
    }

    if (password.length < 6) {
        showError(errorElement, 'La contraseña debe tener al menos 6 caracteres');
        return false;
    }

    return true;
}

function setupPasswordValidation() {
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    
    passwordInputs.forEach(input => {
        input.addEventListener('input', function() {
            const confirmInput = this.id.includes('pro') ? 
                document.getElementById('proConfirmPassword') : 
                document.getElementById('confirmPassword');
            
            if (this.value && confirmInput.value) {
                if (this.value !== confirmInput.value) {
                    this.style.borderColor = '#f44336';
                    confirmInput.style.borderColor = '#f44336';
                } else {
                    this.style.borderColor = '#4caf50';
                    confirmInput.style.borderColor = '#4caf50';
                }
            }
        });
    });
}

function showError(element, message) {
    if (element) {
        element.textContent = message;
        element.style.display = 'block';
        
        // Scroll al error
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        
        // Ocultar después de 5 segundos
        setTimeout(() => {
            element.style.display = 'none';
        }, 5000);
    }
}

// Efectos visuales para los inputs
const inputs = document.querySelectorAll('input, select');
inputs.forEach(input => {
    input.addEventListener('focus', function() {
        this.parentElement.style.transform = 'translateY(-2px)';
        this.parentElement.style.transition = 'all 0.3s ease';
    });

    input.addEventListener('blur', function() {
        this.parentElement.style.transform = 'translateY(0)';
    });
});