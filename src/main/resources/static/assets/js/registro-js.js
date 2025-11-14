document.addEventListener('DOMContentLoaded', function() {
	const registerForm = document.getElementById('registerForm');
	const errorMessage = document.getElementById('errorMessage');
	const successMessage = document.getElementById('successMessage');
	const passwordInput = document.getElementById('password');
	const confirmPasswordInput = document.getElementById('confirmPassword');

	// Mostrar mensajes flash si existen
	const urlParams = new URLSearchParams(window.location.search);
	if (urlParams.get('error')) {
		showMessage(errorMessage, decodeURIComponent(urlParams.get('error')));
	}
	if (urlParams.get('success')) {
		showMessage(successMessage, decodeURIComponent(urlParams.get('success')));
	}

	// Verificar fuerza de contraseña (esto puede mantenerse)
	if (passwordInput) {
		passwordInput.addEventListener('input', () => {
			const password = passwordInput.value;
			// ... código existente para mostrar fortaleza ...
		});
	}

	// Manejar el envío del formulario - SOLO validar, no prevenir por defecto
	if (registerForm) {
		registerForm.addEventListener('submit', function(e) {
			const password = document.getElementById('password').value;
			const confirmPassword = document.getElementById('confirmPassword').value;

			// Validaciones del frontend
			if (password !== confirmPassword) {
				e.preventDefault(); // Solo prevenir si hay error
				showMessage(errorMessage, 'Las contraseñas no coinciden');
				return;
			}

			if (password.length < 6) {
				e.preventDefault(); // Solo prevenir si hay error
				showMessage(errorMessage, 'La contraseña debe tener al menos 6 caracteres');
				return;
			}

			// Si pasa las validaciones, dejar que Spring maneje el registro
		});
	}

	function showMessage(element, message) {
		if (element) {
			element.textContent = message;
			element.style.display = 'block';
			setTimeout(() => {
				element.style.display = 'none';
			}, 5000);
		}
	}

	// Efecto de animación en los inputs (mantener)
	const inputs = document.querySelectorAll('input, select');
	inputs.forEach(input => {
		input.addEventListener('focus', function() {
			this.parentElement.style.transform = 'scale(1.02)';
			this.parentElement.style.transition = 'transform 0.3s';
		});

		input.addEventListener('blur', function() {
			this.parentElement.style.transform = 'scale(1)';
		});
	});
});