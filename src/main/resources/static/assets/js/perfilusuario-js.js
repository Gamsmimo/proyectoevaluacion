// Datos iniciales cargados desde Thymeleaf
const profileData = {
	id: document.getElementById('usuarioId')?.value || '',
	nombre: document.getElementById('firstName')?.value || '',
	email: document.getElementById('email')?.value || '',
	telefono: document.getElementById('phone')?.value || ''
};

const el = {
	nombre: document.getElementById('firstName'),
	email: document.getElementById('email'),
	telefono: document.getElementById('phone'),
	displayName: document.getElementById('displayName'),
	editBtn: document.getElementById('editBtn'),
	saveBtn: document.getElementById('saveBtn'),
	cancelBtn: document.getElementById('cancelBtn'),
	logoutBtn: document.getElementById('logoutBtn'),
	deleteAccountBtn: document.getElementById('deleteAccountBtn'),
	alert: document.getElementById('alert'),
	alertText: document.getElementById('alertText')
};

let isEditing = false;
let originalData = { ...profileData };

// ✅ OBTENER TOKEN CSRF
function getCSRFToken() {
	return document.querySelector('input[name="_csrf"]')?.value || '';
}

function showAlert(text, type) {
	if (el.alert && el.alertText) {
		el.alertText.textContent = text;
		el.alert.className = `alert ${type} show`;

		setTimeout(() => {
			el.alert.classList.remove('show');
		}, 4000);
	}
}

function updateDisplay() {
	if (el.displayName) {
		el.displayName.textContent = profileData.nombre;
	}
}

function toggleEdit() {
	isEditing = !isEditing;

	const inputs = [el.nombre, el.email, el.telefono];
	inputs.forEach(input => {
		if (input) {
			input.disabled = !isEditing;
		}
	});

	// Mostrar/ocultar botones
	if (el.editBtn) el.editBtn.style.display = isEditing ? 'none' : 'flex';
	if (el.saveBtn) el.saveBtn.style.display = isEditing ? 'flex' : 'none';
	if (el.cancelBtn) el.cancelBtn.style.display = isEditing ? 'flex' : 'none';

	if (isEditing) {
		// Guardar datos originales
		originalData = {
			nombre: el.nombre.value,
			email: el.email.value,
			telefono: el.telefono.value
		};
		if (el.nombre) el.nombre.focus();
	}
}

function guardarCambios() {
	if (!isEditing) return;

	const datosActualizados = {
		nombre: el.nombre.value,
		email: el.email.value,
		telefono: el.telefono.value
	};

	showAlert('Guardando cambios...', 'success');

	// ✅ USAR FORM-DATA EN LUGAR DE URL ENCODED
	const formData = new FormData();
	formData.append('nombre', datosActualizados.nombre);
	formData.append('email', datosActualizados.email);
	formData.append('telefono', datosActualizados.telefono);

	fetch('/usuarios/actualizar-perfil-ajax', {
		method: 'POST',
		headers: {
			// ✅ AGREGAR HEADER CSRF
			'X-CSRF-TOKEN': getCSRFToken()
		},
		body: formData
	})
		.then(response => {
			if (!response.ok) {
				throw new Error('Error en la actualización');
			}
			return response.json();
		})
		.then(data => {
			if (data.success) {
				profileData.nombre = el.nombre.value;
				profileData.email = el.email.value;
				profileData.telefono = el.telefono.value;

				updateDisplay();
				toggleEdit();
				showAlert('✓ ' + data.message, 'success');
			} else {
				throw new Error(data.message);
			}
		})
		.catch(error => {
			console.error('Error:', error);
			showAlert('✗ ' + error.message, 'error');

			// ✅ REVERTIR CAMBIOS EN CASO DE ERROR
			el.nombre.value = originalData.nombre;
			el.email.value = originalData.email;
			el.telefono.value = originalData.telefono;
		});
}

// ✅ NUEVA FUNCIÓN: ENVIAR FORMULARIO TRADICIONAL (ALTERNATIVA)
function guardarCambiosFormularioTradicional() {
	if (!isEditing) return;

	const form = document.getElementById('profileForm');
	if (form) {
		// ✅ HABILITAR CAMPOS TEMPORALMENTE PARA QUE SE ENVÍEN
		const inputs = [el.nombre, el.email, el.telefono];
		inputs.forEach(input => {
			if (input) input.disabled = false;
		});

		// Enviar formulario
		form.submit();

		// Volver a deshabilitar (aunque la página se recargará)
		setTimeout(() => {
			inputs.forEach(input => {
				if (input) input.disabled = true;
			});
		}, 100);
	}
}

// Event Listeners
if (el.editBtn) {
	el.editBtn.addEventListener('click', toggleEdit);
}

