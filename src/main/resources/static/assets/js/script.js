// Smooth scroll para los enlaces de navegación
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
	anchor.addEventListener('click', function(e) {
		e.preventDefault();
		const target = document.querySelector(this.getAttribute('href'));
		if (target) {
			target.scrollIntoView({
				behavior: 'smooth',
				block: 'start'
			});
		}
	});
});

// Animación de aparición de las tarjetas al hacer scroll
const observerOptions = {
	threshold: 0.1,
	rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
	entries.forEach(entry => {
		if (entry.isIntersecting) {
			entry.target.style.opacity = '1';
			entry.target.style.transform = 'translateY(0)';
		}
	});
}, observerOptions);

// Aplicar animación inicial a las tarjetas
document.querySelectorAll('.card').forEach(card => {
	card.style.opacity = '0';
	card.style.transform = 'translateY(20px)';
	card.style.transition = 'all 0.5s ease';
	observer.observe(card);
});

// Efecto al hacer clic en el botón CTA
document.querySelector('.cta-button').addEventListener('click', () => {
	alert('¡Gracias por tu interés! Pronto nos pondremos en contacto contigo.');
});

// Efecto de cambio de color en el header al hacer scroll
let lastScroll = 0;
const header = document.querySelector('header');

window.addEventListener('scroll', () => {
	const currentScroll = window.pageYOffset;

	if (currentScroll > 100) {
		header.style.background = '#1a252f';
	} else {
		header.style.background = '#2c3e50';
	}

	lastScroll = currentScroll;
});


// Script adicional para el botón CTA mejorado
document.querySelector('.cta-button').addEventListener('click', () => {
	// Verificar si el usuario está logueado
	const isLoggedIn = false; // Aquí verificarías si hay sesión activa

	if (isLoggedIn) {
		// Si está logueado, ir a solicitar servicio
		alert('¡Gracias por tu interés! Pronto nos pondremos en contacto contigo.');
	} else {
		// Si no está logueado, sugerir registro
		const response = confirm('Para solicitar un servicio necesitas una cuenta. ¿Deseas registrarte?');
		if (response) {
			window.location.href = 'registro.html';
		}
	}
});