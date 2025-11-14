// dashboard-profesional.js
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    loadDashboardData();
    setupEventListeners();
});

function initializeDashboard() {
    console.log('Inicializando dashboard profesional...');
    
    // Mostrar datos iniciales
    updateStats();
    loadAppointments();
}

function loadDashboardData() {
    // Simular carga de datos del dashboard
    setTimeout(() => {
        updateStats({
            citasHoy: 3,
            ingresosMes: 1250000,
            calificacion: 4.8,
            totalClientes: 24
        });
        
        loadSampleAppointments();
    }, 1000);
}

function updateStats(data = {}) {
    const stats = {
        citasHoy: data.citasHoy || 0,
        ingresosMes: data.ingresosMes || 0,
        calificacion: data.calificacion || 5.0,
        totalClientes: data.totalClientes || 0
    };

    // Actualizar UI
    document.getElementById('citasHoy').textContent = stats.citasHoy;
    document.getElementById('ingresosMes').textContent = formatCurrency(stats.ingresosMes);
    document.getElementById('calificacion').textContent = stats.calificacion;
    document.getElementById('totalClientes').textContent = stats.totalClientes;
}

function loadAppointments() {
    // En una implementación real, aquí harías una petición al servidor
    console.log('Cargando citas...');
}

function loadSampleAppointments() {
    const appointments = [
        {
            id: 1,
            cliente: 'María González',
            servicio: 'Reparación de computadora',
            hora: '10:00 AM',
            fecha: 'Hoy'
        },
        {
            id: 2,
            cliente: 'Carlos Rodríguez',
            servicio: 'Instalación de software',
            hora: '2:30 PM',
            fecha: 'Hoy'
        },
        {
            id: 3,
            cliente: 'Ana Martínez',
            servicio: 'Mantenimiento preventivo',
            hora: '4:00 PM',
            fecha: 'Mañana'
        }
    ];

    displayAppointments(appointments);
}

function displayAppointments(appointments) {
    const container = document.getElementById('appointmentsList');
    
    if (appointments.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <p>No tienes citas programadas</p>
            </div>
        `;
        return;
    }

    container.innerHTML = appointments.map(appointment => `
        <div class="appointment-item" data-id="${appointment.id}">
            <div class="appointment-info">
                <h4>${appointment.cliente}</h4>
                <p>${appointment.servicio}</p>
                <small>${appointment.fecha}</small>
            </div>
            <div class="appointment-time">
                ${appointment.hora}
            </div>
        </div>
    `).join('');
}

function setupEventListeners() {
    // Agregar event listeners para las acciones
    console.log('Configurando event listeners...');
}

// Funciones de acciones
function gestionarCitas() {
    showNotification('Redirigiendo a gestión de citas...');
    // window.location.href = '/profesional/citas';
}

function verClientes() {
    showNotification('Cargando lista de clientes...');
    // window.location.href = '/profesional/clientes';
}

function actualizarHorario() {
    showNotification('Abriendo editor de horario...');
    // window.location.href = '/profesional/horario';
}

function verCalificaciones() {
    showNotification('Mostrando calificaciones...');
    // window.location.href = '/profesional/calificaciones';
}

function verTodasCitas() {
    showNotification('Cargando todas las citas...');
    // window.location.href = '/profesional/citas';
}

// Utilidades
function formatCurrency(amount) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(amount);
}

function showNotification(message, type = 'info') {
    // Crear notificación temporal
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'error' ? '#e53e3e' : '#38a169'};
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 1000;
        animation: slideInRight 0.3s ease;
    `;

    document.body.appendChild(notification);

    // Remover después de 3 segundos
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// CSS para animaciones de notificación
const notificationStyles = `
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

// Inyectar estilos de notificación
const styleSheet = document.createElement('style');
styleSheet.textContent = notificationStyles;
document.head.appendChild(styleSheet);

// Simular datos en tiempo real (para demo)
setInterval(() => {
    const randomChange = Math.random() > 0.5 ? 1 : -1;
    const citasElement = document.getElementById('citasHoy');
    let citas = parseInt(citasElement.textContent) || 0;
    
    if (citas + randomChange >= 0) {
        citasElement.textContent = citas + randomChange;
    }
}, 10000);