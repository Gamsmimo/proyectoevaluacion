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

// Función para guardar cambios
function guardarCambios() {
	if (!isEditing) return;

	const datosActualizados = {
		id: profileData.id,
		nombre: el.nombre.value,
		email: el.email.value,
		telefono: el.telefono.value
	};

	showAlert('Guardando cambios...', 'success');

	fetch('/usuarios/actualizar', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(datosActualizados)
	})
		.then(response => {
			if (!response.ok) {
				throw new Error('Error en la actualización');
			}
			return response.json();
		})
		.then(data => {
			if (data.success) {
				// Actualizar datos locales
				profileData.nombre = el.nombre.value;
				profileData.email = el.email.value;
				profileData.telefono = el.telefono.value;

				updateDisplay();
				toggleEdit();
				showAlert('✓ Información actualizada correctamente', 'success');

				// Recargar la página después de un tiempo para mostrar cambios
				setTimeout(() => {
					window.location.reload();
				}, 1500);
			} else {
				throw new Error(data.message || 'Error al actualizar');
			}
		})
		.catch(error => {
			console.error('Error:', error);
			showAlert('✗ Error al actualizar: ' + error.message, 'error');
		});
}

// Event Listeners
if (el.editBtn) {
	el.editBtn.addEventListener('click', toggleEdit);
}

if (el.saveBtn) {
	el.saveBtn.addEventListener('click', guardarCambios);
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

	document.body.insertAdjacentHTML('beforeend', modalHTML);

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

// Función para eliminar la cuenta
function eliminarCuenta() {
	const modal = document.getElementById('deleteModal');
	const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

	// Deshabilitar botón durante la solicitud
	confirmDeleteBtn.disabled = true;
	confirmDeleteBtn.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2a10 10 0 0 1 7.38 16.75"></path>
            <path d="M12 2v4"></path>
            <path d="M12 18v4"></path>
        </svg>
        Eliminando...
    `;

	showAlert('Eliminando cuenta...', 'success');

	fetch(`/usuarios/eliminar-cuenta/${profileData.id}`, {
		method: 'DELETE'
	})
		.then(response => {
			if (!response.ok) {
				throw new Error('Error al eliminar la cuenta');
			}
			return response.json();
		})
		.then(data => {
			if (data.success) {
				showAlert('✓ Cuenta eliminada correctamente', 'success');
				modal.style.display = 'none';

				// Redirigir al login después de un tiempo
				setTimeout(() => {
					window.location.href = '/usuarios/login';
				}, 2000);
			} else {
				throw new Error(data.message || 'Error al eliminar la cuenta');
			}
		})
		.catch(error => {
			console.error('Error:', error);
			showAlert('✗ Error al eliminar cuenta: ' + error.message, 'error');

			// Rehabilitar botón
			confirmDeleteBtn.disabled = false;
			confirmDeleteBtn.innerHTML = `
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 6h18"></path>
                <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
                <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
            </svg>
            Eliminar Cuenta Permanentemente
        `;
		});
}

// Inicializar display y modal cuando se cargue la página
document.addEventListener('DOMContentLoaded', function() {
	updateDisplay();
	crearModalEliminacion();
});