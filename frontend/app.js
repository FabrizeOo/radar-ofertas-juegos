// URL del backend (cámbiala por tu URL de Render/Railway al subir a Vercel)
const API_BASE_URL = new URLSearchParams(window.location.search).get("api") || "http://localhost:8081";

let paginaActual = 0;
const TAMANO_PAGINA = 24;
let tiendaSeleccionada = "TODAS";
let dealSeleccionado = "GENERAL";
let ofertasActuales = [];
let eventSource = null;
let debounceTimer = null;

const el = {
    liveIndicator: document.getElementById("liveIndicator"),
    liveText: document.getElementById("liveText"),
    btnRefreshCatalog: document.getElementById("btnRefreshCatalog"),
    heroSpotlight: document.getElementById("heroSpotlight"),
    statOfertas: document.getElementById("statOfertas"),
    statAhorro: document.getElementById("statAhorro"),
    statComentarios: document.getElementById("statComentarios"),
    statTiendas: document.getElementById("statTiendas"),
    inputBuscar: document.getElementById("inputBuscar"),
    selectAhorro: document.getElementById("selectAhorro"),
    storePills: document.getElementById("storePills"),
    ofertasGrid: document.getElementById("ofertasGrid"),
    btnPrev: document.getElementById("btnPrev"),
    btnNext: document.getElementById("btnNext"),
    paginaLabel: document.getElementById("paginaLabel"),
    formComentario: document.getElementById("formComentarioGlobal"),
    targetLabel: document.getElementById("targetLabel"),
    btnLimpiarTarget: document.getElementById("btnLimpiarTarget"),
    inputAutor: document.getElementById("inputAutor"),
    inputContenido: document.getElementById("inputContenido"),
    selectCalificacion: document.getElementById("selectCalificacion"),
    feedComentarios: document.getElementById("feedComentarios"),
    modalDetalle: document.getElementById("modalDetalle"),
    btnCerrarDetalle: document.getElementById("btnCerrarDetalle"),
    detalleBody: document.getElementById("detalleBody")
};

function iniciar() {
    registrarEventosUI();
    cargarTodo();
    conectarTiempoReal();
}

function registrarEventosUI() {
    el.inputBuscar.addEventListener("input", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            paginaActual = 0;
            cargarOfertas();
        }, 240);
    });

    el.selectAhorro.addEventListener("change", () => {
        paginaActual = 0;
        cargarOfertas();
    });

    el.storePills.addEventListener("click", (e) => {
        const btn = e.target.closest(".pill");
        if (!btn) return;
        el.storePills.querySelectorAll(".pill").forEach(p => p.classList.remove("active"));
        btn.classList.add("active");
        tiendaSeleccionada = btn.dataset.store;
        paginaActual = 0;
        cargarOfertas();
    });

    el.btnPrev.addEventListener("click", () => {
        if (paginaActual > 0) {
            paginaActual--;
            cargarOfertas();
        }
    });

    el.btnNext.addEventListener("click", () => {
        paginaActual++;
        cargarOfertas();
    });

    el.btnRefreshCatalog.addEventListener("click", actualizarCatalogo);
    el.formComentario.addEventListener("submit", enviarComentario);
    el.btnLimpiarTarget.addEventListener("click", () => seleccionarObjetivo("GENERAL", "Comunidad General"));
    el.btnCerrarDetalle.addEventListener("click", () => el.modalDetalle.classList.add("hidden"));
    el.modalDetalle.addEventListener("click", (e) => {
        if (e.target === el.modalDetalle) el.modalDetalle.classList.add("hidden");
    });
}

async function cargarTodo() {
    await Promise.all([cargarEstadisticas(), cargarOfertas(), cargarComentarios()]);
}