if (el.saveBtn) {
	// ✅ CAMBIAR A FORMULARIO TRADICIONAL PARA EVITAR PROBLEMAS
	el.saveBtn.addEventListener('click', guardarCambiosFormularioTradicional);
}

if (el.cancelBtn) {
	el.cancelBtn.addEventListener('click', () => {
		// Restaurar valores originales
		el.nombre.value = originalData.nombre;
		el.email.value = originalData.email;
		el.telefono.value = originalData.telefono;

		toggleEdit();
		showAlert('✗ Cambios descartados', 'error');
	});
}

// Cerrar sesión
if (el.logoutBtn) {
	el.logoutBtn.addEventListener('click', () => {
		if (confirm('¿Estás seguro que deseas cerrar sesión?')) {
			showAlert('Cerrando sesión...', 'success');
			setTimeout(() => {
				window.location.href = '/usuarios/logout';
			}, 1500);
		}
	});
}

// Eliminar cuenta
if (el.deleteAccountBtn) {
	el.deleteAccountBtn.addEventListener('click', () => {
		const modal = document.getElementById('deleteModal');
		if (modal) {
			modal.style.display = 'flex';
		}
	});
}

// Tabs
const tabs = document.querySelectorAll('.tab');
tabs.forEach(tab => {
	tab.addEventListener('click', () => {
		tabs.forEach(t => t.classList.remove('active'));
		tab.classList.add('active');
	});
});

// Crear modal para confirmar eliminación
function crearModalEliminacion() {
	const modalHTML = `
        <div class="modal-overlay" id="deleteModal">
            <div class="modal-content">
                <h3 class="modal-title">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                        <line x1="12" y1="9" x2="12" y2="13"></line>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                    Eliminar Cuenta Permanentemente
                </h3>
                <div class="warning-text">
                    ⚠️ Esta acción no se puede deshacer. Se eliminarán todos tus datos permanentemente.
                </div>
                <p class="modal-message">
                    Para confirmar que deseas eliminar tu cuenta, escribe <strong>"ELIMINAR CUENTA"</strong> en el campo de abajo:
                </p>
                <input type="text" class="confirm-input" id="confirmInput" placeholder='Escribe "ELIMINAR CUENTA" aquí'>
                <div class="modal-actions">
                    <button type="button" class="btn btn-secondary" id="cancelDeleteBtn">
                        Cancelar
                    </button>
                    <button type="button" class="btn btn-danger" id="confirmDeleteBtn" disabled>
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M3 6h18"></path>
                            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
                            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
                        </svg>
                        Eliminar Cuenta Permanentemente
                    </button>
                </div>
            </div>
        </div>
    `;

	document.getElementById('modalContainer').insertAdjacentHTML('beforeend', modalHTML);


	// Configurar eventos del modal
	const modal = document.getElementById('deleteModal');
	const confirmInput = document.getElementById('confirmInput');
	const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
	const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');

	// Validar texto de confirmación
	confirmInput.addEventListener('input', function() {
		const isValid = this.value === 'ELIMINAR CUENTA';
		confirmDeleteBtn.disabled = !isValid;
	});

	// Confirmar eliminación
	confirmDeleteBtn.addEventListener('click', eliminarCuenta);

	// Cancelar eliminación
	cancelDeleteBtn.addEventListener('click', () => {
		modal.style.display = 'none';
		confirmInput.value = '';
		confirmDeleteBtn.disabled = true;
	});

	// Cerrar modal al hacer clic fuera
	modal.addEventListener('click', (e) => {
		if (e.target === modal) {
			modal.style.display = 'none';
			confirmInput.value = '';
			confirmDeleteBtn.disabled = true;
		}
	});
}

function eliminarCuenta() {
	if (!confirm("¿Seguro que quieres eliminar tu cuenta? Esta acción es irreversible.")) {
		return;
	}

	const token = document.querySelector('meta[name="_csrf"]').content;
	const header = document.querySelector('meta[name="_csrf_header"]').content;

	fetch("/usuarios/eliminar-cuenta", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			[header]: token
		}
	})
		.then(response => {
			if (response.redirected) {
				window.location.href = response.url;
				return;
			}
			if (!response.ok) {
				throw new Error("No se pudo eliminar la cuenta");
			}
		})
		.catch(err => alert("Error: " + err));
}

function configurarDashboardProfesional() {
	const dashboardBtn = document.getElementById('dashboardBtn');
	if (dashboardBtn) {
		dashboardBtn.addEventListener('click', () => {
			window.location.href = '/profesional/dashboard';
		});
	}
}

document.addEventListener('DOMContentLoaded', function() {
	updateDisplay();
	crearModalEliminacion();
	configurarDashboardProfesional();
});