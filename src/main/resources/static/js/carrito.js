document.addEventListener('DOMContentLoaded', function() {
    var rutaSelect = document.getElementById('ruta');
    var deliveryCostDiv = document.getElementById('delivery-cost');
    var deliveryCostValue = document.getElementById('delivery-cost-value');
    var grandTotalSpan = document.getElementById('grand-total');
    var esDomicilioCheck = document.getElementById('esDomicilio');
    var deliveryFields = document.getElementById('delivery-fields');
    var pickupInfo = document.getElementById('pickup-info');
    var productosTotalElement = document.getElementById('productos-total-data');

    function updateTotal() {
        var productosTotal = productosTotalElement ? parseFloat(productosTotalElement.value) || 0 : 0;
        var isDelivery = esDomicilioCheck && esDomicilioCheck.checked;

        if (isDelivery) {
            var selectedOption = rutaSelect.options[rutaSelect.selectedIndex];
            var precioRuta = selectedOption && selectedOption.value ? parseFloat(selectedOption.text.split('$')[1].replace(',', '')) || 0 : 0;

            if (precioRuta > 0) {
                deliveryCostDiv.style.display = 'flex';
                deliveryCostValue.textContent = '$' + precioRuta.toLocaleString('es-CO');
            } else {
                deliveryCostDiv.style.display = 'none';
            }

            var total = productosTotal + precioRuta;
            grandTotalSpan.textContent = '$' + total.toLocaleString('es-CO');
        } else {
            deliveryCostDiv.style.display = 'none';
            grandTotalSpan.textContent = '$' + productosTotal.toLocaleString('es-CO');
        }
    }

    function toggleDeliveryFields() {
        var isDelivery = esDomicilioCheck && esDomicilioCheck.checked;
        if (deliveryFields) {
            deliveryFields.style.display = isDelivery ? 'block' : 'none';
        }
        if (pickupInfo) {
            pickupInfo.style.display = isDelivery ? 'none' : 'flex';
        }
        updateTotal();
    }

    if (esDomicilioCheck) {
        esDomicilioCheck.addEventListener('change', toggleDeliveryFields);
    }

    if (rutaSelect) {
        rutaSelect.addEventListener('change', updateTotal);
    }

    toggleDeliveryFields();
});

function mostrarToast(mensaje, tipo) {
    var toast = document.createElement('div');
    toast.className = 'toast toast-' + tipo;
    toast.textContent = mensaje;
    document.body.appendChild(toast);
    setTimeout(function() {
        toast.classList.add('toast-hide');
        setTimeout(function() { toast.remove(); }, 400);
    }, 3000);
}

function getCsrfHeaders() {
    var token = document.querySelector('meta[name="_csrf"]');
    var header = document.querySelector('meta[name="_csrf_header"]');
    if (token && header) {
        var headers = { 'Content-Type': 'application/json' };
        headers[header.content] = token.content;
        return headers;
    }
    return { 'Content-Type': 'application/json' };
}

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

function getTotalProductos() {
    var el = document.getElementById('productos-total-data');
    return el ? parseFloat(el.value) || 0 : 0;
}

function getTotalGeneral() {
    var esDomicilioCheck = document.getElementById('esDomicilio');
    var rutaSelect = document.getElementById('ruta');
    var totalProductos = getTotalProductos();
    var total = totalProductos;
    if (esDomicilioCheck && esDomicilioCheck.checked) {
        var selectedOption = rutaSelect ? rutaSelect.options[rutaSelect.selectedIndex] : null;
        var precioRuta = selectedOption && selectedOption.value ? parseFloat(selectedOption.text.split('$')[1].replace(',', '')) || 0 : 0;
        total += precioRuta;
    }
    return total;
}

function togglePaymentMethod() {
    var radios = document.querySelectorAll('input[name="metodoPago"]');
    var selected;
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) { selected = radios[i].value; break; }
    }

    document.getElementById('payment-efectivo').style.display = selected === 'EFECTIVO' ? 'block' : 'none';
    document.getElementById('payment-transferencia').style.display = selected === 'TRANSFERENCIA' ? 'block' : 'none';
    document.getElementById('payment-mixto').style.display = selected === 'MIXTO' ? 'block' : 'none';

    if (selected === 'TRANSFERENCIA') {
        var total = getTotalGeneral();
        document.getElementById('transfer-amount').textContent = '$' + total.toLocaleString('es-CO');
    }
}

function calcularCambio() {
    var input = document.getElementById('efectivoRecibidoInput');
    var monto = parseFloat(input.value) || 0;
    var total = getTotalGeneral();
    var cambioInfo = document.getElementById('cambio-info');
    var cambioValue = document.getElementById('cambio-value');

    if (monto >= total) {
        cambioInfo.style.display = 'flex';
        cambioValue.textContent = '$' + (monto - total).toLocaleString('es-CO');
    } else if (monto > 0) {
        cambioInfo.style.display = 'flex';
        cambioValue.textContent = '$0 (te falta $' + (total - monto).toLocaleString('es-CO') + ')';
    } else {
        cambioInfo.style.display = 'none';
    }
}