async function cargarEstadisticas() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/estadisticas`);
        if (!res.ok) throw new Error();
        const stats = await res.json();
        pintarEstadisticas(stats);
    } catch {
        el.statTiendas.innerHTML = `<span class="store-chip">Conectando con catálogo...</span>`;
    }
}

function pintarEstadisticas(stats) {
    el.statOfertas.textContent = (stats.totalOfertas ?? 0).toLocaleString("es-ES");
    el.statComentarios.textContent = (stats.totalComentarios ?? 0).toLocaleString("es-ES");
    el.statAhorro.textContent = `${stats.ahorroPromedio ?? 0}%`;

    const tiendas = stats.porTienda || [];
    if (tiendas.length === 0) {
        el.statTiendas.innerHTML = `<span class="store-chip">Sin tiendas aún</span>`;
        return;
    }
    el.statTiendas.innerHTML = tiendas.slice(0, 5)
        .map(t => `<span class="store-chip">${escaparHtml(t.tiendaNombre)} (${t.cantidadOfertas})</span>`)
        .join("");
}

async function cargarOfertas() {
    const params = new URLSearchParams({
        q: el.inputBuscar.value.trim(),
        tienda: tiendaSeleccionada,
        ahorroMin: el.selectAhorro.value,
        pagina: paginaActual,
        tamano: TAMANO_PAGINA
    });

    el.paginaLabel.textContent = `Página ${paginaActual + 1}`;
    el.btnPrev.disabled = paginaActual === 0;

    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas?${params.toString()}`);
        if (!res.ok) throw new Error();
        ofertasActuales = await res.json();
        pintarHero(ofertasActuales[0]);
        pintarOfertas(ofertasActuales);
        el.btnNext.disabled = ofertasActuales.length < TAMANO_PAGINA;
    } catch {
        el.ofertasGrid.innerHTML = `<div class="empty-state">No pudimos cargar las ofertas en este momento. Intenta actualizar en unos segundos.</div>`;
    }
}

function urlCompraDirecta(dealId) {
    const idFinal = String(dealId).includes("%") ? dealId : encodeURIComponent(dealId);
    return `https://www.cheapshark.com/redirect?dealID=${idFinal}`;
}

function pintarHero(top) {
    if (!top) return;
    el.heroSpotlight.innerHTML = `
        <div class="hero-content">
            <span class="hero-tag">🔥 Destacada en ${escaparHtml(top.tiendaNombre || "Tienda oficial")}</span>
            <h2>${escaparHtml(top.titulo)}</h2>
            <div class="hero-meta">
                ${top.metacriticScore > 0 ? `<span>🎯 Crítica: ${top.metacriticScore}/100</span>` : ""}
                ${top.steamRatingPercent > 0 ? `<span>👍 Jugadores: ${top.steamRatingPercent}%</span>` : ""}
                <span>⭐ ${top.likes || 0} me gusta</span>
            </div>
            <div class="hero-prices">
                <span class="hero-discount">-${Math.round(top.porcentajeAhorro)}%</span>
                <span class="hero-price-now">$${Number(top.precioOferta).toFixed(2)}</span>
                <span class="hero-price-old">$${Number(top.precioNormal).toFixed(2)}</span>
                <a class="btn btn-mint" href="${urlCompraDirecta(top.dealId)}" target="_blank" rel="noopener noreferrer">
                    🛒 Comprar en ${escaparHtml(top.tiendaNombre || "tienda")}
                </a>
                <button class="btn btn-outline" onclick="abrirDetalle('${encodeURIComponent(top.dealId)}')">
                    Ver detalles
                </button>
            </div>
        </div>
        <div class="hero-visual">
            <img src="${escaparHtml(top.urlImagen || "")}" alt="${escaparHtml(top.titulo)}">
        </div>
    `;
}

function pintarOfertas(ofertas) {
    if (!ofertas || ofertas.length === 0) {
        el.ofertasGrid.innerHTML = `<div class="empty-state">No encontramos juegos con esos filtros. Prueba otra búsqueda.</div>`;
        return;
    }

    el.ofertasGrid.innerHTML = ofertas.map((o, idx) => `
        <article class="deal-card" data-deal="${escaparHtml(o.dealId)}" style="animation-delay: ${Math.min(idx * 25, 300)}ms">
            <div class="deal-thumb" onclick="abrirDetalle('${encodeURIComponent(o.dealId)}')">
                <img src="${escaparHtml(o.urlImagen || "")}" alt="${escaparHtml(o.titulo)}" loading="lazy">
                <span class="deal-discount-tag">-${Math.round(o.porcentajeAhorro)}%</span>
                <span class="deal-store-tag">${escaparHtml(o.tiendaNombre || "Tienda")}</span>
            </div>
            <div class="deal-body">
                <h3 class="deal-title" onclick="abrirDetalle('${encodeURIComponent(o.dealId)}')" title="${escaparHtml(o.titulo)}">
                    ${escaparHtml(o.titulo)}
                </h3>
                <div class="deal-scores">
                    ${o.metacriticScore > 0 ? `<span class="score-pill">🎯 ${o.metacriticScore}</span>` : ""}
                    ${o.steamRatingPercent > 0 ? `<span class="score-pill">👍 ${o.steamRatingPercent}%</span>` : ""}
                </div>
                <div class="deal-price-row">
                    <span class="price-now">$${Number(o.precioOferta).toFixed(2)}</span>
                    <span class="price-old">$${Number(o.precioNormal).toFixed(2)}</span>
                </div>
                <div class="deal-actions">
                    <a class="btn btn-mint" href="${urlCompraDirecta(o.dealId)}" target="_blank" rel="noopener noreferrer" title="Ir a la tienda para comprar">
                        🛒 Ir a tienda
                    </a>
                    <button class="btn btn-outline" onclick="darLike('${encodeURIComponent(o.dealId)}')" title="Me gusta">
                        ❤️ <span class="like-count">${o.likes || 0}</span>
                    </button>
                    <button class="btn btn-outline" onclick="seleccionarObjetivoDesdeBoton('${encodeURIComponent(o.dealId)}', '${encodeURIComponent(o.titulo)}')" title="Comentar sobre este juego">
                        💬
                    </button>
                </div>
            </div>
        </article>
    `).join("");
}

