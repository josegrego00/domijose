let stompClient = null;

function conectarWebSocket() {
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') return;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/ventas', function (notificacion) {
            const data = JSON.parse(notificacion.body);
            mostrarNotificacion(data);
        });
        stompClient.subscribe('/topic/dashboard', function (data) {
            const dto = JSON.parse(data.body);
            actualizarDashboard(dto);
        });
    });
}

function mostrarNotificacion(data) {
    actualizarSidebarVentasBadge();
    mostrarToastNotificacion(data);
    reproducirSonidoNotificacion();
}

function reproducirSonidoNotificacion() {
    try {
        var audio = new Audio('/sounds/notificacion.mp3');
        audio.volume = 0.5;
        audio.play();
    } catch (e) {}
}

function mostrarToastNotificacion(data) {
    var icono = '🛵';
    var titulo = 'Nuevo pedido';
    if (data.tipo === 'CANCELADA_CLIENTE') {
        icono = '❌';
        titulo = 'Pedido cancelado por cliente';
    }
    var toast = document.createElement('div');
    toast.className = 'notif-toast';
    toast.innerHTML =
        '<div class="notif-toast-icon">' + icono + '</div>' +
        '<div class="notif-toast-content">' +
        '<div class="notif-toast-title">' + titulo + '</div>' +
        '<div class="notif-toast-msg">' + data.mensaje + '</div>' +
        '</div>';
    document.body.appendChild(toast);
    setTimeout(function () {
        toast.classList.add('notif-toast-hide');
        setTimeout(function () { toast.remove(); }, 400);
    }, 5000);
}

function actualizarDashboard(dto) {
    var el;
    el = document.getElementById('stat-pendientes');
    if (el) el.textContent = dto.cantidadPendientes;
    el = document.getElementById('stat-productos');
    if (el) el.textContent = dto.totalProductos;
    el = document.getElementById('stat-usuarios');
    if (el) el.textContent = dto.totalUsuarios;
    el = document.getElementById('stat-promo');
    if (el) el.textContent = dto.productosPromocion;

    var sidebarBadge = document.getElementById('sidebar-ventas-badge');
    if (sidebarBadge) {
        sidebarBadge.textContent = dto.cantidadPendientes;
        sidebarBadge.style.display = dto.cantidadPendientes > 0 ? 'inline-flex' : 'none';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    conectarWebSocket();
});
