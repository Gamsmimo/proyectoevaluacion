document.addEventListener('DOMContentLoaded', function() {
	const loginForm = document.getElementById('loginForm');
	const loginButton = document.getElementById('loginButton');
	const buttonText = loginButton.querySelector('.button-text');
	const loadingSpinner = loginButton.querySelector('.loading-spinner');

	// Manejar envío del formulario
	loginForm.addEventListener('submit', function(e) {
		const username = document.getElementById('username').value.trim();
		const password = document.getElementById('password').value;

		// Validación básica
		if (!username || !password) {
			e.preventDefault();
			showMessage('Por favor, completa todos los campos', 'error');
			return;
		}

		// Validación de formato de email
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(username)) {
			e.preventDefault();
			showMessage('Por favor, ingresa un email válido', 'error');
			return;
		}

		// Mostrar estado de carga
		loginButton.disabled = true;
		buttonText.style.display = 'none';
		loadingSpinner.style.display = 'inline-block';

		// Limpiar mensajes existentes al enviar
		const existingMessages = document.querySelectorAll('.custom-message');
		existingMessages.forEach(msg => msg.remove());
	});

	// Función para mostrar mensajes
	function showMessage(message, type) {
		// Eliminar mensajes existentes
		const existingMessages = document.querySelectorAll('.custom-message');
		existingMessages.forEach(msg => msg.remove());

		// Crear nuevo mensaje
		const messageDiv = document.createElement('div');
		messageDiv.className = `custom-message ${type}-message`;

		const icon = type === 'error' ? '❌' : '✅';
		messageDiv.innerHTML = `
            <span class="icon">${icon}</span>
            <span class="text">${message}</span>
        `;

		loginForm.insertBefore(messageDiv, loginForm.firstChild);

		// Auto-eliminar después de 5 segundos
		setTimeout(() => {
			if (messageDiv.parentNode) {
				messageDiv.style.opacity = '0';
				messageDiv.style.transform = 'translateY(-10px)';
				setTimeout(() => {
					if (messageDiv.parentNode) {
						messageDiv.remove();
					}
				}, 300);
			}
		}, 5000);

		// Permitir cerrar manualmente
		messageDiv.addEventListener('click', function() {
			this.style.opacity = '0';
			this.style.transform = 'translateY(-10px)';
			setTimeout(() => {
				if (this.parentNode) {
					this.remove();
				}
			}, 300);
		});
	}

	// Auto-focus en el campo de email
	const usernameField = document.getElementById('username');
	if (usernameField && !usernameField.value) {
		usernameField.focus();
	}

	// Limpiar mensajes al empezar a escribir
	const inputs = loginForm.querySelectorAll('input');
	inputs.forEach(input => {
		input.addEventListener('input', function() {
			const messages = document.querySelectorAll('.custom-message');
			messages.forEach(msg => {
				msg.style.opacity = '0';
				setTimeout(() => msg.remove(), 300);
			});

			// Restaurar botón si estaba deshabilitado por error de validación
			if (loginButton.disabled) {
				loginButton.disabled = false;
				buttonText.style.display = 'inline-block';
				loadingSpinner.style.display = 'none';
			}
		});
	});

	// Auto-eliminar mensajes del servidor después de 5 segundos
	setTimeout(function() {
		const serverMessages = document.querySelectorAll('.alert-message');
		serverMessages.forEach(function(message) {
			message.style.opacity = '0';
			setTimeout(function() {
				if (message.parentNode) {
					message.remove();
				}
			}, 500);
		});
	}, 5000);

	// Permitir cerrar mensajes del servidor manualmente
	document.querySelectorAll('.alert-message').forEach(function(message) {
		message.style.cursor = 'pointer';
		message.addEventListener('click', function() {
			this.style.opacity = '0';
			setTimeout(() => {
				if (this.parentNode) {
					this.remove();
				}
			}, 500);
		});
	});

	// Manejar el evento de antes de descargar la página
	window.addEventListener('beforeunload', function() {
		if (loginButton.disabled) {
			loginButton.disabled = false;
			buttonText.style.display = 'inline-block';
			loadingSpinner.style.display = 'none';
		}
	});
});