window.abrirDetalle = async function (encodedDealId) {
    const dealId = decodeURIComponent(encodedDealId);
    const oferta = ofertasActuales.find(x => x.dealId === dealId);
    if (!oferta) return;

    const ahorroDinero = Math.max(0, oferta.precioNormal - oferta.precioOferta).toFixed(2);
    el.detalleBody.innerHTML = `
        <img class="detail-banner" src="${escaparHtml(oferta.urlImagen || "")}" alt="${escaparHtml(oferta.titulo)}">
        <h2>${escaparHtml(oferta.titulo)}</h2>
        <p style="color: var(--muted); font-size: 0.85rem;">Disponible en <strong>${escaparHtml(oferta.tiendaNombre)}</strong></p>

        <div class="detail-grid">
            <div class="detail-stat">
                <span>Precio hoy</span>
                <strong style="color: var(--mint)">$${Number(oferta.precioOferta).toFixed(2)}</strong>
            </div>
            <div class="detail-stat">
                <span>Ahorras</span>
                <strong>$${ahorroDinero} (-${Math.round(oferta.porcentajeAhorro)}%)</strong>
            </div>
            <div class="detail-stat">
                <span>Valoración</span>
                <strong>${oferta.metacriticScore > 0 ? oferta.metacriticScore + "/100" : (oferta.steamRatingPercent > 0 ? oferta.steamRatingPercent + "%" : "N/D")}</strong>
            </div>
        </div>

        <div style="display:flex; gap:0.6rem; flex-wrap:wrap;">
            <a class="btn btn-mint" style="flex:1" href="${urlCompraDirecta(oferta.dealId)}" target="_blank" rel="noopener noreferrer">
                🛒 Comprar ahora en ${escaparHtml(oferta.tiendaNombre)} →
            </a>
            <button class="btn btn-primary" onclick="seleccionarDesdeModal('${encodeURIComponent(oferta.dealId)}', '${encodeURIComponent(oferta.titulo)}')">
                💬 Escribir opinión
            </button>
        </div>

        <div class="detail-comments" id="detalleComentarios">
            <p style="color: var(--muted); font-size: 0.84rem;">Cargando opiniones de este juego...</p>
        </div>
    `;
    el.modalDetalle.classList.remove("hidden");

    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios/oferta?dealId=${encodeURIComponent(dealId)}`);
        const lista = await res.json();
        const cont = document.getElementById("detalleComentarios");
        if (!lista.length) {
            cont.innerHTML = `<p style="color: var(--muted); font-size: 0.84rem;">Aún no hay opiniones sobre este juego. ¡Sé el primero!</p>`;
        } else {
            cont.innerHTML = `<h3>Opiniones de jugadores</h3>` + lista.map(renderComentarioHtml).join("");
        }
    } catch {
        // Silencioso
    }
};

window.seleccionarDesdeModal = function (encodedDealId, encodedTitulo) {
    el.modalDetalle.classList.add("hidden");
    window.seleccionarObjetivoDesdeBoton(encodedDealId, encodedTitulo);
};

async function actualizarCatalogo() {
    el.btnRefreshCatalog.disabled = true;
    const original = el.btnRefreshCatalog.textContent;
    el.btnRefreshCatalog.textContent = "⏳ Buscando rebajas...";
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/sincronizar?paginas=10`, { method: "POST" });
        if (!res.ok) throw new Error();
        await cargarTodo();
    } catch {
        alert("No se pudo actualizar el catálogo en este momento.");
    } finally {
        el.btnRefreshCatalog.disabled = false;
        el.btnRefreshCatalog.textContent = original;
    }
}

