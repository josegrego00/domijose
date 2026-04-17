(function() {
    const initializedContainers = new WeakSet();
    
    const containers = document.querySelectorAll('.carrusel-container');
    
    containers.forEach(container => {
        if (!initializedContainers.has(container)) {
            initializedContainers.add(container);
            initCarrusel(container);
        }
    });

    function initCarrusel(container) {
        const track = container.querySelector('.carrusel-track');
        const indicatorsContainer = container.querySelector('.carrusel-indicators');
        const prevBtn = container.querySelector('.carrusel-btn.prev');
        const nextBtn = container.querySelector('.carrusel-btn.next');
        
        const items = track.querySelectorAll('.carrusel-item');
        const itemCount = items.length;
        
        if (itemCount <= 1) {
            if (indicatorsContainer) indicatorsContainer.style.display = 'none';
            if (prevBtn) prevBtn.style.display = 'none';
            if (nextBtn) nextBtn.style.display = 'none';
            return;
        }
        
        createIndicators();
        
        let currentIndex = 0;
        
        function createIndicators() {
            for (let i = 0; i < itemCount; i++) {
                const dot = document.createElement('button');
                dot.className = 'carrusel-dot' + (i === 0 ? ' active' : '');
                dot.setAttribute('aria-label', 'Ir a slide ' + (i + 1));
                dot.addEventListener('click', () => goToSlide(i));
                indicatorsContainer.appendChild(dot);
            }
        }
        
        function updateIndicators(index) {
            const dots = indicatorsContainer.querySelectorAll('.carrusel-dot');
            dots.forEach((dot, i) => {
                dot.classList.toggle('active', i === index);
            });
            currentIndex = index;
        }
        
        function goToSlide(index) {
            if (index < 0) index = itemCount - 1;
            if (index >= itemCount) index = 0;
            
            items[index].scrollIntoView({
                behavior: 'smooth',
                inline: 'start'
            });
            updateIndicators(index);
        }
        
        track.addEventListener('scroll', function() {
            const scrollLeft = track.scrollLeft;
            const itemWidth = items[0].offsetWidth + 12;
            const newIndex = Math.round(scrollLeft / itemWidth);
            if (newIndex !== currentIndex && newIndex >= 0 && newIndex < itemCount) {
                updateIndicators(newIndex);
            }
        });
        
        if (prevBtn) {
            prevBtn.addEventListener('click', () => goToSlide(currentIndex - 1));
        }
        
        if (nextBtn) {
            nextBtn.addEventListener('click', () => goToSlide(currentIndex + 1));
        }
        
        let isScrolling = false;
        track.addEventListener('scroll', () => {
            isScrolling = true;
        }, { passive: true });
        
        track.addEventListener('scrollend', () => {
            isScrolling = false;
        });
        
        let scrollTimeout;
        track.addEventListener('scroll', () => {
            clearTimeout(scrollTimeout);
            scrollTimeout = setTimeout(() => {
                const scrollLeft = track.scrollLeft;
                const itemWidth = items[0].offsetWidth + 12;
                const newIndex = Math.round(scrollLeft / itemWidth);
                updateIndicators(newIndex);
            }, 100);
        }, { passive: true });
    }
    
    })();