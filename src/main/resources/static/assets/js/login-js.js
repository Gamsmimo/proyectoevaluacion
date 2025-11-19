document.addEventListener('DOMContentLoaded', function() {
	const loginForm = document.getElementById('loginForm');
	const loginButton = document.getElementById('loginButton');
	const buttonText = loginButton.querySelector('.button-text');
	const loadingSpinner = loginButton.querySelector('.loading-spinner');

	// Manejar envío del formulario
	loginForm.addEventListener('submit', function(e) {
		e.preventDefault(); // Evitar envío por defecto

		const email = document.getElementById('email').value.trim();
		const password = document.getElementById('password').value;

		// Validación básica
		if (!email || !password) {
			showMessage('Por favor, completa todos los campos', 'error');
			return;
		}

		// Validación de formato de email
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			showMessage('Por favor, ingresa un email válido', 'error');
			return;
		}

		// Mostrar estado de carga
		loginButton.disabled = true;
		buttonText.style.display = 'none';
		loadingSpinner.style.display = 'inline-block';

		// Limpiar mensajes existentes
		const existingMessages = document.querySelectorAll('.custom-message, .alert-message');
		existingMessages.forEach(msg => msg.remove());

		// Enviar datos al backend
		fetch('http://localhost:51370/api/usuarios/login', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ email, password })
		})
			.then(res => res.json().then(data => ({ status: res.status, body: data })))
			.then(({ status, body }) => {
				loginButton.disabled = false;
				buttonText.style.display = 'inline-block';
				loadingSpinner.style.display = 'none';

				if (status === 200) {
					showMessage('Inicio de sesión exitoso ✅', 'success');

					// Aquí deberías redirigir según tu rol si usas APIs.
					// Para la lógica de Spring Security no aplica este login API.
					setTimeout(() => window.location.href = '/dashboard', 1500);
				} else {
					showMessage(body.error || 'Usuario o contraseña incorrectos ❌', 'error');
				}
			})
			.catch(err => {
				loginButton.disabled = false;
				buttonText.style.display = 'inline-block';
				loadingSpinner.style.display = 'none';
				showMessage('Error de conexión con el servidor ❌', 'error');
				console.error(err);
			});
	});

	// Función para mostrar mensajes
	function showMessage(message, type) {
		const messageDiv = document.createElement('div');
		messageDiv.className = `custom-message ${type}-message`;

		const icon = type === 'error' ? '❌' : '✅';
		messageDiv.innerHTML = `<span class="icon">${icon}</span> <span class="text">${message}</span>`;

		loginForm.insertBefore(messageDiv, loginForm.firstChild);

		// Auto-eliminar después de 5s
		setTimeout(() => {
			if (messageDiv.parentNode) {
				messageDiv.style.opacity = '0';
				messageDiv.style.transform = 'translateY(-10px)';
				setTimeout(() => { if (messageDiv.parentNode) messageDiv.remove(); }, 300);
			}
		}, 5000);

		// Cerrar manualmente
		messageDiv.addEventListener('click', function() {
			this.style.opacity = '0';
			this.style.transform = 'translateY(-10px)';
			setTimeout(() => { if (this.parentNode) this.remove(); }, 300);
		});
	}

	// Auto-focus en email
	const emailField = document.getElementById('email');
	if (emailField && !emailField.value) emailField.focus();

	// Limpiar mensajes al escribir
	const inputs = loginForm.querySelectorAll('input');
	inputs.forEach(input => {
		input.addEventListener('input', function() {
			const messages = document.querySelectorAll('.custom-message, .alert-message');
			messages.forEach(msg => {
				msg.style.opacity = '0';
				setTimeout(() => msg.remove(), 300);
			});

			if (loginButton.disabled) {
				loginButton.disabled = false;
				buttonText.style.display = 'inline-block';
				loadingSpinner.style.display = 'none';
			}
		});
	});

	// Reset botón si se recarga o cierra página
	window.addEventListener('beforeunload', function() {
		loginButton.disabled = false;
		buttonText.style.display = 'inline-block';
		loadingSpinner.style.display = 'none';
	});
});