function calcularMixto() {
    var input = document.getElementById('efectivoMixtoInput');
    var efectivo = parseFloat(input.value) || 0;
    var total = getTotalGeneral();
    var resto = Math.max(0, total - efectivo);
    var restoEl = document.getElementById('resto-transferencia-value');

    if (efectivo > 0) {
        restoEl.textContent = '$' + resto.toLocaleString('es-CO');
    } else {
        restoEl.textContent = '$' + total.toLocaleString('es-CO');
    }
}

function mostrarInfoBanco(select, containerId) {
    var container = document.getElementById(containerId);
    if (!select || !select.value) {
        container.style.display = 'none';
        return;
    }

    var option = select.options[select.selectedIndex];
    document.getElementById(containerId === 'banco-info' ? 'banco-nombre' : 'banco-nombre-mixto').textContent = option.getAttribute('data-banco');
    document.getElementById(containerId === 'banco-info' ? 'banco-tipo' : 'banco-tipo-mixto').textContent = option.getAttribute('data-tipo');
    document.getElementById(containerId === 'banco-info' ? 'banco-numero' : 'banco-numero-mixto').textContent = option.getAttribute('data-numero');
    document.getElementById(containerId === 'banco-info' ? 'banco-titular' : 'banco-titular-mixto').textContent = option.getAttribute('data-titular');

    var qrUrl = option.getAttribute('data-qr');
    var qrContainer = document.getElementById(containerId === 'banco-info' ? 'banco-qr-container' : 'banco-qr-container-mixto');
    var qrImg = document.getElementById(containerId === 'banco-info' ? 'banco-qr-img' : 'banco-qr-img-mixto');
    if (qrUrl) {
        qrImg.src = qrUrl;
        qrContainer.style.display = 'flex';
    } else {
        qrContainer.style.display = 'none';
    }

    container.style.display = 'block';
}

function syncPaymentFields() {
    var radios = document.querySelectorAll('input[name="metodoPago"]');
    var selected;
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) { selected = radios[i].value; break; }
    }

    var efectivoHidden = document.getElementById('efectivoRecibidoHidden');
    var cuentaHidden = document.getElementById('cuentaBancoIdHidden');

    if (selected === 'EFECTIVO') {
        efectivoHidden.value = document.getElementById('efectivoRecibidoInput').value;
        cuentaHidden.value = '';
    } else if (selected === 'TRANSFERENCIA') {
        efectivoHidden.value = '';
        cuentaHidden.value = document.getElementById('cuentaBancoSelect').value;
    } else if (selected === 'MIXTO') {
        efectivoHidden.value = document.getElementById('efectivoMixtoInput').value;
        cuentaHidden.value = document.getElementById('cuentaBancoMixtoSelect').value;
    }
}

function showFieldError(elementId, message) {
    var errorEl = document.getElementById(elementId);
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.style.display = 'block';
    }
}

function clearFieldErrors() {
    var errors = document.querySelectorAll('.field-error');
    for (var i = 0; i < errors.length; i++) {
        errors[i].style.display = 'none';
        errors[i].textContent = '';
    }
    var groups = document.querySelectorAll('.form-group.invalid');
    for (var i = 0; i < groups.length; i++) {
        groups[i].classList.remove('invalid');
    }
}

function showCheckoutErrors(messages) {
    var container = document.getElementById('checkout-errors');
    if (!container) return;
    container.innerHTML = '';
    for (var i = 0; i < messages.length; i++) {
        var div = document.createElement('div');
        div.textContent = messages[i];
        container.appendChild(div);
    }
    container.style.display = 'block';
}

