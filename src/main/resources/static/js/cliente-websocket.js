function conectarWebSocketCliente() {
    var token = document.querySelector('meta[name="_csrf"]');
    if (!token) return;

    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') return;

    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        stompClient.subscribe('/user/topic/pedidos', function (notificacion) {
            var data = JSON.parse(notificacion.body);
            mostrarToastCliente(data);
            actualizarEstadoPedido(data);
            reproducirSonidoCliente();
        });
    });
}

function mostrarToastCliente(data) {
    var icono = '🛵';
    if (data.tipo === 'ENVIADO') icono = '🚚';
    if (data.tipo === 'COMPLETADO') icono = '✅';
    if (data.tipo === 'CANCELADA') icono = '❌';

    var toast = document.createElement('div');
    toast.className = 'toast toast-success';
    toast.innerHTML = '<span style="margin-right: 0.5rem;">' + icono + '</span>' + data.mensaje;
    document.body.appendChild(toast);

    setTimeout(function () {
        toast.classList.add('toast-hide');
        setTimeout(function () { toast.remove(); }, 400);
    }, 5000);
}

function actualizarEstadoPedido(data) {
    if (!data.estado || !data.ventaId) return;
    var cards = document.querySelectorAll('.order-card');
    for (var i = 0; i < cards.length; i++) {
        var idEl = cards[i].querySelector('.order-id');
        if (idEl && idEl.textContent.includes('#' + data.ventaId)) {
            var badge = cards[i].querySelector('.order-status');
            if (badge) {
                badge.textContent = data.estado.replace(/_/g, ' ');
                badge.className = 'order-status status-' + data.estado.toLowerCase();
            }
            break;
        }
    }
}

function reproducirSonidoCliente() {
    try {
        var audio = new Audio('/sounds/notificacion.mp3');
        audio.volume = 0.5;
        audio.play();
    } catch (e) {}
}

document.addEventListener('DOMContentLoaded', function () {
    conectarWebSocketCliente();
});
