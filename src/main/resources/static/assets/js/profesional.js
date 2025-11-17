// profesional.js - ARCHIVO COMPLETO
// ✅ AGREGAR al principio de profesional.js (después de los comentarios iniciales)
console.log('🔧 profesional.js cargado correctamente');

// Debug: verificar que las funciones estén disponibles
console.log('🔍 Funciones disponibles:', {
	actualizarEstadoCita: typeof actualizarEstadoCita,
	aceptarCita: typeof aceptarCita,
	rechazarCita: typeof rechazarCita,
	completarCita: typeof completarCita
});

// Interceptar fetch para debug
const originalFetch = window.fetch;
window.fetch = function(...args) {
	console.log('🌐 Fetch llamado:', args[0], args[1]?.method);
	return originalFetch.apply(this, args)
		.then(response => {
			console.log('📨 Fetch respuesta:', args[0], response.status);
			return response;
		})
		.catch(error => {
			console.error('❌ Fetch error:', args[0], error);
			throw error;
		});
};
// Función para mostrar notificaciones
function showNotification(message, type = 'info') {
	// Crear contenedor de notificaciones si no existe
	let notificationContainer = document.getElementById('notification-container');
	if (!notificationContainer) {
		notificationContainer = document.createElement('div');
		notificationContainer.id = 'notification-container';
		notificationContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 10px;
        `;
		document.body.appendChild(notificationContainer);
	}

	// Crear notificación
	const notification = document.createElement('div');
	notification.style.cssText = `
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#e53e3e' : '#3b82f6'};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        animation: slideInRight 0.3s ease;
        display: flex;
        align-items: center;
        gap: 10px;
        min-width: 300px;
        max-width: 400px;
    `;

	notification.innerHTML = `
        <span style="font-size: 1.2em;">${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</span>
        <span>${message}</span>
    `;

	notificationContainer.appendChild(notification);

	// Auto-eliminar después de 5 segundos
	setTimeout(() => {
		if (notification.parentNode) {
			notification.style.animation = 'slideOutRight 0.3s ease';
			setTimeout(() => {
				if (notification.parentNode) {
					notification.parentNode.removeChild(notification);
				}
			}, 300);
		}
	}, 5000);

	// Permitir cerrar manualmente
	notification.addEventListener('click', () => {
		if (notification.parentNode) {
			notification.style.animation = 'slideOutRight 0.3s ease';
			setTimeout(() => {
				if (notification.parentNode) {
					notification.parentNode.removeChild(notification);
				}
			}, 300);
		}
	});
}

// Función para formatear moneda
function formatCurrency(amount) {
	return new Intl.NumberFormat('es-CO', {
		style: 'currency',
		currency: 'COP',
		minimumFractionDigits: 0
	}).format(amount);
}

// Función para crear servicio (conexión con backend)
// ✅ MEJORAR la función crearServicio en profesional.js
function crearServicio(event) {
	event.preventDefault();
	console.log('🔄 Intentando crear servicio...');

	const form = event.target;

	// Validar campos
	if (!form.nombreServicio.value.trim()) {
		showNotification('❌ El nombre del servicio es requerido', 'error');
		form.nombreServicio.focus();
		return;
	}

	if (!form.descripcionServicio.value.trim()) {
		showNotification('❌ La descripción del servicio es requerida', 'error');
		form.descripcionServicio.focus();
		return;
	}

	const duracion = parseInt(form.duracionServicio.value);
	const precio = parseFloat(form.precioServicio.value);

	if (isNaN(duracion) || duracion < 15) {
		showNotification('❌ La duración mínima es 15 minutos', 'error');
		form.duracionServicio.focus();
		return;
	}

	if (isNaN(precio) || precio < 0) {
		showNotification('❌ El precio debe ser un número válido', 'error');
		form.precioServicio.focus();
		return;
	}

	const formData = new FormData();
	formData.append('nombreServicio', form.nombreServicio.value.trim());
	formData.append('descripcionServicio', form.descripcionServicio.value.trim());
	formData.append('duracionServicio', duracion);
	formData.append('precioServicio', precio);

	// Mostrar loading
	const submitBtn = form.querySelector('.btn-submit-service');
	const originalText = submitBtn.innerHTML;
	submitBtn.innerHTML = '⏳ Guardando...';
	submitBtn.disabled = true;

	console.log('📤 Enviando datos del servicio:', {
		nombre: form.nombreServicio.value.trim(),
		duracion: duracion,
		precio: precio
	});

	const token = document.querySelector('meta[name="_csrf"]').content;
	const header = document.querySelector('meta[name="_csrf_header"]').content;

	fetch('/profesional/crear-servicio', {
		method: 'POST',
		headers: {
			[header]: token
		},
		body: formData
	})

		.then(response => {
			console.log('📨 Respuesta crear servicio:', response.status);
			if (!response.ok) {
				return response.json().then(errorData => {
					throw new Error(errorData.message || 'Error del servidor');
				});
			}
			return response.json();
		})
		.then(data => {
			console.log('✅ Servicio creado:', data);
			if (data.success) {
				showNotification('✅ Servicio creado exitosamente', 'success');
				form.reset(); // Limpiar formulario
				cargarServiciosBackend(); // Recargar lista
			} else {
				throw new Error(data.message || 'Error al crear servicio');
			}
		})
		.catch(error => {
			console.error('❌ Error crear servicio:', error);
			showNotification('❌ ' + error.message, 'error');
		})
		.finally(() => {
			submitBtn.innerHTML = originalText;
			submitBtn.disabled = false;
		});
}

// Función para cargar servicios desde el backend
function cargarServiciosBackend() {
	fetch('/profesional/servicios')
		.then(response => {
			if (!response.ok) {
				throw new Error('Error al cargar servicios');
			}
			return response.json();
		})
		.then(servicios => {
			mostrarServiciosBackend(servicios);
		})
		.catch(error => {
			console.error('Error al cargar servicios:', error);
			showNotification('❌ Error al cargar servicios', 'error');
		});
}

// Función para mostrar servicios desde backend
function mostrarServiciosBackend(servicios) {
	const container = document.getElementById('servicesList');

	if (!servicios || servicios.length === 0) {
		container.innerHTML = `
            <h4 style="margin-top: 2rem; margin-bottom: 1rem; color: #4a5568;">Mis Servicios</h4>
            <div class="empty-services">
                <p>No has creado servicios aún</p>
            </div>
        `;
		return;
	}

	container.innerHTML = `
        <h4 style="margin-top: 2rem; margin-bottom: 1rem; color: #4a5568;">Mis Servicios (${servicios.length})</h4>
        ${servicios.map(servicio => `
            <div class="service-item" data-id="${servicio.id}">
                <div class="service-header">
                    <div class="service-title">${servicio.nombre}</div>
                    <button class="btn-delete-service" onclick="eliminarServicioBackend(${servicio.id})" title="Eliminar servicio">
                        🗑️ Eliminar
                    </button>
                </div>
                <div class="service-description">${servicio.descripcion}</div>
                <div class="service-details">
                    <div class="service-detail">
                        <span>⏱️ Duración:</span>
                        <span>${servicio.duracion}</span>
                    </div>
                    <div class="service-detail">
                        <span>💰 Precio:</span>
                        <span>${formatCurrency(servicio.precio)}</span>
                    </div>
                </div>
            </div>
        `).join('')}
    `;
}

// Función para eliminar servicio en el backend
function eliminarServicioBackend(id) {
	if (confirm('¿Estás seguro de que deseas eliminar este servicio?')) {
		fetch(`/profesional/eliminar-servicio/${id}`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			}
		})
			.then(response => {
				if (!response.ok) {
					throw new Error('Error en la respuesta del servidor');
				}
				return response.json();
			})
			.then(data => {
				if (data.success) {
					showNotification('✅ Servicio eliminado correctamente', 'info');
					cargarServiciosBackend();
				} else {
					showNotification('❌ Error: ' + data.message, 'error');
				}
			})
			.catch(error => {
				console.error('Error:', error);
				showNotification('❌ Error al eliminar servicio', 'error');
			});
	}
}

// ===== FUNCIONES PARA GESTIONAR CITAS =====

// Función para aceptar una cita
function aceptarCita(citaId) {
	if (confirm('¿Estás seguro de que deseas aceptar esta cita?')) {
		actualizarEstadoCita(citaId, 'ACEPTADA');
	}
}

// Función para rechazar una cita
function rechazarCita(citaId) {
	if (confirm('¿Estás seguro de que deseas rechazar esta cita?')) {
		actualizarEstadoCita(citaId, 'RECHAZADA');
	}
}

// Función para marcar cita como completada
function completarCita(citaId) {
	if (confirm('¿Marcar esta cita como completada?')) {
		actualizarEstadoCita(citaId, 'COMPLETADA');
	}
}

// profesional.js - REEMPLAZAR la función actualizarEstadoCita completa
function actualizarEstadoCita(citaId, estado) {
	console.log(`🔄 Actualizando cita ${citaId} a estado: ${estado}`);

	// Obtener el token CSRF del formulario
	const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

	if (!csrfToken) {
		console.error('❌ No se encontró token CSRF');
		showNotification('❌ Error de seguridad: token no encontrado', 'error');
		return;
	}

	const formData = new FormData();
	formData.append('estado', estado);
	formData.append('_csrf', csrfToken);

	// Mostrar loading en los botones
	const buttons = document.querySelectorAll(`button[onclick*="${citaId}"]`);
	buttons.forEach(btn => {
		btn.disabled = true;
		btn.dataset.originalText = btn.innerHTML;
		btn.innerHTML = '⏳ Procesando...';
	});

	fetch(`/profesional/cita/${citaId}/estado`, {
		method: 'POST',
		body: formData,
		headers: {
			'X-Requested-With': 'XMLHttpRequest'
		}
	})
		.then(response => {
			console.log('📨 Respuesta recibida:', response.status);

			if (response.status === 403) {
				throw new Error('No tienes permisos para esta acción');
			}
			if (response.status === 404) {
				throw new Error('Cita no encontrada');
			}
			if (!response.ok) {
				return response.json().then(errorData => {
					throw new Error(errorData.message || `Error del servidor: ${response.status}`);
				}).catch(() => {
					throw new Error(`Error del servidor: ${response.status}`);
				});
			}
			return response.json();
		})
		.then(data => {
			console.log('✅ Respuesta exitosa:', data);

			if (data.success) {
				const mensajes = {
					'ACEPTADA': '✅ Cita aceptada correctamente',
					'RECHAZADA': '❌ Cita rechazada correctamente',
					'COMPLETADA': '🎉 Cita marcada como completada'
				};
				showNotification(mensajes[estado] || '✅ Estado actualizado', 'success');

				// Recargar la página después de 1.5 segundos
				setTimeout(() => {
					window.location.reload();
				}, 1500);
			} else {
				throw new Error(data.message || 'Error al actualizar la cita');
			}
		})
		.catch(error => {
			console.error('❌ Error completo:', error);
			showNotification('❌ ' + error.message, 'error');

			// Restaurar botones en caso de error
			buttons.forEach(btn => {
				btn.disabled = false;
				const originalText = btn.dataset.originalText;
				if (originalText) {
					btn.innerHTML = originalText;
				}
			});
		});
}



fetch(`/profesional/cita/${citaId}/estado`, {
	method: 'POST',
	body: formData
})
	.then(response => {
		if (!response.ok) {
			throw new Error('Error en la respuesta del servidor');
		}
		return response.text();
	})
	.then(() => {
		const mensajes = {
			'ACEPTADA': '✅ Cita aceptada correctamente',
			'RECHAZADA': '❌ Cita rechazada correctamente',
			'COMPLETADA': '🎉 Cita marcada como completada'
		};
		showNotification(mensajes[estado], 'success');

		// Recargar la página después de 1.5 segundos para ver los cambios
		setTimeout(() => {
			window.location.reload();
		}, 1500);
	})
	.catch(error => {
		console.error('Error:', error);
		showNotification('❌ Error al actualizar la cita', 'error');

		// Restaurar botones en caso de error
		buttons.forEach(btn => {
			btn.disabled = false;
			if (estado === 'ACEPTADA') {
				btn.innerHTML = '✅ Aceptar';
			} else if (estado === 'RECHAZADA') {
				btn.innerHTML = '❌ Rechazar';
			} else if (estado === 'COMPLETADA') {
				btn.innerHTML = '🎉 Completada';
			}
		});
	});


// Funciones para eliminar cuenta
function abrirModalEliminar() {
	const modal = document.getElementById('modalEliminar');
	const input = document.getElementById('confirmacionTexto');
	const btnConfirmar = document.getElementById('btnConfirmar');

	modal.style.display = 'flex';
	input.value = '';
	btnConfirmar.disabled = true;
	input.focus();
}

function cerrarModalEliminar() {
	const modal = document.getElementById('modalEliminar');
	modal.style.display = 'none';
}

function verificarConfirmacion() {
	const input = document.getElementById('confirmacionTexto');
	const btnConfirmar = document.getElementById('btnConfirmar');

	if (input.value.trim().toUpperCase() === 'ELIMINAR') {
		btnConfirmar.disabled = false;
	} else {
		btnConfirmar.disabled = true;
	}
}

function confirmarEliminacion() {
	const btnConfirmar = document.getElementById('btnConfirmar');

	if (btnConfirmar.disabled) {
		return;
	}

	// Mostrar loading
	btnConfirmar.innerHTML = '⏳ Eliminando...';
	btnConfirmar.disabled = true;

	// Enviar el formulario de eliminación
	document.getElementById('formEliminarCuenta').submit();
}

// Funciones del dashboard
function initializeDashboard() {
	console.log('Dashboard profesional inicializado');
}

function loadDashboardData() {
	console.log('Cargando datos del dashboard profesional');
}

function setupEventListeners() {
	console.log('Configurando event listeners del dashboard');
}

function verTodasCitas() {
	showNotification('🔧 Función en desarrollo - Próximamente podrás ver todas tus citas', 'info');
}


// ===== FUNCIONES PARA EDITAR SERVICIOS =====

// Función para abrir el modal de editar servicio
function abrirModalEditarServicio(servicioId) {
	// Mostrar loading en el modal
	const modal = document.getElementById('modalEditarServicio');
	const contenido = modal.querySelector('.modal-editar-content');
	contenido.classList.add('modal-loading');

	modal.style.display = 'flex';

	// Cargar los datos del servicio
	fetch(`/profesional/servicio/${servicioId}`)
		.then(response => {
			if (!response.ok) {
				throw new Error('Error al cargar servicio');
			}
			return response.json();
		})
		.then(servicio => {
			// Llenar el formulario con los datos del servicio
			document.getElementById('editServicioId').value = servicio.id;
			document.getElementById('editNombreServicio').value = servicio.nombre;
			document.getElementById('editDescripcionServicio').value = servicio.descripcion;

			// Extraer solo el número de la duración (eliminar " minutos")
			const duracion = servicio.duracion ? parseInt(servicio.duracion) : 60;
			document.getElementById('editDuracionServicio').value = duracion;

			document.getElementById('editPrecioServicio').value = servicio.precio;

			// Quitar loading
			contenido.classList.remove('modal-loading');
		})
		.catch(error => {
			console.error('Error:', error);
			showNotification('❌ Error al cargar servicio', 'error');
			cerrarModalEditarServicio();
		});
}

// Función para cerrar el modal de editar servicio
function cerrarModalEditarServicio() {
	const modal = document.getElementById('modalEditarServicio');
	modal.style.display = 'none';

	// Limpiar formulario
	document.getElementById('formEditarServicio').reset();
}

// Función para guardar los cambios del servicio
function guardarServicioEditado(event) {
	event.preventDefault();

	const form = event.target;
	const servicioId = form.editServicioId.value;

	// Validar campos
	if (!form.editNombreServicio.value.trim() || !form.editDescripcionServicio.value.trim()) {
		showNotification('❌ Por favor completa todos los campos', 'error');
		return;
	}

	const formData = new FormData();
	formData.append('servicioId', servicioId);
	formData.append('nombreServicio', form.editNombreServicio.value);
	formData.append('descripcionServicio', form.editDescripcionServicio.value);
	formData.append('duracionServicio', parseInt(form.editDuracionServicio.value));
	formData.append('precioServicio', parseFloat(form.editPrecioServicio.value));

	// Mostrar loading
	const submitBtn = form.querySelector('.btn-guardar-editar');
	const originalText = submitBtn.innerHTML;
	submitBtn.innerHTML = '⏳ Guardando...';
	submitBtn.disabled = true;

	fetch('/profesional/actualizar-servicio', {
		method: 'POST',
		body: formData
	})
		.then(response => {
			if (!response.ok) {
				throw new Error('Error en la respuesta del servidor');
			}
			return response.json();
		})
		.then(data => {
			if (data.success) {
				showNotification('✅ Servicio actualizado exitosamente', 'success');
				cerrarModalEditarServicio();
				cargarServiciosBackend(); // Recargar la lista
			} else {
				showNotification('❌ Error: ' + data.message, 'error');
			}
		})
		.catch(error => {
			console.error('Error:', error);
			showNotification('❌ Error de conexión con el servidor', 'error');
		})
		.finally(() => {
			submitBtn.innerHTML = originalText;
			submitBtn.disabled = false;
		});
}

// Actualizar la función mostrarServiciosBackend para incluir botón de editar
function mostrarServiciosBackend(servicios) {
	const container = document.getElementById('servicesList');

	if (!servicios || servicios.length === 0) {
		container.innerHTML = `
            <h4 style="margin-top: 2rem; margin-bottom: 1rem; color: #4a5568;">Mis Servicios</h4>
            <div class="empty-services">
                <p>No has creado servicios aún</p>
            </div>
        `;
		return;
	}

	container.innerHTML = `
        <h4 style="margin-top: 2rem; margin-bottom: 1rem; color: #4a5568;">Mis Servicios (${servicios.length})</h4>
        ${servicios.map(servicio => `
            <div class="service-item" data-id="${servicio.id}">
                <div class="service-header">
                    <div class="service-title">${servicio.nombre}</div>
                    <div class="service-actions">
                        <button class="btn-edit-service" onclick="abrirModalEditarServicio(${servicio.id})" title="Editar servicio">
                            ✏️ Editar
                        </button>
                        <button class="btn-delete-service" onclick="eliminarServicioBackend(${servicio.id})" title="Eliminar servicio">
                            🗑️ Eliminar
                        </button>
                    </div>
                </div>
                <div class="service-description">${servicio.descripcion}</div>
                <div class="service-details">
                    <div class="service-detail">
                        <span>⏱️ Duración:</span>
                        <span>${servicio.duracion}</span>
                    </div>
                    <div class="service-detail">
                        <span>💰 Precio:</span>
                        <span>${formatCurrency(servicio.precio)}</span>
                    </div>
                </div>
            </div>
        `).join('')}
    `;
}

// Animaciones CSS para las notificaciones
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// profesional.js - ARCHIVO CORREGIDO

// ===== ELIMINAR ESTA SECCIÓN DUPLICADA (líneas 165-210) =====
// (Todo el código desde "// Mostrar loading en el botón correspondiente" 
// hasta el final del catch duplicado)

// ===== COMBINAR LOS DOS DOMContentLoaded =====
document.addEventListener('DOMContentLoaded', function() {
	initializeDashboard();
	loadDashboardData();
	setupEventListeners();
	cargarServiciosBackend();

	// Agregar event listener para el input de confirmación de eliminación
	const inputConfirmacion = document.getElementById('confirmacionTexto');
	if (inputConfirmacion) {
		inputConfirmacion.addEventListener('input', verificarConfirmacion);
		inputConfirmacion.addEventListener('keypress', function(e) {
			if (e.key === 'Enter' && !document.getElementById('btnConfirmar').disabled) {
				confirmarEliminacion();
			}
		});
	}

	// Cerrar modal al hacer clic fuera del contenido
	const modal = document.getElementById('modalEliminar');
	if (modal) {
		modal.addEventListener('click', function(e) {
			if (e.target === modal) {
				cerrarModalEliminar();
			}
		});
	}

	// Cerrar modal de editar servicio al hacer clic fuera
	const modalEditar = document.getElementById('modalEditarServicio');
	if (modalEditar) {
		modalEditar.addEventListener('click', function(e) {
			if (e.target === modalEditar) {
				cerrarModalEditarServicio();
			}
		});
	}

	// Manejar mensajes flash del servidor (si existen)
	const successMessage = document.querySelector('[th\\:if="${success}"]') ||
		document.querySelector('.alert-success');
	const errorMessage = document.querySelector('[th\\:if="${error}"]') ||
		document.querySelector('.alert-error');

	if (successMessage) {
		showNotification(successMessage.textContent, 'success');
	}
	if (errorMessage) {
		showNotification(errorMessage.textContent, 'error');
	}
});