window.darLike = async function (encodedDealId) {
    const dealId = decodeURIComponent(encodedDealId);
    try {
        const res = await fetch(`${API_BASE_URL}/api/ofertas/like?dealId=${encodeURIComponent(dealId)}`, { method: "POST" });
        if (res.ok) {
            const actualizada = await res.json();
            actualizarLikesEnTarjeta(actualizada);
        }
    } catch (e) {
        console.error(e);
    }
};

window.seleccionarObjetivoDesdeBoton = function (encodedDealId, encodedTitulo) {
    seleccionarObjetivo(decodeURIComponent(encodedDealId), decodeURIComponent(encodedTitulo));
    el.inputContenido.focus();
};

function seleccionarObjetivo(dealId, titulo) {
    dealSeleccionado = dealId;
    el.targetLabel.querySelector("strong").textContent = titulo;
    el.btnLimpiarTarget.classList.toggle("hidden", dealId === "GENERAL");
}

function actualizarLikesEnTarjeta(oferta) {
    const cards = document.querySelectorAll(".deal-card");
    for (const card of cards) {
        if (card.dataset.deal === oferta.dealId) {
            const span = card.querySelector(".like-count");
            if (span) span.textContent = oferta.likes;
        }
    }
}

async function cargarComentarios() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios?limite=30`);
        if (!res.ok) throw new Error();
        const lista = await res.json();
        pintarComentarios(lista);
    } catch {
        // Silencioso
    }
}

function pintarComentarios(lista) {
    if (!lista || lista.length === 0) {
        el.feedComentarios.innerHTML = `<div class="empty-state">Sé el primero en opinar sobre una oferta.</div>`;
        return;
    }
    el.feedComentarios.innerHTML = lista.map(renderComentarioHtml).join("");
}

function renderComentarioHtml(c) {
    const estrellas = "★".repeat(c.calificacion || 5) + "☆".repeat(Math.max(0, 5 - (c.calificacion || 5)));
    const fecha = c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleString("es-ES") : "Recién publicado";
    return `
        <div class="comment-card">
            <div class="comment-head">
                <span class="comment-author">${escaparHtml(c.autor)}</span>
                <span class="comment-stars">${estrellas}</span>
            </div>
            <div class="comment-game">🎮 ${escaparHtml(c.tituloJuego || "Comunidad General")}</div>
            <p class="comment-body">${escaparHtml(c.contenido)}</p>
            <div class="comment-time">${fecha}</div>
        </div>
    `;
}

async function enviarComentario(e) {
    e.preventDefault();
    const payload = {
        dealId: dealSeleccionado,
        autor: el.inputAutor.value.trim(),
        contenido: el.inputContenido.value.trim(),
        calificacion: Number(el.selectCalificacion.value)
    };
    if (!payload.contenido) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/comentarios`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            el.inputContenido.value = "";
        }
    } catch {
        alert("No se pudo publicar tu comentario.");
    }
}

function conectarTiempoReal() {
    if (eventSource) eventSource.close();
    eventSource = new EventSource(`${API_BASE_URL}/api/ofertas/stream`);

    eventSource.addEventListener("conectado", () => {
        el.liveIndicator.className = "pulse-badge online";
        el.liveText.textContent = "Actualizado al instante";
    });

    eventSource.addEventListener("comentario", (event) => {
        const nuevo = JSON.parse(event.data);
        const vacio = el.feedComentarios.querySelector(".empty-state");
        if (vacio) vacio.remove();
        el.feedComentarios.insertAdjacentHTML("afterbegin", renderComentarioHtml(nuevo));
        const actual = Number(el.statComentarios.textContent.replace(/\D/g, "")) || 0;
        el.statComentarios.textContent = (actual + 1).toLocaleString("es-ES");
    });

    eventSource.addEventListener("like", (event) => {
        actualizarLikesEnTarjeta(JSON.parse(event.data));
    });

    eventSource.addEventListener("sincronizacion", (event) => {
        const data = JSON.parse(event.data);
        if (data.estadisticas) pintarEstadisticas(data.estadisticas);
        cargarOfertas();
    });

    eventSource.onerror = () => {
        el.liveIndicator.className = "pulse-badge";
        el.liveText.textContent = "Reconectando...";
    };
}

function escaparHtml(texto) {
    if (texto === null || texto === undefined) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

iniciar();
