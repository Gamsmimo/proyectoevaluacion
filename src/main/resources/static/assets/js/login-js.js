document.addEventListener('DOMContentLoaded', function() {
	const loginForm = document.getElementById('loginForm');
	const errorMessage = document.getElementById('errorMessage');
	const successMessage = document.getElementById('successMessage');

	// Mostrar mensajes flash si existen
	const urlParams = new URLSearchParams(window.location.search);
	if (urlParams.get('error')) {
		showError(decodeURIComponent(urlParams.get('error')));
	}
	if (urlParams.get('success')) {
		showSuccess(decodeURIComponent(urlParams.get('success')));
	}

	// QUITAR el event listener que previene el envío
	// Y dejar que Spring maneje el formulario

	// Solo mantener las validaciones básicas si quieres
	loginForm.addEventListener('submit', function(e) {
		const email = document.getElementById('email').value;
		const password = document.getElementById('password').value;

		// Validaciones básicas del frontend (opcional)
		if (!email || !password) {
			e.preventDefault(); // Solo prevenir si hay errores de validación
			showError('Por favor, completa todos los campos');
			return;
		}

		if (!isValidEmail(email)) {
			e.preventDefault(); // Solo prevenir si hay errores de validación
			showError('Por favor, ingresa un email válido');
			return;
		}

		// Si pasa las validaciones, dejar que el formulario se envíe normalmente
		// Spring se encargará del procesamiento
	});

	function showError(message) {
		if (errorMessage) {
			errorMessage.textContent = message;
			errorMessage.style.display = 'block';
			if (successMessage) successMessage.style.display = 'none';
		}
	}

	function showSuccess(message) {
		if (successMessage) {
			successMessage.textContent = message;
			successMessage.style.display = 'block';
			if (errorMessage) errorMessage.style.display = 'none';
		}
	}

	function isValidEmail(email) {
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return emailRegex.test(email);
	}

	// Efecto de animación en los inputs (esto sí puede mantenerse)
	const inputs = document.querySelectorAll('input');
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