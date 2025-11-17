document.addEventListener('DOMContentLoaded', function() {
	// Elementos del DOM
	const serviceButtons = document.querySelectorAll('.service-btn');
	const serviceInput = document.getElementById('servicio');
	const professionalSelect = document.getElementById('profesional');
	const professionalDetails = document.getElementById('professional-details');
	const deviceSelect = document.getElementById('dispositivo');
	const dateInput = document.getElementById('fecha');
	const timeSelect = document.getElementById('hora');
	const duracionInput = document.getElementById('duracion');
	const precioInput = document.getElementById('precio');
	const form = document.querySelector('.reservation-form');

	// Establecer fecha mínima como hoy
	const today = new Date();
	const formattedDate = today.toISOString().split('T')[0];
	dateInput.min = formattedDate;

	// Manejar selección de servicio
	serviceButtons.forEach(button => {
		button.addEventListener('click', function() {
			// Remover clase activa de todos los botones
			serviceButtons.forEach(btn => btn.classList.remove('active'));

			// Agregar clase activa al botón clickeado
			this.classList.add('active');

			// Actualizar campo oculto
			const serviceType = this.getAttribute('data-service');
			serviceInput.value = serviceType;

			// Filtrar profesionales según el servicio seleccionado
			filterProfessionals(serviceType);

			// Actualizar campos ocultos de duración y precio
			updateHiddenFields(this);
		});
	});

	// Filtrar profesionales según el servicio seleccionado
	function filterProfessionals(serviceType) {
		const options = professionalSelect.querySelectorAll('option');

		// Mostrar todos los profesionales primero
		options.forEach(option => {
			if (option.value !== '') {
				option.style.display = '';
			}
		});

		// Si no hay servicio seleccionado, mostrar todos
		if (!serviceType) return;

		// Determinar qué especialidades mostrar según el servicio
		let specialtiesToShow = [];
		if (serviceType.toLowerCase().includes('hardware')) {
			specialtiesToShow = ['hardware', 'ambos', 'both', 'general'];
		} else if (serviceType.toLowerCase().includes('software')) {
			specialtiesToShow = ['software', 'ambos', 'both', 'general'];
		}

		// Ocultar profesionales que no coincidan con el servicio
		options.forEach(option => {
			if (option.value !== '') {
				const specialty = option.getAttribute('data-specialty');
				if (specialty) {
					const specialtyLower = specialty.toLowerCase();
					const shouldShow = specialtiesToShow.some(allowedSpecialty =>
						specialtyLower.includes(allowedSpecialty.toLowerCase())
					);

					if (!shouldShow) {
						option.style.display = 'none';
					}
				}
			}
		});

		// Resetear selección si el profesional actual no está disponible
		const currentValue = professionalSelect.value;
		const currentOption = professionalSelect.querySelector(`option[value="${currentValue}"]`);

		if (currentOption && currentOption.style.display === 'none') {
			professionalSelect.value = '';
			professionalDetails.style.display = 'none';
		}

		// Si solo queda un profesional disponible, seleccionarlo automáticamente
		const availableOptions = Array.from(options).filter(opt =>
			opt.value !== '' && opt.style.display !== 'none'
		);

		if (availableOptions.length === 1) {
			professionalSelect.value = availableOptions[0].value;
			professionalSelect.dispatchEvent(new Event('change'));
		}
	}

	// Actualizar campos ocultos de duración y precio
	function updateHiddenFields(serviceButton) {
		const duration = serviceButton.getAttribute('data-duration');
		const price = serviceButton.getAttribute('data-price');

		if (duracionInput) {
			duracionInput.value = duration;
		}
		if (precioInput) {
			precioInput.value = price;
		}
	}

	// Mostrar detalles del profesional
	professionalSelect.addEventListener('change', function() {
		const selectedOption = this.options[this.selectedIndex];

		if (this.value && selectedOption) {
			// Obtener datos del profesional
			const description = selectedOption.getAttribute('data-description') || 'Sin descripción disponible';
			const duration = selectedOption.getAttribute('data-duration') || '2-4 horas';
			const price = selectedOption.getAttribute('data-price') || '$50.000';

			// Actualizar contenido
			document.getElementById('detail-description').textContent = description;
			document.getElementById('detail-duration').textContent = duration;
			document.getElementById('detail-price').textContent = price;

			// Actualizar campos ocultos si es necesario
			if (duracionInput && !duracionInput.value) {
				duracionInput.value = duration;
			}
			if (precioInput && !precioInput.value) {
				precioInput.value = price.replace(/[$.]/g, '');
			}

			// Mostrar el contenedor con animación
			professionalDetails.style.display = 'block';
			professionalDetails.style.animation = 'fadeIn 0.3s ease-in-out';
		} else {
			// Ocultar si no hay selección
			professionalDetails.style.display = 'none';
		}
	});

	// Validación de fecha (no permitir fechas pasadas)
	dateInput.addEventListener('change', function() {
		const selectedDate = new Date(this.value);
		if (selectedDate < today) {
			highlightError(this);
			showNotification('La fecha seleccionada no puede ser en el pasado', 'error');
			this.value = '';
		} else {
			removeErrorHighlight(this);
		}
	});

	// Validación del formulario antes de enviar
	form.addEventListener('submit', function(e) {
		let isValid = true;
		const requiredFields = form.querySelectorAll('[required]');

		// Verificar campos requeridos
		requiredFields.forEach(field => {
			if (!field.value.trim()) {
				isValid = false;
				highlightError(field);
			} else {
				removeErrorHighlight(field);
			}
		});

		// Verificar que se haya seleccionado un servicio
		if (!serviceInput.value) {
			isValid = false;
			showNotification('Por favor selecciona un tipo de servicio', 'error');
			serviceButtons.forEach(btn => {
				btn.style.borderColor = '#e74c3c';
				btn.style.boxShadow = '0 0 0 2px rgba(231, 76, 60, 0.2)';
			});
		}

		// Verificar fecha válida (no en el pasado)
		if (dateInput.value) {
			const selectedDate = new Date(dateInput.value);
			if (selectedDate < today) {
				isValid = false;
				highlightError(dateInput);
				showNotification('La fecha seleccionada no puede ser en el pasado', 'error');
			}
		}

		// Verificar que se haya seleccionado un profesional
		if (!professionalSelect.value) {
			isValid = false;
			highlightError(professionalSelect);
			showNotification('Por favor selecciona un profesional', 'error');
		}

		if (!isValid) {
			e.preventDefault();
			showNotification('Por favor completa todos los campos requeridos correctamente', 'error');

			// Desplazarse al primer campo con error
			const firstError = form.querySelector('.error-highlight');
			if (firstError) {
				firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
			}
		} else {
			// Mostrar confirmación antes de enviar
			if (!confirm('¿Estás seguro de que quieres reservar esta cita?')) {
				e.preventDefault();
			}
		}
	});

	// Funciones para resaltar errores
	function highlightError(field) {
		field.classList.add('error-highlight');
		field.style.borderColor = '#e74c3c';
		field.style.boxShadow = '0 0 0 2px rgba(231, 76, 60, 0.2)';
	}

	function removeErrorHighlight(field) {
		field.classList.remove('error-highlight');
		field.style.borderColor = '#ddd';
		field.style.boxShadow = 'none';
	}

	// Función para mostrar notificaciones
	function showNotification(message, type) {
		// Crear elemento de notificación
		const notification = document.createElement('div');
		notification.className = `notification ${type}`;
		notification.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()">&times;</button>
        `;

		// Estilos para la notificación
		notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 5px;
            color: white;
            font-weight: 500;
            z-index: 1000;
            display: flex;
            align-items: center;
            justify-content: space-between;
            min-width: 300px;
            max-width: 500px;
            animation: slideIn 0.3s ease-out;
        `;

		if (type === 'error') {
			notification.style.backgroundColor = '#e74c3c';
		} else {
			notification.style.backgroundColor = '#2ecc71';
		}

		// Botón de cerrar
		notification.querySelector('button').style.cssText = `
            background: none;
            border: none;
            color: white;
            font-size: 20px;
            cursor: pointer;
            margin-left: 10px;
        `;

		document.body.appendChild(notification);

		// Auto-eliminar después de 5 segundos
		setTimeout(() => {
			if (notification.parentElement) {
				notification.remove();
			}
		}, 5000);
	}

	// Mejorar la experiencia de usuario con validación en tiempo real
	const formInputs = form.querySelectorAll('input, select, textarea');
	formInputs.forEach(input => {
		input.addEventListener('blur', function() {
			if (this.hasAttribute('required') && !this.value.trim()) {
				highlightError(this);
			} else {
				removeErrorHighlight(this);
			}
		});

		input.addEventListener('input', function() {
			if (this.value.trim()) {
				removeErrorHighlight(this);
			}
		});
	});

	// Inicializar el filtrado de profesionales si ya hay un servicio seleccionado
	if (serviceInput.value) {
		const activeButton = document.querySelector(`.service-btn[data-service="${serviceInput.value}"]`);
		if (activeButton) {
			activeButton.classList.add('active');
			filterProfessionals(serviceInput.value);
		}
	}

	// Auto-seleccionar el primer servicio si no hay ninguno seleccionado
	if (!serviceInput.value && serviceButtons.length > 0) {
		serviceButtons[0].click();
	}

	// Animación para elementos del formulario
	const formSections = document.querySelectorAll('.form-section');
	formSections.forEach((section, index) => {
		section.style.opacity = '0';
		section.style.transform = 'translateY(20px)';
		section.style.transition = 'opacity 0.5s ease, transform 0.5s ease';

		setTimeout(() => {
			section.style.opacity = '1';
			section.style.transform = 'translateY(0)';
		}, index * 200);
	});
});

// Agregar estilos CSS para las animaciones
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    .error-highlight {
        border-color: #e74c3c !important;
        box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.2) !important;
    }
    
    .service-btn.error-highlight {
        border-color: #e74c3c !important;
        box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.2) !important;
    }
    
    .notification {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
`;
document.head.appendChild(style);