function validateCheckoutForm() {
    var errors = [];
    clearFieldErrors();
    var errorsContainer = document.getElementById('checkout-errors');
    if (errorsContainer) errorsContainer.style.display = 'none';

    var esDomicilioCheck = document.getElementById('esDomicilio');
    if (esDomicilioCheck && esDomicilioCheck.checked) {
        var rutaSelect = document.getElementById('ruta');
        if (rutaSelect && !rutaSelect.value) {
            errors.push('Selecciona un barrio para el domicilio');
            rutaSelect.closest('.form-group').classList.add('invalid');
        }
        var direccionInput = document.getElementById('direccion');
        if (direccionInput && !direccionInput.value.trim()) {
            errors.push('Escribe la dirección de entrega');
            direccionInput.closest('.form-group').classList.add('invalid');
        }
    }

    var radios = document.querySelectorAll('input[name="metodoPago"]');
    var selected;
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) { selected = radios[i].value; break; }
    }

    if (selected === 'EFECTIVO') {
        var efectivoInput = document.getElementById('efectivoRecibidoInput');
        var efectivoVal = parseFloat(efectivoInput.value) || 0;
        var total = getTotalGeneral();
        if (!efectivoInput.value.trim()) {
            errors.push('Indica con cuánto dinero vas a pagar');
            efectivoInput.closest('.form-group').classList.add('invalid');
        } else if (efectivoVal < total) {
            errors.push('El efectivo recibido ($' + efectivoVal.toLocaleString('es-CO') + ') es menor que el total ($' + total.toLocaleString('es-CO') + ')');
            efectivoInput.closest('.form-group').classList.add('invalid');
        }
    } else if (selected === 'TRANSFERENCIA') {
        var bancoSelect = document.getElementById('cuentaBancoSelect');
        if (!bancoSelect.value) {
            errors.push('Selecciona un banco para la transferencia');
            bancoSelect.closest('.form-group').classList.add('invalid');
        }
    } else if (selected === 'MIXTO') {
        var mixtoInput = document.getElementById('efectivoMixtoInput');
        var mixtoVal = parseFloat(mixtoInput.value) || 0;
        var total = getTotalGeneral();
        if (!mixtoInput.value.trim()) {
            errors.push('Indica cuánto vas a pagar en efectivo');
            mixtoInput.closest('.form-group').classList.add('invalid');
        } else if (mixtoVal <= 0) {
            errors.push('El efectivo debe ser mayor a $0');
            mixtoInput.closest('.form-group').classList.add('invalid');
        } else if (mixtoVal >= total) {
            errors.push('Si pagas todo en efectivo, selecciona el método "Efectivo" en vez de "Efectivo + Transferencia"');
            mixtoInput.closest('.form-group').classList.add('invalid');
        }
        var bancoMixtoSelect = document.getElementById('cuentaBancoMixtoSelect');
        if (!bancoMixtoSelect.value) {
            errors.push('Selecciona un banco para transferir el resto');
            bancoMixtoSelect.closest('.form-group').classList.add('invalid');
        }
    }

    if (errors.length > 0) {
        showCheckoutErrors(errors);
        window.scrollTo({ top: document.querySelector('.checkout-form').offsetTop - 20, behavior: 'smooth' });
        return false;
    }
    return true;
}

document.addEventListener('DOMContentLoaded', function() {
    var cartCountBadge = document.getElementById('cart-count');
    if (cartCountBadge) {
        updateCartCount();
    }

    var checkoutForm = document.querySelector('.checkout-form');
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function(e) {
            syncPaymentFields();
            if (!validateCheckoutForm()) {
                e.preventDefault();
                return false;
            }
        });
    }
});

function actualizarCantidad(productoId, cantidad) {
    if (cantidad < 1) {
        showConfirmModal('¿Remover este producto del carrito?').then(function(confirmed) {
            if (confirmed) {
                window.location.href = '/carrito/remover/' + productoId;
            }
        });
        return;
    }
    var csrf = getCsrfHeaders();
    delete csrf['Content-Type'];
    fetch('/carrito/actualizar?productoId=' + productoId + '&cantidad=' + cantidad, {
        method: 'POST',
        headers: csrf
    }).then(function() { location.reload(); });
}

function agregarAlCarrito(productoId, btnElement) {
    var card = btnElement ? btnElement.closest('.product-card') : null;
    var qtyInput = card ? card.querySelector('.card-qty') : null;
    var cantidad = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
    enviarAlCarrito(productoId, cantidad, null);
}

function agregarAlCarritoConObs(productoId) {
    var qtyInput = document.getElementById('stepper-qty');
    var cantidad = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
    var obs = document.getElementById('observacion');
    var observacion = obs ? obs.value : null;
    enviarAlCarrito(productoId, cantidad, observacion).then(function() {
        window.location.href = '/catalogo';
    });
}

function stepperSub(btn) {
    var input = btn ? btn.parentNode.querySelector('.qty-input') : document.getElementById('stepper-qty');
    if (!input) return;
    var val = parseInt(input.value) || 1;
    if (val > 1) input.value = val - 1;
}

function stepperAdd(btn) {
    var input = btn ? btn.parentNode.querySelector('.qty-input') : document.getElementById('stepper-qty');
    if (!input) return;
    input.value = (parseInt(input.value) || 1) + 1;
}

function enviarAlCarrito(productoId, cantidad, observacion) {
    return fetch('/carrito/agregar', {
        method: 'POST',
        headers: getCsrfHeaders(),
        body: JSON.stringify({
            productoId: productoId,
            cantidad: cantidad,
            observacion: observacion || null
        })
    })
    .then(function(response) {
        if (response.ok) {
            updateCartCount();
            mostrarToast('Producto agregado al carrito', 'success');
        } else if (response.status === 401) {
            window.location.href = '/login';
        }
    })
    .catch(function(error) {
        console.error('Error:', error);
        mostrarToast('Error al agregar al carrito', 'error');
    });
}

function updateCartCount() {
    fetch('/carrito/count')
        .then(function(response) { return response.json(); })
        .then(function(data) {
            var badge = document.getElementById('cart-count');
            if (badge) {
                badge.textContent = data;
                badge.style.display = data > 0 ? 'inline-block' : 'none';
            }
        })
        .catch(function(error) { console.error('Error updating cart count:', error); });
}
