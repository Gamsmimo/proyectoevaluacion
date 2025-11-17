// En profesional.js - SOLO las funciones modificadas:

// Función para crear servicio (conexión con backend)
function crearServicio(event) {
	event.preventDefault();

	const form = event.target;

	// Validar campos
	if (!form.nombreServicio.value.trim() || !form.descripcionServicio.value.trim()) {
		showNotification('❌ Por favor completa todos los campos', 'error');
		return;
	}

	const formData = new FormData();

	formData.append('nombreServicio', form.nombreServicio.value);
	formData.append('descripcionServicio', form.descripcionServicio.value);
	formData.append('duracionServicio', parseInt(form.duracionServicio.value));
	formData.append('precioServicio', parseFloat(form.precioServicio.value));

	// Mostrar loading
	const submitBtn = form.querySelector('.btn-submit-service');
	const originalText = submitBtn.innerHTML;
	submitBtn.innerHTML = '⏳ Guardando...';
	submitBtn.disabled = true;

	fetch('/profesional/crear-servicio', {
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
				showNotification('✅ Servicio creado exitosamente', 'success');
				form.reset();
				cargarServiciosBackend();
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

// En DOMContentLoaded, cambiar a:
document.addEventListener('DOMContentLoaded', function() {
	initializeDashboard();
	loadDashboardData();
	setupEventListeners();
	cargarServiciosBackend(); // Cambiado para cargar desde backend
});