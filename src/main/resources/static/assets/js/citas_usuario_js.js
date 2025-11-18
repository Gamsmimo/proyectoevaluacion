function cancelarCita(id) {
	if (!confirm("¿Seguro que deseas cancelar esta cita?")) return;

	const token = document.querySelector('meta[name="_csrf"]').content;
	const header = document.querySelector('meta[name="_csrf_header"]').content;

	fetch(`/citas/cancelar-cita/${id}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			[header]: token
		}
	})
		.then(res => res.json())
		.then(data => {
			alert(data.message);
			location.reload();
		})
		.catch(err => console.error(err));
}
