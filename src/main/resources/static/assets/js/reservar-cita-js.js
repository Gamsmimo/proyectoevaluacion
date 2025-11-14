// Configurar fecha mínima (hoy) y máxima (30 días adelante)
document.addEventListener('DOMContentLoaded', function() {
	const fechaInput = document.getElementById('fecha');
	const today = new Date();
	const maxDate = new Date();
	maxDate.setDate(today.getDate() + 30);

	// Formatear fechas a YYYY-MM-DD
	const formatDate = (date) => {
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, '0');
		const day = String(date.getDate()).padStart(2, '0');
		return `${year}-${month}-${day}`;
	};

	fechaInput.min = formatDate(today);
	fechaInput.max = formatDate(maxDate);

	// Deshabilitar domingos
	fechaInput.addEventListener('input', function() {
		const selectedDate = new Date(this.value + 'T00:00:00');
		if (selectedDate.getDay() === 0) {
			alert('Los domingos no hay atención. Por favor selecciona otro día.');
			this.value = '';
		}
	});
});

// Animación de selección de servicio
document.querySelectorAll('.service-option input[type="radio"]').forEach(radio => {
	radio.addEventListener('change', function() {
		// Remover animación de todas las tarjetas
		document.querySelectorAll('.service-card').forEach(card => {
			card.style.animation = 'none';
		});

		// Agregar animación a la tarjeta seleccionada
		if (this.checked) {
			const card = this.nextElementSibling;
			card.style.animation = 'pulse 0.5s ease';

			// Actualizar campos ocultos según el servicio seleccionado
			const duracionInput = document.getElementById('duracion');
			const precioInput = document.getElementById('precio');

			if (this.value === 'Reparación de Hardware') {
				duracionInput.value = '2-4 horas';
				precioInput.value = '50000';
			} else if (this.value === 'Reparación de Software') {
				duracionInput.value = '1-3 horas';
				precioInput.value = '30000';
			}
		}
	});
});

// Validación del formulario antes de enviar
document.querySelector('.reservation-form').addEventListener('submit', function(e) {
	e.preventDefault();

	// Verificar que se haya seleccionado un servicio
	const servicioSeleccionado = document.querySelector('input[name="servicio"]:checked');
	if (!servicioSeleccionado) {
		alert('Por favor selecciona un tipo de servicio.');
		return;
	}

	// Verificar fecha
	const fecha = document.getElementById('fecha').value;
	if (!fecha) {
		alert('Por favor selecciona una fecha.');
		return;
	}

	// Verificar hora
	const hora = document.getElementById('hora').value;
	if (!hora) {
		alert('Por favor selecciona una hora.');
		return;
	}

	// Validar que la fecha no sea sábado después de las 2 PM
	const selectedDate = new Date(fecha + 'T00:00:00');
	if (selectedDate.getDay() === 6 && parseInt(hora.split(':')[0]) >= 14) {
		alert('Los sábados solo hay atención hasta las 2:00 PM.');
		return;
	}

	// Confirmación antes de enviar
	const nombre = document.getElementById('nombre').value;
	const tipoServicio = servicioSeleccionado.value;
	const confirmacion = confirm(
		`¿Confirmas tu reserva?\n\n` +
		`Nombre: ${nombre}\n` +
		`Servicio: ${tipoServicio}\n` +
		`Fecha: ${fecha}\n` +
		`Hora: ${hora}`
	);

	if (confirmacion) {
		// Aquí se enviaría el formulario
		this.submit();
		// O mostrar mensaje de éxito
		// alert('¡Cita reservada exitosamente! Recibirás un correo de confirmación.');
	}
});

// Animación CSS para el pulso
const style = document.createElement('style');
style.textContent = `
    @keyframes pulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.05); }
        100% { transform: scale(1); }
    }
`;
document.head.appendChild(style);

// Actualizar opciones de hora según el día seleccionado
document.getElementById('fecha').addEventListener('change', function() {
	const selectedDate = new Date(this.value + 'T00:00:00');
	const horaSelect = document.getElementById('hora');
	const isSaturday = selectedDate.getDay() === 6;

	// Si es sábado, deshabilitar horas después de las 2 PM
	Array.from(horaSelect.options).forEach(option => {
		if (option.value) {
			const hour = parseInt(option.value.split(':')[0]);
			if (isSaturday && hour >= 14) {
				option.disabled = true;
				option.text = option.text + ' (No disponible)';
			} else {
				option.disabled = false;
				option.text = option.text.replace(' (No disponible)', '');
			}
		}
	});
});

// Mostrar información adicional según el tipo de dispositivo
document.getElementById('dispositivo').addEventListener('change', function() {
	const descripcionTextarea = document.getElementById('descripcion');
	const dispositivo = this.value;

	let placeholder = 'Describe detalladamente el problema que presenta tu dispositivo...';

	switch (dispositivo) {
		case 'pc-escritorio':
			placeholder += '\n\nEjemplo: No enciende, hace ruidos extraños, se apaga solo, etc.';
			break;
		case 'laptop':
			placeholder += '\n\nEjemplo: No carga la batería, pantalla rota, teclado no funciona, etc.';
			break;
		case 'all-in-one':
			placeholder += '\n\nEjemplo: Pantalla táctil no responde, problemas de conectividad, etc.';
			break;
		case 'tablet':
			placeholder += '\n\nEjemplo: Pantalla rota, no enciende, problemas de carga, etc.';
			break;
	}

	descripcionTextarea.placeholder = placeholder;
});