function cancelarCita(citaId) {
	if (confirm('¿Estás seguro de que deseas cancelar esta cita?')) {
		fetch('/citas/cancelar/' + citaId, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			}
		})
			.then(response => {
				if (!response.ok) {
					throw new Error('Error en la respuesta del servidor');
				}
				return response.text();
			})
			.then(result => {
				if (result === 'OK') {
					alert('Cita cancelada exitosamente');
					location.reload();
				} else {
					alert('Error al cancelar la cita: ' + result);
				}
			})
			.catch(error => {
				console.error('Error:', error);
				alert('Error al cancelar la cita: ' + error.message);
			});
	}
}