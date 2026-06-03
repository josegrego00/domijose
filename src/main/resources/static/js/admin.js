document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    const sidebar = document.getElementById('adminSidebar');
    const overlay = document.getElementById('sidebarOverlay');

    function openSidebar() {
        sidebar.classList.add('active');
        overlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeSidebar() {
        sidebar.classList.remove('active');
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    }

    if (menuToggle) {
        menuToggle.addEventListener('click', openSidebar);
    }

    if (sidebarClose) {
        sidebarClose.addEventListener('click', closeSidebar);
    }

    if (overlay) {
        overlay.addEventListener('click', closeSidebar);
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeSidebar();
        }
    });

    actualizarSidebarVentasBadge();
});

function showConfirmModal(message) {
    return new Promise(function(resolve) {
        var overlay = document.getElementById('confirmModalOverlay');
        var modal = document.getElementById('confirmModal');
        var messageEl = document.getElementById('confirmModalMessage');
        var cancelBtn = document.getElementById('confirmModalCancel');
        var confirmBtn = document.getElementById('confirmModalConfirm');

        if (!overlay || !modal || !messageEl || !cancelBtn || !confirmBtn) {
            resolve(true);
            return;
        }

        messageEl.textContent = message;
        overlay.classList.add('active');
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';

        function cleanup(result) {
            overlay.classList.remove('active');
            modal.classList.remove('active');
            document.body.style.overflow = '';
            setTimeout(function() { resolve(result); }, 300);
        }

        cancelBtn.onclick = function() { cleanup(false); };
        confirmBtn.onclick = function() { cleanup(true); };
        overlay.onclick = function(e) {
            if (e.target === overlay) cleanup(false);
        };

        function handleKeydown(e) {
            if (e.key === 'Escape') {
                document.removeEventListener('keydown', handleKeydown);
                cleanup(false);
            }
        }
        document.addEventListener('keydown', handleKeydown);
    });
}

document.addEventListener('click', function(e) {
    var btn = e.target.closest('[data-confirm]');
    if (btn) {
        e.preventDefault();
        var form = btn.closest('form');
        showConfirmModal(btn.getAttribute('data-confirm')).then(function(confirmed) {
            if (confirmed && form) form.submit();
        });
    }
});

function actualizarSidebarVentasBadge() {
    const badge = document.getElementById('sidebar-ventas-badge');
    if (!badge) return;

    fetch('/admin/ventas/pendientes/count')
        .then(function(res) { return res.json(); })
        .then(function(count) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-flex' : 'none';
        })
        .catch(function() {});
}
